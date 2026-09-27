package com.web.gallery.application.model.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.domain.model.common.TokenHash;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class RefreshTokenModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・トークンハッシュ・有効期限からインスタンスを生成できること")
    void of_success() {
      AccountNo accountNo = new AccountNo(1L);
      TokenHash tokenHash = new TokenHash("hash-value");
      ExpiresAt expiresAt = new ExpiresAt(OffsetDateTime.now().plusDays(1));

      RefreshTokenModel actual = RefreshTokenModel.of(accountNo, tokenHash, expiresAt);

      assertEquals(accountNo, actual.getAccountNo());
      assertEquals(tokenHash, actual.getTokenHash());
      assertEquals(expiresAt, actual.getExpiresAt());
      assertNull(actual.getIsRevoked());
      assertNull(actual.getUpdatedAt());
    }
  }
}
