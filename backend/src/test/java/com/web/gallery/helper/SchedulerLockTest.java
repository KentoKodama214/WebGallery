package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.enumeration.SchedulerLockName;
import com.web.gallery.repository.impl.SchedulerLockRepositoryImpl;
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
public class SchedulerLockTest {
  @InjectMocks private SchedulerLock schedulerLock;

  @Mock private SchedulerLockRepositoryImpl schedulerLockRepositoryImpl;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class runIfLocked {
    @Test
    @Order(1)
    @DisplayName("正常系：ロックを取得できた場合はtaskを実行する")
    void runIfLocked_acquired() {
      doReturn(true)
          .when(schedulerLockRepositoryImpl)
          .tryLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP);
      Runnable task = mock(Runnable.class);

      schedulerLock.runIfLocked(SchedulerLockName.REFRESH_TOKEN_CLEANUP, task);

      verify(task, times(1)).run();
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ロックを取得できなかった場合はtaskを実行しない")
    void runIfLocked_notAcquired() {
      doReturn(false)
          .when(schedulerLockRepositoryImpl)
          .tryLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP);
      Runnable task = mock(Runnable.class);

      schedulerLock.runIfLocked(SchedulerLockName.REFRESH_TOKEN_CLEANUP, task);

      verify(task, never()).run();
    }

    @Test
    @Order(3)
    @DisplayName("異常系：taskが例外を投げた場合はそのまま再スローする")
    void runIfLocked_taskThrows() {
      doReturn(true)
          .when(schedulerLockRepositoryImpl)
          .tryLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP);
      RuntimeException cause = new RuntimeException("処理失敗");
      Runnable task =
          () -> {
            throw cause;
          };

      RuntimeException actual =
          assertThrows(
              RuntimeException.class,
              () -> schedulerLock.runIfLocked(SchedulerLockName.REFRESH_TOKEN_CLEANUP, task));

      assertSame(cause, actual);
    }
  }
}
