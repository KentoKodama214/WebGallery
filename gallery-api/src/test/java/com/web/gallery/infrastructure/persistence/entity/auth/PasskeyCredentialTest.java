package com.web.gallery.infrastructure.persistence.entity.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.SignCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PasskeyCredentialTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映され、作成者・更新者に所有者のアカウント番号が設定されること")
    void from_allFieldsPresent() {
      PasskeyCredentialModel model =
          PasskeyCredentialModel.builder()
              .passkeyNo(new PasskeyNo(10L))
              .accountNo(new AccountNo(1L))
              .credentialId(new CredentialId("credential-1"))
              .attestedCredentialData(new AttestedCredentialData(new byte[] {1, 2}))
              .signCount(new SignCount(5L))
              .transports(new PasskeyTransports("hybrid,internal"))
              .isBackupEligible(new IsBackupEligible(true))
              .isBackedUp(new IsBackedUp(false))
              .isUvInitialized(new IsUvInitialized(true))
              .passkeyName(new PasskeyName("MacBook"))
              .build();

      PasskeyCredential actual = PasskeyCredential.from(model);

      // 採番前のため登録時はパスキー番号を渡さない
      assertNull(actual.getPasskeyNo());
      assertEquals(1L, actual.getAccountNo());
      assertEquals("credential-1", actual.getCredentialId());
      assertArrayEquals(new byte[] {1, 2}, actual.getAttestedCredentialData());
      assertEquals(5L, actual.getSignCount());
      assertEquals("hybrid,internal", actual.getTransports());
      assertTrue(actual.getIsBackupEligible());
      assertFalse(actual.getIsBackedUp());
      assertTrue(actual.getIsUvInitialized());
      assertEquals("MacBook", actual.getPasskeyName());
      assertEquals(1L, actual.getCreatedBy());
      assertEquals(1L, actual.getUpdatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：最終使用日時・作成日時はDB側の既定値を使うため設定されないこと")
    void from_timestampsDelegatedToDb() {
      PasskeyCredential actual =
          PasskeyCredential.from(
              PasskeyCredentialModel.builder()
                  .passkeyNo(new PasskeyNo(10L))
                  .accountNo(new AccountNo(1L))
                  .credentialId(new CredentialId("credential-1"))
                  .attestedCredentialData(new AttestedCredentialData(new byte[] {1, 2}))
                  .signCount(new SignCount(5L))
                  .transports(new PasskeyTransports("hybrid,internal"))
                  .isBackupEligible(new IsBackupEligible(true))
                  .isBackedUp(new IsBackedUp(false))
                  .isUvInitialized(new IsUvInitialized(true))
                  .passkeyName(new PasskeyName("MacBook"))
                  .build());

      assertNull(actual.getLastUsedAt());
      assertNull(actual.getCreatedAt());
      assertNull(actual.getUpdatedAt());
    }
  }
}
