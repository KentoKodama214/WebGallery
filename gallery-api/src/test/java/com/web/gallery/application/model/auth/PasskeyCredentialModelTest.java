package com.web.gallery.application.model.auth;

import static org.junit.jupiter.api.Assertions.*;

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
class PasskeyCredentialModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：検証済みの登録レスポンスの値とパスキー名を転写し、採番・日時項目は未設定とすること")
    void forRegist_success() {
      PasskeyAttestationModel attestation =
          PasskeyAttestationModel.builder()
              .credentialId(new CredentialId("credential-1"))
              .attestedCredentialData(new AttestedCredentialData(new byte[] {1}))
              .signCount(new SignCount(3L))
              .transports(new PasskeyTransports("usb"))
              .isBackupEligible(new IsBackupEligible(false))
              .isBackedUp(new IsBackedUp(false))
              .isUvInitialized(new IsUvInitialized(true))
              .build();

      PasskeyCredentialModel actual =
          PasskeyCredentialModel.forRegist(
              new AccountNo(1L), attestation, new PasskeyName("セキュリティキー"));

      assertNull(actual.getPasskeyNo());
      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new CredentialId("credential-1"), actual.getCredentialId());
      assertEquals(new AttestedCredentialData(new byte[] {1}), actual.getAttestedCredentialData());
      assertEquals(new SignCount(3L), actual.getSignCount());
      assertEquals(new PasskeyTransports("usb"), actual.getTransports());
      assertEquals(new IsBackupEligible(false), actual.getIsBackupEligible());
      assertEquals(new IsBackedUp(false), actual.getIsBackedUp());
      assertEquals(new IsUvInitialized(true), actual.getIsUvInitialized());
      assertEquals(new PasskeyName("セキュリティキー"), actual.getPasskeyName());
      assertNull(actual.getLastUsedAt());
      assertNull(actual.getCreatedAt());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class withAuthenticationResult {
    @Test
    @Order(1)
    @DisplayName("正常系：署名カウンタ・バックアップ済みフラグ・最終使用日時だけを差し替えた新しいインスタンスを返すこと")
    void withAuthenticationResult_success() {
      PasskeyCredentialModel original =
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
      LastUsedAt lastUsedAt =
          new LastUsedAt(OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)));

      PasskeyCredentialModel actual =
          original.withAuthenticationResult(
              PasskeyAssertionResultModel.builder()
                  .signCount(new SignCount(6L))
                  .isBackedUp(new IsBackedUp(true))
                  .build(),
              lastUsedAt);

      assertEquals(new SignCount(6L), actual.getSignCount());
      assertEquals(new IsBackedUp(true), actual.getIsBackedUp());
      assertEquals(lastUsedAt, actual.getLastUsedAt());
      assertEquals(original.getPasskeyNo(), actual.getPasskeyNo());
      assertEquals(original.getCredentialId(), actual.getCredentialId());
      assertEquals(original.getPasskeyName(), actual.getPasskeyName());
      // 元のインスタンスは変更しない
      assertEquals(new SignCount(5L), original.getSignCount());
      assertNull(original.getLastUsedAt());
    }
  }
}
