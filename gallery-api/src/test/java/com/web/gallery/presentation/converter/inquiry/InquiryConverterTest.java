package com.web.gallery.presentation.converter.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.inquiry.InquiryDetailModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryBody;
import com.web.gallery.domain.model.inquiry.InquirySubject;
import com.web.gallery.presentation.request.inquiry.InquiryRegistRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryConverterTest {

  private final InquiryConverter inquiryConverter = new InquiryConverter();

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toInquiryDetailModel {
    @Test
    @Order(1)
    @DisplayName("正常系：件名・本文がそのまま設定され、アカウント番号はセッションの値が採用されること")
    void toInquiryDetailModel_success() {
      InquiryRegistRequest request = new InquiryRegistRequest();
      request.setSubject("件名");
      request.setBody("本文");

      InquiryDetailModel actual = inquiryConverter.toInquiryDetailModel(request, new AccountNo(1L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new InquirySubject("件名"), actual.getSubject());
      assertEquals(new InquiryBody("本文"), actual.getBody());
      assertNull(actual.getInquiryId());
      assertNull(actual.getInquiryNo());
      assertNull(actual.getStatusKbn());
      assertNull(actual.getIsReadByUser());
      assertNull(actual.getCreatedAt());
      assertNull(actual.getReplyModelList());
    }
  }
}
