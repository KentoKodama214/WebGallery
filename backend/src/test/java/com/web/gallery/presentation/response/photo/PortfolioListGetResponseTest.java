package com.web.gallery.presentation.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PortfolioModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioPageModel;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioListGetResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、一覧の並び順のままそのまま値が転写されること")
    void from_allFieldsSet() {
      OffsetDateTime createdAt = OffsetDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));
      OffsetDateTime updatedAt = OffsetDateTime.of(2026, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));
      PortfolioModelList modelList =
          PortfolioModelList.of(
              List.of(
                  PortfolioModel.builder()
                      .accountNo(new AccountNo(1L))
                      .portfolioNo(new PortfolioNo(2L))
                      .theme(new PortfolioTheme("日本の自然"))
                      .templateKbn(PortfolioTemplateEnum.FEATURE)
                      .statusKbn(PortfolioStatusEnum.PUBLISHED)
                      .photoCount(new PhotoCount(4))
                      .createdAt(createdAt)
                      .updatedAt(updatedAt)
                      .build(),
                  PortfolioModel.builder()
                      .accountNo(new AccountNo(1L))
                      .portfolioNo(new PortfolioNo(1L))
                      .theme(new PortfolioTheme("光と影"))
                      .templateKbn(PortfolioTemplateEnum.GRID)
                      .statusKbn(PortfolioStatusEnum.DRAFT)
                      .photoCount(new PhotoCount(0))
                      .build()));

      PortfolioListGetResponse actual =
          PortfolioListGetResponse.from(PortfolioPageModel.of(modelList, false));

      assertFalse(actual.getIsLast());
      assertEquals(2, actual.getPortfolioList().size());
      PortfolioListItemResponse first = actual.getPortfolioList().get(0);
      assertEquals(2L, first.getPortfolioNo());
      assertEquals("日本の自然", first.getTheme());
      assertEquals(PortfolioTemplateEnum.FEATURE, first.getTemplateKbn());
      assertEquals(PortfolioStatusEnum.PUBLISHED, first.getStatusKbn());
      assertEquals(4, first.getPhotoCount());
      assertEquals(createdAt, first.getCreatedAt());
      assertEquals(updatedAt, first.getUpdatedAt());
      PortfolioListItemResponse second = actual.getPortfolioList().get(1);
      assertEquals(1L, second.getPortfolioNo());
      assertEquals(0, second.getPhotoCount());
      assertNull(second.getCreatedAt());
      assertNull(second.getUpdatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ポートフォリオが0件の場合、空リストとなること")
    void from_empty() {
      PortfolioListGetResponse actual =
          PortfolioListGetResponse.from(PortfolioPageModel.of(PortfolioModelList.empty(), true));

      assertTrue(actual.getPortfolioList().isEmpty());
      assertTrue(actual.getIsLast());
    }
  }
}
