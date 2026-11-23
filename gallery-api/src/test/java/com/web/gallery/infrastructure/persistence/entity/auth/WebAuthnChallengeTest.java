package com.web.gallery.infrastructure.persistence.entity.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.common.ExpiresAt;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class WebAuthnChallengeTest {

  private static final OffsetDateTime EXPIRES_AT =
      OffsetDateTime.of(2026, 1, 1, 0, 5, 0, 0, ZoneOffset.ofHours(9));

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：登録用チャレンジの場合、アカウント番号を作成者にも設定すること")
    void from_registration() {
      WebAuthnChallenge actual =
          WebAuthnChallenge.from(
              WebAuthnChallengeModel.of(
                  new PasskeyChallenge("challenge-1"),
                  WebAuthnChallengePurposeEnum.REGISTRATION,
                  new AccountNo(1L),
                  new ExpiresAt(EXPIRES_AT)));

      assertNull(actual.getChallengeNo());
      assertEquals("challenge-1", actual.getChallenge());
      assertEquals(WebAuthnChallengePurposeEnum.REGISTRATION, actual.getPurposeKbn());
      assertEquals(1L, actual.getAccountNo());
      assertEquals(EXPIRES_AT, actual.getExpiresAt());
      assertEquals(1L, actual.getCreatedBy());
      assertNull(actual.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント未確定（null）の認証用チャレンジの場合、アカウント番号・作成者に0を設定すること")
    void from_authenticationWithoutAccount() {
      WebAuthnChallenge actual =
          WebAuthnChallenge.from(
              WebAuthnChallengeModel.of(
                  new PasskeyChallenge("challenge-2"),
                  WebAuthnChallengePurposeEnum.AUTHENTICATION,
                  null,
                  new ExpiresAt(EXPIRES_AT)));

      assertEquals(WebAuthnChallengePurposeEnum.AUTHENTICATION, actual.getPurposeKbn());
      assertEquals(Consts.ACCOUNT_NO_UNKNOWN, actual.getAccountNo());
      assertEquals(Consts.ACCOUNT_NO_UNKNOWN, actual.getCreatedBy());
    }
  }
}
