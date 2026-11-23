package com.web.gallery.domain.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class WebAuthnChallengePurposeEnumTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrNull {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致する場合、対応するEnum値を返すこと")
    void getOrNull_matchedByDbValue() {
      assertSame(
          WebAuthnChallengePurposeEnum.REGISTRATION,
          WebAuthnChallengePurposeEnum.getOrNull("registration"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：Enum名に一致する場合、対応するEnum値を返すこと")
    void getOrNull_matchedByName() {
      assertSame(
          WebAuthnChallengePurposeEnum.AUTHENTICATION,
          WebAuthnChallengePurposeEnum.getOrNull("AUTHENTICATION"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：nullを指定した場合、既定値に倒さずnullを返すこと")
    void getOrNull_null() {
      assertNull(WebAuthnChallengePurposeEnum.getOrNull(null));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：該当するEnum値がない場合、既定値に倒さずnullを返すこと")
    void getOrNull_unmatched() {
      assertNull(WebAuthnChallengePurposeEnum.getOrNull("unknown"));
    }
  }
}
