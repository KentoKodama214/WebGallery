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
import com.web.gallery.entity.account.Account;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.account.AccountModel;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
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
public class AdminAccountControllerIntegrationTest {
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
  @Sql("/sql/controller/AdminAccountControllerIntegrationTest.sql")
  class getAdminAccountList {
    @Test
    @Order(1)
    @DisplayName("正常系：管理者が全アカウント一覧を取得できる（削除済み含む）")
    void getAdminAccountList_success() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/accounts")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.accountList.length()").value(3))
          .andExpect(jsonPath("$.accountList[0].accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.accountList[0].accountName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.accountList[0].authorityKbn").value("administrator"))
          .andExpect(jsonPath("$.accountList[0].isDeleted").value(false))
          .andExpect(jsonPath("$.accountList[0].loginFailureCount").value(0))
          .andExpect(jsonPath("$.accountList[1].accountId").value("bbbbbbbb"))
          .andExpect(jsonPath("$.accountList[1].isDeleted").value(false))
          .andExpect(jsonPath("$.accountList[1].loginFailureCount").value(10))
          .andExpect(jsonPath("$.accountList[2].accountId").value("cccccccc"))
          .andExpect(jsonPath("$.accountList[2].isDeleted").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：管理者以外は403を返す")
    void getAdminAccountList_forbidden() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/accounts")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createNonAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isForbidden())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_ADMIN.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_ADMIN.getErrorMessage()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：pageNoが不正な場合は400を返す")
    void getAdminAccountList_badRequest() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/accounts")
                  .param("pageNo", "0")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_INPUT));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AdminAccountControllerIntegrationTest.sql")
  class unlockAccountTest {
    @Test
    @Order(1)
    @DisplayName("正常系：ロックされたアカウントを解除できる")
    void unlockAccount_success() throws Exception {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/2/unlock")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value(MessageConst.UNLOCK_ACCOUNT));

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account WHERE account_no = 2",
              (rs, rowNum) ->
                  Account.builder()
                      .accountNo(rs.getLong("account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .accountId(rs.getString("account_id"))
                      .accountName(rs.getString("account_name"))
                      .password(rs.getString("password"))
                      .birthdate(rs.getObject("birthdate", LocalDate.class))
                      .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                      .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                      .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                      .freeMemo(rs.getString("free_memo"))
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(2L, actualData.getFirst().getAccountNo());
      assertEquals(2L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      // updated_byは本操作では更新対象に含まれないため、フィクスチャの値がそのまま残る
      assertEquals(2L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("bbbbbbbb", actualData.getFirst().getAccountId());
      assertEquals("BBBBBBBB", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password2", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.MAN, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(0, actualData.getFirst().getLoginFailureCount());
      assertFalse(actualData.getFirst().getIsAdminLocked());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：管理者以外は403を返す")
    void unlockAccount_forbidden() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/2/unlock")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createNonAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isForbidden());

      Integer loginFailureCount =
          jdbcTemplate.queryForObject(
              "SELECT login_failure_count FROM common.account WHERE account_no = 2", Integer.class);
      assertEquals(10, loginFailureCount);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：存在しないアカウント番号の場合は409を返す")
    void unlockAccount_notFound() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/999/unlock")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AdminAccountControllerIntegrationTest.sql")
  class lockAccountTest {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントを強制ロックできる")
    void lockAccount_success() throws Exception {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/1/lock")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value(MessageConst.LOCK_ACCOUNT));

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account WHERE account_no = 1",
              (rs, rowNum) ->
                  Account.builder()
                      .accountNo(rs.getLong("account_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .accountId(rs.getString("account_id"))
                      .accountName(rs.getString("account_name"))
                      .password(rs.getString("password"))
                      .birthdate(rs.getObject("birthdate", LocalDate.class))
                      .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                      .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                      .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                      .freeMemo(rs.getString("free_memo"))
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      // updated_byは本操作では更新対象に含まれないため、フィクスチャの値がそのまま残る
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("aaaaaaaa", actualData.getFirst().getAccountId());
      assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password1", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertTrue(actualData.getFirst().getLoginFailureCount() > 0);
      assertTrue(actualData.getFirst().getIsAdminLocked());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：管理者以外は403を返す")
    void lockAccount_forbidden() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/1/lock")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createNonAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isForbidden());

      Integer loginFailureCount =
          jdbcTemplate.queryForObject(
              "SELECT login_failure_count FROM common.account WHERE account_no = 1", Integer.class);
      assertEquals(0, loginFailureCount);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：存在しないアカウント番号の場合は409を返す")
    void lockAccount_notFound() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/999/lock")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：accountNoが数値でない場合、共通のJSONエラー形式で400を返す")
    void lockAccount_badRequest_notNumeric() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/not-a-number/lock")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AdminAccountControllerIntegrationTest.sql")
  class updateAccountAuthorityTest {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントの権限を変更できる")
    void updateAccountAuthority_success() throws Exception {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/2/authority")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"authorityKbn\": \"normal-user\"}"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value(MessageConst.UPDATE_ACCOUNT_AUTHORITY));

      Long createdBy =
          jdbcTemplate.queryForObject(
              "SELECT created_by FROM common.account_authority WHERE account_no = 2", Long.class);
      assertEquals(2L, createdBy);
      OffsetDateTime createdAt =
          jdbcTemplate.queryForObject(
              "SELECT created_at FROM common.account_authority WHERE account_no = 2",
              OffsetDateTime.class);
      assertEquals(OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)), createdAt);
      // updated_byは権限変更時に0固定で更新される仕様
      Long updatedBy =
          jdbcTemplate.queryForObject(
              "SELECT updated_by FROM common.account_authority WHERE account_no = 2", Long.class);
      assertEquals(0L, updatedBy);
      OffsetDateTime updatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM common.account_authority WHERE account_no = 2",
              OffsetDateTime.class);
      assertEquals(transactionNow, updatedAt);
      String authorityKbn =
          jdbcTemplate.queryForObject(
              "SELECT authority_kbn FROM common.account_authority WHERE account_no = 2",
              String.class);
      assertEquals("normal-user", authorityKbn);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：管理者以外は403を返す")
    void updateAccountAuthority_forbidden() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/2/authority")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createNonAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"authorityKbn\": \"normal-user\"}"))
          .andExpect(status().isForbidden());

      String authorityKbn =
          jdbcTemplate.queryForObject(
              "SELECT authority_kbn FROM common.account_authority WHERE account_no = 2",
              String.class);
      assertEquals("mini-user", authorityKbn);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：authorityKbnが未指定の場合は400を返す")
    void updateAccountAuthority_badRequest_missing_authorityKbn() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/2/authority")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：存在しないアカウント番号の場合は409を返す")
    void updateAccountAuthority_notFound() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/999/authority")
                  .with(
                      SecurityMockMvcRequestPostProcessors.authentication(
                          createAdminAuthentication()))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"authorityKbn\": \"normal-user\"}"))
          .andExpect(status().isConflict());
    }
  }

  /** 未認証（認証情報なし）で管理者用アカウント管理エンドポイントにアクセスした場合の共通挙動を検証する */
  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AdminAccountControllerIntegrationTest.sql")
  class unauthenticatedAccess {
    @Test
    @Order(1)
    @DisplayName("異常系：未認証でアカウント一覧取得にアクセスすると403ではなく401で共通JSONエラーを返す")
    void getAdminAccountList_unauthenticated() throws Exception {
      mockMvc
          .perform(get("/api/v1/admin/accounts"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未認証でアカウントロック解除にアクセスすると403ではなく401で共通JSONエラーを返し、DBに副作用が発生しない")
    void unlockAccount_unauthenticated() throws Exception {
      mockMvc
          .perform(put("/api/v1/admin/accounts/2/unlock"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));

      Integer loginFailureCount =
          jdbcTemplate.queryForObject(
              "SELECT login_failure_count FROM common.account WHERE account_no = 2", Integer.class);
      assertEquals(10, loginFailureCount);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未認証でアカウント強制ロックにアクセスすると403ではなく401で共通JSONエラーを返し、DBに副作用が発生しない")
    void lockAccount_unauthenticated() throws Exception {
      mockMvc
          .perform(put("/api/v1/admin/accounts/1/lock"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));

      Integer loginFailureCount =
          jdbcTemplate.queryForObject(
              "SELECT login_failure_count FROM common.account WHERE account_no = 1", Integer.class);
      assertEquals(0, loginFailureCount);
      Boolean isAdminLocked =
          jdbcTemplate.queryForObject(
              "SELECT is_admin_locked FROM common.account WHERE account_no = 1", Boolean.class);
      assertFalse(isAdminLocked);
    }

    @Test
    @Order(4)
    @DisplayName("異常系：未認証でアカウント権限変更にアクセスすると403ではなく401で共通JSONエラーを返し、DBに副作用が発生しない")
    void updateAccountAuthority_unauthenticated() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/admin/accounts/2/authority")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"authorityKbn\": \"normal-user\"}"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));

      String authorityKbn =
          jdbcTemplate.queryForObject(
              "SELECT authority_kbn FROM common.account_authority WHERE account_no = 2",
              String.class);
      assertEquals("mini-user", authorityKbn);
    }
  }
}
