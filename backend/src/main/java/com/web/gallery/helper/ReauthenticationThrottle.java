package com.web.gallery.helper;

import java.time.Clock;
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
 */
@Component
public class ReauthenticationThrottle {

	/** エントリ数の上限（超過時は全クリアしてメモリ暴走を防ぐ） */
	private static final int MAX_ENTRIES = 100_000;

	private final int maxFailures;
	private final long lockoutMillis;
	private final Clock clock;

	private final Map<Long, Attempt> attempts = new ConcurrentHashMap<>();

	/** アカウント単位の失敗記録（失敗回数と、直近の失敗時刻） */
	private static final class Attempt {
		private int count;
		private long lastFailureAtMillis;
	}

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
			if (now() - attempt.lastFailureAtMillis >= lockoutMillis) {
				attempts.remove(accountNo);
				return false;
			}
			return attempt.count >= maxFailures;
		}
	}

	/**
	 * 再認証の失敗を1回記録する
	 *
	 * @param	accountNo	アカウント番号
	 */
	public void recordFailure(Long accountNo) {
		if (maxFailures <= 0) {
			return;
		}
		if (attempts.size() >= MAX_ENTRIES) {
			attempts.clear();
		}
		attempts.compute(accountNo, (key, existing) -> {
			Attempt attempt = existing != null ? existing : new Attempt();
			long current = now();
			// 前回失敗からロックアウト時間以上あいていれば、カウントをリセットして数え直す
			if (existing != null && current - attempt.lastFailureAtMillis >= lockoutMillis) {
				attempt.count = 0;
			}
			attempt.count++;
			attempt.lastFailureAtMillis = current;
			return attempt;
		});
	}

	/**
	 * 当該アカウントの失敗記録を破棄する（再認証成功時に呼ぶ）
	 *
	 * @param	accountNo	アカウント番号
	 */
	public void reset(Long accountNo) {
		attempts.remove(accountNo);
	}

	private long now() {
		return clock.millis();
	}
}
