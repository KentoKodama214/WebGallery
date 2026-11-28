package com.web.gallery.infrastructure.persistence.mapper.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandle;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandleCondition;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PasskeyUserHandleMapperTest {
  @Autowired private PasskeyUserHandleMapper passkeyUserHandleMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyUserHandleMapperTest.sql")
  class insertIfAbsent {
    @Test
    @Order(1)
    @DisplayName("正常系：未発行のアカウントには登録され、作成日時にトランザクションの現在時刻が設定されること")
    void insertIfAbsent_new() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      assertEquals(1, passkeyUserHandleMapper.insertIfAbsent(PasskeyUserHandle.of(2L, "handle-2")));

      PasskeyUserHandle actual =
          passkeyUserHandleMapper.select(PasskeyUserHandleCondition.byAccountNo(2L)).getFirst();
      assertEquals("handle-2", actual.getUserHandle());
      assertEquals(2L, actual.getCreatedBy());
      assertEquals(transactionNow, actual.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：発行済みのアカウントでは何もせず、既存のユーザーハンドルを残すこと")
    void insertIfAbsent_existing() {
      assertEquals(0, passkeyUserHandleMapper.insertIfAbsent(PasskeyUserHandle.of(1L, "handle-x")));

      List<PasskeyUserHandle> actual =
          passkeyUserHandleMapper.select(PasskeyUserHandleCondition.byAccountNo(1L));
      assertEquals(1, actual.size());
      assertEquals("handle-1", actual.getFirst().getUserHandle());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyUserHandleMapperTest.sql")
  class select {
    @Test
    @Order(1)
    @DisplayName("アカウント番号で絞り込み、全項目を取得すること")
    void select_byAccountNo() {
      List<PasskeyUserHandle> actual =
          passkeyUserHandleMapper.select(PasskeyUserHandleCondition.byAccountNo(1L));

      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getPasskeyUserHandleNo());
      assertEquals(1L, actual.getFirst().getAccountNo());
      assertEquals("handle-1", actual.getFirst().getUserHandle());
      assertEquals(1L, actual.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2003, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9))
              .withOffsetSameInstant(ZoneOffset.UTC),
          actual.getFirst().getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("未発行のアカウント・条件未指定の場合、空のリストを返すこと")
    void select_notFound() {
      assertTrue(
          passkeyUserHandleMapper.select(PasskeyUserHandleCondition.byAccountNo(2L)).isEmpty());
      assertTrue(
          passkeyUserHandleMapper.select(PasskeyUserHandleCondition.builder().build()).isEmpty());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyUserHandleMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("アカウント番号を条件に削除すること")
    void delete_byAccountNo() {
      assertEquals(1, passkeyUserHandleMapper.delete(PasskeyUserHandleCondition.byAccountNo(1L)));
      assertTrue(
          passkeyUserHandleMapper.select(PasskeyUserHandleCondition.byAccountNo(1L)).isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("条件が1つも指定されていない場合、全件削除せず0件になること")
    void delete_noCondition() {
      assertEquals(0, passkeyUserHandleMapper.delete(PasskeyUserHandleCondition.builder().build()));
    }
  }
}
