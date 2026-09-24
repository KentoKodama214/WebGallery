package com.web.gallery.entity.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.model.auth.RefreshTokenModel;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class RefreshTokenTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・トークンハッシュ・有効期限が反映され、更新者にもアカウント番号が設定されること")
    void from_success() {
      OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7);
      RefreshTokenModel model =
          RefreshTokenModel.of(
              new AccountNo(1L), new TokenHash("hashed-token-value"), new ExpiresAt(expiresAt));

      RefreshToken actual = RefreshToken.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals("hashed-token-value", actual.getTokenHash());
      assertEquals(expiresAt, actual.getExpiresAt());
      assertEquals(1L, actual.getUpdatedBy());
    }
  }
}
