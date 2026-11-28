package com.web.gallery.infrastructure.persistence.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioMstTest {

  /**
   * 集約ルートで所有者・ポートフォリオ番号・公開状態を確定済みのポートフォリオ詳細情報を生成する
   *
   * @param description 説明文
   * @param templateSettings テンプレート設定
   * @return {@link PortfolioDetailModel}
   */
  private static PortfolioDetailModel detail(
      String description, TemplateSettings templateSettings) {
    return PortfolioDetailModel.builder()
        .accountNo(new AccountNo(1L))
        .portfolioNo(new PortfolioNo(2L))
        .theme(new PortfolioTheme("日本の自然"))
        .description(new PortfolioDescription(description))
        .templateKbn(PortfolioTemplateEnum.GRID)
        .templateSettings(templateSettings)
        .statusKbn(PortfolioStatusEnum.PUBLISHED)
        .portfolioPhotoModelList(PortfolioPhotoModelList.empty())
        .portfolioTagModelList(PortfolioTagModelList.empty())
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映され、削除フラグ・設定バージョン・作成者・更新者が設定されること")
    void fromForRegist_allFieldsSet() {
      PortfolioMst actual =
          PortfolioMst.fromForRegist(
              detail(
                  "説明文",
                  new TemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))))));

      assertNull(actual.getId());
      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPortfolioNo());
      assertEquals(1L, actual.getCreatedBy());
      assertNull(actual.getCreatedAt());
      assertEquals(1L, actual.getUpdatedBy());
      assertNull(actual.getUpdatedAt());
      assertEquals("日本の自然", actual.getTheme());
      assertEquals("説明文", actual.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, actual.getTemplateKbn());
      assertEquals(
          Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))), actual.getTemplateSettings());
      assertEquals(1, actual.getSettingsVersion());
      assertEquals(PortfolioStatusEnum.PUBLISHED, actual.getStatusKbn());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：説明文・テンプレート設定が未入力の場合、空文字・空のJSONオブジェクトが反映されること")
    void fromForRegist_optionalFieldsEmpty() {
      PortfolioMst actual = PortfolioMst.fromForRegist(detail("", TemplateSettings.empty()));

      assertEquals("", actual.getDescription());
      assertEquals(Map.of(), actual.getTemplateSettings());
    }
  }
}
