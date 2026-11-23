package com.web.gallery.infrastructure.persistence.mapper.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredential;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialCondition;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialUpdateTarget;
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
public class PasskeyCredentialMapperTest {
  @Autowired private PasskeyCredentialMapper passkeyCredentialMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final OffsetDateTime CREATED_AT_1 =
      OffsetDateTime.of(2003, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9))
          .withOffsetSameInstant(ZoneOffset.UTC);

  private Integer count(String condition) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM common.passkey_credential WHERE " + condition, Integer.class);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyCredentialMapperTest.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：登録でき、最終使用日時・作成日時・更新日時にトランザクションの現在時刻が設定されること")
    void insert_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actualCount =
          passkeyCredentialMapper.insert(
              PasskeyCredential.builder()
                  .accountNo(2L)
                  .credentialId("credential-new")
                  .attestedCredentialData(new byte[] {7, 8, 9})
                  .signCount(1L)
                  .transports("usb")
                  .isBackupEligible(false)
                  .isBackedUp(false)
                  .isUvInitialized(true)
                  .passkeyName("YubiKey")
                  .createdBy(2L)
                  .updatedBy(2L)
                  .build());

      assertEquals(1, actualCount);
      PasskeyCredential actual =
          passkeyCredentialMapper
              .select(PasskeyCredentialCondition.byCredentialId("credential-new"))
              .getFirst();
      assertEquals(2L, actual.getAccountNo());
      assertArrayEquals(new byte[] {7, 8, 9}, actual.getAttestedCredentialData());
      assertEquals(1L, actual.getSignCount());
      assertEquals("usb", actual.getTransports());
      assertFalse(actual.getIsBackupEligible());
      assertFalse(actual.getIsBackedUp());
      assertTrue(actual.getIsUvInitialized());
      assertEquals("YubiKey", actual.getPasskeyName());
      assertEquals(transactionNow, actual.getLastUsedAt());
      assertEquals(transactionNow, actual.getCreatedAt());
      assertEquals(transactionNow, actual.getUpdatedAt());
      assertEquals(2L, actual.getCreatedBy());
      assertEquals(2L, actual.getUpdatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：登録済みのクレデンシャルIDは一意制約違反になること")
    void insert_duplicateCredentialId() {
      assertThrows(
          org.springframework.dao.DuplicateKeyException.class,
          () ->
              passkeyCredentialMapper.insert(
                  PasskeyCredential.builder()
                      .accountNo(2L)
                      .credentialId("credential-1")
                      .attestedCredentialData(new byte[] {1})
                      .signCount(0L)
                      .transports("")
                      .isBackupEligible(false)
                      .isBackedUp(false)
                      .isUvInitialized(true)
                      .passkeyName("dup")
                      .createdBy(2L)
                      .updatedBy(2L)
                      .build()));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyCredentialMapperTest.sql")
  class select {
    @Test
    @Order(1)
    @DisplayName("アカウント番号で絞り込み、登録日時の昇順で全項目を取得すること")
    void select_byAccountNo() {
      List<PasskeyCredential> actual =
          passkeyCredentialMapper.select(PasskeyCredentialCondition.byAccountNo(1L));

      assertEquals(2, actual.size());
      PasskeyCredential first = actual.getFirst();
      assertEquals(1L, first.getPasskeyNo());
      assertEquals(1L, first.getAccountNo());
      assertEquals("credential-1", first.getCredentialId());
      assertArrayEquals(new byte[] {1, 2}, first.getAttestedCredentialData());
      assertEquals(5L, first.getSignCount());
      assertEquals("internal", first.getTransports());
      assertTrue(first.getIsBackupEligible());
      assertFalse(first.getIsBackedUp());
      assertTrue(first.getIsUvInitialized());
      assertEquals("MacBook", first.getPasskeyName());
      assertEquals(CREATED_AT_1, first.getLastUsedAt());
      assertEquals(CREATED_AT_1, first.getCreatedAt());
      assertEquals(2L, actual.get(1).getPasskeyNo());
    }

    @Test
    @Order(2)
    @DisplayName("クレデンシャルIDで絞り込むこと")
    void select_byCredentialId() {
      List<PasskeyCredential> actual =
          passkeyCredentialMapper.select(PasskeyCredentialCondition.byCredentialId("credential-3"));

      assertEquals(1, actual.size());
      assertEquals(3L, actual.getFirst().getPasskeyNo());
    }

    @Test
    @Order(3)
    @DisplayName("パスキー番号で絞り込むこと")
    void select_byPasskeyNo() {
      List<PasskeyCredential> actual =
          passkeyCredentialMapper.select(PasskeyCredentialCondition.byPasskeyNo(2L));

      assertEquals(1, actual.size());
      assertEquals("credential-2", actual.getFirst().getCredentialId());
    }

    @Test
    @Order(4)
    @DisplayName("アカウント番号・パスキー番号のAND条件で、他人のパスキーは取得しないこと")
    void select_byAccountNoAndPasskeyNo() {
      assertEquals(
          1,
          passkeyCredentialMapper
              .select(PasskeyCredentialCondition.byAccountNoAndPasskeyNo(1L, 1L))
              .size());
      assertTrue(
          passkeyCredentialMapper
              .select(PasskeyCredentialCondition.byAccountNoAndPasskeyNo(1L, 3L))
              .isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("条件が1つも指定されていない場合、全件ではなく0件を返すこと")
    void select_noCondition() {
      assertTrue(
          passkeyCredentialMapper.select(PasskeyCredentialCondition.builder().build()).isEmpty());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyCredentialMapperTest.sql")
  class selectForUpdate {
    @Test
    @Order(1)
    @DisplayName("クレデンシャルIDで絞り込んで取得すること")
    void selectForUpdate_byCredentialId() {
      List<PasskeyCredential> actual =
          passkeyCredentialMapper.selectForUpdate(
              PasskeyCredentialCondition.byCredentialId("credential-1"));

      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getPasskeyNo());
    }

    @Test
    @Order(2)
    @DisplayName("該当しない場合、空のリストを返すこと")
    void selectForUpdate_notFound() {
      assertTrue(
          passkeyCredentialMapper
              .selectForUpdate(PasskeyCredentialCondition.byCredentialId("none"))
              .isEmpty());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyCredentialMapperTest.sql")
  class count {
    @Test
    @Order(1)
    @DisplayName("アカウント番号で絞り込んだ件数を返すこと")
    void count_byAccountNo() {
      assertEquals(2, passkeyCredentialMapper.count(PasskeyCredentialCondition.byAccountNo(1L)));
      assertEquals(1, passkeyCredentialMapper.count(PasskeyCredentialCondition.byAccountNo(2L)));
      assertEquals(0, passkeyCredentialMapper.count(PasskeyCredentialCondition.byAccountNo(99L)));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyCredentialMapperTest.sql")
  class update {
    @Test
    @Order(1)
    @DisplayName("パスキー番号を条件に認証結果を反映し、パスキー名は変更しないこと")
    void update_authenticationResult() {
      OffsetDateTime lastUsedAt = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actualCount =
          passkeyCredentialMapper.update(
              PasskeyCredentialCondition.byPasskeyNo(1L),
              PasskeyCredentialUpdateTarget.builder()
                  .signCount(6L)
                  .isBackedUp(true)
                  .lastUsedAt(lastUsedAt)
                  .updatedBy(1L)
                  .build());

      assertEquals(1, actualCount);
      PasskeyCredential actual =
          passkeyCredentialMapper.select(PasskeyCredentialCondition.byPasskeyNo(1L)).getFirst();
      assertEquals(6L, actual.getSignCount());
      assertTrue(actual.getIsBackedUp());
      assertEquals(
          lastUsedAt.withOffsetSameInstant(actual.getLastUsedAt().getOffset()),
          actual.getLastUsedAt());
      assertEquals("MacBook", actual.getPasskeyName());
      assertEquals(transactionNow, actual.getUpdatedAt());
      // 他のパスキーは変更しない
      assertEquals(0, count("sign_count = 6 AND passkey_no <> 1"));
    }

    @Test
    @Order(2)
    @DisplayName("アカウント番号・パスキー番号を条件にパスキー名だけを変更すること")
    void update_rename() {
      Integer actualCount =
          passkeyCredentialMapper.update(
              PasskeyCredentialCondition.byAccountNoAndPasskeyNo(1L, 2L),
              PasskeyCredentialUpdateTarget.forRename("新しい名前", 1L));

      assertEquals(1, actualCount);
      PasskeyCredential actual =
          passkeyCredentialMapper.select(PasskeyCredentialCondition.byPasskeyNo(2L)).getFirst();
      assertEquals("新しい名前", actual.getPasskeyName());
      assertEquals(0L, actual.getSignCount());
    }

    @Test
    @Order(3)
    @DisplayName("他人のパスキーを条件にした場合、0件更新になること")
    void update_otherAccount() {
      assertEquals(
          0,
          passkeyCredentialMapper.update(
              PasskeyCredentialCondition.byAccountNoAndPasskeyNo(1L, 3L),
              PasskeyCredentialUpdateTarget.forRename("乗っ取り", 1L)));
      assertEquals(1, count("passkey_no = 3 AND passkey_name = 'iPhone'"));
    }

    @Test
    @Order(4)
    @DisplayName("条件が1つも指定されていない場合、全件更新せず0件になること")
    void update_noCondition() {
      assertEquals(
          0,
          passkeyCredentialMapper.update(
              PasskeyCredentialCondition.builder().build(),
              PasskeyCredentialUpdateTarget.forRename("全件", 1L)));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PasskeyCredentialMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("アカウント番号・パスキー番号を条件に1件削除すること")
    void delete_byAccountNoAndPasskeyNo() {
      assertEquals(
          1,
          passkeyCredentialMapper.delete(
              PasskeyCredentialCondition.byAccountNoAndPasskeyNo(1L, 1L)));
      assertEquals(0, count("passkey_no = 1"));
      assertEquals(2, count("1 = 1"));
    }

    @Test
    @Order(2)
    @DisplayName("アカウント番号を条件に、そのアカウントのパスキーをすべて削除すること")
    void delete_byAccountNo() {
      assertEquals(2, passkeyCredentialMapper.delete(PasskeyCredentialCondition.byAccountNo(1L)));
      assertEquals(1, count("account_no = 2"));
    }

    @Test
    @Order(3)
    @DisplayName("他人のパスキーを条件にした場合、削除しないこと")
    void delete_otherAccount() {
      assertEquals(
          0,
          passkeyCredentialMapper.delete(
              PasskeyCredentialCondition.byAccountNoAndPasskeyNo(1L, 3L)));
      assertEquals(3, count("1 = 1"));
    }

    @Test
    @Order(4)
    @DisplayName("条件が1つも指定されていない場合、全件削除せず0件になること")
    void delete_noCondition() {
      assertEquals(0, passkeyCredentialMapper.delete(PasskeyCredentialCondition.builder().build()));
      assertEquals(3, count("1 = 1"));
    }
  }
}
