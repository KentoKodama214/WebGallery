package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.dto.InquiryDto;
import com.web.gallery.enumeration.InquiryStatusEnum;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryModelTest {

  private InquiryDto baseDto() {
    InquiryDto dto = new InquiryDto();
    dto.setId(1L);
    dto.setAccountNo(1L);
    dto.setInquiryNo(1L);
    dto.setSubject("件名");
    dto.setStatusKbn(InquiryStatusEnum.UNREPLIED);
    dto.setIsReadByUser(true);
    dto.setCreatedAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)));
    return dto;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントID・アカウント名が設定されている場合、そのまま設定されること")
    void from_allFieldsSet() {
      InquiryDto dto = baseDto();
      dto.setAccountId("testuser01");
      dto.setAccountName("テストユーザー");

      InquiryModel actual = InquiryModel.from(dto);

      assertEquals(new InquiryId(1L), actual.getInquiryId());
      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountId("testuser01"), actual.getAccountId());
      assertEquals(new AccountName("テストユーザー"), actual.getAccountName());
      assertEquals(new InquiryNo(1L), actual.getInquiryNo());
      assertEquals(new InquirySubject("件名"), actual.getSubject());
      assertEquals(InquiryStatusEnum.UNREPLIED, actual.getStatusKbn());
      assertTrue(actual.getIsReadByUser());
      assertEquals(
          OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), actual.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントID・アカウント名が未設定（ユーザー向け一覧）の場合、nullが設定されること")
    void from_optionalFieldsNull() {
      InquiryDto dto = baseDto();
      dto.setAccountId(null);
      dto.setAccountName(null);

      InquiryModel actual = InquiryModel.from(dto);

      assertNull(actual.getAccountId());
      assertNull(actual.getAccountName());
    }
  }
}
