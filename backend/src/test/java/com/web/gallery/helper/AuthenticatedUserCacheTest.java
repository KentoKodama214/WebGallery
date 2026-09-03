package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.event.AccountDeletedEvent;
import com.web.gallery.event.AccountLockedEvent;
import com.web.gallery.event.AccountUnlockedEvent;
import com.web.gallery.event.AccountUpdatedEvent;
import com.web.gallery.model.AccountModel;

/**
 * {@link AuthenticatedUserCache}のユニットテスト
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthenticatedUserCacheTest {

	private AccountPrincipal principal() {
		return new AccountPrincipal(AccountModel.builder().accountNo(new AccountNo(1L)).build(), 3);
	}

	@Nested
	@DisplayName("get")
	class Get {
		@Test
		@DisplayName("TTL内は2回目以降loaderを呼ばずキャッシュを返す")
		void cache_hit_within_ttl() {
			AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
			AtomicInteger calls = new AtomicInteger();
			Supplier<AccountPrincipal> loader = () -> {
				calls.incrementAndGet();
				return principal();
			};

			cache.get("aaaaaaaa", loader);
			cache.get("aaaaaaaa", loader);

			assertEquals(1, calls.get());
		}

		@Test
		@DisplayName("TTLが0以下ならキャッシュせず毎回loaderを呼ぶ")
		void cache_disabled_when_ttl_zero() {
			AuthenticatedUserCache cache = new AuthenticatedUserCache(0L);
			AtomicInteger calls = new AtomicInteger();
			Supplier<AccountPrincipal> loader = () -> {
				calls.incrementAndGet();
				return principal();
			};

			cache.get("aaaaaaaa", loader);
			cache.get("aaaaaaaa", loader);

			assertEquals(2, calls.get());
		}
	}

	@Nested
	@DisplayName("イベントによる全消去")
	class ClearOnEvent {
		@Test
		@DisplayName("アカウント更新・削除・ロック・ロック解除の各イベントでキャッシュが消える")
		void clears_on_account_events() {
			AccountNo accountNo = new AccountNo(1L);

			assertCleared(cache -> cache.onAccountUpdated(new AccountUpdatedEvent(accountNo, null)));
			assertCleared(cache -> cache.onAccountDeleted(new AccountDeletedEvent(accountNo, null)));
			assertCleared(cache -> cache.onAccountLocked(new AccountLockedEvent(accountNo)));
			assertCleared(cache -> cache.onAccountUnlocked(new AccountUnlockedEvent(accountNo)));
		}

		private void assertCleared(java.util.function.Consumer<AuthenticatedUserCache> fireEvent) {
			AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
			AtomicInteger calls = new AtomicInteger();
			Supplier<AccountPrincipal> loader = () -> {
				calls.incrementAndGet();
				return principal();
			};

			cache.get("aaaaaaaa", loader);
			fireEvent.accept(cache);
			cache.get("aaaaaaaa", loader);

			assertEquals(2, calls.get(), "イベント後はキャッシュが消え、loaderが再度呼ばれる");
		}
	}
}
