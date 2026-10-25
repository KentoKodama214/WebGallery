package com.web.gallery.application.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.photo.PortfolioNo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioNoListTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：ポートフォリオ番号のLongリストが、並び順を保ったままPortfolioNoへ変換されること")
    void from_allFieldsSet() {
      PortfolioNoList actual = PortfolioNoList.from(List.of(3L, 1L, 2L));

      assertEquals(3, actual.size());
      assertEquals(new PortfolioNo(3L), actual.get(0));
      assertEquals(new PortfolioNo(1L), actual.get(1));
      assertEquals(new PortfolioNo(2L), actual.get(2));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空リストの場合、空のPortfolioNoListが返ること")
    void from_emptyList() {
      assertTrue(PortfolioNoList.from(List.of()).isEmpty());
      assertTrue(PortfolioNoList.empty().isEmpty());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isSameSetAs {
    @Test
    @Order(1)
    @DisplayName("正常系：並び順が異なっても同じポートフォリオ番号の集合であればtrueを返すこと")
    void isSameSetAs_sameSet() {
      assertTrue(
          PortfolioNoList.from(List.of(3L, 1L, 2L))
              .isSameSetAs(PortfolioNoList.from(List.of(1L, 2L, 3L))));
      assertTrue(PortfolioNoList.empty().isSameSetAs(PortfolioNoList.empty()));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：過不足がある場合、falseを返すこと")
    void isSameSetAs_differentSet() {
      PortfolioNoList existing = PortfolioNoList.from(List.of(1L, 2L, 3L));

      assertFalse(PortfolioNoList.from(List.of(1L, 2L)).isSameSetAs(existing));
      assertFalse(PortfolioNoList.from(List.of(1L, 2L, 3L, 4L)).isSameSetAs(existing));
      assertFalse(PortfolioNoList.from(List.of(1L, 2L, 4L)).isSameSetAs(existing));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：重複がある場合、要素の種類が同じでもfalseを返すこと")
    void isSameSetAs_duplicate() {
      assertFalse(
          PortfolioNoList.from(List.of(1L, 2L, 2L, 3L))
              .isSameSetAs(PortfolioNoList.from(List.of(1L, 2L, 3L))));
    }
  }
}
