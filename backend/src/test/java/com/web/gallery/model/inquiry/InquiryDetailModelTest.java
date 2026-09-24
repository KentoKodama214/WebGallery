package com.web.gallery.model.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.inquiry.InquiryRegistRequest;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.dto.InquiryDetailDto;
import com.web.gallery.entity.inquiry.InquiryReplyMst;
import com.web.gallery.enumeration.InquiryStatusEnum;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryDetailModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByInquiryDetailDto {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsSet() {
      InquiryDetailDto dto = new InquiryDetailDto();
      dto.setId(1L);
      dto.setAccountNo(1L);
      dto.setAccountId("testuser01");
      dto.setAccountName("テストユーザー");
      dto.setInquiryNo(2L);
      dto.setSubject("件名");
      dto.setBody("本文");
      dto.setStatusKbn(InquiryStatusEnum.REPLIED);
      dto.setIsReadByUser(true);
      OffsetDateTime createdAt = OffsetDateTime.now();
      dto.setCreatedAt(createdAt);

      InquiryReplyMst reply =
          InquiryReplyMst.builder()
              .inquiryId(1L)
              .replyNo(1L)
              .adminAccountNo(9L)
              .body("返信本文")
              .createdAt(createdAt)
              .build();

      InquiryDetailModel actual = InquiryDetailModel.from(dto, List.of(reply));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountId("testuser01"), actual.getAccountId());
      assertEquals(new AccountName("テストユーザー"), actual.getAccountName());
      assertEquals(new InquiryId(1L), actual.getInquiryId());
      assertEquals(new InquiryNo(2L), actual.getInquiryNo());
      assertEquals(new InquirySubject("件名"), actual.getSubject());
      assertEquals(new InquiryBody("本文"), actual.getBody());
      assertEquals(InquiryStatusEnum.REPLIED, actual.getStatusKbn());
      assertTrue(actual.getIsReadByUser());
      assertEquals(createdAt, actual.getCreatedAt());
      assertEquals(1, actual.getReplyModelList().size());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントID・アカウント名が未設定の場合、それぞれnullが設定されること")
    void from_accountIdAndAccountNameNull() {
      InquiryDetailDto dto = new InquiryDetailDto();
      dto.setId(1L);
      dto.setAccountNo(1L);
      dto.setAccountId(null);
      dto.setAccountName(null);
      dto.setInquiryNo(2L);
      dto.setSubject("件名");
      dto.setBody("本文");

      InquiryDetailModel actual = InquiryDetailModel.from(dto, List.of());

      assertNull(actual.getAccountId());
      assertNull(actual.getAccountName());
      assertTrue(actual.getReplyModelList().isEmpty());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByInquiryRegistRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：件名・本文がそのまま設定され、アカウント番号はセッションの値が採用されること")
    void from_success() {
      InquiryRegistRequest request = new InquiryRegistRequest();
      request.setSubject("件名");
      request.setBody("本文");

      InquiryDetailModel actual = InquiryDetailModel.from(request, new AccountNo(1L));

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
