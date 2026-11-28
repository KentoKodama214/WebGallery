package com.web.gallery.infrastructure.persistence.entity.auth;

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
class PasskeyCredentialUpdateTargetTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forAuthenticationResult {
    @Test
    @Order(1)
    @DisplayName("正常系：署名カウンタ・バックアップ済みフラグ・最終使用日時を設定し、パスキー名は変更しないこと")
    void forAuthenticationResult_success() {
      OffsetDateTime lastUsedAt = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));
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
              .lastUsedAt(new LastUsedAt(lastUsedAt))
              .build();

      PasskeyCredentialUpdateTarget actual =
          PasskeyCredentialUpdateTarget.forAuthenticationResult(model);

      assertEquals(5L, actual.getSignCount());
      assertFalse(actual.getIsBackedUp());
      assertEquals(lastUsedAt, actual.getLastUsedAt());
      assertEquals(1L, actual.getUpdatedBy());
      // パスキー名は認証では変わらないため更新対象にしない
      assertNull(actual.getPasskeyName());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forRename {
    @Test
    @Order(1)
    @DisplayName("正常系：パスキー名と更新者のみを設定し、認証結果の項目は変更しないこと")
    void forRename_success() {
      PasskeyCredentialUpdateTarget actual = PasskeyCredentialUpdateTarget.forRename("iPhone", 1L);

      assertEquals("iPhone", actual.getPasskeyName());
      assertEquals(1L, actual.getUpdatedBy());
      assertNull(actual.getSignCount());
      assertNull(actual.getIsBackedUp());
      assertNull(actual.getLastUsedAt());
    }
  }
}
