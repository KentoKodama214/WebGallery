package com.web.gallery.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.enumeration.InquiryStatusEnum;
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
public class InquiryControllerTest {
  @InjectMocks private InquiryController inquiryController;

  @Mock private InquiryService inquiryService;

  @Mock private SessionHelper sessionHelper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    lenient().when(sessionHelper.getAccountNo()).thenReturn(1L);
    mockMvc =
        MockMvcBuilders.standaloneSetup(inquiryController)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/InquiryControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせを登録できること")
    void registInquiry_success() throws Exception {
      doReturn(new InquiryNo(1L)).when(inquiryService).registInquiry(any());

      mockMvc
          .perform(
              post("/api/v1/inquiries")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.inquiryNo").value(1));

      verify(inquiryService, times(1)).registInquiry(any());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：件名が空。BadRequestExceptionをthrowする")
    void registInquiry_BadRequestException_blank_subject() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/inquiries")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_blank_subject.json")))
          .andExpect(status().isBadRequest());

      verify(inquiryService, times(0)).registInquiry(any());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryList {
    @Test
    @Order(1)
    @DisplayName("正常系：自分のお問い合わせ一覧を取得できること")
    void getInquiryList_success() throws Exception {
      InquiryModelList inquiryModelList =
          InquiryModelList.of(
              List.of(
                  InquiryModel.builder()
                      .inquiryId(new InquiryId(1L))
                      .accountNo(new AccountNo(1L))
                      .inquiryNo(new InquiryNo(1L))
                      .subject(new InquirySubject("件名"))
                      .statusKbn(InquiryStatusEnum.UNREPLIED)
                      .isReadByUser(true)
                      .createdAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
                      .build()));

      doReturn(InquiryPageModel.of(inquiryModelList, true))
          .when(inquiryService)
          .getInquiryList(any());

      mockMvc
          .perform(get("/api/v1/inquiries"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.inquiryList[0].inquiryNo").value(1))
          .andExpect(jsonPath("$.inquiryList[0].subject").value("件名"))
          .andExpect(jsonPath("$.inquiryList[0].statusKbn").value("unreplied"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ページ番号が0以下。BadRequestExceptionをthrowする")
    void getInquiryList_BadRequestException_pageNo_not_positive() throws Exception {
      mockMvc
          .perform(get("/api/v1/inquiries").param("pageNo", "0"))
          .andExpect(status().isBadRequest());

      verify(inquiryService, times(0)).getInquiryList(any());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getInquiryDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせ詳細を取得できること")
    void getInquiryDetail_success() throws Exception {
      InquiryDetailModel detail =
          InquiryDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .inquiryId(new InquiryId(1L))
              .inquiryNo(new InquiryNo(1L))
              .subject(new InquirySubject("件名"))
              .body(new InquiryBody("本文"))
              .statusKbn(InquiryStatusEnum.UNREPLIED)
              .createdAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
              .replyModelList(InquiryReplyModelList.empty())
              .build();

      doReturn(detail).when(inquiryService).getInquiryDetail(any(), any());

      mockMvc
          .perform(get("/api/v1/inquiries/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inquiryNo").value(1))
          .andExpect(jsonPath("$.subject").value("件名"))
          .andExpect(jsonPath("$.body").value("本文"))
          .andExpect(jsonPath("$.replyList").isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：お問い合わせが存在しない。InquiryNotFoundExceptionをthrowする")
    void getInquiryDetail_InquiryNotFoundException() throws Exception {
      doThrow(InquiryNotFoundException.class).when(inquiryService).getInquiryDetail(any(), any());

      mockMvc.perform(get("/api/v1/inquiries/999")).andExpect(status().isBadRequest());
    }
  }
}
