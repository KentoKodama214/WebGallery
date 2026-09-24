package com.web.gallery.model.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.enumeration.InquiryStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryGetModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：1ページ目の場合、offsetが0でlimitが表示件数+1になること")
    void of_firstPage() {
      InquiryListGetModel inquiryListGetModel =
          InquiryListGetModel.builder()
              .accountNo(new AccountNo(1L))
              .statusKbn(InquiryStatusEnum.UNREPLIED)
              .pageNo(1)
              .build();

      InquiryGetModel actual = InquiryGetModel.of(inquiryListGetModel, 20);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(InquiryStatusEnum.UNREPLIED, actual.getStatusKbn());
      assertEquals(21, actual.getLimit());
      assertEquals(0, actual.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：2ページ目以降の場合、pageNoに応じてoffsetが計算されること")
    void of_secondPage() {
      InquiryListGetModel inquiryListGetModel = InquiryListGetModel.builder().pageNo(3).build();

      InquiryGetModel actual = InquiryGetModel.of(inquiryListGetModel, 20);

      assertEquals(21, actual.getLimit());
      assertEquals(40, actual.getOffset());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウント番号・ステータス区分が未設定の場合、nullが設定されること")
    void of_optionalFieldsNull() {
      InquiryListGetModel inquiryListGetModel =
          InquiryListGetModel.builder().accountNo(null).statusKbn(null).pageNo(1).build();

      InquiryGetModel actual = InquiryGetModel.of(inquiryListGetModel, 20);

      assertNull(actual.getAccountNo());
      assertNull(actual.getStatusKbn());
    }
  }
}
