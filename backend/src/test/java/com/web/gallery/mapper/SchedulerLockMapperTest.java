package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.enumeration.SchedulerLockName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SchedulerLockMapperTest {
  @Autowired private SchedulerLockMapper schedulerLockMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class tryAdvisoryXactLock {
    @Test
    @Order(1)
    @DisplayName("正常系：他セッションが保持していないロックは取得できてtrueを返す")
    void tryAdvisoryXactLock_acquired() {
      Boolean actual =
          schedulerLockMapper.tryAdvisoryXactLock(
              SchedulerLockName.REFRESH_TOKEN_CLEANUP.getLockKey());

      assertEquals(Boolean.TRUE, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：同一トランザクションからの再取得もtrueを返す（再入可能）")
    void tryAdvisoryXactLock_reentrant() {
      long lockKey = SchedulerLockName.REFRESH_TOKEN_CLEANUP.getLockKey();

      assertEquals(Boolean.TRUE, schedulerLockMapper.tryAdvisoryXactLock(lockKey));
      assertEquals(Boolean.TRUE, schedulerLockMapper.tryAdvisoryXactLock(lockKey));
    }
  }
}
