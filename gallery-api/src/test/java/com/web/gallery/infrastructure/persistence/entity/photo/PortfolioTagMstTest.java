package com.web.gallery.infrastructure.persistence.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioTagMstTest {
  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映され、作成者に所有者のアカウント番号が設定されること")
    void from_allFieldsSet() {
      PortfolioTagMst actual =
          PortfolioTagMst.from(
              PortfolioTagModel.builder()
                  .accountNo(new AccountNo(1L))
                  .portfolioNo(new PortfolioNo(2L))
                  .tagNo(new TagNo(3L))
                  .tagJapaneseName(new TagJapaneseName("自然"))
                  .tagEnglishName(new TagEnglishName("nature"))
                  .build());

      assertNull(actual.getId());
      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPortfolioNo());
      assertEquals(3L, actual.getTagNo());
      assertEquals(1L, actual.getCreatedBy());
      assertNull(actual.getCreatedAt());
      assertEquals("自然", actual.getTagJapaneseName());
      assertEquals("nature", actual.getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：タグ英語名が未入力（空文字）の場合、空文字が反映されること")
    void from_emptyEnglishName() {
      PortfolioTagMst actual =
          PortfolioTagMst.from(
              PortfolioTagModel.builder()
                  .accountNo(new AccountNo(1L))
                  .portfolioNo(new PortfolioNo(2L))
                  .tagNo(new TagNo(1L))
                  .tagJapaneseName(new TagJapaneseName("自然"))
                  .tagEnglishName(new TagEnglishName(""))
                  .build());

      assertEquals("", actual.getTagEnglishName());
    }
  }
}
