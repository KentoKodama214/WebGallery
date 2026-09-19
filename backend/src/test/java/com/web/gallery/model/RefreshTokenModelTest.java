package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.entity.RefreshToken;
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
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：無効化フラグ・更新日時が設定されている場合、そのまま設定されること")
    void from_allFieldsSet() {
      OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(1);
      OffsetDateTime updatedAt = OffsetDateTime.now();
      RefreshToken entity =
          RefreshToken.builder()
              .accountNo(1L)
              .tokenHash("hash-value")
              .expiresAt(expiresAt)
              .isRevoked(true)
              .updatedAt(updatedAt)
              .build();

      RefreshTokenModel actual = RefreshTokenModel.from(entity);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new TokenHash("hash-value"), actual.getTokenHash());
      assertEquals(new ExpiresAt(expiresAt), actual.getExpiresAt());
      assertTrue(actual.getIsRevoked().value());
      assertEquals(updatedAt, actual.getUpdatedAt().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：無効化フラグ・更新日時が未設定の場合、nullが設定されること")
    void from_optionalFieldsNull() {
      OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(1);
      RefreshToken entity =
          RefreshToken.builder()
              .accountNo(1L)
              .tokenHash("hash-value")
              .expiresAt(expiresAt)
              .isRevoked(null)
              .updatedAt(null)
              .build();

      RefreshTokenModel actual = RefreshTokenModel.from(entity);

      assertNull(actual.getIsRevoked());
      assertNull(actual.getUpdatedAt());
    }
  }

  @Nested
  @Order(2)
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
