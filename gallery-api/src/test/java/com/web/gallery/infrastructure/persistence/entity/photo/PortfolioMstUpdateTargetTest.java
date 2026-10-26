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
class PortfolioMstUpdateTargetTest {

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
  class forUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：内容の更新に必要な項目が設定されこと")
    void forUpdate_success() {
      PortfolioMstUpdateTarget actual =
          PortfolioMstUpdateTarget.forUpdate(
              detail("説明文", new TemplateSettings(Map.of("rows", List.of()))));

      assertEquals(1L, actual.getUpdatedBy());
      assertEquals("日本の自然", actual.getTheme());
      assertEquals("説明文", actual.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, actual.getTemplateKbn());
      assertEquals(Map.of("rows", List.of()), actual.getTemplateSettings());
      assertEquals(1, actual.getSettingsVersion());
      assertEquals(PortfolioStatusEnum.PUBLISHED, actual.getStatusKbn());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forStatus {
    @Test
    @Order(1)
    @DisplayName("正常系：公開状態・更新者のみが設定され、それ以外は更新対象外（null）となること")
    void forStatus_success() {
      PortfolioMstUpdateTarget actual =
          PortfolioMstUpdateTarget.forStatus(detail("説明文", TemplateSettings.empty()));

      assertEquals(1L, actual.getUpdatedBy());
      assertEquals(PortfolioStatusEnum.PUBLISHED, actual.getStatusKbn());
      // 公開状態の変更では内容を変更しないため、他の項目は「変更なし」としてnullで渡す
      assertNull(actual.getTheme());
      assertNull(actual.getDescription());
      assertNull(actual.getTemplateKbn());
      assertNull(actual.getTemplateSettings());
      assertNull(actual.getSettingsVersion());
    }
  }
}
