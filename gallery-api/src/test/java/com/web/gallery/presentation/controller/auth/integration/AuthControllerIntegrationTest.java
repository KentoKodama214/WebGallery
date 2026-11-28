package com.web.gallery.presentation.controller.auth.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.testsupport.PasskeyTestAuthenticator;
import jakarta.servlet.http.Cookie;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
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
public class AuthControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final JsonMapper JSON = JsonMapper.builder().build();

  /** テストアカウント（testuser01）のパスキーを保持する擬似認証器 */
  private PasskeyTestAuthenticator authenticator;

  /**
   * テストデータ投入
   *
   * <p>パスワードが無いため、テストアカウントは本番と同じ新規登録API（パスキーの作成）で登録する
   */
  private void insertTestData() throws Exception {
    authenticator = new PasskeyTestAuthenticator();
    String optionsBody =
        mockMvc
            .perform(
                post("/api/v1/accounts/signup-options")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        JSON.writeValueAsString(
                            Map.of("accountId", "testuser01", "accountName", "テストユーザー01"))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode options = JSON.readTree(optionsBody);
    String credential =
        authenticator.createRegistrationResponseJson(
            options.get("rp").get("id").asString(),
            options.get("challenge").asString(),
            options.get("user").get("id").asString(),
            "testuser01");
    mockMvc
        .perform(
            post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    JSON.writeValueAsString(
                        Map.of(
                            "accountId",
                            "testuser01",
                            "accountName",
                            "テストユーザー01",
                            "credential",
                            credential,
                            "passkeyName",
                            "テスト用パスキー"))))
        .andExpect(status().isOk());
  }

  /**
   * パスキー認証オプションを発行し、テストアカウントのパスキーで署名した認証レスポンスを返す
   *
   * @return 認証レスポンス（PublicKeyCredential#toJSON()のJSON文字列）
   */
  private String signAuthentication() throws Exception {
    String body =
        mockMvc
            .perform(post("/api/v1/auth/passkey/options"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode options = JSON.readTree(body);
    return authenticator.createAuthenticationResponseJson(
        options.get("rpId").asString(), options.get("challenge").asString());
  }

  /** テストアカウントのパスキーでログインする */
  private ResultActions passkeyLogin() throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/passkey/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON.writeValueAsString(Map.of("credential", signAuthentication()))));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class passkeyLogin {
    @BeforeEach
    void setUp() throws Exception {
      insertTestData();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：パスキーでログインでき、リフレッシュトークンを安全な属性のcookieで返す")
    void passkeyLogin_success() throws Exception {
      MvcResult result =
          passkeyLogin()
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.accessToken").isNotEmpty())
              .andExpect(jsonPath("$.expiresIn").isNumber())
              .andExpect(header().exists("Set-Cookie"))
              .andReturn();

      // Set-CookieヘッダーにrefreshTokenが含まれていることを検証
      String setCookie = result.getResponse().getHeader("Set-Cookie");
      assertNotNull(setCookie);
      assertTrue(setCookie.contains("refreshToken="));
      assertTrue(setCookie.contains("HttpOnly"));
      assertTrue(setCookie.contains("Secure"));
      assertTrue(setCookie.contains("Path=/api/v1/auth"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ログイン失敗回数の上限に達しロック中の場合、423を返す")
    void passkeyLogin_locked_account() throws Exception {
      jdbcTemplate.update(
          "UPDATE common.account SET login_failure_count = 3, updated_at = NOW() WHERE account_id = 'testuser01'");

      passkeyLogin()
          .andExpect(status().is(423))
          .andExpect(jsonPath("$.message").value("アカウントがロックされています。"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：管理者に強制ロックされたアカウントは、更新日時が十分過去でも自動解除されず423を返す")
    void passkeyLogin_admin_locked_account_not_auto_released() throws Exception {
      jdbcTemplate.update(
          "UPDATE common.account SET is_admin_locked = true, login_failure_count = 3,"
              + " updated_at = '2001-01-03 09:00:00 Asia/Tokyo' WHERE account_id = 'testuser01'");

      passkeyLogin()
          .andExpect(status().is(423))
          .andExpect(jsonPath("$.message").value("アカウントがロックされています。"));

      Boolean isAdminLocked =
          jdbcTemplate.queryForObject(
              "SELECT is_admin_locked FROM common.account WHERE account_id = 'testuser01'",
              Boolean.class);
      assertTrue(isAdminLocked);
    }

    @Test
    @Order(4)
    @DisplayName("異常系：credentialが空の場合、400を返す")
    void passkeyLogin_blank_credential() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auth/passkey/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"credential\":\"\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：リクエストボディのJSON構文が不正な場合、共通のJSONエラー形式で400を返す")
    void passkeyLogin_malformed_json() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auth/passkey/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"credential\":"))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message").value(MessageConst.ERR_INVALID_INPUT));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：廃止したパスワードログインAPIは存在せず、未認証として401を返す")
    void passwordLogin_removed() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"accountId\":\"testuser01\",\"password\":\"password01\"}"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class refresh {
    @BeforeEach
    void setUp() throws Exception {
      insertTestData();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：リフレッシュ成功時、リフレッシュトークンがローテーション（新しいcookieに差し替え）されること")
    void refresh_success() throws Exception {
      // まずログインしてリフレッシュトークンを取得
      MvcResult loginResult = passkeyLogin().andExpect(status().isOk()).andReturn();

      Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
      assertNotNull(refreshTokenCookie);

      // リフレッシュトークンを使ってアクセストークンを更新
      MvcResult refreshResult =
          mockMvc
              .perform(post("/api/v1/auth/refresh").cookie(refreshTokenCookie))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.accessToken").isNotEmpty())
              .andExpect(jsonPath("$.expiresIn").isNumber())
              .andReturn();

      // 新しいリフレッシュトークンがcookieに設定され、旧トークンとは異なること
      Cookie rotatedRefreshTokenCookie = refreshResult.getResponse().getCookie("refreshToken");
      assertNotNull(rotatedRefreshTokenCookie);
      assertNotEquals(refreshTokenCookie.getValue(), rotatedRefreshTokenCookie.getValue());

      // 無効化済みの旧トークンでのリフレッシュは失敗すること
      mockMvc
          .perform(post("/api/v1/auth/refresh").cookie(refreshTokenCookie))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：refreshTokenがない（未ログイン）場合、エラーではなく本文なしの204を返す")
    void refresh_no_cookie() throws Exception {
      mockMvc
          .perform(post("/api/v1/auth/refresh"))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""))
          .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：無効なrefreshTokenの場合、401を返す")
    void refresh_invalid_token() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auth/refresh")
                  .cookie(new Cookie("refreshToken", "invalid-refresh-token")))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("無効なリフレッシュトークンです。"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class logout {
    @BeforeEach
    void setUp() throws Exception {
      insertTestData();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：ログアウト成功（refreshTokenあり）")
    void logout_with_token() throws Exception {
      // まずログインしてリフレッシュトークンを取得
      MvcResult loginResult = passkeyLogin().andExpect(status().isOk()).andReturn();

      Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
      assertNotNull(refreshTokenCookie);

      // ログアウト
      MvcResult logoutResult =
          mockMvc
              .perform(post("/api/v1/auth/logout").cookie(refreshTokenCookie))
              .andExpect(status().isNoContent())
              .andExpect(header().exists("Set-Cookie"))
              .andReturn();

      // Set-CookieでrefreshTokenがクリアされていることを検証
      String setCookie = logoutResult.getResponse().getHeader("Set-Cookie");
      assertNotNull(setCookie);
      assertTrue(setCookie.contains("refreshToken="));
      assertTrue(setCookie.contains("Max-Age=0"));

      // ログアウト後、同じリフレッシュトークンでリフレッシュが失敗することを検証
      mockMvc
          .perform(post("/api/v1/auth/refresh").cookie(refreshTokenCookie))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ログアウト成功（refreshTokenなし）")
    void logout_without_token() throws Exception {
      MvcResult result =
          mockMvc
              .perform(post("/api/v1/auth/logout"))
              .andExpect(status().isNoContent())
              .andExpect(header().exists("Set-Cookie"))
              .andReturn();

      String setCookie = result.getResponse().getHeader("Set-Cookie");
      assertNotNull(setCookie);
      assertTrue(setCookie.contains("Max-Age=0"));
    }
  }

  /**
   * {@link com.web.gallery.infrastructure.web.JwtAuthenticationFilter}の異常系を検証するテストクラス
   *
   * <p>アクセストークン自体は正当だが、検証後の付随チェック（アカウントロック・アカウント削除）で 認証情報を設定しないパスを、保護対象API（アカウント詳細取得）経由で検証する
   */
  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class jwtAuthenticationFilter {
    @BeforeEach
    void setUp() throws Exception {
      insertTestData();
    }

    /** パスキーでログインしてアクセストークンを取得する */
    private String obtainAccessToken() throws Exception {
      MvcResult result = passkeyLogin().andExpect(status().isOk()).andReturn();
      return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    @Test
    @Order(1)
    @DisplayName("異常系：AuthorizationヘッダーがBearer形式でない場合、未認証として401を返す")
    void getAccount_nonBearerAuthorizationHeader() throws Exception {
      String accessToken = obtainAccessToken();

      mockMvc
          .perform(
              get("/api/v1/accounts/testuser01")
                  // "Bearer "プレフィックスを付けず、そのままトークン値のみを設定する
                  .header("Authorization", accessToken))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：不正な形式のアクセストークンの場合、未認証として401を返す")
    void getAccount_malformedToken() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/accounts/testuser01")
                  .header("Authorization", "Bearer not-a-valid-jwt-token"))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：トークン発行後に管理者ロックされたアカウントの場合、有効なトークンでも未認証として401を返す")
    void getAccount_lockedAfterTokenIssued() throws Exception {
      String accessToken = obtainAccessToken();

      // トークン発行後、管理者による強制ロックが行われた状態を再現する
      jdbcTemplate.update(
          "UPDATE common.account SET is_admin_locked = true WHERE account_id = 'testuser01'");

      mockMvc
          .perform(
              get("/api/v1/accounts/testuser01").header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：トークン発行後にアカウントが削除された場合、有効なトークンでも未認証として401を返す")
    void getAccount_deletedAfterTokenIssued() throws Exception {
      String accessToken = obtainAccessToken();

      // トークン発行後、本人によるアカウント削除（物理削除）が行われた状態を再現する
      // （新規登録・ログインによりパスキー・リカバリーコード・login_history・refresh_tokenにも行が作成されているため、先に削除する）
      jdbcTemplate.update(
          "DELETE FROM common.account_authority WHERE account_no ="
              + " (SELECT account_no FROM common.account WHERE account_id = 'testuser01')");
      for (String table :
          new String[] {
            "common.passkey_credential", "common.passkey_user_handle", "common.recovery_code"
          }) {
        jdbcTemplate.update(
            "DELETE FROM "
                + table
                + " WHERE account_no ="
                + " (SELECT account_no FROM common.account WHERE account_id = 'testuser01')");
      }
      jdbcTemplate.update(
          "DELETE FROM common.login_history WHERE account_no ="
              + " (SELECT account_no FROM common.account WHERE account_id = 'testuser01')");
      jdbcTemplate.update(
          "DELETE FROM common.refresh_token WHERE account_no ="
              + " (SELECT account_no FROM common.account WHERE account_id = 'testuser01')");
      jdbcTemplate.update("DELETE FROM common.account WHERE account_id = 'testuser01'");

      mockMvc
          .perform(
              get("/api/v1/accounts/testuser01").header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.errorCode").value("E-A-0002"));
    }
  }
}
