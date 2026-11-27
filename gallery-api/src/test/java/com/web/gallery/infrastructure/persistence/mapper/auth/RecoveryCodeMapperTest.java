package com.web.gallery.infrastructure.persistence.mapper.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCode;
import com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCodeCondition;
import java.time.OffsetDateTime;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RecoveryCodeMapperTest {
  @Autowired private RecoveryCodeMapper recoveryCodeMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private Integer countRows(Long accountNo) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM common.recovery_code WHERE account_no = ?", Integer.class, accountNo);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RecoveryCodeMapperTest.sql")
  class bulkInsert {
    @Test
    @Order(1)
    @DisplayName("正常系：まとめて登録でき、作成者・作成日時（トランザクションの現在時刻）が設定されること")
    void bulkInsert_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      assertEquals(
          2,
          recoveryCodeMapper.bulkInsert(
              List.of(RecoveryCode.of(2L, "hash-2b"), RecoveryCode.of(2L, "hash-2c"))));

      assertEquals(3, countRows(2L));
      List<Object[]> rows =
          jdbcTemplate.query(
              "SELECT created_by, created_at FROM common.recovery_code WHERE code_hash = 'hash-2b'",
              (rs, rowNum) -> new Object[] {rs.getLong(1), rs.getObject(2, OffsetDateTime.class)});
      assertEquals(2L, rows.getFirst()[0]);
      assertEquals(transactionNow, rows.getFirst()[1]);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じアカウントに同じコードハッシュは登録できないこと")
    void bulkInsert_duplicate() {
      assertThrows(
          DuplicateKeyException.class,
          () -> recoveryCodeMapper.bulkInsert(List.of(RecoveryCode.of(1L, "hash-1a"))));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RecoveryCodeMapperTest.sql")
  class count {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号で件数を数えること")
    void count_byAccountNo() {
      assertEquals(2, recoveryCodeMapper.count(RecoveryCodeCondition.byAccountNo(1L)));
      assertEquals(1, recoveryCodeMapper.count(RecoveryCodeCondition.byAccountNo(2L)));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウント番号が未指定の場合、全件を数えず0件とすること")
    void count_noAccountNo() {
      assertEquals(0, recoveryCodeMapper.count(RecoveryCodeCondition.builder().build()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RecoveryCodeMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・コードハッシュが一致する1件だけを削除すること")
    void delete_byAccountNoAndCodeHash() {
      assertEquals(
          1,
          recoveryCodeMapper.delete(RecoveryCodeCondition.byAccountNoAndCodeHash(1L, "hash-1a")));

      assertEquals(1, countRows(1L));
      assertEquals(1, countRows(2L));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：他のアカウントのコードハッシュでは削除しないこと")
    void delete_otherAccountsCode() {
      assertEquals(
          0,
          recoveryCodeMapper.delete(RecoveryCodeCondition.byAccountNoAndCodeHash(1L, "hash-2a")));

      assertEquals(1, countRows(2L));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウント番号のみ指定した場合、そのアカウントのコードをすべて削除すること")
    void delete_byAccountNo() {
      assertEquals(2, recoveryCodeMapper.delete(RecoveryCodeCondition.byAccountNo(1L)));

      assertEquals(0, countRows(1L));
      assertEquals(1, countRows(2L));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：アカウント番号が未指定の場合、全件を削除しないこと")
    void delete_noAccountNo() {
      assertEquals(
          0,
          recoveryCodeMapper.delete(RecoveryCodeCondition.builder().codeHash("hash-1a").build()));

      assertEquals(2, countRows(1L));
    }
  }
}
