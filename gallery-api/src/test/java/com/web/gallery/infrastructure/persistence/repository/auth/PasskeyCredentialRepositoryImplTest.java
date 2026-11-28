package com.web.gallery.infrastructure.persistence.repository.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
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
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredential;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialCondition;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialUpdateTarget;
import com.web.gallery.infrastructure.persistence.mapper.auth.PasskeyCredentialMapper;
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
class PasskeyCredentialRepositoryImplTest {
  private static final OffsetDateTime NOW =
      OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));

  @InjectMocks private PasskeyCredentialRepositoryImpl passkeyCredentialRepositoryImpl;

  @Mock private PasskeyCredentialMapper passkeyCredentialMapper;

  private static PasskeyCredential entity() {
    return PasskeyCredential.builder()
        .passkeyNo(10L)
        .accountNo(1L)
        .credentialId("credential-1")
        .attestedCredentialData(new byte[] {1, 2})
        .signCount(5L)
        .transports("internal")
        .isBackupEligible(true)
        .isBackedUp(false)
        .isUvInitialized(true)
        .passkeyName("MacBook")
        .lastUsedAt(NOW)
        .createdBy(1L)
        .createdAt(NOW)
        .updatedBy(1L)
        .updatedAt(NOW)
        .build();
  }

  private static PasskeyCredentialModel model() {
    return PasskeyCredentialModel.builder()
        .passkeyNo(new PasskeyNo(10L))
        .accountNo(new AccountNo(1L))
        .credentialId(new CredentialId("credential-1"))
        .attestedCredentialData(new AttestedCredentialData(new byte[] {1, 2}))
        .signCount(new SignCount(6L))
        .transports(new PasskeyTransports("internal"))
        .isBackupEligible(new IsBackupEligible(true))
        .isBackedUp(new IsBackedUp(true))
        .isUvInitialized(new IsUvInitialized(true))
        .passkeyName(new PasskeyName("MacBook"))
        .lastUsedAt(new LastUsedAt(NOW))
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
      passkeyCredentialRepositoryImpl.save(model());

      ArgumentCaptor<PasskeyCredential> captor = ArgumentCaptor.forClass(PasskeyCredential.class);
      verify(passkeyCredentialMapper).insert(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals("credential-1", captor.getValue().getCredentialId());
      assertArrayEquals(new byte[] {1, 2}, captor.getValue().getAttestedCredentialData());
      assertEquals(6L, captor.getValue().getSignCount());
      assertEquals("internal", captor.getValue().getTransports());
      assertTrue(captor.getValue().getIsBackupEligible());
      assertTrue(captor.getValue().getIsBackedUp());
      assertTrue(captor.getValue().getIsUvInitialized());
      assertEquals("MacBook", captor.getValue().getPasskeyName());
      assertEquals(1L, captor.getValue().getCreatedBy());
      assertEquals(1L, captor.getValue().getUpdatedBy());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class findByCredentialId {
    @Test
    @Order(1)
    @DisplayName("正常系：クレデンシャルIDを条件に取得し、全項目をModelへ変換すること")
    void findByCredentialId_found() {
      doReturn(List.of(entity())).when(passkeyCredentialMapper).select(any());

      PasskeyCredentialModel actual =
          passkeyCredentialRepositoryImpl.findByCredentialId(new CredentialId("credential-1"));

      ArgumentCaptor<PasskeyCredentialCondition> captor =
          ArgumentCaptor.forClass(PasskeyCredentialCondition.class);
      verify(passkeyCredentialMapper).select(captor.capture());
      assertEquals("credential-1", captor.getValue().getCredentialId());
      assertNull(captor.getValue().getAccountNo());
      assertNull(captor.getValue().getPasskeyNo());

      assertEquals(new PasskeyNo(10L), actual.getPasskeyNo());
      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new CredentialId("credential-1"), actual.getCredentialId());
      assertEquals(
          new AttestedCredentialData(new byte[] {1, 2}), actual.getAttestedCredentialData());
      assertEquals(new SignCount(5L), actual.getSignCount());
      assertEquals(new PasskeyTransports("internal"), actual.getTransports());
      assertEquals(new IsBackupEligible(true), actual.getIsBackupEligible());
      assertEquals(new IsBackedUp(false), actual.getIsBackedUp());
      assertEquals(new IsUvInitialized(true), actual.getIsUvInitialized());
      assertEquals(new PasskeyName("MacBook"), actual.getPasskeyName());
      assertEquals(NOW, actual.getLastUsedAt().value());
      assertEquals(NOW, actual.getCreatedAt().value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：該当しない場合、nullを返すこと")
    void findByCredentialId_notFound() {
      doReturn(List.of()).when(passkeyCredentialMapper).select(any());

      assertNull(passkeyCredentialRepositoryImpl.findByCredentialId(new CredentialId("none")));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class findByCredentialIdForUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：行ロック付きの取得を用い、Modelへ変換すること")
    void findByCredentialIdForUpdate_found() {
      doReturn(List.of(entity())).when(passkeyCredentialMapper).selectForUpdate(any());

      PasskeyCredentialModel actual =
          passkeyCredentialRepositoryImpl.findByCredentialIdForUpdate(
              new CredentialId("credential-1"));

      verify(passkeyCredentialMapper, never()).select(any());
      assertEquals(new PasskeyNo(10L), actual.getPasskeyNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：該当しない場合、nullを返すこと")
    void findByCredentialIdForUpdate_notFound() {
      doReturn(List.of()).when(passkeyCredentialMapper).selectForUpdate(any());

      assertNull(
          passkeyCredentialRepositoryImpl.findByCredentialIdForUpdate(new CredentialId("none")));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class findByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号を条件に取得し、ModelListへ変換すること")
    void findByAccountNo_found() {
      doReturn(List.of(entity())).when(passkeyCredentialMapper).select(any());

      PasskeyCredentialModelList actual =
          passkeyCredentialRepositoryImpl.findByAccountNo(new AccountNo(1L));

      ArgumentCaptor<PasskeyCredentialCondition> captor =
          ArgumentCaptor.forClass(PasskeyCredentialCondition.class);
      verify(passkeyCredentialMapper).select(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals(1, actual.size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当しない場合、空のModelListを返すこと")
    void findByAccountNo_empty() {
      doReturn(List.of()).when(passkeyCredentialMapper).select(any());

      assertTrue(passkeyCredentialRepositoryImpl.findByAccountNo(new AccountNo(1L)).isEmpty());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class countByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号を条件に件数を返すこと")
    void countByAccountNo_success() {
      doReturn(3).when(passkeyCredentialMapper).count(any());

      assertEquals(3, passkeyCredentialRepositoryImpl.countByAccountNo(new AccountNo(1L)));

      ArgumentCaptor<PasskeyCredentialCondition> captor =
          ArgumentCaptor.forClass(PasskeyCredentialCondition.class);
      verify(passkeyCredentialMapper).count(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateAuthenticationResult {
    @Test
    @Order(1)
    @DisplayName("正常系：パスキー番号を条件に、署名カウンタ・バックアップ済みフラグ・最終使用日時を更新すること")
    void updateAuthenticationResult_success() {
      passkeyCredentialRepositoryImpl.updateAuthenticationResult(model());

      ArgumentCaptor<PasskeyCredentialCondition> conditionCaptor =
          ArgumentCaptor.forClass(PasskeyCredentialCondition.class);
      ArgumentCaptor<PasskeyCredentialUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PasskeyCredentialUpdateTarget.class);
      verify(passkeyCredentialMapper).update(conditionCaptor.capture(), targetCaptor.capture());
      assertEquals(10L, conditionCaptor.getValue().getPasskeyNo());
      assertNull(conditionCaptor.getValue().getAccountNo());
      assertNull(conditionCaptor.getValue().getCredentialId());
      assertEquals(6L, targetCaptor.getValue().getSignCount());
      assertTrue(targetCaptor.getValue().getIsBackedUp());
      assertEquals(NOW, targetCaptor.getValue().getLastUsedAt());
      assertEquals(1L, targetCaptor.getValue().getUpdatedBy());
      // パスキー名は変更しない
      assertNull(targetCaptor.getValue().getPasskeyName());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class rename {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・パスキー番号を条件にパスキー名を更新し、更新件数を返すこと")
    void rename_success() {
      doReturn(1).when(passkeyCredentialMapper).update(any(), any());

      Integer actual =
          passkeyCredentialRepositoryImpl.rename(
              new AccountNo(1L), new PasskeyNo(10L), new PasskeyName("iPhone"));

      assertEquals(1, actual);
      ArgumentCaptor<PasskeyCredentialCondition> conditionCaptor =
          ArgumentCaptor.forClass(PasskeyCredentialCondition.class);
      ArgumentCaptor<PasskeyCredentialUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PasskeyCredentialUpdateTarget.class);
      verify(passkeyCredentialMapper).update(conditionCaptor.capture(), targetCaptor.capture());
      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertEquals(10L, conditionCaptor.getValue().getPasskeyNo());
      assertEquals("iPhone", targetCaptor.getValue().getPasskeyName());
      assertEquals(1L, targetCaptor.getValue().getUpdatedBy());
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・パスキー番号を条件に削除し、削除件数を返すこと")
    void delete_success() {
      doReturn(1).when(passkeyCredentialMapper).delete(any());

      assertEquals(
          1, passkeyCredentialRepositoryImpl.delete(new AccountNo(1L), new PasskeyNo(10L)));

      ArgumentCaptor<PasskeyCredentialCondition> captor =
          ArgumentCaptor.forClass(PasskeyCredentialCondition.class);
      verify(passkeyCredentialMapper).delete(captor.capture());
      assertEquals(1L, captor.getValue().getAccountNo());
      assertEquals(10L, captor.getValue().getPasskeyNo());
    }
  }
}
