package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.KbnMst;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class KbnMstModelListTest {

  private KbnMst.KbnMstBuilder baseBuilder() {
    return KbnMst.builder()
        .kbnClassCode("prefecture")
        .createdBy(1L)
        .createdAt(OffsetDateTime.now())
        .kbnGroupCode("group")
        .kbnClassJapaneseName("都道府県")
        .kbnGroupJapaneseName("グループ")
        .kbnClassEnglishName("prefecture")
        .kbnGroupEnglishName("group")
        .explanation("");
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：エンティティのリストからModelListが生成されること")
    void from_success() {
      KbnMst entity1 =
          baseBuilder()
              .kbnCode("Hokkaido")
              .sortOrder(1)
              .kbnJapaneseName("北海道")
              .kbnEnglishName("Hokkaido")
              .build();
      KbnMst entity2 =
          baseBuilder()
              .kbnCode("Aomori")
              .sortOrder(2)
              .kbnJapaneseName("青森県")
              .kbnEnglishName("Aomori")
              .build();

      KbnMstModelList actual = KbnMstModelList.from(List.of(entity1, entity2));

      assertEquals(2, actual.size());
      assertEquals("Hokkaido", actual.get(0).getKbnCode().value());
      assertEquals("Aomori", actual.get(1).getKbnCode().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空リストの場合、空のModelListが生成されること")
    void from_empty() {
      KbnMstModelList actual = KbnMstModelList.from(List.of());

      assertTrue(actual.isEmpty());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class sortBySortOrder {
    @Test
    @Order(1)
    @DisplayName("正常系：並び順の昇順でソートされること")
    void sortBySortOrder_success() {
      KbnMstModel model1 =
          KbnMstModel.from(
              baseBuilder()
                  .kbnCode("b")
                  .sortOrder(2)
                  .kbnJapaneseName("b")
                  .kbnEnglishName("b")
                  .build());
      KbnMstModel model2 =
          KbnMstModel.from(
              baseBuilder()
                  .kbnCode("a")
                  .sortOrder(1)
                  .kbnJapaneseName("a")
                  .kbnEnglishName("a")
                  .build());
      KbnMstModelList list = KbnMstModelList.of(List.of(model1, model2));

      KbnMstModelList actual = list.sortBySortOrder();

      assertEquals("a", actual.get(0).getKbnCode().value());
      assertEquals("b", actual.get(1).getKbnCode().value());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class filterByKbnGroupJapaneseName {
    @Test
    @Order(1)
    @DisplayName("正常系：区分グループ日本語名が一致する要素のみに絞り込まれること")
    void filterByKbnGroupJapaneseName_success() {
      KbnMstModel model1 =
          KbnMstModel.from(
              baseBuilder()
                  .kbnCode("a")
                  .sortOrder(1)
                  .kbnGroupJapaneseName("四国")
                  .kbnJapaneseName("a")
                  .kbnEnglishName("a")
                  .build());
      KbnMstModel model2 =
          KbnMstModel.from(
              baseBuilder()
                  .kbnCode("b")
                  .sortOrder(2)
                  .kbnGroupJapaneseName("九州")
                  .kbnJapaneseName("b")
                  .kbnEnglishName("b")
                  .build());
      KbnMstModelList list = KbnMstModelList.of(List.of(model1, model2));

      KbnMstModelList actual = list.filterByKbnGroupJapaneseName("四国");

      assertEquals(1, actual.size());
      assertEquals("a", actual.get(0).getKbnCode().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：一致する要素がない場合、空のModelListが生成されること")
    void filterByKbnGroupJapaneseName_noMatch() {
      KbnMstModel model1 =
          KbnMstModel.from(
              baseBuilder()
                  .kbnCode("a")
                  .sortOrder(1)
                  .kbnGroupJapaneseName("四国")
                  .kbnJapaneseName("a")
                  .kbnEnglishName("a")
                  .build());
      KbnMstModelList list = KbnMstModelList.of(List.of(model1));

      KbnMstModelList actual = list.filterByKbnGroupJapaneseName("九州");

      assertTrue(actual.isEmpty());
    }
  }
}
