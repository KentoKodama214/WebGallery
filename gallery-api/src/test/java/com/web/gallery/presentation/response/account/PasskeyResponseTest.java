package com.web.gallery.presentation.response.account;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.LastUsedAt;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.SignCount;
import com.web.gallery.domain.model.common.CreatedAt;
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
class PasskeyResponseTest {

  private static PasskeyCredentialModel.PasskeyCredentialModelBuilder base() {
    return PasskeyCredentialModel.builder()
        .accountNo(new AccountNo(1L))
        .credentialId(new CredentialId("credential-1"))
        .attestedCredentialData(new AttestedCredentialData(new byte[] {1}))
        .signCount(new SignCount(0L))
        .transports(new PasskeyTransports("internal"))
        .isBackupEligible(new IsBackupEligible(true))
        .isBackedUp(new IsBackedUp(true))
        .isUvInitialized(new IsUvInitialized(true))
        .passkeyName(new PasskeyName("MacBook"));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、表示用の項目のみを転写すること")
    void from_allFieldsSet() {
      OffsetDateTime createdAt = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));
      OffsetDateTime lastUsedAt = createdAt.plusDays(1);

      PasskeyResponse actual =
          PasskeyResponse.from(
              base()
                  .passkeyNo(new PasskeyNo(10L))
                  .createdAt(new CreatedAt(createdAt))
                  .lastUsedAt(new LastUsedAt(lastUsedAt))
                  .build());

      assertEquals(10L, actual.getPasskeyNo());
      assertEquals("MacBook", actual.getPasskeyName());
      assertTrue(actual.getIsSynced());
      assertEquals(createdAt, actual.getCreatedAt());
      assertEquals(lastUsedAt, actual.getLastUsedAt());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：任意項目（パスキー番号・日時）が未設定の場合、nullになること")
    void from_optionalFieldsNull() {
      PasskeyResponse actual = PasskeyResponse.from(base().build());

      assertNull(actual.getPasskeyNo());
      assertNull(actual.getCreatedAt());
      assertNull(actual.getLastUsedAt());
    }
  }
}
