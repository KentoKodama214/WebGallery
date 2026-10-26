package com.web.gallery.application.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.MoveDirectionEnum;
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
  class contains {
    @Test
    @Order(1)
    @DisplayName("正常系：含まれるポートフォリオ番号はtrue、含まれないものはfalseを返すこと")
    void contains_success() {
      PortfolioNoList list = PortfolioNoList.from(List.of(3L, 1L));

      assertTrue(list.contains(new PortfolioNo(1L)));
      assertFalse(list.contains(new PortfolioNo(2L)));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class move {
    private final PortfolioNoList list = PortfolioNoList.from(List.of(3L, 1L, 2L));

    @Test
    @Order(1)
    @DisplayName("正常系：上へ移動すると1つ前の要素と入れ替わり、元のインスタンスは変更しないこと")
    void move_up() {
      PortfolioNoList actual = list.move(new PortfolioNo(1L), MoveDirectionEnum.UP);

      assertEquals(PortfolioNoList.from(List.of(1L, 3L, 2L)), actual);
      assertEquals(PortfolioNoList.from(List.of(3L, 1L, 2L)), list);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：下へ移動すると1つ後ろの要素と入れ替わること")
    void move_down() {
      assertEquals(
          PortfolioNoList.from(List.of(3L, 2L, 1L)),
          list.move(new PortfolioNo(1L), MoveDirectionEnum.DOWN));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：先頭を上へ・末尾を下へ移動しようとした場合、並びを変えないこと")
    void move_boundary() {
      assertEquals(list, list.move(new PortfolioNo(3L), MoveDirectionEnum.UP));
      assertEquals(list, list.move(new PortfolioNo(2L), MoveDirectionEnum.DOWN));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：含まれないポートフォリオ番号を指定した場合、並びを変えないこと")
    void move_notContained() {
      assertEquals(list, list.move(new PortfolioNo(9L), MoveDirectionEnum.UP));
    }
  }
}
