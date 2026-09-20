package com.web.gallery.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.aggregate.Inquiry;
import com.web.gallery.config.InquiryConfig;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.event.InquiryRegisteredEvent;
import com.web.gallery.event.InquiryRepliedEvent;
import com.web.gallery.event.InquiryWithdrawnEvent;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.InquiryNotFoundException;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryGetModel;
import com.web.gallery.model.InquiryListGetModel;
import com.web.gallery.model.InquiryModelList;
import com.web.gallery.model.InquiryPageModel;
import com.web.gallery.model.InquiryReplyModelList;
import com.web.gallery.repository.impl.InquiryAggregateRepositoryImpl;
import com.web.gallery.repository.impl.InquiryMstRepositoryImpl;
import com.web.gallery.repository.impl.InquiryReplyMstRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class InquiryServiceImplTest {

  @InjectMocks private InquiryServiceImpl inquiryServiceImpl;

  @Mock private InquiryMstRepositoryImpl inquiryMstRepositoryImpl;

  @Mock private InquiryReplyMstRepositoryImpl inquiryReplyMstRepositoryImpl;

  @Mock private InquiryAggregateRepositoryImpl inquiryAggregateRepositoryImpl;

  @Mock private InquiryConfig inquiryConfig;

  @Mock private ApplicationEventPublisher applicationEventPublisher;

  @BeforeEach
  void setUpConfigStub() {
    lenient().when(inquiryConfig.getInquiryCountPerPage()).thenReturn(20);
  }

  private InquiryDetailModel createDetailModel(InquiryStatusEnum statusKbn, Boolean isReadByUser) {
    return InquiryDetailModel.builder()
        .accountNo(new AccountNo(1L))
        .inquiryId(new InquiryId(1L))
        .inquiryNo(new InquiryNo(1L))
        .subject(new InquirySubject("件名"))
        .body(new InquiryBody("本文"))
        .statusKbn(statusKbn)
        .isReadByUser(isReadByUser)
        .replyModelList(InquiryReplyModelList.empty())
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：新規採番したお問い合わせ番号で登録し、イベントを発行する")
    void registInquiry_success() throws GalleryException {
      doReturn(new InquiryNo(3L))
          .when(inquiryMstRepositoryImpl)
          .getNewInquiryNo(any(AccountNo.class));

      InquiryDetailModel requestDetail =
          InquiryDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .subject(new InquirySubject("件名"))
              .body(new InquiryBody("本文"))
              .build();

      InquiryNo result = inquiryServiceImpl.registInquiry(requestDetail);

      assertEquals(3L, result.value());

      ArgumentCaptor<Inquiry> inquiryCaptor = ArgumentCaptor.forClass(Inquiry.class);
      verify(inquiryAggregateRepositoryImpl).regist(inquiryCaptor.capture());
      assertEquals(new AccountNo(1L), inquiryCaptor.getValue().getAccountNo());
      assertEquals(3L, inquiryCaptor.getValue().getInquiryNo().value());

      ArgumentCaptor<InquiryRegisteredEvent> eventCaptor =
          ArgumentCaptor.forClass(InquiryRegisteredEvent.class);
      verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
      assertEquals(new AccountNo(1L), eventCaptor.getValue().accountNo());
      assertEquals(3L, eventCaptor.getValue().inquiryNo().value());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryList {
    @Test
    @Order(1)
    @DisplayName("正常系：InquiryGetModelへ変換してRepositoryへ委譲する")
    void getInquiryList_success() {
      InquiryPageModel pageModel = InquiryPageModel.of(InquiryModelList.empty(), true);
      doReturn(pageModel).when(inquiryMstRepositoryImpl).getInquiryList(any(InquiryGetModel.class));

      InquiryListGetModel listGetModel =
          InquiryListGetModel.builder().accountNo(new AccountNo(1L)).pageNo(1).build();

      InquiryPageModel result = inquiryServiceImpl.getInquiryList(listGetModel);

      assertEquals(pageModel, result);

      ArgumentCaptor<InquiryGetModel> getModelCaptor =
          ArgumentCaptor.forClass(InquiryGetModel.class);
      verify(inquiryMstRepositoryImpl).getInquiryList(getModelCaptor.capture());
      assertEquals(new AccountNo(1L), getModelCaptor.getValue().getAccountNo());
      assertEquals(21, getModelCaptor.getValue().getLimit());
      assertEquals(0, getModelCaptor.getValue().getOffset());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：既読済みの場合、既読化処理を行わない")
    void getInquiryDetail_alreadyRead() throws GalleryException {
      InquiryDetailModel detail = createDetailModel(InquiryStatusEnum.REPLIED, true);
      doReturn(detail)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetail(any(AccountNo.class), any(InquiryNo.class));

      InquiryDetailModel result =
          inquiryServiceImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(1L));

      assertEquals(detail, result);
      verify(inquiryAggregateRepositoryImpl, never()).markReadByUser(any(Inquiry.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未読の返信が存在する場合、既読化する")
    void getInquiryDetail_markAsRead() throws GalleryException {
      InquiryDetailModel detail = createDetailModel(InquiryStatusEnum.REPLIED, false);
      doReturn(detail)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetail(any(AccountNo.class), any(InquiryNo.class));

      InquiryDetailModel result =
          inquiryServiceImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(1L));

      assertTrue(result.getIsReadByUser());

      ArgumentCaptor<Inquiry> inquiryCaptor = ArgumentCaptor.forClass(Inquiry.class);
      verify(inquiryAggregateRepositoryImpl).markReadByUser(inquiryCaptor.capture());
      assertTrue(inquiryCaptor.getValue().getDetail().getIsReadByUser());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：お問い合わせが存在しない場合、InquiryNotFoundExceptionをthrowする")
    void getInquiryDetail_notFound() throws GalleryException {
      doThrow(InquiryNotFoundException.class)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetail(any(AccountNo.class), any(InquiryNo.class));

      assertThrows(
          InquiryNotFoundException.class,
          () -> inquiryServiceImpl.getInquiryDetail(new AccountNo(1L), new InquiryNo(1L)));

      verify(inquiryAggregateRepositoryImpl, never()).markReadByUser(any(Inquiry.class));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：accountNoを設定せずInquiryGetModelへ変換してRepositoryへ委譲する")
    void getInquiryListForAdmin_success() {
      InquiryPageModel pageModel = InquiryPageModel.of(InquiryModelList.empty(), true);
      doReturn(pageModel)
          .when(inquiryMstRepositoryImpl)
          .getInquiryListForAdmin(any(InquiryGetModel.class));

      InquiryListGetModel listGetModel =
          InquiryListGetModel.builder().statusKbn(InquiryStatusEnum.UNREPLIED).pageNo(1).build();

      InquiryPageModel result = inquiryServiceImpl.getInquiryListForAdmin(listGetModel);

      assertEquals(pageModel, result);

      ArgumentCaptor<InquiryGetModel> getModelCaptor =
          ArgumentCaptor.forClass(InquiryGetModel.class);
      verify(inquiryMstRepositoryImpl).getInquiryListForAdmin(getModelCaptor.capture());
      assertNull(getModelCaptor.getValue().getAccountNo());
      assertEquals(InquiryStatusEnum.UNREPLIED, getModelCaptor.getValue().getStatusKbn());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryDetailForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：Repositoryへ委譲する")
    void getInquiryDetailForAdmin_success() throws GalleryException {
      InquiryDetailModel detail = createDetailModel(InquiryStatusEnum.UNREPLIED, true);
      doReturn(detail)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetailForAdmin(any(InquiryId.class));

      InquiryDetailModel result = inquiryServiceImpl.getInquiryDetailForAdmin(new InquiryId(1L));

      assertEquals(detail, result);
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class replyToInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：返信を追加し、ステータスを回答済みへ遷移させ、イベントを発行する")
    void replyToInquiry_success() throws GalleryException {
      InquiryDetailModel detail = createDetailModel(InquiryStatusEnum.UNREPLIED, true);
      doReturn(detail)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetailForAdmin(any(InquiryId.class));
      doReturn(new ReplyNo(1L))
          .when(inquiryReplyMstRepositoryImpl)
          .getNewReplyNo(any(InquiryId.class));

      ReplyNo result =
          inquiryServiceImpl.replyToInquiry(
              new InquiryId(1L), new AccountNo(2L), new ReplyBody("返信本文"));

      assertEquals(1L, result.value());

      ArgumentCaptor<Inquiry> inquiryCaptor = ArgumentCaptor.forClass(Inquiry.class);
      verify(inquiryAggregateRepositoryImpl).addReply(inquiryCaptor.capture());
      assertEquals(InquiryStatusEnum.REPLIED, inquiryCaptor.getValue().getDetail().getStatusKbn());
      assertFalse(inquiryCaptor.getValue().getDetail().getIsReadByUser());
      assertEquals(new AccountNo(2L), inquiryCaptor.getValue().getNewReply().getAdminAccountNo());

      ArgumentCaptor<InquiryRepliedEvent> eventCaptor =
          ArgumentCaptor.forClass(InquiryRepliedEvent.class);
      verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
      assertEquals(1L, eventCaptor.getValue().replyNo().value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：お問い合わせが存在しない場合、InquiryNotFoundExceptionをthrowする")
    void replyToInquiry_notFound() throws GalleryException {
      doThrow(InquiryNotFoundException.class)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetailForAdmin(any(InquiryId.class));

      assertThrows(
          InquiryNotFoundException.class,
          () ->
              inquiryServiceImpl.replyToInquiry(
                  new InquiryId(1L), new AccountNo(2L), new ReplyBody("返信本文")));

      verify(inquiryAggregateRepositoryImpl, never()).addReply(any(Inquiry.class));
      verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：取り下げ済みの場合、BadRequestExceptionをthrowする")
    void replyToInquiry_withdrawn() throws GalleryException {
      InquiryDetailModel detail = createDetailModel(InquiryStatusEnum.WITHDRAWN, true);
      doReturn(detail)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetailForAdmin(any(InquiryId.class));

      assertThrows(
          BadRequestException.class,
          () ->
              inquiryServiceImpl.replyToInquiry(
                  new InquiryId(1L), new AccountNo(2L), new ReplyBody("返信本文")));

      verify(inquiryAggregateRepositoryImpl, never()).addReply(any(Inquiry.class));
      verify(applicationEventPublisher, never()).publishEvent(any());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class withdrawInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：ステータスを取り下げへ遷移させ、イベントを発行する")
    void withdrawInquiry_success() throws GalleryException {
      InquiryDetailModel detail = createDetailModel(InquiryStatusEnum.UNREPLIED, true);
      doReturn(detail)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetail(any(AccountNo.class), any(InquiryNo.class));

      inquiryServiceImpl.withdrawInquiry(new AccountNo(1L), new InquiryNo(1L));

      ArgumentCaptor<Inquiry> inquiryCaptor = ArgumentCaptor.forClass(Inquiry.class);
      verify(inquiryAggregateRepositoryImpl).withdraw(inquiryCaptor.capture());
      assertEquals(
          InquiryStatusEnum.WITHDRAWN, inquiryCaptor.getValue().getDetail().getStatusKbn());

      ArgumentCaptor<InquiryWithdrawnEvent> eventCaptor =
          ArgumentCaptor.forClass(InquiryWithdrawnEvent.class);
      verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
      assertEquals(new AccountNo(1L), eventCaptor.getValue().accountNo());
      assertEquals(1L, eventCaptor.getValue().inquiryNo().value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：お問い合わせが存在しない場合、InquiryNotFoundExceptionをthrowする")
    void withdrawInquiry_notFound() throws GalleryException {
      doThrow(InquiryNotFoundException.class)
          .when(inquiryMstRepositoryImpl)
          .getInquiryDetail(any(AccountNo.class), any(InquiryNo.class));

      assertThrows(
          InquiryNotFoundException.class,
          () -> inquiryServiceImpl.withdrawInquiry(new AccountNo(1L), new InquiryNo(1L)));

      verify(inquiryAggregateRepositoryImpl, never()).withdraw(any(Inquiry.class));
      verify(applicationEventPublisher, never()).publishEvent(any());
    }
  }
}
