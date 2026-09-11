package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.enumeration.SchedulerLockName;
import com.web.gallery.mapper.SchedulerLockMapper;
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
public class SchedulerLockRepositoryImplTest {
  @InjectMocks private SchedulerLockRepositoryImpl schedulerLockRepositoryImpl;

  @Mock private SchedulerLockMapper schedulerLockMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class tryLock {
    @Test
    @Order(1)
    @DisplayName("正常系：ロックを取得できた場合はtrueを返す")
    void tryLock_acquired() {
      doReturn(Boolean.TRUE)
          .when(schedulerLockMapper)
          .tryAdvisoryXactLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP.getLockKey());

      boolean actual = schedulerLockRepositoryImpl.tryLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP);

      assertTrue(actual);
      verify(schedulerLockMapper, times(1))
          .tryAdvisoryXactLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP.getLockKey());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ロックを取得できなかった場合はfalseを返す")
    void tryLock_notAcquired() {
      doReturn(Boolean.FALSE)
          .when(schedulerLockMapper)
          .tryAdvisoryXactLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP.getLockKey());

      boolean actual = schedulerLockRepositoryImpl.tryLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP);

      assertFalse(actual);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：Mapperがnullを返した場合はfalseとして扱う")
    void tryLock_null() {
      doReturn(null)
          .when(schedulerLockMapper)
          .tryAdvisoryXactLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP.getLockKey());

      boolean actual = schedulerLockRepositoryImpl.tryLock(SchedulerLockName.REFRESH_TOKEN_CLEANUP);

      assertFalse(actual);
    }
  }
}
