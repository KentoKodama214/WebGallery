package com.web.gallery.presentation.response.account;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import com.web.gallery.domain.model.auth.RelyingPartyName;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PasskeyRegistrationOptionsResponseTest {

  private static PasskeyRegistrationOptionsModel model(List<CredentialId> excludeCredentialIds) {
    return PasskeyRegistrationOptionsModel.builder()
        .challenge(new PasskeyChallenge("challenge-1"))
        .rpId(new RelyingPartyId("localhost"))
        .rpName(new RelyingPartyName("WebGallery"))
        .userHandle(new PasskeyUserHandle("handle-1"))
        .accountId(new AccountId("aaaaaaaa"))
        .accountName(new AccountName("テストユーザー"))
        .excludeCredentialIds(excludeCredentialIds)
        .timeoutMillis(new PasskeyTimeoutMillis(300_000L))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：RP・ユーザー・チャレンジ・除外するクレデンシャルを転写すること")
    void from_success() {
      PasskeyRegistrationOptionsResponse actual =
          PasskeyRegistrationOptionsResponse.from(
              model(List.of(new CredentialId("credential-1"), new CredentialId("credential-2"))));

      assertEquals("localhost", actual.getRp().id());
      assertEquals("WebGallery", actual.getRp().name());
      assertEquals("handle-1", actual.getUser().id());
      assertEquals("aaaaaaaa", actual.getUser().name());
      assertEquals("テストユーザー", actual.getUser().displayName());
      assertEquals("challenge-1", actual.getChallenge());
      assertEquals(300_000L, actual.getTimeout());
      assertEquals(2, actual.getExcludeCredentials().size());
      assertEquals("credential-1", actual.getExcludeCredentials().get(0).id());
      assertEquals("public-key", actual.getExcludeCredentials().get(0).type());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：パスキー（discoverable credential）・ユーザー検証を必須とし、attestationは要求しないこと")
    void from_fixedRequirements() {
      PasskeyRegistrationOptionsResponse actual =
          PasskeyRegistrationOptionsResponse.from(model(List.of()));

      assertEquals("required", actual.getAuthenticatorSelection().residentKey());
      assertTrue(actual.getAuthenticatorSelection().requireResidentKey());
      assertEquals("required", actual.getAuthenticatorSelection().userVerification());
      assertEquals("none", actual.getAttestation());
      assertTrue(actual.getExcludeCredentials().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：受け付ける署名アルゴリズムをES256・EdDSA・RS256の順で返すこと")
    void from_pubKeyCredParams() {
      PasskeyRegistrationOptionsResponse actual =
          PasskeyRegistrationOptionsResponse.from(model(List.of()));

      assertEquals(
          List.of(-7L, -8L, -257L),
          actual.getPubKeyCredParams().stream()
              .map(PasskeyRegistrationOptionsResponse.PublicKeyCredentialParameter::alg)
              .toList());
      assertTrue(
          actual.getPubKeyCredParams().stream().allMatch(p -> "public-key".equals(p.type())));
    }
  }
}
