package com.web.gallery.presentation.controller.account.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * パスキーの登録・管理・ログインを、実DBとwebauthn4j-testの擬似認証器で通しで検証する統合テスト
 *
 * <p>登録・認証レスポンスはサーバが発行したチャレンジに対して擬似認証器がその場で署名して作るため、 リクエストボディはJSONファイルではなく実行時に組み立てる
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@MockitoBean(types = FileRepository.class)
public class PasskeyControllerIntegrationTest {
  private static final String PASSWORD = "password01";

  private static final JsonMapper JSON = JsonMapper.builder().build();

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  private PasskeyTestAuthenticator authenticator;

  @BeforeEach
  void setUpAuthenticator() {
    authenticator = new PasskeyTestAuthenticator();
  }

  /**
   * パスワードでログインし、アクセストークンを取得する
   *
   * @param accountId アカウントID
   * @return アクセストークン
   */
  private String loginWithPassword(String accountId) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        JSON.writeValueAsString(
                            Map.of("accountId", accountId, "password", PASSWORD))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body).get("accessToken").asString();
  }

  /**
   * 登録オプションを発行する
   *
   * @param accountId アカウントID
   * @param accessToken アクセストークン
   * @return 登録オプション（PublicKeyCredentialCreationOptionsJSON）
   */
  private JsonNode registrationOptions(String accountId, String accessToken) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/accounts/" + accountId + "/passkeys/registration-options")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON.writeValueAsString(Map.of("currentPassword", PASSWORD))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JSON.readTree(body);
  }

  /**
   * 擬似認証器でパスキーを作成し、登録APIを呼び出す
   *
   * @param accountId アカウントID
   * @param accessToken アクセストークン
   * @param options 登録オプション
   * @return 登録APIのレスポンス
   */
  private org.springframework.test.web.servlet.ResultActions registPasskey(
      String accountId, String accessToken, JsonNode options) throws Exception {
    String credential =
        authenticator.createRegistrationResponseJson(
            options.get("rp").get("id").asString(),
            options.get("challenge").asString(),
            options.get("user").get("id").asString(),
            options.get("user").get("name").asString());
    return mockMvc.perform(
        post("/api/v1/accounts/" + accountId + "/passkeys")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                JSON.writeValueAsString(
                    Map.of("credential", credential, "passkeyName", "MacBookのTouch ID"))));
  }

  /**
   * 認証オプションを発行し、擬似認証器で署名した認証レスポンスを返す
   *
   * @return 認証レスポンス（PublicKeyCredential#toJSON()のJSON文字列）
   */
  private String signAuthentication() throws Exception {
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
    return authenticator.createAuthenticationResponseJson(
        options.get("rpId").asString(), options.get("challenge").asString());
  }

  private org.springframework.test.web.servlet.ResultActions passkeyLogin(String credential)
      throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/passkey/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON.writeValueAsString(Map.of("credential", credential))));
  }

  private Integer count(String sql) {
    return jdbcTemplate.queryForObject(sql, Integer.class);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PasskeyControllerIntegrationTest.sql")
  class registPasskey {
    @Test
    @Order(1)
    @DisplayName("正常系：本人確認のうえ登録オプションを発行し、擬似認証器で作成したパスキーを登録できる")
    void registPasskey_success() throws Exception {
      String accessToken = loginWithPassword("aaaaaaaa");
      JsonNode options = registrationOptions("aaaaaaaa", accessToken);

      assertEquals(PasskeyTestAuthenticator.RP_ID, options.get("rp").get("id").asString());
      assertEquals("aaaaaaaa", options.get("user").get("name").asString());
      assertTrue(options.get("excludeCredentials").isEmpty());

      registPasskey("aaaaaaaa", accessToken, options)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.passkeyName").value("MacBookのTouch ID"));

      assertEquals(1, count("SELECT COUNT(*) FROM common.passkey_credential WHERE account_no = 1"));
      assertEquals(
          1, count("SELECT COUNT(*) FROM common.passkey_user_handle WHERE account_no = 1"));
      // 登録に使ったチャレンジは消費されている
      assertEquals(
          0,
          count(
              "SELECT COUNT(*) FROM common.webauthn_challenge WHERE purpose_kbn = 'registration'"));

      // 2つ目の登録オプションでは、登録済みのパスキーが除外対象になり、ユーザーハンドルは同じものが使われる
      JsonNode second = registrationOptions("aaaaaaaa", accessToken);
      assertEquals(1, second.get("excludeCredentials").size());
      assertEquals(
          options.get("user").get("id").asString(), second.get("user").get("id").asString());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じ登録オプション（チャレンジ）で2回登録しようとした場合、2回目は400になる")
    void registPasskey_challengeReused() throws Exception {
      String accessToken = loginWithPassword("aaaaaaaa");
      JsonNode options = registrationOptions("aaaaaaaa", accessToken);
      registPasskey("aaaaaaaa", accessToken, options).andExpect(status().isOk());

      registPasskey("aaaaaaaa", accessToken, options)
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorMessage()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：他のアカウントに発行されたチャレンジでは登録できない")
    void registPasskey_challengeOfOtherAccount() throws Exception {
      String otherToken = loginWithPassword("bbbbbbbb");
      JsonNode otherOptions = registrationOptions("bbbbbbbb", otherToken);
      String accessToken = loginWithPassword("aaaaaaaa");

      registPasskey("aaaaaaaa", accessToken, otherOptions).andExpect(status().isBadRequest());
      assertEquals(0, count("SELECT COUNT(*) FROM common.passkey_credential"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：現在のパスワードが一致しない場合、登録オプションを発行せず403になる")
    void registPasskey_passwordMismatch() throws Exception {
      String accessToken = loginWithPassword("aaaaaaaa");

      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/passkeys/registration-options")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JSON.writeValueAsString(Map.of("currentPassword", "wrong-password"))))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.CURRENT_PASSWORD_MISMATCH.getErrorCode()));
      assertEquals(0, count("SELECT COUNT(*) FROM common.webauthn_challenge"));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：未ログインの場合、登録オプションの発行は401になる")
    void registPasskey_unauthenticated() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/passkeys/registration-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JSON.writeValueAsString(Map.of("currentPassword", PASSWORD))))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PasskeyControllerIntegrationTest.sql")
  class passkeyLogin {
    @BeforeEach
    void registPasskeyForAccount1() throws Exception {
      String accessToken = loginWithPassword("aaaaaaaa");
      registPasskey("aaaaaaaa", accessToken, registrationOptions("aaaaaaaa", accessToken))
          .andExpect(status().isOk());
    }

    @Test
    @Order(1)
    @DisplayName("正常系：登録したパスキーでログインでき、トークンが発行され、ログイン履歴にパスキーとして記録される")
    void passkeyLogin_success() throws Exception {
      passkeyLogin(signAuthentication())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").isNotEmpty())
          .andExpect(header().string("Set-Cookie", containsString("refreshToken=")));

      assertEquals(
          1,
          count(
              "SELECT COUNT(*) FROM common.login_history"
                  + " WHERE account_no = 1 AND login_method_kbn = 'passkey'"));
      // パスワードでのログインは password として記録されている
      assertEquals(
          1,
          count(
              "SELECT COUNT(*) FROM common.login_history"
                  + " WHERE account_no = 1 AND login_method_kbn = 'password'"));
      // 署名カウンタが更新されている
      assertEquals(
          1,
          count(
              "SELECT COUNT(*) FROM common.passkey_credential"
                  + " WHERE account_no = 1 AND sign_count > 1"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：パスキーログインで発行されたアクセストークンで、パスキー一覧を取得できる")
    void passkeyLogin_tokenUsable() throws Exception {
      String body =
          passkeyLogin(signAuthentication())
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      String accessToken = JSON.readTree(body).get("accessToken").asString();

      mockMvc
          .perform(
              get("/api/v1/accounts/aaaaaaaa/passkeys")
                  .header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.passkeys.length()").value(1))
          .andExpect(jsonPath("$.passkeys[0].passkeyName").value("MacBookのTouch ID"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：同じ認証レスポンスを再送した場合（リプレイ）、401になる")
    void passkeyLogin_replay() throws Exception {
      String credential = signAuthentication();
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
              + " WHERE account_no = 1");

      passkeyLogin(signAuthentication()).andExpect(status().isLocked());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：管理者ロック中の場合、パスキーでもログインできず423になる")
    void passkeyLogin_adminLocked() throws Exception {
      jdbcTemplate.update("UPDATE common.account SET is_admin_locked = true WHERE account_no = 1");

      passkeyLogin(signAuthentication()).andExpect(status().isLocked());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：パスキーを削除した後は、そのパスキーでログインできず401になる")
    void passkeyLogin_afterDeletion() throws Exception {
      String accessToken = loginWithPassword("aaaaaaaa");
      Long passkeyNo =
          jdbcTemplate.queryForObject(
              "SELECT passkey_no FROM common.passkey_credential WHERE account_no = 1", Long.class);

      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/passkeys/" + passkeyNo + "/deletion")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JSON.writeValueAsString(Map.of("currentPassword", PASSWORD))))
          .andExpect(status().isOk());

      passkeyLogin(signAuthentication()).andExpect(status().isUnauthorized());
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
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/controller/PasskeyControllerIntegrationTest.sql")
  class updatePasskey {
    @Test
    @Order(1)
    @DisplayName("異常系：他人のパスキーの名前は変更できず404になる")
    void updatePasskey_otherAccount() throws Exception {
      String otherToken = loginWithPassword("bbbbbbbb");
      registPasskey("bbbbbbbb", otherToken, registrationOptions("bbbbbbbb", otherToken))
          .andExpect(status().isOk());
      Long otherPasskeyNo =
          jdbcTemplate.queryForObject(
              "SELECT passkey_no FROM common.passkey_credential WHERE account_no = 2", Long.class);
      String accessToken = loginWithPassword("aaaaaaaa");

      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa/passkeys/" + otherPasskeyNo)
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JSON.writeValueAsString(Map.of("passkeyName", "乗っ取り"))))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PASSKEY_NOT_FOUND.getErrorCode()));

      assertEquals(
          0, count("SELECT COUNT(*) FROM common.passkey_credential WHERE passkey_name = '乗っ取り'"));
    }
  }
}
