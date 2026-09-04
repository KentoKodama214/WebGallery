package com.web.gallery.helper;

import java.time.Clock;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 現在のパスワードによる本人確認（再認証）の失敗回数を、アカウント単位でインメモリに数えるHelperクラス<p>
 * アクセストークンを盗んだ攻撃者がパスワード変更・アカウント削除エンドポイント経由で
 * {@code currentPassword} をオンラインで総当たりするのを抑止する。
 * <p>
 * ログイン失敗回数（{@code login_failure_count}）への相乗り加算は、パスワード変更・削除処理が
 * 例外でロールバックされる文脈で独立トランザクション書き込みを行うことになり、統合テストで
 * {@code common.account} の排他ロックとデッドロックするため採用していない。判定の入口では
 * {@code login_failure_count} と管理者ロックも参照する（{@code AccountServiceImpl#isReauthLocked}）。
 * <p>
 * カウンタはプロセスローカルで、再起動で消える。総当たりは BCrypt 照合律速で低速なため許容する。
 * 上限は {@code auth.reauth.max-failures}（既定5）、ロックアウト時間は
 * {@code auth.reauth.lockout-minutes}（既定15分）で調整できる。
 * <p>
 * スレッド安全性：エントリ（{@link Attempt}）ごとにそのインスタンスのモニタで参照・更新を直列化する。
 * {@code isLockedOut} と {@code recordFailure} は同一モニタで排他するため、競合下でも失敗回数の
 * 数え漏れ・数え過ぎは生じない。
 */
@Component
public class ReauthenticationThrottle {

	/** エントリ数の上限。超過時は期限切れ→直近失敗が古い順に間引く（全消去はしない） */
	private static final int MAX_ENTRIES = 100_000;

	/** 上限超過時に残すエントリ数の割合（MAX_ENTRIES の 90%） */
	private static final int EVICTION_KEEP_RATIO_NUMERATOR = 9;
	private static final int EVICTION_KEEP_RATIO_DENOMINATOR = 10;

	private final int maxFailures;
	private final long lockoutMillis;
	private final Clock clock;

	private final Map<Long, Attempt> attempts = new ConcurrentHashMap<>();

	/** アカウント単位の失敗記録（失敗回数と、直近の失敗時刻） */
	private static final class Attempt {
		private int count;
		private long lastFailureAtMillis;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	maxFailures		ロックアウトする失敗回数の上限（0以下で機能無効）
	 * @param	lockoutMinutes	ロックアウト時間（分）
	 * @param	clock			現在時刻の取得に用いる{@link Clock}
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
	 * 当該アカウントが再認証のロックアウト中かどうかを判定する<p>
	 * 直近の失敗からロックアウト時間が経過していれば記録を破棄し、ロックアウトを解除する。
	 *
	 * @param	accountNo	アカウント番号
	 * @return				ロックアウト中の場合true
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
	 * 再認証の失敗を1回記録する<p>
	 * ロックアウト中の試行に対しても呼び出してよい（直近失敗時刻が更新され、ロックアウトが延長される）。
	 *
	 * @param	accountNo	アカウント番号
	 */
	public void recordFailure(Long accountNo) {
		if (maxFailures <= 0) {
			return;
		}
		evictIfOverCapacity();
		Attempt attempt = attempts.computeIfAbsent(accountNo, key -> new Attempt());
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
	 * @param	accountNo	アカウント番号
	 */
	public void reset(Long accountNo) {
		attempts.remove(accountNo);
	}

	/**
	 * エントリ数が上限に達している場合、まず期限切れエントリを掃き出し、
	 * それでも上限のままなら直近失敗時刻が古い順に間引く<p>
	 * 全消去は行わない（全消去すると、上限到達の瞬間にロックアウト中の全アカウントが
	 * 一斉に解除され、失敗枠を意図的に膨らませてロックアウトを流す増幅経路になるため）。
	 */
	private void evictIfOverCapacity() {
		if (attempts.size() < MAX_ENTRIES) {
			return;
		}
		// まず期限切れを掃除する
		attempts.forEach((key, attempt) -> {
			synchronized (attempt) {
				if (isExpired(attempt)) {
					attempts.remove(key, attempt);
				}
			}
		});
		// なお上限を超えるなら、直近失敗が古い順に一定数を間引く
		int keepCount = MAX_ENTRIES / EVICTION_KEEP_RATIO_DENOMINATOR * EVICTION_KEEP_RATIO_NUMERATOR;
		int evictCount = attempts.size() - keepCount;
		if (evictCount <= 0) {
			return;
		}
		attempts.entrySet().stream()
				.sorted(Comparator.comparingLong((Map.Entry<Long, Attempt> entry) -> lastFailureAt(entry.getValue())))
				.limit(evictCount)
				.map(Map.Entry::getKey)
				.forEach(attempts::remove);
	}

	/**
	 * エントリの直近失敗時刻をモニタ下で読み取る
	 *
	 * @param	attempt	{@link Attempt}
	 * @return			直近失敗時刻（エポックミリ秒）
	 */
	private long lastFailureAt(Attempt attempt) {
		synchronized (attempt) {
			return attempt.lastFailureAtMillis;
		}
	}

	/**
	 * エントリのロックアウト時間が経過しているかどうかを判定する（呼び出し側でモニタを保持していること）
	 *
	 * @param	attempt	{@link Attempt}
	 * @return			経過している場合true
	 */
	private boolean isExpired(Attempt attempt) {
		return now() - attempt.lastFailureAtMillis >= lockoutMillis;
	}

	/**
	 * 現在時刻（エポックミリ秒）を返す
	 *
	 * @return	現在時刻（エポックミリ秒）
	 */
	private long now() {
		return clock.millis();
	}
}
