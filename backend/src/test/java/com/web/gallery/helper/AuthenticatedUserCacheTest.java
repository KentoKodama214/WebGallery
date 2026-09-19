package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.event.AccountDeletedEvent;
import com.web.gallery.event.AccountLockedEvent;
import com.web.gallery.event.AccountUnlockedEvent;
import com.web.gallery.event.AccountUpdatedEvent;
import com.web.gallery.model.AccountModel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/** {@link AuthenticatedUserCache}のユニットテスト */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthenticatedUserCacheTest {

  private AccountPrincipal principal() {
    return new AccountPrincipal(AccountModel.builder().accountNo(new AccountNo(1L)).build(), 3);
  }

  private void assertCleared(Consumer<AuthenticatedUserCache> fireEvent) {
    AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
    AtomicInteger calls = new AtomicInteger();
    Supplier<AccountPrincipal> loader =
        () -> {
          calls.incrementAndGet();
          return principal();
        };

    cache.get("aaaaaaaa", loader);
    cache.get("bbbbbbbb", loader);
    fireEvent.accept(cache);
    cache.get("aaaaaaaa", loader);
    cache.get("bbbbbbbb", loader);

    assertEquals(4, calls.get(), "イベント後はキャッシュが全消去され、loaderが再度呼ばれる");
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class get {
    @Test
    @Order(1)
    @DisplayName("正常系：TTL内は2回目以降loaderを呼ばずキャッシュを返すこと")
    void cache_hit_within_ttl() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      cache.get("aaaaaaaa", loader);
      cache.get("aaaaaaaa", loader);

      assertEquals(1, calls.get());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：TTLが0以下ならキャッシュせず毎回loaderを呼ぶこと")
    void cache_disabled_when_ttl_zero() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(0L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      cache.get("aaaaaaaa", loader);
      cache.get("aaaaaaaa", loader);

      assertEquals(2, calls.get());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：TTLを過ぎた場合はキャッシュを使わずloaderを再度呼ぶこと")
    void cache_miss_after_ttl_expired() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(1L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      cache.get("aaaaaaaa", loader);
      try {
        Thread.sleep(10L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      cache.get("aaaaaaaa", loader);

      assertEquals(2, calls.get(), "TTL切れ後はキャッシュを使わずloaderが再度呼ばれる");
    }

    @Test
    @Order(4)
    @DisplayName("正常系：loaderがnullを返した場合はキャッシュに格納せず、次回もloaderを呼ぶこと")
    void does_not_cache_null_result() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return null;
          };

      AccountPrincipal first = cache.get("aaaaaaaa", loader);
      AccountPrincipal second = cache.get("aaaaaaaa", loader);

      assertNull(first);
      assertNull(second);
      assertEquals(2, calls.get(), "loaderがnullを返した結果はキャッシュされない");
    }

    @Test
    @Order(5)
    @DisplayName("正常系：エントリ数が上限に達した場合、キャッシュを全クリアしてから格納すること")
    void clears_all_when_over_capacity() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      for (int i = 0; i < 10_000; i++) {
        cache.get("acc" + i, loader);
      }
      // 上限到達により、次のgetでキャッシュが全クリアされてから格納される
      cache.get("acc-over", loader);

      // 全クリアされているため、既存エントリは再度loaderが呼ばれる
      cache.get("acc0", loader);

      assertEquals(10_002, calls.get(), "上限到達時に全クリアされ、既存エントリのloaderが再度呼ばれる");
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class evict {
    @Test
    @Order(1)
    @DisplayName("正常系：accountIdがnullの場合は何もしないこと")
    void evict_ignoresNullAccountId() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };
      cache.get("aaaaaaaa", loader);

      assertDoesNotThrow(() -> cache.evict(null));

      cache.get("aaaaaaaa", loader);
      assertEquals(1, calls.get(), "evict(null)は既存キャッシュに影響しない");
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class onAccountDeleted {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント削除イベントでキャッシュが全消去されること")
    void clears_on_account_deleted() {
      AccountNo accountNo = new AccountNo(1L);

      assertCleared(cache -> cache.onAccountDeleted(new AccountDeletedEvent(accountNo, null)));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class onAccountLocked {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントロックイベントでキャッシュが全消去されること")
    void clears_on_account_locked() {
      AccountNo accountNo = new AccountNo(1L);

      assertCleared(cache -> cache.onAccountLocked(new AccountLockedEvent(accountNo)));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class onAccountUnlocked {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントロック解除イベントでキャッシュが全消去されること")
    void clears_on_account_unlocked() {
      AccountNo accountNo = new AccountNo(1L);

      assertCleared(cache -> cache.onAccountUnlocked(new AccountUnlockedEvent(accountNo)));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class onAccountUpdated {
    @Test
    @Order(1)
    @DisplayName("正常系：更新されたアカウントのエントリだけが失効し、他アカウントのキャッシュは保持されること")
    void evicts_only_updated_account() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      cache.get("aaaaaaaa", loader);
      cache.get("bbbbbbbb", loader);
      assertEquals(2, calls.get());

      cache.onAccountUpdated(
          new AccountUpdatedEvent(
              new AccountNo(1L), new AccountId("aaaaaaaa"), new AccountId("aaaaaaaa")));

      cache.get("aaaaaaaa", loader);
      cache.get("bbbbbbbb", loader);

      assertEquals(3, calls.get(), "更新アカウントのみloaderが再度呼ばれ、他アカウントはキャッシュヒットする");
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントID変更時は新旧どちらのエントリも失効すること")
    void evicts_both_old_and_new_account_id() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      cache.get("oldid000", loader);
      cache.get("newid000", loader);
      assertEquals(2, calls.get());

      cache.onAccountUpdated(
          new AccountUpdatedEvent(
              new AccountNo(1L), new AccountId("newid000"), new AccountId("oldid000")));

      cache.get("oldid000", loader);
      cache.get("newid000", loader);

      assertEquals(4, calls.get(), "新旧どちらのアカウントIDのエントリも失効している");
    }

    @Test
    @Order(3)
    @DisplayName("正常系：更新後アカウントIDが不明（accountId=null）の場合は旧IDのみ失効すること")
    void evicts_only_previous_account_id_when_new_id_unknown() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      cache.get("oldid000", loader);
      cache.get("otherid0", loader);
      assertEquals(2, calls.get());

      cache.onAccountUpdated(
          new AccountUpdatedEvent(new AccountNo(1L), null, new AccountId("oldid000")));

      cache.get("oldid000", loader);
      cache.get("otherid0", loader);

      assertEquals(3, calls.get(), "旧IDのみ失効し、無関係なアカウントのキャッシュは保持される");
    }

    @Test
    @Order(4)
    @DisplayName("正常系：更新前アカウントIDが不明（previousAccountId=null）の場合は安全側に倒して全消去すること")
    void clears_all_when_previous_account_id_unknown() {
      AuthenticatedUserCache cache = new AuthenticatedUserCache(10_000L);
      AtomicInteger calls = new AtomicInteger();
      Supplier<AccountPrincipal> loader =
          () -> {
            calls.incrementAndGet();
            return principal();
          };

      cache.get("aaaaaaaa", loader);
      cache.get("bbbbbbbb", loader);
      assertEquals(2, calls.get());

      cache.onAccountUpdated(
          new AccountUpdatedEvent(new AccountNo(1L), new AccountId("aaaaaaaa"), null));

      cache.get("aaaaaaaa", loader);
      cache.get("bbbbbbbb", loader);

      assertEquals(4, calls.get(), "旧IDを個別失効できないため全消去され、両アカウントで loader が再度呼ばれる");
    }
  }
}
