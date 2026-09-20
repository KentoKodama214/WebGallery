package com.web.gallery.entity.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.model.inquiry.InquiryReplyModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryReplyMstTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせID・返信番号・管理者アカウント番号・本文がそのまま反映され、作成者は管理者アカウント番号になること")
    void fromForRegist_success() {
      InquiryReplyModel model =
          InquiryReplyModel.builder()
              .inquiryId(new InquiryId(1L))
              .replyNo(new ReplyNo(2L))
              .adminAccountNo(new AccountNo(3L))
              .body(new ReplyBody("返信本文"))
              .build();

      InquiryReplyMst actual = InquiryReplyMst.fromForRegist(model);

      assertEquals(1L, actual.getInquiryId());
      assertEquals(2L, actual.getReplyNo());
      assertEquals(3L, actual.getAdminAccountNo());
      assertEquals(3L, actual.getCreatedBy());
      assertEquals("返信本文", actual.getBody());
    }
  }
}
