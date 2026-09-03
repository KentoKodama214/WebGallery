package com.web.gallery.helper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.event.AccountDeletedEvent;
import com.web.gallery.event.AccountLockedEvent;
import com.web.gallery.event.AccountUnlockedEvent;
import com.web.gallery.event.AccountUpdatedEvent;

/**
 * アクセストークン検証後のアカウント情報（{@link AccountPrincipal}）を短時間キャッシュするヘルパークラス<p>
 * {@link com.web.gallery.config.JwtAuthenticationFilter} は毎リクエストでアカウントをDB参照するため、
 * 高頻度アクセス時の負荷が大きい。ごく短いTTLでキャッシュすることでDB参照を間引く。
 * <p>
 * トレードオフ：管理者ロック・アカウント削除の反映が最大 TTL ミリ秒遅延する。
 * アクセストークンの有効期限（15分）より十分に短く、許容範囲とする。
 * TTLは {@code app.auth.principal-cache-ttl-millis}（既定10秒）で調整でき、
 * {@code 0}以下を指定するとキャッシュを無効化する（テストで利用）。
 */
@Component
public class AuthenticatedUserCache {

	/** キャッシュエントリ数の上限（超過時は全クリアして暴走を防ぐ） */
	private static final int MAX_ENTRIES = 10_000;

	/** キャッシュの有効期間（ミリ秒）。0以下でキャッシュ無効 */
	private final long ttlMillis;

	private final Map<String, Entry> cache = new ConcurrentHashMap<>();

	private record Entry(AccountPrincipal principal, long expiresAtMillis) { }

	public AuthenticatedUserCache(
			@Value("${app.auth.principal-cache-ttl-millis:10000}") long ttlMillis) {
		this.ttlMillis = ttlMillis;
	}

	/**
	 * アカウントIDに対応する{@link AccountPrincipal}をキャッシュから取得する。<p>
	 * キャッシュミス・TTL切れの場合は{@code loader}で読み込み、キャッシュに格納する。
	 *
	 * @param	accountId	アカウントID
	 * @param	loader		キャッシュミス時にアカウント情報を読み込む処理
	 * @return				{@link AccountPrincipal}
	 */
	public AccountPrincipal get(String accountId, Supplier<AccountPrincipal> loader) {
		if (ttlMillis <= 0) {
			return loader.get();
		}

		long now = System.currentTimeMillis();

		Entry cached = cache.get(accountId);
		if (cached != null && cached.expiresAtMillis() > now) {
			return cached.principal();
		}

		AccountPrincipal loaded = loader.get();
		if (loaded != null) {
			if (cache.size() >= MAX_ENTRIES) {
				cache.clear();
			}
			cache.put(accountId, new Entry(loaded, now + ttlMillis));
		}
		return loaded;
	}

	/**
	 * キャッシュを全消去する<p>
	 * アカウントの削除・ロック・ロック解除（いずれも低頻度）が確定したときに呼び出し、
	 * 古い{@link AccountPrincipal}が最大TTLぶん参照され続けるのを防ぐ。
	 */
	public void clear() {
		cache.clear();
	}

	/**
	 * 指定したアカウントIDのキャッシュエントリだけを消去する
	 *
	 * @param	accountId	消去対象のアカウントID（nullは無視する）
	 */
	public void evict(String accountId) {
		if (accountId != null) {
			cache.remove(accountId);
		}
	}

	/**
	 * アカウント更新（アカウントID・パスワード・プロフィール変更）の確定時にキャッシュを消去する<p>
	 * プロフィール項目のみの更新でも発行されるイベントのため、全消去せず当該アカウントの
	 * エントリ（新旧アカウントID）だけを個別に失効させる。これにより、あるユーザーの軽微な
	 * プロフィール編集で認証ホットパスのキャッシュ全体が飛ぶのを防ぐ。
	 *
	 * @param	event	{@link AccountUpdatedEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAccountUpdated(AccountUpdatedEvent event) {
		evict(event.accountId() != null ? event.accountId().value() : null);
		evict(event.previousAccountId() != null ? event.previousAccountId().value() : null);
	}

	/**
	 * アカウント削除の確定時にキャッシュを消去する
	 *
	 * @param	event	{@link AccountDeletedEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAccountDeleted(AccountDeletedEvent event) {
		clear();
	}

	/**
	 * 管理者によるアカウント強制ロックの確定時にキャッシュを消去する（ロックを即時反映する）
	 *
	 * @param	event	{@link AccountLockedEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAccountLocked(AccountLockedEvent event) {
		clear();
	}

	/**
	 * 管理者によるアカウントロック解除の確定時にキャッシュを消去する
	 *
	 * @param	event	{@link AccountUnlockedEvent}
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAccountUnlocked(AccountUnlockedEvent event) {
		clear();
	}
}
