package com.web.gallery.application.model.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.auth.RecoveryCode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class RecoveryCodeListTest {

  private static final RecoveryCode FIRST = new RecoveryCode("ABCDEFGHJKMNPQRS");
  private static final RecoveryCode SECOND = new RecoveryCode("0123456789ABCDEF");

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("異常系：nullの場合、NullPointerExceptionをスローすること")
    void constructor_null() {
      assertThrows(NullPointerException.class, () -> new RecoveryCodeList(null));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：リストの順序のまま保持し、各アクセサで参照できること")
    void of_success() {
      RecoveryCodeList actual = RecoveryCodeList.of(List.of(FIRST, SECOND));

      assertEquals(2, actual.size());
      assertFalse(actual.isEmpty());
      assertEquals(FIRST, actual.get(0));
      assertEquals(SECOND, actual.get(1));
      assertEquals(List.of(FIRST, SECOND), actual.stream().toList());
      assertEquals(List.of(FIRST, SECOND), actual.toList());
      List<RecoveryCode> iterated = new ArrayList<>();
      actual.forEach(iterated::add);
      assertEquals(List.of(FIRST, SECOND), iterated);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class empty {
    @Test
    @Order(1)
    @DisplayName("正常系：空のリストを生成すること")
    void empty_success() {
      RecoveryCodeList actual = RecoveryCodeList.empty();

      assertEquals(0, actual.size());
      assertTrue(actual.isEmpty());
    }
  }
}
