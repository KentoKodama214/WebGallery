package com.web.gallery.domain.model.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.constant.Consts;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class RecoveryCodeTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：ハイフン・空白を取り除き、英字を大文字に揃えて保持すること")
    void constructor_normalized() {
      assertEquals("ABCDEFGHJKMNPQRS", new RecoveryCode(" abcd-efgh jkmn-pqrs ").value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：表記の揺れがあっても、正規化後が同じなら等価になること")
    void constructor_equality() {
      assertEquals(new RecoveryCode("ABCD-EFGH-JKMN-PQRS"), new RecoveryCode("abcdefghjkmnpqrs"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：nullの場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new RecoveryCode(null));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：ハイフン・空白のみの場合、IllegalArgumentExceptionをスローすること")
    void constructor_blank() {
      assertThrows(IllegalArgumentException.class, () -> new RecoveryCode(" - - "));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class generate {
    @Test
    @Order(1)
    @DisplayName("正常系：読み間違えやすい文字を除いた32種類の文字で、16文字のコードを生成すること")
    void generate_format() {
      RecoveryCode actual = RecoveryCode.generate(new SecureRandom());

      assertEquals(Consts.RECOVERY_CODE_LENGTH, actual.value().length());
      assertTrue(actual.value().matches("[0-9A-HJKMNP-TV-Z]+"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：生成のたびに異なるコードになること")
    void generate_unique() {
      SecureRandom random = new SecureRandom();
      Set<RecoveryCode> codes = new HashSet<>();
      for (int i = 0; i < 100; i++) {
        codes.add(RecoveryCode.generate(random));
      }
      assertEquals(100, codes.size());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toHash {
    @Test
    @Order(1)
    @DisplayName("正常系：正規化後の値のSHA-256（16進数64文字）を返し、表記の揺れによらず同じハッシュになること")
    void toHash_sha256() {
      // printf 'ABCDEFGHJKMNPQRS' | shasum -a 256
      String expected = "f598056127fcd4387651a49b3a4235d8ce50d71f4091068df51511cfc10f388f";

      assertEquals(expected, new RecoveryCode("abcd-efgh-jkmn-pqrs").toHash().value());
      assertEquals(expected, new RecoveryCode("ABCD EFGH JKMN PQRS").toHash().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：異なるコードは異なるハッシュになること")
    void toHash_different() {
      assertNotEquals(
          new RecoveryCode("ABCDEFGHJKMNPQRS").toHash(),
          new RecoveryCode("ABCDEFGHJKMNPQRT").toHash());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toDisplayString {
    @Test
    @Order(1)
    @DisplayName("正常系：4文字ごとにハイフンで区切った表示用の形を返すこと")
    void toDisplayString_grouped() {
      assertEquals("ABCD-EFGH-JKMN-PQRS", new RecoveryCode("abcdefghjkmnpqrs").toDisplayString());
    }
  }
}
