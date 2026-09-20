package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.auth.AccessToken;
import com.web.gallery.domain.auth.ExpiresIn;
import com.web.gallery.domain.auth.RefreshTokenValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AuthTokenModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：指定した値がそれぞれ値オブジェクトにラップされて設定されること")
    void of_success() {
      AuthTokenModel actual = AuthTokenModel.of("access-token", "refresh-token", 900L);

      assertEquals(new AccessToken("access-token"), actual.getAccessToken());
      assertEquals(new RefreshTokenValue("refresh-token"), actual.getRefreshToken());
      assertEquals(new ExpiresIn(900L), actual.getExpiresIn());
    }
  }
}
