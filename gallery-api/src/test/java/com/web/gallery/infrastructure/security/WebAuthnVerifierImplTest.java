package com.web.gallery.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

import com.web.gallery.application.config.WebAuthnConfig;
import com.web.gallery.application.model.auth.PasskeyAssertionModel;
import com.web.gallery.application.model.auth.PasskeyAssertionResultModel;
import com.web.gallery.application.model.auth.PasskeyAttestationModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.SignCount;
import com.web.gallery.testsupport.PasskeyTestAuthenticator;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * {@link WebAuthnVerifierImpl}の単体テスト
 *
 * <p>署名・origin・チャレンジ等の検証はwebauthn4jに委ねているため、モックで偽装せず、webauthn4j-testの擬似認証器が
 * 実際に生成した登録・認証レスポンスを使って検証する
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WebAuthnVerifierImplTest {

  private static final String USER_HANDLE =
      Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);

  @Mock private WebAuthnConfig webAuthnConfig;

  @InjectMocks private WebAuthnVerifierImpl webAuthnVerifierImpl;

  private PasskeyTestAuthenticator authenticator;

  @BeforeEach
  void setUp() {
    lenient().when(webAuthnConfig.getRpId()).thenReturn(PasskeyTestAuthenticator.RP_ID);
    lenient()
        .when(webAuthnConfig.getAllowedOrigins())
        .thenReturn(List.of(PasskeyTestAuthenticator.ORIGIN));
    authenticator = new PasskeyTestAuthenticator();
  }

  /**
   * 擬似認証器でパスキーを登録し、検証済みのクレデンシャルを保存済みのModelとして返す
   *
   * @return 保存済み相当の{@link PasskeyCredentialModel}
   */
  private PasskeyCredentialModel registerCredential() {
    PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
    String json =
        authenticator.createRegistrationResponseJson(
            PasskeyTestAuthenticator.RP_ID, challenge.value(), USER_HANDLE, "aaaaaaaa");
    PasskeyAttestationModel attestation =
        webAuthnVerifierImpl.verifyRegistration(new PasskeyResponseJson(json), challenge);
    return PasskeyCredentialModel.forRegist(new AccountNo(1L), attestation, new PasskeyName("test"))
        .toBuilder()
        .passkeyNo(new PasskeyNo(1L))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class generateChallenge {
    @Test
    @Order(1)
    @DisplayName("正常系：32バイトの乱数をパディングなしのbase64urlで返し、呼び出しごとに異なること")
    void generateChallenge_random() {
      PasskeyChallenge first = webAuthnVerifierImpl.generateChallenge();
      PasskeyChallenge second = webAuthnVerifierImpl.generateChallenge();

      assertEquals(32, Base64.getUrlDecoder().decode(first.value()).length);
      assertTrue(first.value().matches("[A-Za-z0-9_-]+"));
      assertNotEquals(first, second);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class generateUserHandle {
    @Test
    @Order(1)
    @DisplayName("正常系：32バイトの乱数をパディングなしのbase64urlで返し、呼び出しごとに異なること")
    void generateUserHandle_random() {
      PasskeyUserHandle first = webAuthnVerifierImpl.generateUserHandle();
      PasskeyUserHandle second = webAuthnVerifierImpl.generateUserHandle();

      assertEquals(32, Base64.getUrlDecoder().decode(first.value()).length);
      assertTrue(first.value().matches("[A-Za-z0-9_-]+"));
      assertNotEquals(first, second);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class readRegistrationChallenge {
    @Test
    @Order(1)
    @DisplayName("正常系：登録レスポンスのclientDataJSONからチャレンジを読み取ること")
    void readRegistrationChallenge_success() {
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createRegistrationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value(), USER_HANDLE, "aaaaaaaa");

      assertEquals(
          challenge, webAuthnVerifierImpl.readRegistrationChallenge(new PasskeyResponseJson(json)));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：JSONとして解釈できない場合、PasskeyVerificationExceptionをスローすること")
    void readRegistrationChallenge_invalidJson() {
      assertThrows(
          PasskeyVerificationException.class,
          () -> webAuthnVerifierImpl.readRegistrationChallenge(new PasskeyResponseJson("{")));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class verifyRegistration {
    @Test
    @Order(1)
    @DisplayName("正常系：検証に成功し、クレデンシャルID・認証器データ・ユーザー検証済みフラグを返すこと")
    void verifyRegistration_success() {
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createRegistrationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value(), USER_HANDLE, "aaaaaaaa");

      PasskeyAttestationModel actual =
          webAuthnVerifierImpl.verifyRegistration(new PasskeyResponseJson(json), challenge);

      // クレデンシャルIDはレスポンスのrawIdと一致する
      String rawId = json.replaceAll(".*\"rawId\":\"([^\"]+)\".*", "$1");
      assertEquals(new CredentialId(rawId), actual.getCredentialId());
      assertTrue(actual.getAttestedCredentialData().value().length > 0);
      assertTrue(actual.getIsUvInitialized().value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：サーバが発行したものと異なるチャレンジの場合、PasskeyVerificationExceptionをスローすること")
    void verifyRegistration_challengeMismatch() {
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createRegistrationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value(), USER_HANDLE, "aaaaaaaa");

      assertThrows(
          PasskeyVerificationException.class,
          () ->
              webAuthnVerifierImpl.verifyRegistration(
                  new PasskeyResponseJson(json), webAuthnVerifierImpl.generateChallenge()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：許可していないoriginで作成された場合、PasskeyVerificationExceptionをスローすること")
    void verifyRegistration_originMismatch() {
      PasskeyTestAuthenticator phishing = new PasskeyTestAuthenticator("http://localhost:4000");
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          phishing.createRegistrationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value(), USER_HANDLE, "aaaaaaaa");

      assertThrows(
          PasskeyVerificationException.class,
          () -> webAuthnVerifierImpl.verifyRegistration(new PasskeyResponseJson(json), challenge));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：別のRP ID向けに作成された場合、PasskeyVerificationExceptionをスローすること")
    void verifyRegistration_rpIdMismatch() {
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createRegistrationResponseJson(
              "example.com", challenge.value(), USER_HANDLE, "aaaaaaaa");

      assertThrows(
          PasskeyVerificationException.class,
          () -> webAuthnVerifierImpl.verifyRegistration(new PasskeyResponseJson(json), challenge));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class readAuthentication {
    @Test
    @Order(1)
    @DisplayName("正常系：認証レスポンスからクレデンシャルID・チャレンジ・ユーザーハンドルを読み取ること")
    void readAuthentication_success() {
      PasskeyCredentialModel credential = registerCredential();
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value());

      PasskeyAssertionModel actual =
          webAuthnVerifierImpl.readAuthentication(new PasskeyResponseJson(json));

      assertEquals(credential.getCredentialId(), actual.getCredentialId());
      assertEquals(challenge, actual.getChallenge());
      assertEquals(new PasskeyUserHandle(USER_HANDLE), actual.getUserHandle());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：JSONとして解釈できない場合、PasskeyVerificationExceptionをスローすること")
    void readAuthentication_invalidJson() {
      assertThrows(
          PasskeyVerificationException.class,
          () -> webAuthnVerifierImpl.readAuthentication(new PasskeyResponseJson("not json")));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class verifyAuthentication {
    @Test
    @Order(1)
    @DisplayName("正常系：保存済みの公開鍵で署名を検証し、増加した署名カウンタを返すこと")
    void verifyAuthentication_success() {
      PasskeyCredentialModel credential = registerCredential();
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value());

      PasskeyAssertionResultModel actual =
          webAuthnVerifierImpl.verifyAuthentication(
              new PasskeyResponseJson(json), challenge, credential);

      assertTrue(actual.getSignCount().value() > credential.getSignCount().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：署名カウンタを持たない（常に0の）同期パスキーでも検証に成功すること")
    void verifyAuthentication_syncedPasskeyWithZeroCounter() {
      authenticator.setCountUpEnabled(false);
      PasskeyCredentialModel credential = registerCredential();
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value());

      PasskeyAssertionResultModel actual =
          webAuthnVerifierImpl.verifyAuthentication(
              new PasskeyResponseJson(json), challenge, credential);

      assertEquals(new SignCount(0L), actual.getSignCount());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：サーバが発行したものと異なるチャレンジの場合、PasskeyVerificationExceptionをスローすること")
    void verifyAuthentication_challengeMismatch() {
      PasskeyCredentialModel credential = registerCredential();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, webAuthnVerifierImpl.generateChallenge().value());

      assertThrows(
          PasskeyVerificationException.class,
          () ->
              webAuthnVerifierImpl.verifyAuthentication(
                  new PasskeyResponseJson(json),
                  webAuthnVerifierImpl.generateChallenge(),
                  credential));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：署名カウンタが保存値以下に巻き戻った（クローンの疑い）場合、PasskeyVerificationExceptionをスローすること")
    void verifyAuthentication_counterRegression() {
      PasskeyCredentialModel credential =
          registerCredential().toBuilder().signCount(new SignCount(1_000L)).build();
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value());

      assertThrows(
          PasskeyVerificationException.class,
          () ->
              webAuthnVerifierImpl.verifyAuthentication(
                  new PasskeyResponseJson(json), challenge, credential));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：保存済みのクレデンシャルとレスポンスのクレデンシャルIDが異なる場合、PasskeyVerificationExceptionをスローすること")
    void verifyAuthentication_credentialIdMismatch() {
      PasskeyCredentialModel credential =
          registerCredential().toBuilder().credentialId(new CredentialId("AAAA")).build();
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value());

      assertThrows(
          PasskeyVerificationException.class,
          () ->
              webAuthnVerifierImpl.verifyAuthentication(
                  new PasskeyResponseJson(json), challenge, credential));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：別の認証器の公開鍵で検証した場合、PasskeyVerificationExceptionをスローすること")
    void verifyAuthentication_signatureMismatch() {
      PasskeyCredentialModel credential = registerCredential();
      // 別の認証器で作成したパスキーの公開鍵を、元のパスキーのクレデンシャルIDに差し替える
      PasskeyTestAuthenticator other = new PasskeyTestAuthenticator();
      PasskeyChallenge otherChallenge = webAuthnVerifierImpl.generateChallenge();
      PasskeyAttestationModel otherAttestation =
          webAuthnVerifierImpl.verifyRegistration(
              new PasskeyResponseJson(
                  other.createRegistrationResponseJson(
                      PasskeyTestAuthenticator.RP_ID,
                      otherChallenge.value(),
                      USER_HANDLE,
                      "bbbbbbbb")),
              otherChallenge);
      PasskeyCredentialModel tampered =
          credential.toBuilder()
              .attestedCredentialData(otherAttestation.getAttestedCredentialData())
              .build();
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value());

      assertThrows(
          PasskeyVerificationException.class,
          () ->
              webAuthnVerifierImpl.verifyAuthentication(
                  new PasskeyResponseJson(json), challenge, tampered));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：許可していないoriginで署名された場合、PasskeyVerificationExceptionをスローすること")
    void verifyAuthentication_originMismatch() {
      PasskeyCredentialModel credential = registerCredential();
      doReturn(List.of("https://gallery.example.com")).when(webAuthnConfig).getAllowedOrigins();
      PasskeyChallenge challenge = webAuthnVerifierImpl.generateChallenge();
      String json =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, challenge.value());

      assertThrows(
          PasskeyVerificationException.class,
          () ->
              webAuthnVerifierImpl.verifyAuthentication(
                  new PasskeyResponseJson(json), challenge, credential));
    }
  }
}
