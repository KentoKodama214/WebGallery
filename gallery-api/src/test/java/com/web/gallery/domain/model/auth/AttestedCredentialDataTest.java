package com.web.gallery.domain.model.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AttestedCredentialDataTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：有効な値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      AttestedCredentialData actual = new AttestedCredentialData(new byte[] {1, 2, 3});

      assertArrayEquals(new byte[] {1, 2, 3}, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new AttestedCredentialData(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：空の配列を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_empty() {
      assertThrows(IllegalArgumentException.class, () -> new AttestedCredentialData(new byte[0]));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：生成後に元の配列を書き換えても、保持している値は変わらないこと")
    void constructor_defensiveCopy() {
      byte[] source = {1, 2, 3};
      AttestedCredentialData actual = new AttestedCredentialData(source);

      source[0] = 9;

      assertArrayEquals(new byte[] {1, 2, 3}, actual.value());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class value {
    @Test
    @Order(1)
    @DisplayName("正常系：返された配列を書き換えても、保持している値は変わらないこと")
    void value_returnsCopy() {
      AttestedCredentialData actual = new AttestedCredentialData(new byte[] {1, 2, 3});

      actual.value()[0] = 9;

      assertArrayEquals(new byte[] {1, 2, 3}, actual.value());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class equalsAndHashCode {
    @Test
    @Order(1)
    @DisplayName("正常系：内容が同じであれば、別の配列から生成しても等価で同じハッシュコードになること")
    void equals_sameContent() {
      AttestedCredentialData first = new AttestedCredentialData(new byte[] {1, 2, 3});
      AttestedCredentialData second = new AttestedCredentialData(new byte[] {1, 2, 3});

      assertEquals(first, second);
      assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：内容が異なれば等価にならないこと")
    void equals_differentContent() {
      assertNotEquals(
          new AttestedCredentialData(new byte[] {1}), new AttestedCredentialData(new byte[] {2}));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toString {
    @Test
    @Order(1)
    @DisplayName("正常系：バイナリの内容は出さず、長さのみを文字列表現に含めること")
    void toString_masksContent() {
      assertEquals(
          "AttestedCredentialData[length=3]",
          new AttestedCredentialData(new byte[] {1, 2, 3}).toString());
    }
  }
}
