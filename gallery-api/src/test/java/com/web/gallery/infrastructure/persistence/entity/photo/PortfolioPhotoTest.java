package com.web.gallery.infrastructure.persistence.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioPhotoTest {
  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映され、作成者に所有者のアカウント番号が設定されること")
    void from_allFieldsSet() {
      PortfolioPhoto actual =
          PortfolioPhoto.from(
              PortfolioPhotoModel.builder()
                  .accountNo(new AccountNo(1L))
                  .portfolioNo(new PortfolioNo(2L))
                  .photoNo(new PhotoNo(3L))
                  .displayOrder(new DisplayOrder(4))
                  .photoSettings(new TemplateSettings(Map.of("caption", "金閣寺")))
                  .build());

      assertNull(actual.getId());
      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPortfolioNo());
      assertEquals(3L, actual.getPhotoNo());
      assertEquals(1L, actual.getCreatedBy());
      assertNull(actual.getCreatedAt());
      assertEquals(4, actual.getDisplayOrder());
      assertEquals(Map.of("caption", "金閣寺"), actual.getPhotoSettings());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真設定が未入力の場合、空のJSONオブジェクトが反映されること")
    void from_emptySettings() {
      PortfolioPhoto actual =
          PortfolioPhoto.from(
              PortfolioPhotoModel.builder()
                  .accountNo(new AccountNo(1L))
                  .portfolioNo(new PortfolioNo(2L))
                  .photoNo(new PhotoNo(3L))
                  .displayOrder(new DisplayOrder(1))
                  .photoSettings(TemplateSettings.empty())
                  .build());

      assertEquals(Map.of(), actual.getPhotoSettings());
    }
  }
}
