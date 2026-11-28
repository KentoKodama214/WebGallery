package com.web.gallery.application.model.common;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.common.Explanation;
import com.web.gallery.domain.model.common.KbnClassCode;
import com.web.gallery.domain.model.common.KbnClassEnglishName;
import com.web.gallery.domain.model.common.KbnClassJapaneseName;
import com.web.gallery.domain.model.common.KbnCode;
import com.web.gallery.domain.model.common.KbnEnglishName;
import com.web.gallery.domain.model.common.KbnGroupCode;
import com.web.gallery.domain.model.common.KbnGroupEnglishName;
import com.web.gallery.domain.model.common.KbnGroupJapaneseName;
import com.web.gallery.domain.model.common.KbnJapaneseName;
import com.web.gallery.domain.model.common.SortOrder;
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

  private KbnMstModel.KbnMstModelBuilder baseBuilder() {
    return KbnMstModel.builder()
        .kbnClassCode(new KbnClassCode("prefecture"))
        .kbnGroupCode(new KbnGroupCode("group"))
        .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
        .kbnGroupJapaneseName(new KbnGroupJapaneseName("グループ"))
        .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
        .kbnGroupEnglishName(new KbnGroupEnglishName("group"))
        .explanation(new Explanation(""));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class sortBySortOrder {
    @Test
    @Order(1)
    @DisplayName("正常系：並び順の昇順でソートされること")
    void sortBySortOrder_success() {
      KbnMstModel model1 =
          baseBuilder()
              .kbnCode(new KbnCode("b"))
              .sortOrder(new SortOrder(2))
              .kbnJapaneseName(new KbnJapaneseName("b"))
              .kbnEnglishName(new KbnEnglishName("b"))
              .build();
      KbnMstModel model2 =
          baseBuilder()
              .kbnCode(new KbnCode("a"))
              .sortOrder(new SortOrder(1))
              .kbnJapaneseName(new KbnJapaneseName("a"))
              .kbnEnglishName(new KbnEnglishName("a"))
              .build();
      KbnMstModelList list = KbnMstModelList.of(List.of(model1, model2));

      KbnMstModelList actual = list.sortBySortOrder();

      assertEquals("a", actual.get(0).getKbnCode().value());
      assertEquals("b", actual.get(1).getKbnCode().value());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class filterByKbnGroupJapaneseName {
    @Test
    @Order(1)
    @DisplayName("正常系：区分グループ日本語名が一致する要素のみに絞り込まれること")
    void filterByKbnGroupJapaneseName_success() {
      KbnMstModel model1 =
          baseBuilder()
              .kbnCode(new KbnCode("a"))
              .sortOrder(new SortOrder(1))
              .kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
              .kbnJapaneseName(new KbnJapaneseName("a"))
              .kbnEnglishName(new KbnEnglishName("a"))
              .build();
      KbnMstModel model2 =
          baseBuilder()
              .kbnCode(new KbnCode("b"))
              .sortOrder(new SortOrder(2))
              .kbnGroupJapaneseName(new KbnGroupJapaneseName("九州"))
              .kbnJapaneseName(new KbnJapaneseName("b"))
              .kbnEnglishName(new KbnEnglishName("b"))
              .build();
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
          baseBuilder()
              .kbnCode(new KbnCode("a"))
              .sortOrder(new SortOrder(1))
              .kbnGroupJapaneseName(new KbnGroupJapaneseName("四国"))
              .kbnJapaneseName(new KbnJapaneseName("a"))
              .kbnEnglishName(new KbnEnglishName("a"))
              .build();
      KbnMstModelList list = KbnMstModelList.of(List.of(model1));

      KbnMstModelList actual = list.filterByKbnGroupJapaneseName("九州");

      assertTrue(actual.isEmpty());
    }
  }
}
