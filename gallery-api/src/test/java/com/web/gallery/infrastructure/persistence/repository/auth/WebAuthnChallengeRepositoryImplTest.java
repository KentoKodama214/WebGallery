package com.web.gallery.infrastructure.persistence.repository.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallenge;
import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallengeCondition;
import com.web.gallery.infrastructure.persistence.mapper.auth.WebAuthnChallengeMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WebAuthnChallengeRepositoryImplTest {
  private static final OffsetDateTime EXPIRES_AT =
      OffsetDateTime.of(2026, 1, 1, 0, 5, 0, 0, ZoneOffset.ofHours(9));

  @InjectMocks private WebAuthnChallengeRepositoryImpl webAuthnChallengeRepositoryImpl;

  @Mock private WebAuthnChallengeMapper webAuthnChallengeMapper;

  private static WebAuthnChallenge entity(WebAuthnChallengePurposeEnum purpose, Long accountNo) {
    return WebAuthnChallenge.builder()
        .challengeNo(1L)
        .challenge("challenge-1")
        .purposeKbn(purpose)
        .accountNo(accountNo)
        .expiresAt(EXPIRES_AT)
        .createdBy(accountNo)
        .createdAt(EXPIRES_AT.minusMinutes(5))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class save {
    @Test
    @Order(1)
    @DisplayName("正常系：Modelから変換したEntityを登録すること")
    void save_success() {
      webAuthnChallengeRepositoryImpl.save(
          WebAuthnChallengeModel.of(
              new PasskeyChallenge("challenge-1"),
              WebAuthnChallengePurposeEnum.REGISTRATION,
              new AccountNo(1L),
              new ExpiresAt(EXPIRES_AT)));

      ArgumentCaptor<WebAuthnChallenge> captor = ArgumentCaptor.forClass(WebAuthnChallenge.class);
      verify(webAuthnChallengeMapper).insert(captor.capture());
      assertEquals("challenge-1", captor.getValue().getChallenge());
      assertEquals(WebAuthnChallengePurposeEnum.REGISTRATION, captor.getValue().getPurposeKbn());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals(EXPIRES_AT, captor.getValue().getExpiresAt());
      assertEquals(1L, captor.getValue().getCreatedBy());
      // 新規登録用以外はユーザーハンドルを空文字で保存する
      assertEquals("", captor.getValue().getUserHandle());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：新規登録用はアカウント番号0とユーザーハンドルを保存すること")
    void save_signup() {
      webAuthnChallengeRepositoryImpl.save(
          WebAuthnChallengeModel.forSignup(
              new PasskeyChallenge("challenge-1"),
              new PasskeyUserHandle("handle-1"),
              new ExpiresAt(EXPIRES_AT)));

      ArgumentCaptor<WebAuthnChallenge> captor = ArgumentCaptor.forClass(WebAuthnChallenge.class);
      verify(webAuthnChallengeMapper).insert(captor.capture());
      assertEquals(WebAuthnChallengePurposeEnum.SIGNUP, captor.getValue().getPurposeKbn());
      assertEquals(0L, captor.getValue().getAccountNo());
      assertEquals("handle-1", captor.getValue().getUserHandle());
      assertEquals(0L, captor.getValue().getCreatedBy());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class consumeForRegistration {
    @Test
    @Order(1)
    @DisplayName("正常系：チャレンジと用途区分を条件に消費し、Modelへ変換すること")
    void consumeForRegistration_found() {
      doReturn(List.of(entity(WebAuthnChallengePurposeEnum.REGISTRATION, 1L)))
          .when(webAuthnChallengeMapper)
          .consume(any());

      WebAuthnChallengeModel actual =
          webAuthnChallengeRepositoryImpl.consumeForRegistration(
              new PasskeyChallenge("challenge-1"));

      ArgumentCaptor<WebAuthnChallengeCondition> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeCondition.class);
      verify(webAuthnChallengeMapper).consume(captor.capture());
      assertEquals("challenge-1", captor.getValue().getChallenge());
      assertEquals(WebAuthnChallengePurposeEnum.REGISTRATION, captor.getValue().getPurposeKbn());

      assertEquals(new PasskeyChallenge("challenge-1"), actual.getChallenge());
      assertEquals(WebAuthnChallengePurposeEnum.REGISTRATION, actual.getPurposeKbn());
      assertEquals(new AccountNo(1L), actual.getAccountNo());
      // 新規登録用以外はユーザーハンドルを持たない（空文字はnullへ変換する）
      assertNull(actual.getUserHandle());
      assertEquals(EXPIRES_AT, actual.getExpiresAt().value());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class consumeForSignup {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号0をアカウント未確定（null）へ、ユーザーハンドルを値オブジェクトへ変換すること")
    void consumeForSignup_found() {
      WebAuthnChallenge signup = entity(WebAuthnChallengePurposeEnum.SIGNUP, 0L);
      signup.setUserHandle("handle-1");
      doReturn(List.of(signup)).when(webAuthnChallengeMapper).consume(any());

      WebAuthnChallengeModel actual =
          webAuthnChallengeRepositoryImpl.consumeForSignup(new PasskeyChallenge("challenge-1"));

      ArgumentCaptor<WebAuthnChallengeCondition> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeCondition.class);
      verify(webAuthnChallengeMapper).consume(captor.capture());
      assertEquals(WebAuthnChallengePurposeEnum.SIGNUP, captor.getValue().getPurposeKbn());
      assertNull(actual.getAccountNo());
      assertEquals(new PasskeyUserHandle("handle-1"), actual.getUserHandle());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class consumeForAuthentication {
    @Test
    @Order(1)
    @DisplayName("異常系：該当しない（未発行・使用済み・期限切れ・用途違い）場合、nullを返すこと")
    void consumeForAuthentication_notFound() {
      doReturn(List.of()).when(webAuthnChallengeMapper).consume(any());

      assertNull(
          webAuthnChallengeRepositoryImpl.consumeForAuthentication(new PasskeyChallenge("none")));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteExpired {
    @Test
    @Order(1)
    @DisplayName("正常系：有効期限切れのチャレンジの削除をMapperに委譲すること")
    void deleteExpired_success() {
      webAuthnChallengeRepositoryImpl.deleteExpired();

      verify(webAuthnChallengeMapper).deleteExpired();
    }
  }
}
