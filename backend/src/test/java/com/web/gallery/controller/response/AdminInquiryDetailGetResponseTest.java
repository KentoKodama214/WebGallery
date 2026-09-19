package com.web.gallery.controller.response;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryReplyModelList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AdminInquiryDetailGetResponseTest {

  private InquiryDetailModel.InquiryDetailModelBuilder baseBuilder() {
    return InquiryDetailModel.builder()
        .accountNo(new AccountNo(1L))
        .inquiryId(new InquiryId(1L))
        .subject(new InquirySubject("件名"))
        .body(new InquiryBody("本文"))
        .statusKbn(InquiryStatusEnum.UNREPLIED)
        .replyModelList(InquiryReplyModelList.empty());
  }

  @Test
  @DisplayName("正常系：アカウントID・アカウント名が設定されている場合、それぞれの値が設定されること")
  void from_withValue() {
    InquiryDetailModel model =
        baseBuilder()
            .accountId(new AccountId("testuser01"))
            .accountName(new AccountName("テストユーザー"))
            .build();

    AdminInquiryDetailGetResponse actual = AdminInquiryDetailGetResponse.from(model);

    assertEquals("testuser01", actual.getAccountId());
    assertEquals("テストユーザー", actual.getAccountName());
    assertNotNull(actual.getReplyList());
    assertTrue(actual.getReplyList().isEmpty());
  }

  @Test
  @DisplayName("異常系：アカウントID・アカウント名が未設定の場合、それぞれnullが設定されること")
  void from_withoutValue() {
    InquiryDetailModel model = baseBuilder().accountId(null).accountName(null).build();

    AdminInquiryDetailGetResponse actual = AdminInquiryDetailGetResponse.from(model);

    assertNull(actual.getAccountId());
    assertNull(actual.getAccountName());
  }
}
