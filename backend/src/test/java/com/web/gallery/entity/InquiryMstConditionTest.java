package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryGetModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryMstConditionTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・取得件数上限・取得開始位置がそのまま反映され、ステータス区分は設定されないこと")
    void forList_success() {
      InquiryGetModel model =
          InquiryGetModel.builder().accountNo(new AccountNo(1L)).limit(21).offset(0).build();

      InquiryMstCondition actual = InquiryMstCondition.forList(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(21, actual.getLimit());
      assertEquals(0, actual.getOffset());
      assertNull(actual.getStatusKbn());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forAdminList {
    @Test
    @Order(1)
    @DisplayName("正常系：ステータス区分が設定されている場合、その値が反映され、アカウント番号は設定されないこと")
    void forAdminList_statusKbnPresent() {
      InquiryGetModel model =
          InquiryGetModel.builder()
              .statusKbn(InquiryStatusEnum.REPLIED)
              .limit(21)
              .offset(0)
              .build();

      InquiryMstCondition actual = InquiryMstCondition.forAdminList(model);

      assertEquals(InquiryStatusEnum.REPLIED, actual.getStatusKbn());
      assertEquals(21, actual.getLimit());
      assertEquals(0, actual.getOffset());
      assertNull(actual.getAccountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ステータス区分が未設定の場合、nullが反映されること")
    void forAdminList_statusKbnAbsent() {
      InquiryGetModel model = InquiryGetModel.builder().limit(21).offset(0).build();

      InquiryMstCondition actual = InquiryMstCondition.forAdminList(model);

      assertNull(actual.getStatusKbn());
    }
  }
}
