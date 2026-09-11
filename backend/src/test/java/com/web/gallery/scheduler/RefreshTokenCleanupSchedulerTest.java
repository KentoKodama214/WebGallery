package com.web.gallery.scheduler;

import static org.mockito.Mockito.*;

import com.web.gallery.enumeration.SchedulerLockName;
import com.web.gallery.helper.SchedulerLock;
import com.web.gallery.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class RefreshTokenCleanupSchedulerTest {
  @InjectMocks private RefreshTokenCleanupScheduler refreshTokenCleanupScheduler;

  @Mock private SchedulerLock schedulerLock;

  @Mock private AuthService authService;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class purgeExpiredRefreshTokens {
    @Test
    @Order(1)
    @DisplayName("正常系：REFRESH_TOKEN_CLEANUPロックを介してリフレッシュトークン削除処理を実行する")
    void purgeExpiredRefreshTokens_runsUnderLock() {
      doAnswer(
              invocation -> {
                invocation.getArgument(1, Runnable.class).run();
                return null;
              })
          .when(schedulerLock)
          .runIfLocked(eq(SchedulerLockName.REFRESH_TOKEN_CLEANUP), any(Runnable.class));

      refreshTokenCleanupScheduler.purgeExpiredRefreshTokens();

      verify(schedulerLock, times(1))
          .runIfLocked(eq(SchedulerLockName.REFRESH_TOKEN_CLEANUP), any(Runnable.class));
      verify(authService, times(1)).purgeExpiredRefreshTokens();
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ロックを取得できなかった場合はリフレッシュトークン削除処理を実行しない")
    void purgeExpiredRefreshTokens_skippedWhenLockNotAcquired() {
      // runIfLocked が task を実行しない（ロック未取得）ケースを模倣し、何もしない
      refreshTokenCleanupScheduler.purgeExpiredRefreshTokens();

      verify(authService, never()).purgeExpiredRefreshTokens();
    }
  }
}
