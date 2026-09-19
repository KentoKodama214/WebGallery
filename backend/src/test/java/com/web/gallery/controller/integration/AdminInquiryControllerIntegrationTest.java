package com.web.gallery.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
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
public class AdminInquiryControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  private Authentication createAdminAuthentication() {
    AccountModel sessionAccount =
        AccountModel.builder()
            .accountNo(new AccountNo(1L))
            .accountId(new AccountId("aaaaaaaa"))
            .accountName(new AccountName("AAAAAAAA"))
            .password(new Password("$2a$10$password1"))
            .authorityKbn(AuthorityEnum.ADMINISTRATOR)
            .build();
    AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
    return new UsernamePasswordAuthenticationToken(
        accountPrincipal, null, accountPrincipal.getAuthorities());
  }

  private Authentication createNonAdminAuthentication() {
    AccountModel sessionAccount =
        AccountModel.builder()
            .accountNo(new AccountNo(2L))
            .accountId(new AccountId("bbbbbbbb"))
            .accountName(new AccountName("BBBBBBBB"))
            .password(new Password("$2a$10$password2"))
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
  @Sql("/sql/controller/AdminInquiryControllerIntegrationTest.sql")
  class getAdminInquiryList {
    @Test
    @Order(1)
    @DisplayName("正常系：管理者が全アカウントのお問い合わせ一覧を取得できる")
    void getAdminInquiryList_success() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/inquiries")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.inquiryList.length()").value(3))
          .andExpect(jsonPath("$.inquiryList[0].accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.inquiryList[1].accountId").value("bbbbbbbb"))
          .andExpect(jsonPath("$.inquiryList[2].accountId").value("aaaaaaaa"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：管理者以外は403を返す")
    void getAdminInquiryList_forbidden() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/inquiries")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createNonAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_ADMIN.getErrorCode()));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ステータス区分（未対応）で絞り込める")
    void getAdminInquiryList_filterByUnrepliedStatus() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/inquiries")
                  .param("statusKbn", "unreplied")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inquiryList.length()").value(2));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：ステータス区分（回答済み）で絞り込むと該当なしになる")
    void getAdminInquiryList_filterByRepliedStatus() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/inquiries")
                  .param("statusKbn", "replied")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inquiryList.length()").value(0));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：ステータス区分（取り下げ）で絞り込める")
    void getAdminInquiryList_filterByWithdrawnStatus() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/inquiries")
                  .param("statusKbn", "withdrawn")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inquiryList.length()").value(1))
          .andExpect(jsonPath("$.inquiryList[0].inquiryId").value(3));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AdminInquiryControllerIntegrationTest.sql")
  class getAdminInquiryDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：管理者がお問い合わせ詳細を取得できる")
    void getAdminInquiryDetail_success() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/inquiries/1")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inquiryId").value(1))
          .andExpect(jsonPath("$.accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.subject").value("写真が表示されない"))
          .andExpect(jsonPath("$.replyList.length()").value(0));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AdminInquiryControllerIntegrationTest.sql")
  class replyToInquiry {
    @Test
    @Order(1)
    @DisplayName("正常系：返信登録するとステータスが回答済みへ遷移し、ユーザー既読フラグが未読になる")
    void replyToInquiry_success() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/1/replies")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"body\": \"ご報告ありがとうございます。調査いたします。\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.replyNo").value(1));

      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id = 1", String.class);
      assertEquals("replied", statusKbn);
      Boolean isReadByUser =
          jdbcTemplate.queryForObject(
              "SELECT is_read_by_user FROM common.inquiry_mst WHERE id = 1", Boolean.class);
      assertFalse(isReadByUser);

      Integer replyCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst WHERE inquiry_id = 1", Integer.class);
      assertEquals(1, replyCount);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：管理者以外は403を返す")
    void replyToInquiry_forbidden() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/1/replies")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createNonAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"body\": \"返信本文\"}"))
          .andExpect(status().isForbidden());

      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id = 1", String.class);
      assertEquals("unreplied", statusKbn);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：本文が空の場合は400を返す")
    void replyToInquiry_badRequest() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/1/replies")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"body\": \"\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：存在しないお問い合わせIDの場合は400を返す")
    void replyToInquiry_notFound() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/999/replies")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"body\": \"返信本文\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：取り下げ済みのお問い合わせの場合は400を返す")
    void replyToInquiry_withdrawn() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/3/replies")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"body\": \"返信本文\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(MessageConst.ERR_CANNOT_REPLY_TO_WITHDRAWN_INQUIRY));

      Integer replyCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst WHERE inquiry_id = 3", Integer.class);
      assertEquals(0, replyCount);
    }
  }

  /** 未認証（認証情報なし）で管理者用お問い合わせ管理エンドポイントにアクセスした場合の共通挙動を検証する */
  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AdminInquiryControllerIntegrationTest.sql")
  class unauthenticatedAccess {
    @Test
    @Order(1)
    @DisplayName("異常系：未認証でお問い合わせ一覧取得にアクセスすると403ではなく401で共通JSONエラーを返す")
    void getAdminInquiryList_unauthenticated() throws Exception {
      mockMvc
          .perform(get("/api/v1/admin/inquiries"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(401))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未認証でお問い合わせ詳細取得にアクセスすると403ではなく401で共通JSONエラーを返す")
    void getAdminInquiryDetail_unauthenticated() throws Exception {
      mockMvc
          .perform(get("/api/v1/admin/inquiries/1"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(401))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未認証でお問い合わせ返信登録にアクセスすると403ではなく401で共通JSONエラーを返し、DBに副作用が発生しない")
    void replyToInquiry_unauthenticated() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/admin/inquiries/1/replies")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"body\": \"返信本文\"}"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(401))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));

      Integer replyCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst WHERE inquiry_id = 1", Integer.class);
      assertEquals(0, replyCount);
      String statusKbn =
          jdbcTemplate.queryForObject(
              "SELECT status_kbn FROM common.inquiry_mst WHERE id = 1", String.class);
      assertEquals("unreplied", statusKbn);
    }
  }
}
