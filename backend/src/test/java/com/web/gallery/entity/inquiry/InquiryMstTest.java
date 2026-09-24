package com.web.gallery.entity.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.inquiry.InquiryDetailModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryMstTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・件名・本文がそのまま反映され、ステータスは未返信・既読フラグはtrueで初期化されること")
    void fromForRegist_success() {
      InquiryDetailModel model =
          InquiryDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .subject(new InquirySubject("件名"))
              .body(new InquiryBody("本文"))
              .build();

      InquiryMst actual = InquiryMst.fromForRegist(model, 5L);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(5L, actual.getInquiryNo());
      assertEquals(1L, actual.getCreatedBy());
      assertEquals(1L, actual.getUpdatedBy());
      assertEquals("件名", actual.getSubject());
      assertEquals("本文", actual.getBody());
      assertEquals(InquiryStatusEnum.UNREPLIED, actual.getStatusKbn());
      assertTrue(actual.getIsReadByUser());
    }
  }
}
