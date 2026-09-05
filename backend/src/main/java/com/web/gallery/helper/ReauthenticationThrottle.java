package com.web.gallery.helper;

import java.time.Clock;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 現在のパスワードによる本人確認（再認証）の失敗回数を、アカウント単位でインメモリに数えるHelperクラス
 *
 * <p>アクセストークンを盗んだ攻撃者がパスワード変更・アカウント削除エンドポイント経由で {@code currentPassword} をオンラインで総当たりするのを抑止する。
 *
 * <p>ログイン失敗回数（{@code login_failure_count}）への相乗り加算は、パスワード変更・削除処理が
 * 例外でロールバックされる文脈で独立トランザクション書き込みを行うことになり、統合テストで {@code common.account}
 * の排他ロックとデッドロックするため採用していない。判定の入口では {@code login_failure_count} と管理者ロックも参照する（{@code
 * AccountServiceImpl#isReauthLocked}）。
 *
 * <p>カウンタはプロセスローカルで、再起動で消える。総当たりは BCrypt 照合律速で低速なため許容する。 上限は {@code
 * auth.reauth.max-failures}（既定5）、ロックアウト時間は {@code auth.reauth.lockout-minutes}（既定15分）で調整できる。
 *
 * <p>スレッド安全性：エントリ（{@link Attempt}）ごとにそのインスタンスのモニタで参照・更新を直列化する。 {@code isLockedOut} と {@code
 * recordFailure} は同一モニタで排他するため、競合下でも失敗回数の 数え漏れ・数え過ぎは生じない。
 */
@Component
public class ReauthenticationThrottle {

  /** エントリ数の上限。超過時は期限切れ→直近失敗が古い順に間引く（全消去はしない） */
  private static final int MAX_ENTRIES = 100_000;

  /** 上限超過時に残すエントリ数の割合（MAX_ENTRIES の 90%） */
  private static final int EVICTION_KEEP_RATIO_NUMERATOR = 9;

  private static final int EVICTION_KEEP_RATIO_DENOMINATOR = 10;

  /**
   * 間引き処理（全走査＋ソート）の最短実行間隔（ミリ秒）
   *
   * <p>上限到達時に多数のスレッドが同時に {@code recordFailure} へ入っても、 全走査＋ソートを1回だけ行い、残りのスレッドはスキップさせる（thundering
   * herd 対策）。
   */
  private static final long EVICTION_MIN_INTERVAL_MILLIS = 1_000L;

  private final int maxFailures;
  private final long lockoutMillis;
  private final Clock clock;

  private final Map<Long, Attempt> attempts = new ConcurrentHashMap<>();

  /**
   * 直近に間引き処理を実行した時刻（エポックミリ秒）。0は未実行
   *
   * <p>読み取り→判定→書き込みを{@code compareAndSet}で不可分に行い、上限到達時に多数のスレッドが 同時に入っても間引き実行権を1スレッドだけに与える（{@code
   * volatile long}の read-then-write では 複数スレッドが同時にゲートを通過しうるため{@link AtomicLong}を用いる）。
   */
  private final AtomicLong lastEvictionAtMillis = new AtomicLong(0L);

  /** アカウント単位の失敗記録（失敗回数と、直近の失敗時刻） */
  private static final class Attempt {
    private int count;
    private long lastFailureAtMillis;
  }

  /**
   * 間引き対象を並べ替えるためのイミュータブルなスナップショット
   *
   * <p>{@link Attempt} の可変フィールドをソート中に直接読むと、別スレッドの更新で比較結果が 揺れて {@code Comparator} 契約違反（{@code
   * TimSort} の例外）になりうるため、 並べ替えに必要な値をモニタ下で1度だけ写し取ってからソートする。
   *
   * @param accountNo アカウント番号
   * @param lockedOutOrder ロックアウト中なら1、そうでなければ0（0を先に間引く）
   * @param lastFailureAtMillis 直近失敗時刻（エポックミリ秒）
   */
  private record EvictionCandidate(long accountNo, int lockedOutOrder, long lastFailureAtMillis) {}

  /**
   * コンストラクタ
   *
   * @param maxFailures ロックアウトする失敗回数の上限（0以下で機能無効）
   * @param lockoutMinutes ロックアウト時間（分）
   * @param clock 現在時刻の取得に用いる{@link Clock}
   */
  public ReauthenticationThrottle(
      @Value("${auth.reauth.max-failures:5}") int maxFailures,
      @Value("${auth.reauth.lockout-minutes:15}") long lockoutMinutes,
      Clock clock) {
    this.maxFailures = maxFailures;
    this.lockoutMillis = lockoutMinutes * 60_000L;
    this.clock = clock;
  }

  /**
   * 当該アカウントが再認証のロックアウト中かどうかを判定する
   *
   * <p>直近の失敗からロックアウト時間が経過していれば記録を破棄し、ロックアウトを解除する。
   *
   * @param accountNo アカウント番号
   * @return ロックアウト中の場合true
   */
  public boolean isLockedOut(Long accountNo) {
    if (maxFailures <= 0) {
      return false;
    }
    Attempt attempt = attempts.get(accountNo);
    if (attempt == null) {
      return false;
    }
    synchronized (attempt) {
      if (isExpired(attempt)) {
        // 参照した本人のエントリだけを消す（compute で作り直された新しいエントリは消さない）
        attempts.remove(accountNo, attempt);
        return false;
      }
      return attempt.count >= maxFailures;
    }
  }

  /**
   * 再認証の失敗を1回記録する
   *
   * <p>ロックアウト中の試行に対しても呼び出してよい（直近失敗時刻が更新され、ロックアウトが延長される）。
   *
   * <p>エントリ数が上限に達しており、かつ間引きが追いついていない場合は、既存アカウントの失敗のみ 数え、新規アカウントのエントリは追加しない（上限を実質的なハードリミットとして守る）。
   *
   * @param accountNo アカウント番号
   */
  public void recordFailure(Long accountNo) {
    if (maxFailures <= 0) {
      return;
    }
    evictIfOverCapacity();
    Attempt attempt = attempts.get(accountNo);
    if (attempt == null) {
      if (attempts.size() >= MAX_ENTRIES) {
        // 上限超過中は新規エントリを追加しない。入口の login_failure_count / 管理者ロック判定と
        // BCrypt 照合律速が引き続き総当たりを抑止する。
        return;
      }
      attempt = attempts.computeIfAbsent(accountNo, key -> new Attempt());
    }
    synchronized (attempt) {
      long current = now();
      // 前回失敗からロックアウト時間以上あいていれば、カウントをリセットして数え直す
      if (current - attempt.lastFailureAtMillis >= lockoutMillis) {
        attempt.count = 0;
      }
      attempt.count++;
      attempt.lastFailureAtMillis = current;
    }
  }

  /**
   * 当該アカウントの失敗記録を破棄する（再認証成功時に呼ぶ）
   *
   * @param accountNo アカウント番号
   */
  public void reset(Long accountNo) {
    attempts.remove(accountNo);
  }

  /**
   * エントリ数が上限に達している場合、まず期限切れエントリを掃き出し、 それでも上限のままなら間引く
   *
   * <p>間引きの順序は「ロックアウト中でないエントリを、直近失敗時刻が古い順に」優先する。 ロックアウト中（{@code count >= maxFailures}
   * かつ未期限切れ）のエントリは、 他に間引ける候補が尽きた場合の最後の手段としてのみ対象にする （攻撃者が大量アカウントの失敗枠を膨らませて被害者のロックアウトを流すのを防ぐ）。
   * 全消去は行わない（上限到達の瞬間にロックアウト中の全アカウントが一斉解除されるのを防ぐため）。
   *
   * <p>上限到達時に多数のスレッドが同時に入っても、間引き実行権の獲得を{@code compareAndSet}で 不可分に行うため、全走査＋ソートは{@code
   * EVICTION_MIN_INTERVAL_MILLIS}に1回だけ実行される （獲得できなかったスレッドは即座に戻る）。間引きが追いつかない間は{@code recordFailure}が
   * 新規エントリの追加を見送るため、エントリ数は上限付近で頭打ちになる。
   */
  private void evictIfOverCapacity() {
    if (attempts.size() < MAX_ENTRIES) {
      return;
    }
    long current = now();
    long previous = lastEvictionAtMillis.get();
    if (current - previous < EVICTION_MIN_INTERVAL_MILLIS) {
      // 直近で間引き済み。多数スレッドの同時流入時は1回に集約する
      return;
    }
    if (!lastEvictionAtMillis.compareAndSet(previous, current)) {
      // 他スレッドが同時に間引き実行権を獲得した（このスレッドは何もしない）
      return;
    }

    // まず期限切れを掃除する
    attempts.forEach(
        (key, attempt) -> {
          synchronized (attempt) {
            if (isExpired(attempt)) {
              attempts.remove(key, attempt);
            }
          }
        });
    // なお上限を超えるなら、ロックアウト中でないものを古い順に間引く（足りなければロックアウト中も対象）
    int keepCount = MAX_ENTRIES / EVICTION_KEEP_RATIO_DENOMINATOR * EVICTION_KEEP_RATIO_NUMERATOR;
    int evictCount = attempts.size() - keepCount;
    if (evictCount <= 0) {
      return;
    }
    // ソート中に別スレッドが Attempt を更新しても Comparator 契約違反にならないよう、
    // 並べ替えキーをモニタ下で1度だけスナップショットしてからソートする
    attempts.entrySet().stream()
        .map(
            entry -> {
              synchronized (entry.getValue()) {
                int order =
                    entry.getValue().count >= maxFailures && !isExpired(entry.getValue()) ? 1 : 0;
                return new EvictionCandidate(
                    entry.getKey(), order, entry.getValue().lastFailureAtMillis);
              }
            })
        .sorted(
            Comparator.comparingInt(EvictionCandidate::lockedOutOrder)
                .thenComparingLong(EvictionCandidate::lastFailureAtMillis))
        .limit(evictCount)
        .forEach(candidate -> attempts.remove(candidate.accountNo()));
  }

  /**
   * エントリのロックアウト時間が経過しているかどうかを判定する（呼び出し側でモニタを保持していること）
   *
   * @param attempt {@link Attempt}
   * @return 経過している場合true
   */
  private boolean isExpired(Attempt attempt) {
    return now() - attempt.lastFailureAtMillis >= lockoutMillis;
  }

  /**
   * 現在時刻（エポックミリ秒）を返す
   *
   * @return 現在時刻（エポックミリ秒）
   */
  private long now() {
    return clock.millis();
  }
}
