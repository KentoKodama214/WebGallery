package com.web.gallery.application.model.auth;

import static org.junit.jupiter.api.Assertions.*;

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
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PasskeyCredentialModelListTest {

  private static PasskeyCredentialModel passkey(String credentialId) {
    return PasskeyCredentialModel.builder()
        .passkeyNo(new PasskeyNo(10L))
        .accountNo(new AccountNo(1L))
        .credentialId(new CredentialId(credentialId))
        .attestedCredentialData(new AttestedCredentialData(new byte[] {1, 2}))
        .signCount(new SignCount(5L))
        .transports(new PasskeyTransports("hybrid,internal"))
        .isBackupEligible(new IsBackupEligible(true))
        .isBackedUp(new IsBackedUp(false))
        .isUvInitialized(new IsUvInitialized(true))
        .passkeyName(new PasskeyName("MacBook"))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toCredentialIdList {
    @Test
    @Order(1)
    @DisplayName("正常系：各パスキーのクレデンシャルIDを順序を保って返すこと")
    void toCredentialIdList_success() {
      PasskeyCredentialModelList list =
          PasskeyCredentialModelList.of(List.of(passkey("credential-1"), passkey("credential-2")));

      assertEquals(
          List.of(new CredentialId("credential-1"), new CredentialId("credential-2")),
          list.toCredentialIdList());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空の場合は空のリストを返すこと")
    void toCredentialIdList_empty() {
      assertTrue(PasskeyCredentialModelList.empty().toCredentialIdList().isEmpty());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("異常系：nullを指定した場合、NullPointerExceptionをスローすること")
    void of_null() {
      assertThrows(NullPointerException.class, () -> PasskeyCredentialModelList.of(null));
    }
  }
}
