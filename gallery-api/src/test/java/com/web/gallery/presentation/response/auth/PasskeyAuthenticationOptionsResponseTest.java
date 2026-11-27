package com.web.gallery.presentation.response.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PasskeyAuthenticationOptionsResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：チャレンジ・RP ID・タイムアウトを転写し、ユーザー検証を必須・許可するクレデンシャルを空とすること")
    void from_success() {
      PasskeyAuthenticationOptionsResponse actual =
          PasskeyAuthenticationOptionsResponse.from(
              PasskeyAuthenticationOptionsModel.builder()
                  .challenge(new PasskeyChallenge("challenge-1"))
                  .rpId(new RelyingPartyId("localhost"))
                  .allowCredentialIds(List.of())
                  .timeoutMillis(new PasskeyTimeoutMillis(300_000L))
                  .build());

      assertEquals("challenge-1", actual.getChallenge());
      assertEquals("localhost", actual.getRpId());
      assertEquals(300_000L, actual.getTimeout());
      assertEquals("required", actual.getUserVerification());
      assertTrue(actual.getAllowCredentials().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：本人確認用に許可するクレデンシャルIDがある場合、public-keyの識別子として転写すること")
    void from_withAllowCredentials() {
      PasskeyAuthenticationOptionsResponse actual =
          PasskeyAuthenticationOptionsResponse.from(
              PasskeyAuthenticationOptionsModel.builder()
                  .challenge(new PasskeyChallenge("challenge-1"))
                  .rpId(new RelyingPartyId("localhost"))
                  .allowCredentialIds(List.of(new CredentialId("credential-1")))
                  .timeoutMillis(new PasskeyTimeoutMillis(300_000L))
                  .build());

      assertEquals(1, actual.getAllowCredentials().size());
      assertEquals("credential-1", actual.getAllowCredentials().getFirst().id());
      assertEquals("public-key", actual.getAllowCredentials().getFirst().type());
    }
  }
}
