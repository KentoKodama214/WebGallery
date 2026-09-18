package com.web.gallery.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.model.AccountModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class InquiryControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  private Authentication createAuthentication(Long accountNo, String accountId) {
    AccountModel sessionAccount =
        AccountModel.builder()
            .accountNo(new AccountNo(accountNo))
            .accountId(new AccountId(accountId))
            .accountName(new AccountName(accountId.toUpperCase()))
            .password(new Password("$2a$10$password1"))
            .authorityKbn(AuthorityEnum.MINI)
            .build();
    AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
    return new UsernamePasswordAuthenticationToken(
        accountPrincipal, null, accountPrincipal.getAuthorities());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/InquiryControllerIntegrationTest.sql")
  class registInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：お問い合わせを新規登録できる")
    void registInquiry_success() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/inquiries")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(1L, "aaaaaaaa")))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"subject\": \"新規のお問い合わせ\", \"body\": \"新規のお問い合わせ本文です。\"}"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.inquiryNo").value(3));

      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_mst WHERE account_no = 1 AND inquiry_no = 3",
              Integer.class);
      assertEquals(1, count);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：件名が空の場合は400を返す")
    void registInquiry_badRequest() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/inquiries")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(1L, "aaaaaaaa")))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"subject\": \"\", \"body\": \"本文\"}"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/InquiryControllerIntegrationTest.sql")
  class getInquiryList {
    @Test
    @Order(1)
    @DisplayName("正常系：自分のお問い合わせのみ取得できる")
    void getInquiryList_success() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/inquiries")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(1L, "aaaaaaaa")))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.inquiryList.length()").value(2))
          .andExpect(jsonPath("$.inquiryList[0].inquiryNo").value(2))
          .andExpect(jsonPath("$.inquiryList[1].inquiryNo").value(1));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/InquiryControllerIntegrationTest.sql")
  class getInquiryDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：返信付きの詳細を取得でき、未読の返信は既読化される")
    void getInquiryDetail_success() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/inquiries/2")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(1L, "aaaaaaaa")))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.inquiryNo").value(2))
          .andExpect(jsonPath("$.subject").value("パスワード変更方法"))
          .andExpect(jsonPath("$.statusKbn").value("replied"))
          .andExpect(jsonPath("$.replyList.length()").value(1))
          .andExpect(jsonPath("$.replyList[0].body").value("パスワード変更方法は設定画面からご案内できます。"));

      Boolean isReadByUser =
          jdbcTemplate.queryForObject(
              "SELECT is_read_by_user FROM common.inquiry_mst WHERE id = 2", Boolean.class);
      assertTrue(isReadByUser);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：他アカウントのお問い合わせは取得できない（400）")
    void getInquiryDetail_otherAccount() throws Exception {
      // inquiryNo=2はアカウント1のみが持つ番号（アカウント2にはinquiryNo=1しか存在しない）
      mockMvc
          .perform(
              get("/api/v1/inquiries/2")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(2L, "bbbbbbbb")))
                  .with(csrf()))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：存在しないお問い合わせ番号の場合は400を返す")
    void getInquiryDetail_notFound() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/inquiries/999")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(1L, "aaaaaaaa")))
                  .with(csrf()))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/InquiryControllerIntegrationTest.sql")
  class withdrawInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：自分のお問い合わせを取り下げられる")
    void withdrawInquiry_success() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/inquiries/1/withdrawal")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(1L, "aaaaaaaa")))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true));

      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id = 1", String.class);
      assertEquals("withdrawn", statusKbn);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：他アカウントのお問い合わせは取り下げられない（400）")
    void withdrawInquiry_otherAccount() throws Exception {
      // inquiryNo=2はアカウント1のみが持つ番号（アカウント2にはinquiryNo=1しか存在しない）
      mockMvc
          .perform(
              post("/api/v1/inquiries/2/withdrawal")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(2L, "bbbbbbbb")))
                  .with(csrf()))
          .andExpect(status().isBadRequest());

      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id = 2", String.class);
      assertEquals("replied", statusKbn);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：存在しないお問い合わせ番号の場合は400を返す")
    void withdrawInquiry_notFound() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/inquiries/999/withdrawal")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAuthentication(1L, "aaaaaaaa")))
                  .with(csrf()))
          .andExpect(status().isBadRequest());
    }
  }
}
