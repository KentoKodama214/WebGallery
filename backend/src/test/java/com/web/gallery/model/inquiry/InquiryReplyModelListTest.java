package com.web.gallery.model.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.inquiry.InquiryReplyMst;
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
class InquiryReplyModelListTest {

  private InquiryReplyMst.InquiryReplyMstBuilder baseBuilder() {
    return InquiryReplyMst.builder()
        .inquiryId(1L)
        .adminAccountNo(1L)
        .createdBy(1L)
        .createdAt(OffsetDateTime.now())
        .body("返信本文");
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：エンティティのリストからModelListが生成されること")
    void from_success() {
      InquiryReplyMst entity1 = baseBuilder().replyNo(1L).build();
      InquiryReplyMst entity2 = baseBuilder().replyNo(2L).build();

      InquiryReplyModelList actual = InquiryReplyModelList.from(List.of(entity1, entity2));

      assertEquals(2, actual.size());
      assertEquals(1L, actual.toList().get(0).getReplyNo().value());
      assertEquals(2L, actual.toList().get(1).getReplyNo().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空リストの場合、空のModelListが生成されること")
    void from_empty() {
      InquiryReplyModelList actual = InquiryReplyModelList.from(List.of());

      assertTrue(actual.isEmpty());
    }
  }
}
