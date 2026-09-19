package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.web.gallery.aggregate.Inquiry;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.entity.InquiryMst;
import com.web.gallery.entity.InquiryMstCondition;
import com.web.gallery.entity.InquiryMstUpdateTarget;
import com.web.gallery.entity.InquiryReplyMst;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.InquiryMstMapper;
import com.web.gallery.mapper.InquiryReplyMstMapper;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryReplyModelList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InquiryAggregateRepositoryImplTest {
  @InjectMocks private InquiryAggregateRepositoryImpl inquiryAggregateRepositoryImpl;

  @Mock private InquiryMstMapper inquiryMstMapper;
  @Mock private InquiryReplyMstMapper inquiryReplyMstMapper;

  private Inquiry newInquiryForRegist() {
    InquiryDetailModel requestDetail =
        InquiryDetailModel.builder()
            .accountNo(new AccountNo(1L))
            .subject(new InquirySubject("件名"))
            .body(new InquiryBody("本文"))
            .build();
    return Inquiry.forRegist(requestDetail, new InquiryNo(1L));
  }

  private Inquiry newReconstructedInquiry() {
    InquiryDetailModel existingDetail =
        InquiryDetailModel.builder()
            .accountNo(new AccountNo(1L))
            .inquiryId(new InquiryId(1L))
            .inquiryNo(new InquiryNo(1L))
            .subject(new InquirySubject("件名"))
            .body(new InquiryBody("本文"))
            .build();
    return Inquiry.reconstruct(existingDetail, InquiryReplyModelList.empty());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class regist {
    @Test
    @Order(1)
    @DisplayName("正常系：登録に成功すること")
    void regist_success() throws GalleryException {
      doReturn(1).when(inquiryMstMapper).insert(any(InquiryMst.class));

      inquiryAggregateRepositoryImpl.regist(newInquiryForRegist());

      verify(inquiryMstMapper, times(1)).insert(any(InquiryMst.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：登録件数が1未満の場合、GalleryExceptionをthrowすること")
    void regist_failure() {
      doReturn(0).when(inquiryMstMapper).insert(any(InquiryMst.class));

      assertThrows(
          GalleryException.class,
          () -> inquiryAggregateRepositoryImpl.regist(newInquiryForRegist()));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class addReply {
    @Test
    @Order(1)
    @DisplayName("正常系：返信追加とステータス更新に成功すること")
    void addReply_success() throws GalleryException {
      Inquiry inquiry = newReconstructedInquiry();
      inquiry.addReply(new AccountNo(2L), new ReplyBody("返信本文"), new ReplyNo(1L));
      doReturn(1).when(inquiryReplyMstMapper).insert(any(InquiryReplyMst.class));
      doReturn(1)
          .when(inquiryMstMapper)
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));

      inquiryAggregateRepositoryImpl.addReply(inquiry);

      verify(inquiryReplyMstMapper, times(1)).insert(any(InquiryReplyMst.class));
      verify(inquiryMstMapper, times(1))
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：返信の登録件数が1未満の場合、GalleryExceptionをthrowすること")
    void addReply_replyInsertFailure() {
      Inquiry inquiry = newReconstructedInquiry();
      inquiry.addReply(new AccountNo(2L), new ReplyBody("返信本文"), new ReplyNo(1L));
      doReturn(0).when(inquiryReplyMstMapper).insert(any(InquiryReplyMst.class));

      assertThrows(GalleryException.class, () -> inquiryAggregateRepositoryImpl.addReply(inquiry));

      verify(inquiryMstMapper, never())
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：お問い合わせ本体の更新件数が1未満の場合、GalleryExceptionをthrowすること")
    void addReply_mstUpdateFailure() {
      Inquiry inquiry = newReconstructedInquiry();
      inquiry.addReply(new AccountNo(2L), new ReplyBody("返信本文"), new ReplyNo(1L));
      doReturn(1).when(inquiryReplyMstMapper).insert(any(InquiryReplyMst.class));
      doReturn(0)
          .when(inquiryMstMapper)
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));

      assertThrows(GalleryException.class, () -> inquiryAggregateRepositoryImpl.addReply(inquiry));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class markReadByUser {
    @Test
    @Order(1)
    @DisplayName("正常系：既読化に成功すること")
    void markReadByUser_success() throws GalleryException {
      doReturn(1)
          .when(inquiryMstMapper)
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));

      inquiryAggregateRepositoryImpl.markReadByUser(newReconstructedInquiry());

      verify(inquiryMstMapper, times(1))
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：更新件数が1未満の場合、GalleryExceptionをthrowすること")
    void markReadByUser_failure() {
      doReturn(0)
          .when(inquiryMstMapper)
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));

      assertThrows(
          GalleryException.class,
          () -> inquiryAggregateRepositoryImpl.markReadByUser(newReconstructedInquiry()));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class withdraw {
    @Test
    @Order(1)
    @DisplayName("正常系：取り下げに成功すること")
    void withdraw_success() throws GalleryException {
      doReturn(1)
          .when(inquiryMstMapper)
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));

      inquiryAggregateRepositoryImpl.withdraw(newReconstructedInquiry());

      verify(inquiryMstMapper, times(1))
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：更新件数が1未満の場合、GalleryExceptionをthrowすること")
    void withdraw_failure() {
      doReturn(0)
          .when(inquiryMstMapper)
          .update(any(InquiryMstCondition.class), any(InquiryMstUpdateTarget.class));

      assertThrows(
          GalleryException.class,
          () -> inquiryAggregateRepositoryImpl.withdraw(newReconstructedInquiry()));
    }
  }
}
