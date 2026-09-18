package com.web.gallery.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.InquiryNotFoundException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryModel;
import com.web.gallery.model.InquiryModelList;
import com.web.gallery.model.InquiryPageModel;
import com.web.gallery.model.InquiryReplyModelList;
import com.web.gallery.service.InquiryService;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AdminInquiryControllerTest {
  @InjectMocks private AdminInquiryController adminInquiryController;

  @Mock private InquiryService inquiryService;

  @Mock private SessionHelper sessionHelper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    lenient().when(sessionHelper.getAccountNo()).thenReturn(1L);
    mockMvc =
        MockMvcBuilders.standaloneSetup(adminInquiryController)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/AdminInquiryControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAdminInquiryList {
    @Test
    @Order(1)
    @DisplayName("正常系：全アカウントのお問い合わせ一覧を取得できること")
    void getAdminInquiryList_success() throws Exception {
      InquiryModelList inquiryModelList =
          InquiryModelList.of(
              List.of(
                  InquiryModel.builder()
                      .inquiryId(new InquiryId(1L))
                      .accountNo(new AccountNo(1L))
                      .accountId(new AccountId("aaaaaaaa"))
                      .accountName(new AccountName("AAAAAAAA"))
                      .inquiryNo(new InquiryNo(1L))
                      .subject(new InquirySubject("件名"))
                      .statusKbn(InquiryStatusEnum.UNREPLIED)
                      .isReadByUser(true)
                      .createdAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                      .build()));

      doReturn(InquiryPageModel.of(inquiryModelList, true))
          .when(inquiryService)
          .getInquiryListForAdmin(any());

      mockMvc
          .perform(get("/api/v1/admin/inquiries"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.inquiryList[0].inquiryId").value(1))
          .andExpect(jsonPath("$.inquiryList[0].accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.inquiryList[0].accountName").value("AAAAAAAA"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAdminInquiryDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせ詳細を取得できること")
    void getAdminInquiryDetail_success() throws Exception {
      InquiryDetailModel detail =
          InquiryDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .inquiryId(new InquiryId(1L))
              .inquiryNo(new InquiryNo(1L))
              .subject(new InquirySubject("件名"))
              .body(new InquiryBody("本文"))
              .statusKbn(InquiryStatusEnum.UNREPLIED)
              .createdAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
              .replyModelList(InquiryReplyModelList.empty())
              .build();

      doReturn(detail).when(inquiryService).getInquiryDetailForAdmin(any());

      mockMvc
          .perform(get("/api/v1/admin/inquiries/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inquiryId").value(1))
          .andExpect(jsonPath("$.accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.subject").value("件名"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：お問い合わせが存在しない。InquiryNotFoundExceptionをthrowする")
    void getAdminInquiryDetail_InquiryNotFoundException() throws Exception {
      doThrow(InquiryNotFoundException.class).when(inquiryService).getInquiryDetailForAdmin(any());

      mockMvc.perform(get("/api/v1/admin/inquiries/999")).andExpect(status().isBadRequest());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class replyToInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせに返信できること")
    void replyToInquiry_success() throws Exception {
      doReturn(new ReplyNo(1L)).when(inquiryService).replyToInquiry(any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/1/replies")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("reply_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.replyNo").value(1));

      verify(inquiryService, times(1)).replyToInquiry(any(), any(), any());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：返信本文が空。BadRequestExceptionをthrowする")
    void replyToInquiry_BadRequestException_blank_body() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/1/replies")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("reply_badrequest_blank_body.json")))
          .andExpect(status().isBadRequest());

      verify(inquiryService, times(0)).replyToInquiry(any(), any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：お問い合わせが存在しない。InquiryNotFoundExceptionをthrowする")
    void replyToInquiry_InquiryNotFoundException() throws Exception {
      doThrow(InquiryNotFoundException.class)
          .when(inquiryService)
          .replyToInquiry(any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/999/replies")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("reply_success.json")))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：取り下げ済みのお問い合わせ。BadRequestExceptionをthrowする")
    void replyToInquiry_withdrawn() throws Exception {
      doThrow(BadRequestException.class).when(inquiryService).replyToInquiry(any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/1/replies")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("reply_success.json")))
          .andExpect(status().isBadRequest());
    }
  }
}
