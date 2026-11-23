package com.web.gallery.infrastructure.scheduler;

import static org.mockito.Mockito.*;

import com.web.gallery.application.service.auth.AuthService;
import com.web.gallery.infrastructure.scheduler.lock.SchedulerLock;
import com.web.gallery.infrastructure.scheduler.lock.SchedulerLockNameEnum;
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
public class WebAuthnChallengeCleanupSchedulerTest {
  @InjectMocks private WebAuthnChallengeCleanupScheduler webAuthnChallengeCleanupScheduler;

  @Mock private SchedulerLock schedulerLock;

  @Mock private AuthService authService;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class purgeExpiredWebAuthnChallenges {
    @Test
    @Order(1)
    @DisplayName("正常系：WEBAUTHN_CHALLENGE_CLEANUPロックを介してWebAuthnチャレンジ削除処理を実行する")
    void purgeExpiredWebAuthnChallenges_runsUnderLock() {
      doAnswer(
              invocation -> {
                invocation.getArgument(1, Runnable.class).run();
                return null;
              })
          .when(schedulerLock)
          .runIfLocked(eq(SchedulerLockNameEnum.WEBAUTHN_CHALLENGE_CLEANUP), any(Runnable.class));

      webAuthnChallengeCleanupScheduler.purgeExpiredWebAuthnChallenges();

      verify(schedulerLock, times(1))
          .runIfLocked(eq(SchedulerLockNameEnum.WEBAUTHN_CHALLENGE_CLEANUP), any(Runnable.class));
      verify(authService, times(1)).purgeExpiredWebAuthnChallenges();
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ロックを取得できなかった場合はWebAuthnチャレンジ削除処理を実行しない")
    void purgeExpiredWebAuthnChallenges_skippedWhenLockNotAcquired() {
      // runIfLocked が task を実行しない（ロック未取得）ケースを模倣し、何もしない
      webAuthnChallengeCleanupScheduler.purgeExpiredWebAuthnChallenges();

      verify(authService, never()).purgeExpiredWebAuthnChallenges();
    }
  }
}
