package com.web.gallery.event;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.ReplyNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/** {@link InquiryEventListener}の単体テストクラス */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class InquiryEventListenerTest {
  @InjectMocks private InquiryEventListener inquiryEventListener;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleInquiryRegisteredEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：InquiryRegisteredEventを受け取っても例外が発生しないこと")
    void handle_inquiryRegisteredEvent_success() {
      InquiryRegisteredEvent event =
          new InquiryRegisteredEvent(new AccountNo(1L), new InquiryNo(1L));
      assertDoesNotThrow(() -> inquiryEventListener.handle(event));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleInquiryRepliedEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：InquiryRepliedEventを受け取っても例外が発生しないこと")
    void handle_inquiryRepliedEvent_success() {
      InquiryRepliedEvent event =
          new InquiryRepliedEvent(new AccountNo(1L), new InquiryNo(1L), new ReplyNo(1L));
      assertDoesNotThrow(() -> inquiryEventListener.handle(event));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class handleInquiryWithdrawnEvent {
    @Test
    @Order(1)
    @DisplayName("正常系：InquiryWithdrawnEventを受け取っても例外が発生しないこと")
    void handle_inquiryWithdrawnEvent_success() {
      InquiryWithdrawnEvent event = new InquiryWithdrawnEvent(new AccountNo(1L), new InquiryNo(1L));
      assertDoesNotThrow(() -> inquiryEventListener.handle(event));
    }
  }
}
