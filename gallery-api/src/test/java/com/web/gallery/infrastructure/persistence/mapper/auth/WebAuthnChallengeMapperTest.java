package com.web.gallery.infrastructure.persistence.mapper.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallenge;
import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallengeCondition;
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
public class WebAuthnChallengeMapperTest {
  @Autowired private WebAuthnChallengeMapper webAuthnChallengeMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private Integer count(String condition) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM common.webauthn_challenge WHERE " + condition, Integer.class);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/WebAuthnChallengeMapperTest.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：用途区分をEnum型に変換して登録でき、作成日時にトランザクションの現在時刻が設定されること")
    void insert_success() {
      OffsetDateTime expiresAt = OffsetDateTime.of(2099, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      assertEquals(
          1,
          webAuthnChallengeMapper.insert(
              WebAuthnChallenge.builder()
                  .challenge("challenge-new")
                  .purposeKbn(WebAuthnChallengePurposeEnum.REGISTRATION)
                  .accountNo(2L)
                  .expiresAt(expiresAt)
                  .createdBy(2L)
                  .build()));

      assertEquals(
          1,
          count(
              "challenge = 'challenge-new' AND purpose_kbn = 'registration' AND account_no = 2"
                  + " AND created_by = 2"));
      assertEquals(
          transactionNow,
          jdbcTemplate.queryForObject(
              "SELECT created_at FROM common.webauthn_challenge WHERE challenge = 'challenge-new'",
              OffsetDateTime.class));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/WebAuthnChallengeMapperTest.sql")
  class consume {
    @Test
    @Order(1)
    @DisplayName("正常系：チャレンジ・用途区分が一致する有効期限内の行を削除し、削除した行を返すこと")
    void consume_success() {
      List<WebAuthnChallenge> actual =
          webAuthnChallengeMapper.consume(
              WebAuthnChallengeCondition.byChallengeAndPurpose(
                  "challenge-reg", WebAuthnChallengePurposeEnum.REGISTRATION));

      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getChallengeNo());
      assertEquals("challenge-reg", actual.getFirst().getChallenge());
      assertEquals(WebAuthnChallengePurposeEnum.REGISTRATION, actual.getFirst().getPurposeKbn());
      assertEquals(1L, actual.getFirst().getAccountNo());
      assertEquals(
          OffsetDateTime.of(2099, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9))
              .withOffsetSameInstant(ZoneOffset.UTC),
          actual.getFirst().getExpiresAt());
      assertEquals(0, count("challenge = 'challenge-reg'"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じチャレンジを2回消費した場合、2回目は空のリストを返すこと（リプレイ防止）")
    void consume_twice() {
      WebAuthnChallengeCondition condition =
          WebAuthnChallengeCondition.byChallengeAndPurpose(
              "challenge-auth", WebAuthnChallengePurposeEnum.AUTHENTICATION);

      assertEquals(1, webAuthnChallengeMapper.consume(condition).size());
      assertTrue(webAuthnChallengeMapper.consume(condition).isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：用途区分が異なる場合、消費せず空のリストを返すこと")
    void consume_purposeMismatch() {
      assertTrue(
          webAuthnChallengeMapper
              .consume(
                  WebAuthnChallengeCondition.byChallengeAndPurpose(
                      "challenge-reg", WebAuthnChallengePurposeEnum.AUTHENTICATION))
              .isEmpty());
      assertEquals(1, count("challenge = 'challenge-reg'"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：有効期限切れの場合、消費せず空のリストを返すこと")
    void consume_expired() {
      assertTrue(
          webAuthnChallengeMapper
              .consume(
                  WebAuthnChallengeCondition.byChallengeAndPurpose(
                      "challenge-expired", WebAuthnChallengePurposeEnum.AUTHENTICATION))
              .isEmpty());
      assertEquals(1, count("challenge = 'challenge-expired'"));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：チャレンジ・用途区分のいずれかが未指定の場合、何も消費しないこと")
    void consume_missingCondition() {
      assertTrue(
          webAuthnChallengeMapper.consume(WebAuthnChallengeCondition.builder().build()).isEmpty());
      assertTrue(
          webAuthnChallengeMapper
              .consume(WebAuthnChallengeCondition.builder().challenge("challenge-reg").build())
              .isEmpty());
      assertEquals(3, count("1 = 1"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/WebAuthnChallengeMapperTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("アカウント番号を条件に、そのアカウントのチャレンジだけを削除すること")
    void delete_byAccountNo() {
      assertEquals(1, webAuthnChallengeMapper.delete(WebAuthnChallengeCondition.byAccountNo(1L)));
      assertEquals(2, count("account_no = 0"));
    }

    @Test
    @Order(2)
    @DisplayName("条件が1つも指定されていない場合、全件削除せず0件になること")
    void delete_noCondition() {
      assertEquals(0, webAuthnChallengeMapper.delete(WebAuthnChallengeCondition.builder().build()));
      assertEquals(3, count("1 = 1"));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/WebAuthnChallengeMapperTest.sql")
  class deleteExpired {
    @Test
    @Order(1)
    @DisplayName("有効期限切れのチャレンジだけを削除すること")
    void deleteExpired_success() {
      assertEquals(1, webAuthnChallengeMapper.deleteExpired());
      assertEquals(0, count("challenge = 'challenge-expired'"));
      assertEquals(2, count("1 = 1"));
    }
  }
}
