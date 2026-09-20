package com.web.gallery.controller.response.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.inquiry.InquiryModel;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AdminInquiryListItemResponseTest {

  private InquiryModel.InquiryModelBuilder baseBuilder() {
    return InquiryModel.builder()
        .inquiryId(new InquiryId(1L))
        .accountNo(new AccountNo(1L))
        .inquiryNo(new InquiryNo(1L))
        .subject(new InquirySubject("件名"))
        .statusKbn(InquiryStatusEnum.UNREPLIED)
        .isReadByUser(false)
        .createdAt(OffsetDateTime.now());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントID・アカウント名が設定されている場合、それぞれの値が設定されること")
    void from_withValue() {
      InquiryModel model =
          baseBuilder()
              .accountId(new AccountId("testuser01"))
              .accountName(new AccountName("テストユーザー"))
              .build();

      AdminInquiryListItemResponse actual = AdminInquiryListItemResponse.from(model);

      assertEquals("testuser01", actual.getAccountId());
      assertEquals("テストユーザー", actual.getAccountName());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウントID・アカウント名が未設定の場合、それぞれnullが設定されること")
    void from_withoutValue() {
      InquiryModel model = baseBuilder().accountId(null).accountName(null).build();

      AdminInquiryListItemResponse actual = AdminInquiryListItemResponse.from(model);

      assertNull(actual.getAccountId());
      assertNull(actual.getAccountName());
    }
  }
}
