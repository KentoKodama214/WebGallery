package com.web.gallery.model.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.entity.inquiry.InquiryReplyMst;
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
class InquiryReplyModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：作成日時が設定されている場合、そのまま設定されること")
    void from_allFieldsSet() {
      InquiryReplyMst entity =
          InquiryReplyMst.builder()
              .inquiryId(1L)
              .replyNo(2L)
              .adminAccountNo(3L)
              .body("返信本文")
              .createdAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
              .build();

      InquiryReplyModel actual = InquiryReplyModel.from(entity);

      assertEquals(new InquiryId(1L), actual.getInquiryId());
      assertEquals(new ReplyNo(2L), actual.getReplyNo());
      assertEquals(new AccountNo(3L), actual.getAdminAccountNo());
      assertEquals(new ReplyBody("返信本文"), actual.getBody());
      assertEquals(
          OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), actual.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：作成日時が未設定の場合、nullが設定されること")
    void from_createdAtNull() {
      InquiryReplyMst entity =
          InquiryReplyMst.builder()
              .inquiryId(1L)
              .replyNo(2L)
              .adminAccountNo(3L)
              .body("返信本文")
              .createdAt(null)
              .build();

      InquiryReplyModel actual = InquiryReplyModel.from(entity);

      assertNull(actual.getCreatedAt());
    }
  }
}
