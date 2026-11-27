package com.web.gallery.presentation.controller.account.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.AccountPrincipal;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.application.repository.auth.WebAuthnChallengeRepository;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.SexEnum;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.infrastructure.persistence.entity.account.Account;
import com.web.gallery.testsupport.PasskeyTestAuthenticator;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
// S3ストレージアクセスはモックする（統合テストでは実ストレージへ接続しない）。
// Controller層の統合テスト全クラスで同一の宣言に揃えることで、Springのテストコンテキストが
// 共有され（フィールド宣言とクラスレベル宣言が混在するとコンテキストキーが変わり再生成される）、
// コンテキスト生成回数が減ってCIの実行時間が短くなる
@MockitoBean(types = FileRepository.class)
public class AccountControllerIntegrationTest {
  private static final JsonMapper JSON = JsonMapper.builder().build();

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private com.web.gallery.application.helper.JwtTokenProvider jwtTokenProvider;

  @Autowired private AccountService accountService;

  @Autowired private WebAuthnChallengeRepository webAuthnChallengeRepository;

  /**
   * JSONファイルのリクエストボディに、新規登録オプションのチャレンジへ擬似認証器で作成したパスキーの登録レスポンスを加える
   *
   * @param fileName リクエストボディのJSONファイル名
   * @return リクエストボディ
   */
  private String withSignupCredential(String fileName) throws Exception {
    Map<String, Object> body =
        JSON.readValue(
            readJsonFile(fileName), new TypeReference<LinkedHashMap<String, Object>>() {});
    String accountId = (String) body.get("accountId");
    String optionsBody =
        mockMvc
            .perform(
                post("/api/v1/accounts/signup-options")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        JSON.writeValueAsString(
                            Map.of(
                                "accountId", accountId, "accountName", body.get("accountName")))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode options = JSON.readTree(optionsBody);
    body.put(
        "credential",
        new PasskeyTestAuthenticator()
            .createRegistrationResponseJson(
                options.get("rp").get("id").asString(),
                options.get("challenge").asString(),
                options.get("user").get("id").asString(),
                accountId));
    body.put("passkeyName", "MacBookのTouch ID");
    return JSON.writeValueAsString(body);
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource(
                "json/controller/integration/AccountControllerIntegrationTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  /**
   * フィクスチャで投入した既存アカウントに、擬似認証器のパスキーを登録する
   *
   * <p>パスキーの追加登録には本人確認が必要なため、登録用チャレンジを直接発行して登録処理のみを通す
   *
   * @param accountNo アカウント番号
   * @return パスキーを保持する擬似認証器
   */
  private PasskeyTestAuthenticator registerTestPasskey(long accountNo) throws Exception {
    PasskeyTestAuthenticator authenticator = new PasskeyTestAuthenticator();
    String challenge = "cmVnaXN0cmF0aW9uLWNoYWxsZW5nZS0x";
    webAuthnChallengeRepository.save(
        WebAuthnChallengeModel.of(
            new PasskeyChallenge(challenge),
            WebAuthnChallengePurposeEnum.REGISTRATION,
            new AccountNo(accountNo),
            new ExpiresAt(OffsetDateTime.now().plusMinutes(5))));
    accountService.registerPasskey(
        new AccountNo(accountNo),
        new PasskeyResponseJson(
            authenticator.createRegistrationResponseJson(
                PasskeyTestAuthenticator.RP_ID, challenge, "dXNlci1oYW5kbGU", "aaaaaaaa")),
        new PasskeyName("テスト用パスキー"));
    return authenticator;
  }

  /**
   * 本人確認オプションAPIでチャレンジを発行し、擬似認証器のパスキーで署名した認証レスポンスを返す
   *
   * @param authenticator パスキーを保持する擬似認証器
   * @param authentication ログイン中のアカウントの認証情報
   * @return 本人確認用の認証レスポンス
   */
  private String reauthenticate(
      PasskeyTestAuthenticator authenticator, Authentication authentication) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/accounts/aaaaaaaa/reauthentication-options")
                    .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode options = JSON.readTree(body);
    return authenticator.createAuthenticationResponseJson(
        options.get("rpId").asString(), options.get("challenge").asString());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AccountControllerIntegrationTest.sql")
  class getAccountList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント一覧を取得できる")
    void getAccountList_success() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.accountList.length()").value(3))
          .andExpect(jsonPath("$.accountList[0].accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.accountList[0].accountName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.accountList[1].accountId").value("bbbbbbbb"))
          .andExpect(jsonPath("$.accountList[1].accountName").value("BBBBBBBB"))
          .andExpect(jsonPath("$.accountList[2].accountId").value("cccccccc"))
          .andExpect(jsonPath("$.accountList[2].accountName").value("CCCCCCCC"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが0件の場合")
    @Sql("/sql/common/cleanup.sql")
    void getAccountList_empty() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.accountList.length()").value(0));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：pageNoが不正な場合は400を返す")
    void getAccountList_badRequest() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts").param("pageNo", "0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_INPUT));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AccountControllerIntegrationTest.sql")
  class getAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：自分のアカウント詳細を取得できる")
    void getAccount_success() throws Exception {
      String accountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName("AAAAAAAA"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/" + accountId)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.accountName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.birthdate").value("1991-02-14"))
          .andExpect(jsonPath("$.sexKbn").value("none"))
          .andExpect(jsonPath("$.birthplacePrefectureKbnCode").value("none"))
          .andExpect(jsonPath("$.residentPrefectureKbnCode").value("none"))
          .andExpect(jsonPath("$.freeMemo").value(""));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：生年月日が最小日付の場合はnullで返却される")
    void getAccount_birthdate_min_date() throws Exception {
      String accountId = "bbbbbbbb";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName("BBBBBBBB"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/" + accountId)
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.accountId").value("bbbbbbbb"))
          .andExpect(jsonPath("$.accountName").value("BBBBBBBB"))
          .andExpect(jsonPath("$.birthdate").isEmpty())
          .andExpect(jsonPath("$.sexKbn").value("man"))
          .andExpect(jsonPath("$.birthplacePrefectureKbnCode").value("none"))
          .andExpect(jsonPath("$.residentPrefectureKbnCode").value("none"))
          .andExpect(jsonPath("$.freeMemo").value(""));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他人のアカウントIDを指定した場合はForbidden")
    void getAccount_forbidden() throws Exception {
      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              get("/api/v1/accounts/bbbbbbbb")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isForbidden())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.getErrorMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：未認証の場合は403ではなく401で共通JSONエラーを返す")
    void getAccount_unauthenticated() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"))
          .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：署名は正当だがsubjectのアカウントが存在しないトークンは500ではなく401を返す")
    void getAccount_token_subject_not_found() throws Exception {
      // アカウントID変更後の旧トークンや、本人によるアカウント削除後のトークンを想定
      AccountModel ghost =
          AccountModel.builder()
              .accountNo(new AccountNo(9999L))
              .accountId(new AccountId("ghostuser01"))
              .authorityKbn(AuthorityEnum.NORMAL)
              .build();
      String token = jwtTokenProvider.generateAccessToken(new AccountPrincipal(ghost, 3));

      mockMvc
          .perform(get("/api/v1/accounts/ghostuser01").header("Authorization", "Bearer " + token))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AccountControllerIntegrationTest.sql")
  @Sql("/sql/common/PrefectureKbnMst.sql")
  class register {
    private List<Account> getAccountList(String accountId) {
      return jdbcTemplate.query(
          "SELECT * FROM common.account where account_id='" + accountId + "'",
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
                  .birthdate(rs.getObject("birthdate", LocalDate.class))
                  .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                  .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                  .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                  .freeMemo(rs.getString("free_memo"))
                  .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                  .loginFailureCount(rs.getInt("login_failure_count"))
                  .isAdminLocked(rs.getBoolean("is_admin_locked"))
                  .build());
    }

    @Test
    @Order(1)
    @DisplayName("正常系")
    void register_regist_success() throws Exception {
      String accountId = "dddddddd";
      String accountName = "DDDDDDDD";
      LocalDate birthDate = LocalDate.of(2000, 1, 1);
      String birthplacePrefectureKbnCode = "Hokkaido";
      String residentPrefectureKbnCode = "Okinawa";
      String freeMemo = "フリーメモ";

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(withSignupCredential("register_regist_success.json"))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value(""))
          .andExpect(jsonPath("$.recoveryCodes.length()").value(10));

      List<Account> actualData = getAccountList(accountId);

      assertEquals(1, actualData.size());
      assertEquals(4L, actualData.getFirst().getAccountNo());
      assertEquals(0L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(0L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals(accountId, actualData.getFirst().getAccountId());
      assertEquals(accountName, actualData.getFirst().getAccountName());
      assertEquals(birthDate, actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
      assertEquals(
          birthplacePrefectureKbnCode, actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals(residentPrefectureKbnCode, actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals(freeMemo, actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime().plusHours(9));
      assertEquals(0, actualData.getFirst().getLoginFailureCount());
      assertFalse(actualData.getFirst().getIsAdminLocked());

      // account_authorityにも同一のアカウント番号でMINI固定で登録されること
      AuthorityEnum actualAuthorityKbn =
          jdbcTemplate.queryForObject(
              "SELECT authority_kbn FROM common.account_authority WHERE account_no=4",
              (rs, rowNum) -> AuthorityEnum.getOrDefault(rs.getString("authority_kbn")));
      assertEquals(AuthorityEnum.MINI, actualAuthorityKbn);

      // 最初のパスキーとリカバリーコードも登録されること
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.passkey_credential WHERE account_no=4", Integer.class));
      assertEquals(
          10,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.recovery_code WHERE account_no=4", Integer.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：既に使われているアカウントIDの場合、新規登録オプションを発行せず409を返す")
    void register_exist_accountId() throws Exception {
      String accountId = "aaaaaaaa";

      mockMvc
          .perform(
              post("/api/v1/accounts/signup-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of("accountId", accountId, "accountName", "AAAAAAAA")))
                  .with(csrf()))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_ACCOUNT_ID.getErrorCode()));

      List<Account> actualData = getAccountList(accountId);
      assertEquals(1, actualData.size());
      assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.webauthn_challenge", Integer.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_accountId_is_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("register_badrequest_blank_accountid.json"))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：区分マスタに存在しない都道府県コードの場合、400を返す")
    void register_badrequest_invalid_prefecture() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(withSignupCredential("register_badrequest_invalid_prefecture.json"))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));

      assertEquals(0, getAccountList("dddddddd").size());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AccountControllerIntegrationTest.sql")
  class update {
    private List<Account> getAccountList(String accountId) {
      return jdbcTemplate.query(
          "SELECT * FROM common.account where account_id='" + accountId + "'",
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
                  .birthdate(rs.getObject("birthdate", LocalDate.class))
                  .sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
                  .birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
                  .residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
                  .freeMemo(rs.getString("free_memo"))
                  .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                  .loginFailureCount(rs.getInt("login_failure_count"))
                  .isAdminLocked(rs.getBoolean("is_admin_locked"))
                  .build());
    }

    @Test
    @Order(1)
    @DisplayName("正常系：アカウントID変更なし")
    void update_not_change_accountID() throws Exception {
      String accountId = "aaaaaaaa";
      String accountName = "AAAAAAAA";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName(accountName))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_not_change_accountid_and_password.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(false))
          .andExpect(jsonPath("$.message").value(""));

      List<Account> actual = getAccountList(accountId);
      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getAccountNo());
      assertEquals(1L, actual.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actual.getFirst().getCreatedAt());
      assertEquals(1L, actual.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actual.getFirst().getUpdatedAt());
      assertFalse(actual.getFirst().getIsDeleted());
      assertEquals(accountId, actual.getFirst().getAccountId());
      assertEquals(accountName, actual.getFirst().getAccountName());
      // プロフィール更新のリクエストに含まれない項目は「変更なし」としてフィクスチャ値を維持する
      assertEquals(LocalDate.of(1991, 2, 14), actual.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
      assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actual.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actual.getFirst().getLastLoginDatetime());
      assertEquals(0, actual.getFirst().getLoginFailureCount());
      assertFalse(actual.getFirst().getIsAdminLocked());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントID変更あり")
    void update_change_accountID() throws Exception {
      String accountId = "aaaaaaaab";
      String accountName = "AAAAAAAA";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName(accountName))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_change_accountid.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(true))
          .andExpect(jsonPath("$.message").value(""));

      List<Account> actual = getAccountList(accountId);
      assertEquals(1, actual.size());
      assertEquals(1L, actual.getFirst().getAccountNo());
      assertEquals(1L, actual.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actual.getFirst().getCreatedAt());
      assertEquals(1L, actual.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actual.getFirst().getUpdatedAt());
      assertFalse(actual.getFirst().getIsDeleted());
      assertEquals(accountId, actual.getFirst().getAccountId());
      assertEquals(accountName, actual.getFirst().getAccountName());
      // プロフィール更新のリクエストに含まれない項目は「変更なし」としてフィクスチャ値を維持する
      assertEquals(LocalDate.of(1991, 2, 14), actual.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
      assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actual.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actual.getFirst().getLastLoginDatetime());
      assertEquals(0, actual.getFirst().getLoginFailureCount());
      assertFalse(actual.getFirst().getIsAdminLocked());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：アカウントID重複")
    void update_duplicate_accountId() throws Exception {
      String accountName = "AAAAAAAA";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName(accountName))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_duplicate_accountid.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(true))
          .andExpect(jsonPath("$.isAccountIdChanged").value(true))
          .andExpect(jsonPath("$.message").value(""));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：アカウントIDが不正な場合、400を返す")
    void update_BadRequestException_account_id() throws Exception {
      String accountName = "AAAAAAAA";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName(accountName))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_badrequest_account_id.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void update_UpdateFailureException() throws Exception {
      String accountId = "zzzzzzzz";
      String accountName = "AAAAAAAA";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(9L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName(accountName))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_update_failure.json"))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isConflict())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.getErrorCode()))
          .andExpect(
              jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.getErrorMessage()));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/AccountControllerDeleteAccountIntegrationTest.sql")
  class deleteAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント削除に成功する")
    void deleteAccount_success() throws Exception {
      String accountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName("AAAAAAAA"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              post("/api/v1/accounts/" + accountId + "/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of(
                              "credential",
                              reauthenticate(registerTestPasskey(1L), authentication))))
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isOk());

      // パスキーも削除されたことを確認
      assertEquals(
          0,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.passkey_credential where account_no=1", Integer.class));

      // アカウントが削除されたことを確認
      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(0, accountCount);

      // 写真マスタが削除されたことを確認
      Integer photoMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
      assertEquals(0, photoMstCount);

      // 写真タグが削除されたことを確認
      Integer photoTagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_tag_mst where account_no=1", Integer.class);
      assertEquals(0, photoTagCount);

      // お気に入りが削除されたことを確認（自分が登録したもの＋自分の写真に対する他人のもの）
      Integer favoriteCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite where account_no=1 or favorite_photo_account_no=1",
              Integer.class);
      assertEquals(0, favoriteCount);

      // account_no=2は残っていること
      Integer otherAccountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=2", Integer.class);
      assertEquals(1, otherAccountCount);

      // アカウント権限も削除されたことを確認
      Integer accountAuthorityCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account_authority where account_no=1", Integer.class);
      assertEquals(0, accountAuthorityCount);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDの場合は403を返すこと")
    void deleteAccount_forbidden() throws Exception {
      String accountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(2L))
              .accountId(new AccountId("bbbbbbbb"))
              .accountName(new AccountName("BBBBBBBB"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              post("/api/v1/accounts/" + accountId + "/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"credential\": \"{}\"}")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isForbidden());

      // アカウントは削除されていないことを確認
      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(1, accountCount);
    }

    @Test
    @Order(3)
    @DisplayName("異常系：本人確認の認証レスポンスが未入力の場合は400を返すこと")
    void deleteAccount_badRequest() throws Exception {
      String accountId = "aaaaaaaa";

      AccountModel sessionAccount =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName("AAAAAAAA"))
              .authorityKbn(AuthorityEnum.ADMINISTRATOR)
              .build();

      AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              accountPrincipal, null, accountPrincipal.getAuthorities());

      mockMvc
          .perform(
              post("/api/v1/accounts/" + accountId + "/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"credential\": \"\"}")
                  .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                  .with(csrf()))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_INPUT));

      // アカウントは削除されていないことを確認
      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(1, accountCount);
    }
  }
}
