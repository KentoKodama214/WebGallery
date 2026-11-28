package com.web.gallery.presentation.controller.account.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.testsupport.PasskeyTestAuthenticator;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * パスキーによる新規登録・ログイン・本人確認・管理・リカバリーコードによる復旧を、実DBとwebauthn4j-testの擬似認証器で 通しで検証する統合テスト
 *
 * <p>登録・認証レスポンスはサーバが発行したチャレンジに対して擬似認証器がその場で署名して作るため、 リクエストボディはJSONファイルではなく実行時に組み立てる。
 * アカウントもフィクスチャではなく、本番と同じ新規登録APIで作成する（パスワードが無いため、パスキーの無いアカウントではログインできない）
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@MockitoBean(types = FileRepository.class)
public class PasskeyControllerIntegrationTest {
  private static final JsonMapper JSON = JsonMapper.builder().build();

  private static final String ACCOUNT_A = "aaaaaaaa";

  private static final String ACCOUNT_B = "bbbbbbbb";

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  /** アカウントAの最初のパスキーを保持する擬似認証器 */
  private PasskeyTestAuthenticator authenticator;

  @BeforeEach
  void setUpAuthenticator() {
    authenticator = new PasskeyTestAuthenticator();
  }

  /**
   * 新規登録APIでアカウントを作成する（新規登録オプションの発行 → 擬似認証器でパスキーを作成 → 登録）
   *
   * @param accountId アカウントID
   * @param passkeyAuthenticator 最初のパスキーを作成する擬似認証器
   * @return 登録APIのレスポンス（リカバリーコードを含む）
   */
  private JsonNode signUp(String accountId, PasskeyTestAuthenticator passkeyAuthenticator)
      throws Exception {
    JsonNode options = signupOptions(accountId);
    String body =
        register(accountId, passkeyAuthenticator, options)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body);
  }

  private JsonNode signupOptions(String accountId) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/accounts/signup-options")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        JSON.writeValueAsString(
                            Map.of("accountId", accountId, "accountName", "テストユーザー"))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body);
  }

  private ResultActions register(
      String accountId, PasskeyTestAuthenticator passkeyAuthenticator, JsonNode options)
      throws Exception {
    String credential = createCredential(passkeyAuthenticator, options);
    return mockMvc.perform(
        post("/api/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                JSON.writeValueAsString(
                    Map.of(
                        "accountId",
                        accountId,
                        "accountName",
                        "テストユーザー",
                        "credential",
                        credential,
                        "passkeyName",
                        "最初のパスキー"))));
  }

  /**
   * 登録オプションに従って擬似認証器でパスキーを作成し、登録レスポンスを返す
   *
   * @param passkeyAuthenticator 擬似認証器
   * @param options 登録オプション（PublicKeyCredentialCreationOptionsJSON）
   * @return 登録レスポンス（PublicKeyCredential#toJSON()のJSON文字列）
   */
  private String createCredential(PasskeyTestAuthenticator passkeyAuthenticator, JsonNode options) {
    return passkeyAuthenticator.createRegistrationResponseJson(
        options.get("rp").get("id").asString(),
        options.get("challenge").asString(),
        options.get("user").get("id").asString(),
        options.get("user").get("name").asString());
  }

  /**
   * パスキーでログインし、アクセストークンを取得する
   *
   * @param passkeyAuthenticator パスキーを保持する擬似認証器
   * @return アクセストークン
   */
  private String login(PasskeyTestAuthenticator passkeyAuthenticator) throws Exception {
    String body =
        passkeyLogin(signAuthentication(passkeyAuthenticator))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body).get("accessToken").asString();
  }

  /**
   * 認証オプションを発行し、擬似認証器で署名した認証レスポンスを返す
   *
   * @param passkeyAuthenticator パスキーを保持する擬似認証器
   * @return 認証レスポンス（PublicKeyCredential#toJSON()のJSON文字列）
   */
  private String signAuthentication(PasskeyTestAuthenticator passkeyAuthenticator)
      throws Exception {
    String body =
        mockMvc
            .perform(post("/api/v1/auth/passkey/options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rpId").value(PasskeyTestAuthenticator.RP_ID))
            .andExpect(jsonPath("$.userVerification").value("required"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode options = JSON.readTree(body);
    return passkeyAuthenticator.createAuthenticationResponseJson(
        options.get("rpId").asString(), options.get("challenge").asString());
  }

  private ResultActions passkeyLogin(String credential) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/passkey/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON.writeValueAsString(Map.of("credential", credential))));
  }

  /**
   * 本人確認オプションを発行し、擬似認証器で署名した本人確認用の認証レスポンスを返す
   *
   * @param accountId アカウントID
   * @param accessToken アクセストークン
   * @param passkeyAuthenticator パスキーを保持する擬似認証器
   * @return 本人確認用の認証レスポンス
   */
  private String reauthenticate(
      String accountId, String accessToken, PasskeyTestAuthenticator passkeyAuthenticator)
      throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/accounts/" + accountId + "/reauthentication-options")
                    .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode options = JSON.readTree(body);
    return passkeyAuthenticator.createAuthenticationResponseJson(
        options.get("rpId").asString(), options.get("challenge").asString());
  }

  private JsonNode registrationOptions(
      String accountId, String accessToken, String reauthCredential) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/accounts/" + accountId + "/passkeys/registration-options")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.writeValueAsString(Map.of("credential", reauthCredential))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body);
  }

  private ResultActions registPasskey(
      String accountId,
      String accessToken,
      JsonNode options,
      PasskeyTestAuthenticator passkeyAuthenticator)
      throws Exception {
    return mockMvc.perform(
        post("/api/v1/accounts/" + accountId + "/passkeys")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                JSON.writeValueAsString(
                    Map.of(
                        "credential",
                        createCredential(passkeyAuthenticator, options),
                        "passkeyName",
                        "2つ目のパスキー"))));
  }

  /**
   * アカウントAに2つ目のパスキーを登録する
   *
   * @param accessToken アカウントAのアクセストークン
   * @return 2つ目のパスキーを保持する擬似認証器
   */
  private PasskeyTestAuthenticator registSecondPasskey(String accessToken) throws Exception {
    PasskeyTestAuthenticator second = new PasskeyTestAuthenticator();
    JsonNode options =
        registrationOptions(
            ACCOUNT_A, accessToken, reauthenticate(ACCOUNT_A, accessToken, authenticator));
    registPasskey(ACCOUNT_A, accessToken, options, second).andExpect(status().isOk());
    return second;
  }

  private Long accountNo(String accountId) {
    return jdbcTemplate.queryForObject(
        "SELECT account_no FROM common.account WHERE account_id = ?", Long.class, accountId);
  }

  private Integer count(String sql, Object... args) {
    return jdbcTemplate.queryForObject(sql, Integer.class, args);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class register {
    @Test
    @Order(1)
    @DisplayName("正常系：新規登録オプションで作成したパスキーとともにアカウントを登録でき、リカバリーコードが返り、そのパスキーでログインできる")
    void register_success() throws Exception {
      JsonNode options = signupOptions(ACCOUNT_A);
      assertEquals(ACCOUNT_A, options.get("user").get("name").asString());
      assertEquals("required", options.get("authenticatorSelection").get("residentKey").asString());
      // アカウントはまだ作成されていない
      assertEquals(0, count("SELECT COUNT(*) FROM common.account"));

      String body =
          register(ACCOUNT_A, authenticator, options)
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.isSuccess").value(true))
              .andExpect(jsonPath("$.recoveryCodes.length()").value(10))
              .andReturn()
              .getResponse()
              .getContentAsString();
      String firstCode = JSON.readTree(body).get("recoveryCodes").get(0).asString();
      assertTrue(firstCode.matches("[0-9A-Z]{4}-[0-9A-Z]{4}-[0-9A-Z]{4}-[0-9A-Z]{4}"));

      Long accountNo = accountNo(ACCOUNT_A);
      assertEquals(
          1,
          count("SELECT COUNT(*) FROM common.passkey_credential WHERE account_no = ?", accountNo));
      assertEquals(
          10, count("SELECT COUNT(*) FROM common.recovery_code WHERE account_no = ?", accountNo));
      // 登録オプションで払い出したユーザーハンドルがアカウントに紐づく
      assertEquals(
          options.get("user").get("id").asString(),
          jdbcTemplate.queryForObject(
              "SELECT user_handle FROM common.passkey_user_handle WHERE account_no = ?",
              String.class,
              accountNo));

      passkeyLogin(signAuthentication(authenticator)).andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じ新規登録オプション（チャレンジ）で2回登録しようとした場合、2回目は400になる")
    void register_challengeReused() throws Exception {
      JsonNode options = signupOptions(ACCOUNT_A);
      register(ACCOUNT_A, authenticator, options).andExpect(status().isOk());

      register(ACCOUNT_B, new PasskeyTestAuthenticator(), options)
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorMessage()));
      assertEquals(0, count("SELECT COUNT(*) FROM common.account WHERE account_id = ?", ACCOUNT_B));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：既に使われているアカウントIDの場合、新規登録オプションの発行・登録とも409になる")
    void register_duplicateAccountId() throws Exception {
      JsonNode raceOptions = signupOptions(ACCOUNT_A);
      signUp(ACCOUNT_A, authenticator);

      mockMvc
          .perform(
              post("/api/v1/accounts/signup-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of("accountId", ACCOUNT_A, "accountName", "テストユーザー"))))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_ACCOUNT_ID.getErrorCode()));
      // 重複判定の前に発行済みだったオプションで登録しようとしても、登録時に改めて弾かれる
      register(ACCOUNT_A, new PasskeyTestAuthenticator(), raceOptions)
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class registPasskey {
    private String accessToken;

    @BeforeEach
    void signUpAndLogin() throws Exception {
      signUp(ACCOUNT_A, authenticator);
      accessToken = login(authenticator);
    }

    @Test
    @Order(1)
    @DisplayName("正常系：パスキーで本人確認のうえ登録オプションを発行し、別の認証器のパスキーを追加でき、そのパスキーでもログインできる")
    void registPasskey_success() throws Exception {
      JsonNode options =
          registrationOptions(
              ACCOUNT_A, accessToken, reauthenticate(ACCOUNT_A, accessToken, authenticator));

      // 登録済みのパスキーが除外対象になり、ユーザーハンドルは新規登録時と同じものが使われる
      assertEquals(1, options.get("excludeCredentials").size());

      PasskeyTestAuthenticator second = new PasskeyTestAuthenticator();
      registPasskey(ACCOUNT_A, accessToken, options, second)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.passkeyName").value("2つ目のパスキー"));

      assertEquals(
          2,
          count(
              "SELECT COUNT(*) FROM common.passkey_credential WHERE account_no = ?",
              accountNo(ACCOUNT_A)));
      passkeyLogin(signAuthentication(second)).andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じ登録オプション（チャレンジ）で2回登録しようとした場合、2回目は400になる")
    void registPasskey_challengeReused() throws Exception {
      JsonNode options =
          registrationOptions(
              ACCOUNT_A, accessToken, reauthenticate(ACCOUNT_A, accessToken, authenticator));
      registPasskey(ACCOUNT_A, accessToken, options, new PasskeyTestAuthenticator())
          .andExpect(status().isOk());

      registPasskey(ACCOUNT_A, accessToken, options, new PasskeyTestAuthenticator())
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorMessage()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他のアカウントに発行されたチャレンジでは登録できない")
    void registPasskey_challengeOfOtherAccount() throws Exception {
      PasskeyTestAuthenticator otherAuthenticator = new PasskeyTestAuthenticator();
      signUp(ACCOUNT_B, otherAuthenticator);
      String otherToken = login(otherAuthenticator);
      JsonNode otherOptions =
          registrationOptions(
              ACCOUNT_B, otherToken, reauthenticate(ACCOUNT_B, otherToken, otherAuthenticator));

      registPasskey(ACCOUNT_A, accessToken, otherOptions, new PasskeyTestAuthenticator())
          .andExpect(status().isBadRequest());
      assertEquals(
          1,
          count(
              "SELECT COUNT(*) FROM common.passkey_credential WHERE account_no = ?",
              accountNo(ACCOUNT_A)));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：ログイン用のチャレンジに署名した認証レスポンスでは本人確認にならず、登録オプションを発行せず403になる")
    void registPasskey_loginChallengeRejected() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + ACCOUNT_A + "/passkeys/registration-options")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of("credential", signAuthentication(authenticator)))))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.REAUTHENTICATION_FAILED.getErrorCode()));
      assertEquals(
          0,
          count(
              "SELECT COUNT(*) FROM common.webauthn_challenge WHERE purpose_kbn = 'registration'"));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：他のアカウントのパスキーで署名した場合、本人確認にならず403になる")
    void registPasskey_otherAccountsPasskeyRejected() throws Exception {
      PasskeyTestAuthenticator otherAuthenticator = new PasskeyTestAuthenticator();
      signUp(ACCOUNT_B, otherAuthenticator);

      mockMvc
          .perform(
              post("/api/v1/accounts/" + ACCOUNT_A + "/passkeys/registration-options")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of(
                              "credential",
                              reauthenticate(ACCOUNT_A, accessToken, otherAuthenticator)))))
          .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：未ログインの場合、本人確認オプションの発行は401になる")
    void registPasskey_unauthenticated() throws Exception {
      mockMvc
          .perform(post("/api/v1/accounts/" + ACCOUNT_A + "/reauthentication-options"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class passkeyLogin {
    @BeforeEach
    void signUpAccountA() throws Exception {
      signUp(ACCOUNT_A, authenticator);
    }

    @Test
    @Order(1)
    @DisplayName("正常系：パスキーでログインでき、トークンが発行され、ログイン履歴にパスキーとして記録される")
    void passkeyLogin_success() throws Exception {
      passkeyLogin(signAuthentication(authenticator))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").isNotEmpty())
          .andExpect(header().string("Set-Cookie", containsString("refreshToken=")));

      assertEquals(
          1,
          count(
              "SELECT COUNT(*) FROM common.login_history"
                  + " WHERE account_no = ? AND login_method_kbn = 'passkey'",
              accountNo(ACCOUNT_A)));
      // 署名カウンタが更新されている
      assertEquals(
          1,
          count(
              "SELECT COUNT(*) FROM common.passkey_credential"
                  + " WHERE account_no = ? AND sign_count > 1",
              accountNo(ACCOUNT_A)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：パスキーログインで発行されたアクセストークンで、パスキー一覧を取得できる")
    void passkeyLogin_tokenUsable() throws Exception {
      String accessToken = login(authenticator);

      mockMvc
          .perform(
              get("/api/v1/accounts/" + ACCOUNT_A + "/passkeys")
                  .header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.passkeys.length()").value(1))
          .andExpect(jsonPath("$.passkeys[0].passkeyName").value("最初のパスキー"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：同じ認証レスポンスを再送した場合（リプレイ）、401になる")
    void passkeyLogin_replay() throws Exception {
      String credential = signAuthentication(authenticator);
      passkeyLogin(credential).andExpect(status().isOk());

      passkeyLogin(credential)
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_PASSKEY_AUTHENTICATION_FAILED));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：ログイン失敗回数の上限に達しロック中の場合、パスキーでもログインできず423になる")
    void passkeyLogin_locked() throws Exception {
      jdbcTemplate.update(
          "UPDATE common.account SET login_failure_count = 3, updated_at = NOW()"
              + " WHERE account_id = ?",
          ACCOUNT_A);

      passkeyLogin(signAuthentication(authenticator)).andExpect(status().isLocked());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：管理者ロック中の場合、パスキーでもログインできず423になる")
    void passkeyLogin_adminLocked() throws Exception {
      jdbcTemplate.update(
          "UPDATE common.account SET is_admin_locked = true WHERE account_id = ?", ACCOUNT_A);

      passkeyLogin(signAuthentication(authenticator)).andExpect(status().isLocked());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：パスキーを削除した後は、そのパスキーでログインできず401になる")
    void passkeyLogin_afterDeletion() throws Exception {
      String accessToken = login(authenticator);
      PasskeyTestAuthenticator second = registSecondPasskey(accessToken);
      Long firstPasskeyNo =
          jdbcTemplate.queryForObject(
              "SELECT passkey_no FROM common.passkey_credential WHERE passkey_name = '最初のパスキー'",
              Long.class);

      mockMvc
          .perform(
              post("/api/v1/accounts/" + ACCOUNT_A + "/passkeys/" + firstPasskeyNo + "/deletion")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of("credential", reauthenticate(ACCOUNT_A, accessToken, second)))))
          .andExpect(status().isOk());

      passkeyLogin(signAuthentication(authenticator)).andExpect(status().isUnauthorized());
      passkeyLogin(signAuthentication(second)).andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("異常系：サーバが発行していないチャレンジに署名した場合、401になる")
    void passkeyLogin_unknownChallenge() throws Exception {
      String credential =
          authenticator.createAuthenticationResponseJson(
              PasskeyTestAuthenticator.RP_ID, "bm90LWlzc3VlZC1ieS1zZXJ2ZXI");

      passkeyLogin(credential).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class deletePasskey {
    @Test
    @Order(1)
    @DisplayName("異常系：最後の1件のパスキーは、本人確認に成功しても削除できず400になる")
    void deletePasskey_lastPasskey() throws Exception {
      signUp(ACCOUNT_A, authenticator);
      String accessToken = login(authenticator);
      Long passkeyNo =
          jdbcTemplate.queryForObject(
              "SELECT passkey_no FROM common.passkey_credential WHERE account_no = ?",
              Long.class,
              accountNo(ACCOUNT_A));

      mockMvc
          .perform(
              post("/api/v1/accounts/" + ACCOUNT_A + "/passkeys/" + passkeyNo + "/deletion")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of(
                              "credential",
                              reauthenticate(ACCOUNT_A, accessToken, authenticator)))))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.CANNOT_DELETE_LAST_PASSKEY.getErrorMessage()));

      passkeyLogin(signAuthentication(authenticator)).andExpect(status().isOk());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class updatePasskey {
    @Test
    @Order(1)
    @DisplayName("異常系：他人のパスキーの名前は変更できず404になる")
    void updatePasskey_otherAccount() throws Exception {
      signUp(ACCOUNT_B, new PasskeyTestAuthenticator());
      Long otherPasskeyNo =
          jdbcTemplate.queryForObject(
              "SELECT passkey_no FROM common.passkey_credential WHERE account_no = ?",
              Long.class,
              accountNo(ACCOUNT_B));
      signUp(ACCOUNT_A, authenticator);
      String accessToken = login(authenticator);

      mockMvc
          .perform(
              put("/api/v1/accounts/" + ACCOUNT_A + "/passkeys/" + otherPasskeyNo)
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JSON.writeValueAsString(Map.of("passkeyName", "乗っ取り"))))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PASSKEY_NOT_FOUND.getErrorCode()));

      assertEquals(
          0, count("SELECT COUNT(*) FROM common.passkey_credential WHERE passkey_name = '乗っ取り'"));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class recovery {
    private JsonNode recoveryCodes;

    @BeforeEach
    void signUpAccountA() throws Exception {
      recoveryCodes = signUp(ACCOUNT_A, authenticator).get("recoveryCodes");
    }

    private ResultActions recoveryOptions(String accountId, String recoveryCode) throws Exception {
      return mockMvc.perform(
          post("/api/v1/auth/recovery/options")
              .contentType(MediaType.APPLICATION_JSON)
              .content(
                  JSON.writeValueAsString(
                      Map.of("accountId", accountId, "recoveryCode", recoveryCode))));
    }

    @Test
    @Order(1)
    @DisplayName("正常系：パスキーを失っても、リカバリーコードで新しい端末のパスキーを登録してログインでき、以後はそのパスキーでログインできる")
    void recovery_success() throws Exception {
      String body =
          recoveryOptions(ACCOUNT_A, recoveryCodes.get(0).asString())
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      JsonNode options = JSON.readTree(body);
      // 新規登録時と同じユーザーハンドル・アカウントIDで作成させる
      assertEquals(ACCOUNT_A, options.get("user").get("name").asString());

      PasskeyTestAuthenticator newDevice = new PasskeyTestAuthenticator();
      mockMvc
          .perform(
              post("/api/v1/auth/recovery/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of(
                              "credential",
                              createCredential(newDevice, options),
                              "passkeyName",
                              "新しい端末"))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").isNotEmpty())
          .andExpect(header().string("Set-Cookie", containsString("refreshToken=")));

      Long accountNo = accountNo(ACCOUNT_A);
      assertEquals(
          1,
          count(
              "SELECT COUNT(*) FROM common.login_history"
                  + " WHERE account_no = ? AND login_method_kbn = 'recovery_code'",
              accountNo));
      assertEquals(
          9, count("SELECT COUNT(*) FROM common.recovery_code WHERE account_no = ?", accountNo));
      passkeyLogin(signAuthentication(newDevice)).andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：使用済みのリカバリーコード・誤ったコード・存在しないアカウントは、理由を区別せず401になる")
    void recovery_invalidCode() throws Exception {
      recoveryOptions(ACCOUNT_A, recoveryCodes.get(0).asString()).andExpect(status().isOk());

      recoveryOptions(ACCOUNT_A, recoveryCodes.get(0).asString())
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_RECOVERY_CODE));
      recoveryOptions(ACCOUNT_A, "AAAA-AAAA-AAAA-AAAA")
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_RECOVERY_CODE));
      recoveryOptions("notexists", recoveryCodes.get(1).asString())
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_RECOVERY_CODE));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：管理者ロック中のアカウントは、リカバリーコードでも復旧できず423になり、コードは消費されない")
    void recovery_adminLocked() throws Exception {
      jdbcTemplate.update(
          "UPDATE common.account SET is_admin_locked = true WHERE account_id = ?", ACCOUNT_A);

      recoveryOptions(ACCOUNT_A, recoveryCodes.get(0).asString()).andExpect(status().isLocked());
      assertEquals(
          10,
          count(
              "SELECT COUNT(*) FROM common.recovery_code WHERE account_no = ?",
              accountNo(ACCOUNT_A)));
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class recoveryCodes {
    @Test
    @Order(1)
    @DisplayName("正常系：未使用件数を取得でき、パスキーで本人確認のうえ再発行すると以前のコードは使えなくなる")
    void recoveryCodes_regenerate() throws Exception {
      JsonNode oldCodes = signUp(ACCOUNT_A, authenticator).get("recoveryCodes");
      String accessToken = login(authenticator);

      mockMvc
          .perform(
              get("/api/v1/accounts/" + ACCOUNT_A + "/recovery-codes")
                  .header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.remainingCount").value(10))
          .andExpect(jsonPath("$.issueCount").value(10));

      String body =
          mockMvc
              .perform(
                  post("/api/v1/accounts/" + ACCOUNT_A + "/recovery-codes")
                      .header("Authorization", "Bearer " + accessToken)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(
                          JSON.writeValueAsString(
                              Map.of(
                                  "credential",
                                  reauthenticate(ACCOUNT_A, accessToken, authenticator)))))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.recoveryCodes.length()").value(10))
              .andReturn()
              .getResponse()
              .getContentAsString();
      JsonNode newCodes = JSON.readTree(body).get("recoveryCodes");
      assertNotEquals(oldCodes.get(0).asString(), newCodes.get(0).asString());

      mockMvc
          .perform(
              post("/api/v1/auth/recovery/options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      JSON.writeValueAsString(
                          Map.of(
                              "accountId", ACCOUNT_A, "recoveryCode", oldCodes.get(0).asString()))))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：本人確認の認証レスポンスが無い場合、再発行は400になりコードは変わらない")
    void recoveryCodes_regenerate_withoutReauthentication() throws Exception {
      signUp(ACCOUNT_A, authenticator);
      String accessToken = login(authenticator);

      mockMvc
          .perform(
              post("/api/v1/accounts/" + ACCOUNT_A + "/recovery-codes")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isBadRequest());
      assertEquals(
          10,
          count(
              "SELECT COUNT(*) FROM common.recovery_code WHERE account_no = ?",
              accountNo(ACCOUNT_A)));
    }
  }
}
