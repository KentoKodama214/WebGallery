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
class InquiryMstUpdateTargetTest {

  private InquiryDetailModel.InquiryDetailModelBuilder baseBuilder() {
    return InquiryDetailModel.builder()
        .accountNo(new AccountNo(1L))
        .subject(new InquirySubject("件名"))
        .body(new InquiryBody("本文"));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forReply {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void forReply_allFieldsPresent() {
      InquiryDetailModel detail =
          baseBuilder().statusKbn(InquiryStatusEnum.REPLIED).isReadByUser(false).build();

      InquiryMstUpdateTarget actual = InquiryMstUpdateTarget.forReply(detail, 9L);

      assertEquals(9L, actual.getUpdatedBy());
      assertEquals(InquiryStatusEnum.REPLIED, actual.getStatusKbn());
      assertFalse(actual.getIsReadByUser());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未設定項目のみの場合、ステータス区分・既読フラグはnullのままになること")
    void forReply_allFieldsAbsent() {
      InquiryDetailModel detail = baseBuilder().build();

      InquiryMstUpdateTarget actual = InquiryMstUpdateTarget.forReply(detail, 9L);

      assertEquals(9L, actual.getUpdatedBy());
      assertNull(actual.getStatusKbn());
      assertNull(actual.getIsReadByUser());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forMarkRead {
    @Test
    @Order(1)
    @DisplayName("正常系：既読フラグが設定されている場合、その値が反映され、ステータス区分は設定されないこと")
    void forMarkRead_allFieldsPresent() {
      InquiryDetailModel detail = baseBuilder().isReadByUser(true).build();

      InquiryMstUpdateTarget actual = InquiryMstUpdateTarget.forMarkRead(detail, 3L);

      assertEquals(3L, actual.getUpdatedBy());
      assertTrue(actual.getIsReadByUser());
      assertNull(actual.getStatusKbn());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：既読フラグが未設定の場合、nullのままになること")
    void forMarkRead_allFieldsAbsent() {
      InquiryDetailModel detail = baseBuilder().build();

      InquiryMstUpdateTarget actual = InquiryMstUpdateTarget.forMarkRead(detail, 3L);

      assertEquals(3L, actual.getUpdatedBy());
      assertNull(actual.getIsReadByUser());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forWithdraw {
    @Test
    @Order(1)
    @DisplayName("正常系：ステータス区分が設定されている場合、その値が反映され、既読フラグは設定されないこと")
    void forWithdraw_allFieldsPresent() {
      InquiryDetailModel detail = baseBuilder().statusKbn(InquiryStatusEnum.WITHDRAWN).build();

      InquiryMstUpdateTarget actual = InquiryMstUpdateTarget.forWithdraw(detail, 1L);

      assertEquals(1L, actual.getUpdatedBy());
      assertEquals(InquiryStatusEnum.WITHDRAWN, actual.getStatusKbn());
      assertNull(actual.getIsReadByUser());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ステータス区分が未設定の場合、nullのままになること")
    void forWithdraw_allFieldsAbsent() {
      InquiryDetailModel detail = baseBuilder().build();

      InquiryMstUpdateTarget actual = InquiryMstUpdateTarget.forWithdraw(detail, 1L);

      assertEquals(1L, actual.getUpdatedBy());
      assertNull(actual.getStatusKbn());
    }
  }
}
