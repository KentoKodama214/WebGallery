package com.web.gallery.infrastructure.persistence.repository.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
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
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class consumeForRegistration {
    @Test
    @Order(1)
    @DisplayName("正常系：チャレンジと登録用の用途区分を条件に消費し、Modelへ変換すること")
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
      assertEquals(EXPIRES_AT, actual.getExpiresAt().value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：該当しない（未発行・使用済み・期限切れ・用途違い）場合、nullを返すこと")
    void consumeForRegistration_notFound() {
      doReturn(List.of()).when(webAuthnChallengeMapper).consume(any());

      assertNull(
          webAuthnChallengeRepositoryImpl.consumeForRegistration(new PasskeyChallenge("none")));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class consumeForAuthentication {
    @Test
    @Order(1)
    @DisplayName("正常系：認証用の用途区分を条件に消費し、アカウント番号0をアカウント未確定（null）へ変換すること")
    void consumeForAuthentication_found() {
      doReturn(List.of(entity(WebAuthnChallengePurposeEnum.AUTHENTICATION, 0L)))
          .when(webAuthnChallengeMapper)
          .consume(any());

      WebAuthnChallengeModel actual =
          webAuthnChallengeRepositoryImpl.consumeForAuthentication(
              new PasskeyChallenge("challenge-1"));

      ArgumentCaptor<WebAuthnChallengeCondition> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeCondition.class);
      verify(webAuthnChallengeMapper).consume(captor.capture());
      assertEquals(WebAuthnChallengePurposeEnum.AUTHENTICATION, captor.getValue().getPurposeKbn());
      assertNull(actual.getAccountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：該当しない場合、nullを返すこと")
    void consumeForAuthentication_notFound() {
      doReturn(List.of()).when(webAuthnChallengeMapper).consume(any());

      assertNull(
          webAuthnChallengeRepositoryImpl.consumeForAuthentication(new PasskeyChallenge("none")));
    }
  }

  @Nested
  @Order(4)
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
