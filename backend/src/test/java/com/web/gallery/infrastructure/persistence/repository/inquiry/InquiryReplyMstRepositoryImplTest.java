package com.web.gallery.infrastructure.persistence.repository.inquiry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.ReplyNo;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.inquiry.InquiryReplyMstMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class InquiryReplyMstRepositoryImplTest {
  @InjectMocks private InquiryReplyMstRepositoryImpl inquiryReplyMstRepositoryImpl;

  @Mock private InquiryReplyMstMapper inquiryReplyMstMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNewReplyNo {
    @Test
    @Order(1)
    @DisplayName("正常系：既存の返信が存在する場合、最大返信番号の次番を採番すること")
    void getNewReplyNo_existingReplies() {
      doReturn(3L).when(inquiryReplyMstMapper).getMaxReplyNo(1L);

      ReplyNo actual = inquiryReplyMstRepositoryImpl.getNewReplyNo(new InquiryId(1L));

      assertEquals(4L, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：既存の返信が存在しない場合、1番を採番すること")
    void getNewReplyNo_noExistingReplies() {
      doReturn(null).when(inquiryReplyMstMapper).getMaxReplyNo(1L);

      ReplyNo actual = inquiryReplyMstRepositoryImpl.getNewReplyNo(new InquiryId(1L));

      assertEquals(1L, actual.value());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class existsReplyToOthersInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：自分が起票したお問い合わせを除いた抽出条件でMapperへ問い合わせること")
    void existsReplyToOthersInquiry_excludesOwnInquiry() {
      ArgumentCaptor<InquiryReplyMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(InquiryReplyMstCondition.class);
      doReturn(true).when(inquiryReplyMstMapper).exists(any(InquiryReplyMstCondition.class));

      assertTrue(inquiryReplyMstRepositoryImpl.existsReplyToOthersInquiry(new AccountNo(1L)));

      verify(inquiryReplyMstMapper).exists(conditionCaptor.capture());
      assertEquals(1L, conditionCaptor.getValue().getAdminAccountNo());
      assertEquals(1L, conditionCaptor.getValue().getExcludingInquiryAccountNo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当する返信が存在しない場合、falseを返すこと")
    void existsReplyToOthersInquiry_notFound() {
      doReturn(false).when(inquiryReplyMstMapper).exists(any(InquiryReplyMstCondition.class));

      assertFalse(inquiryReplyMstRepositoryImpl.existsReplyToOthersInquiry(new AccountNo(1L)));
    }
  }
}
