package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.common.Explanation;
import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.common.KbnClassEnglishName;
import com.web.gallery.domain.common.KbnClassJapaneseName;
import com.web.gallery.domain.common.KbnCode;
import com.web.gallery.domain.common.KbnEnglishName;
import com.web.gallery.domain.common.KbnGroupCode;
import com.web.gallery.domain.common.KbnGroupEnglishName;
import com.web.gallery.domain.common.KbnGroupJapaneseName;
import com.web.gallery.domain.common.KbnJapaneseName;
import com.web.gallery.domain.common.SortOrder;
import com.web.gallery.model.KbnMstModel;
import com.web.gallery.model.KbnMstModelList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class KbnHelperTest {
  @Autowired private KbnHelper kbnHepler;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class convertToLinkedHashMap {
    @Test
    @Order(1)
    @DisplayName("正常系：区分グループなし")
    void convertToLinkedHashMap_not_kbnGroup() {
      KbnMstModelList kbnMstModelList =
          KbnMstModelList.of(
              List.of(
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Aomori"))
                      .sortOrder(new SortOrder(2))
                      .kbnGroupCode(new KbnGroupCode(""))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
                      .kbnJapaneseName(new KbnJapaneseName("青森"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName(""))
                      .kbnEnglishName(new KbnEnglishName("Aomori"))
                      .explanation(new Explanation("青森は本州最北"))
                      .build(),
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Hokkaido"))
                      .sortOrder(new SortOrder(1))
                      .kbnGroupCode(new KbnGroupCode(""))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
                      .kbnJapaneseName(new KbnJapaneseName("北海道"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName(""))
                      .kbnEnglishName(new KbnEnglishName("Hokkaido"))
                      .explanation(new Explanation("北海道はでっかいどう"))
                      .build(),
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Okinawa"))
                      .sortOrder(new SortOrder(47))
                      .kbnGroupCode(new KbnGroupCode(""))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
                      .kbnJapaneseName(new KbnJapaneseName("沖縄"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName(""))
                      .kbnEnglishName(new KbnEnglishName("Okinawa"))
                      .explanation(new Explanation("沖縄は南国"))
                      .build(),
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Kagoshima"))
                      .sortOrder(new SortOrder(46))
                      .kbnGroupCode(new KbnGroupCode(""))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName(""))
                      .kbnJapaneseName(new KbnJapaneseName("鹿児島"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName(""))
                      .kbnEnglishName(new KbnEnglishName("Kagoshima"))
                      .explanation(new Explanation("鹿児島は九州最南"))
                      .build()));

      Map<String, KbnMstModelList> actual = kbnHepler.convertToLinkedHashMap(kbnMstModelList);

      KbnMstModelList kbnMstModelList1 = actual.get("");
      assertEquals(4, kbnMstModelList1.size());
      assertEquals("Hokkaido", kbnMstModelList1.get(0).getKbnCode().value());
      assertEquals("Aomori", kbnMstModelList1.get(1).getKbnCode().value());
      assertEquals("Kagoshima", kbnMstModelList1.get(2).getKbnCode().value());
      assertEquals("Okinawa", kbnMstModelList1.get(3).getKbnCode().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：区分グループあり")
    void convertToLinkedHashMap_with_kbnGroup() {
      KbnMstModelList kbnMstModelList =
          KbnMstModelList.of(
              List.of(
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Aomori"))
                      .sortOrder(new SortOrder(2))
                      .kbnGroupCode(new KbnGroupCode("Hokkaido_Tohoku"))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName("北海道・東北"))
                      .kbnJapaneseName(new KbnJapaneseName("青森"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName("Hokkaido_Tohoku"))
                      .kbnEnglishName(new KbnEnglishName("Aomori"))
                      .explanation(new Explanation("青森は本州最北"))
                      .build(),
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Hokkaido"))
                      .sortOrder(new SortOrder(1))
                      .kbnGroupCode(new KbnGroupCode("Hokkaido_Tohoku"))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName("北海道・東北"))
                      .kbnJapaneseName(new KbnJapaneseName("北海道"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName("Hokkaido_Tohoku"))
                      .kbnEnglishName(new KbnEnglishName("Hokkaido"))
                      .explanation(new Explanation("北海道はでっかいどう"))
                      .build(),
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Okinawa"))
                      .sortOrder(new SortOrder(47))
                      .kbnGroupCode(new KbnGroupCode("Kyushu_Okinawa"))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName("九州・沖縄"))
                      .kbnJapaneseName(new KbnJapaneseName("沖縄"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName("Kyushu_Okinawa"))
                      .kbnEnglishName(new KbnEnglishName("Okinawa"))
                      .explanation(new Explanation("沖縄は南国"))
                      .build(),
                  KbnMstModel.builder()
                      .kbnClassCode(new KbnClassCode("prefecture"))
                      .kbnCode(new KbnCode("Kagoshima"))
                      .sortOrder(new SortOrder(46))
                      .kbnGroupCode(new KbnGroupCode("Kyushu_Okinawa"))
                      .kbnClassJapaneseName(new KbnClassJapaneseName("都道府県"))
                      .kbnGroupJapaneseName(new KbnGroupJapaneseName("九州・沖縄"))
                      .kbnJapaneseName(new KbnJapaneseName("鹿児島"))
                      .kbnClassEnglishName(new KbnClassEnglishName("prefecture"))
                      .kbnGroupEnglishName(new KbnGroupEnglishName("Kyushu_Okinawa"))
                      .kbnEnglishName(new KbnEnglishName("Kagoshima"))
                      .explanation(new Explanation("鹿児島は九州最南"))
                      .build()));

      Map<String, KbnMstModelList> actual = kbnHepler.convertToLinkedHashMap(kbnMstModelList);

      KbnMstModelList kbnMstModelList1 = actual.get("北海道・東北");
      assertEquals(2, kbnMstModelList1.size());
      assertEquals("Hokkaido", kbnMstModelList1.get(0).getKbnCode().value());
      assertEquals("Aomori", kbnMstModelList1.get(1).getKbnCode().value());
      KbnMstModelList kbnMstModelList2 = actual.get("九州・沖縄");
      assertEquals(2, kbnMstModelList2.size());
      assertEquals("Kagoshima", kbnMstModelList2.get(0).getKbnCode().value());
      assertEquals("Okinawa", kbnMstModelList2.get(1).getKbnCode().value());
    }
  }
}
