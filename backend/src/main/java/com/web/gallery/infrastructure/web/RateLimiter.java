package com.web.gallery.infrastructure.web;

import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * 送信元（IPアドレス等）とカテゴリの組み合わせごとにリクエスト数を固定ウィンドウで数えるHelperクラス
 *
 * <p>ログインの総当たり・アカウント登録スパム・書き込み系の乱打などを、前段（WAF / ロードバランサ）の
 * レート制限に加えてアプリ側でも多層で抑止する。カウンタはプロセスローカルで再起動により消える （複数インスタンス構成では各インスタンスが個別に数えるため実効しきい値はインスタンス数倍になる。
 * 厳密な制御が必要なら前段の WAF / 共有ストア併用へ移行する）。
 *
 * <p>スレッド安全性：エントリ（{@link Window}）ごとにそのインスタンスのモニタで参照・更新を直列化する。
 * ウィンドウ境界での取りこぼし・数え過ぎは、レート制限の用途上許容できる範囲でのみ発生しうる。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class RateLimiter {

  /** エントリ数の上限。超過時は古いエントリを間引き、それでも下回らなければ古い順に強制削除する（全消去はしない） */
  private static final int MAX_ENTRIES = 200_000;

  /** 間引き処理（全走査）の最短実行間隔（ミリ秒）。thundering herd 対策 */
  private static final long SWEEP_MIN_INTERVAL_MILLIS = 60_000L;

  /** エントリを「古い」とみなす経過時間（ミリ秒）。どのウィンドウ長よりも十分に長くとる */
  private static final long STALE_THRESHOLD_MILLIS = 3_600_000L;

  /**
   * エントリ数が上限を超えたのに古いエントリの間引きだけでは上限を下回らなかった場合に、 強制削除したあとの目標エントリ数（{@link #MAX_ENTRIES}に対する割合）
   *
   * <p>削除しすぎるとカウントを失って制限が緩むため、上限の8割まで落として余裕を作る
   */
  private static final double FORCED_EVICTION_TARGET_RATIO = 0.8;

  private final Clock clock;

  private final Map<String, Window> windows = new ConcurrentHashMap<>();

  private final AtomicLong lastSweepAtMillis = new AtomicLong(0L);

  /**
   * コンストラクタ
   *
   * @param clock 時刻取得用の {@link Clock}
   */
  public RateLimiter(Clock clock) {
    this.clock = clock;
  }

  /**
   * 固定ウィンドウのレート制限ルール
   *
   * @param capacity ウィンドウ内で許可する最大リクエスト数
   * @param window ウィンドウの長さ
   */
  public record Rule(int capacity, Duration window) {}

  /**
   * 指定キーのリクエストを1件受け付けられるか判定し、受け付けられる場合はカウントを1加算する
   *
   * @param key 送信元とカテゴリを一意に表すキー（例: {@code "203.0.113.9|AUTH"}）
   * @param rule 適用するレート制限ルール
   * @return ウィンドウ内の上限に達していなければ true（このとき内部カウントを加算する）
   */
  public boolean tryAcquire(String key, Rule rule) {
    long now = clock.millis();
    sweepIfDue(now);

    Window window = windows.computeIfAbsent(key, k -> new Window(now));
    long windowMillis = rule.window().toMillis();
    synchronized (window) {
      if (now - window.startEpochMillis >= windowMillis) {
        window.startEpochMillis = now;
        window.count = 0;
      }
      if (window.count >= rule.capacity()) {
        return false;
      }
      window.count++;
      return true;
    }
  }

  /**
   * テスト・監視用に現在のエントリ数を返す
   *
   * @return 保持しているエントリ数
   */
  public int size() {
    return windows.size();
  }

  /**
   * 一定間隔ごと、またはエントリ数が上限を超えたときに、古いエントリを間引く
   *
   * <p>間引き後もエントリ数が上限を超えている場合は、古い順に強制削除して上限内へ戻す （{@link
   * #evictOldestEntries}）。これにより短時間に大量の異なるキーが生成されても マップが無制限に成長しない。
   *
   * @param now 現在時刻（エポックミリ秒）
   */
  private void sweepIfDue(long now) {
    boolean overCapacity = windows.size() > MAX_ENTRIES;
    long last = lastSweepAtMillis.get();
    if (!overCapacity && now - last < SWEEP_MIN_INTERVAL_MILLIS) {
      return;
    }
    if (!lastSweepAtMillis.compareAndSet(last, now)) {
      return;
    }
    for (Iterator<Window> it = windows.values().iterator(); it.hasNext(); ) {
      Window window = it.next();
      synchronized (window) {
        if (now - window.startEpochMillis >= STALE_THRESHOLD_MILLIS) {
          it.remove();
        }
      }
    }
    if (windows.size() > MAX_ENTRIES) {
      evictOldestEntries();
    }
  }

  /**
   * ウィンドウ開始時刻が古い順にエントリを強制削除し、エントリ数を上限内へ戻す
   *
   * <p>{@link #STALE_THRESHOLD_MILLIS}未満の新しいエントリしか無い状況（短時間に大量の異なるキーが
   * 到来した場合）でもメモリ使用量を抑えるための最終手段。削除されたキーはカウントを失うため 直後のリクエストが通りうるが、IP単位のレート制限としては許容できる範囲とする。
   */
  private void evictOldestEntries() {
    int targetSize = (int) (MAX_ENTRIES * FORCED_EVICTION_TARGET_RATIO);
    int removeCount = windows.size() - targetSize;
    if (removeCount <= 0) {
      return;
    }
    windows.entrySet().stream()
        .sorted(Comparator.comparingLong(entry -> startEpochMillisOf(entry.getValue())))
        .limit(removeCount)
        .map(Map.Entry::getKey)
        .forEach(windows::remove);
  }

  /**
   * エントリのウィンドウ開始時刻を、そのエントリのモニタで同期して読み出す
   *
   * @param window 対象のエントリ
   * @return ウィンドウ開始時刻（エポックミリ秒）
   */
  private static long startEpochMillisOf(Window window) {
    synchronized (window) {
      return window.startEpochMillis;
    }
  }

  /** 固定ウィンドウ1件分のカウント状態 */
  private static final class Window {
    /** ウィンドウの開始時刻（エポックミリ秒） */
    private long startEpochMillis;

    /** ウィンドウ内で受け付けたリクエスト数 */
    private int count;

    private Window(long startEpochMillis) {
      this.startEpochMillis = startEpochMillis;
      this.count = 0;
    }
  }
}
