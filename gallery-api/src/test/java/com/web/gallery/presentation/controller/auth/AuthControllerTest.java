package com.web.gallery.presentation.controller.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.config.JwtConfig;
import com.web.gallery.application.model.auth.AuthTokenModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.service.impl.auth.AuthServiceImpl;
import com.web.gallery.domain.exception.InvalidRecoveryCodeException;
import com.web.gallery.domain.exception.InvalidRefreshTokenException;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.auth.AccessToken;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.ExpiresIn;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.auth.RefreshTokenValue;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import com.web.gallery.domain.model.auth.RelyingPartyName;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.infrastructure.web.ClientIpResolver;
import com.web.gallery.presentation.controller.common.CommonControllerAdvice;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
  @InjectMocks private AuthController authController;

  @Mock private AuthServiceImpl authServiceImpl;

  @Mock private JwtConfig jwtConfig;

  @Mock private ClientIpResolver clientIpResolver;

  private MockMvc mockMvc;

  private static final IpAddress TEST_IP_ADDRESS = new IpAddress("203.0.113.1");

  @BeforeEach
  void setUp() {
    lenient().when(clientIpResolver.resolve(any())).thenReturn(TEST_IP_ADDRESS);
    mockMvc =
        MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/AuthControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class refresh {
    @Test
    @Order(1)
    @DisplayName("正常系：リフレッシュ成功")
    void refresh_success() throws Exception {
      AuthTokenModel tokenModel =
          AuthTokenModel.builder()
              .accessToken(new AccessToken("new-access-token"))
              .refreshToken(new RefreshTokenValue("test-refresh-token"))
              .expiresIn(new ExpiresIn(900L))
              .build();

      doReturn(tokenModel)
          .when(authServiceImpl)
          .refresh(new RefreshTokenValue("test-refresh-token"));
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();

      mockMvc
          .perform(
              post("/api/v1/auth/refresh").cookie(new Cookie("refreshToken", "test-refresh-token")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").value("new-access-token"))
          .andExpect(jsonPath("$.expiresIn").value(900))
          .andExpect(header().exists("Set-Cookie"));

      verify(authServiceImpl, times(1)).refresh(new RefreshTokenValue("test-refresh-token"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：refreshTokenがnull（未ログイン）の場合、エラーではなく204を返す")
    void refresh_no_cookie() throws Exception {
      mockMvc
          .perform(post("/api/v1/auth/refresh"))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(authServiceImpl, times(0)).refresh(any(RefreshTokenValue.class));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：refreshTokenが空（未ログイン）の場合、エラーではなく204を返す")
    void refresh_empty_cookie() throws Exception {
      mockMvc
          .perform(post("/api/v1/auth/refresh").cookie(new Cookie("refreshToken", "")))
          .andExpect(status().isNoContent());

      verify(authServiceImpl, times(0)).refresh(any(RefreshTokenValue.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：無効なリフレッシュトークンの場合、401を返す")
    void refresh_invalid_token() throws Exception {
      doThrow(new InvalidRefreshTokenException("無効なリフレッシュトークンです"))
          .when(authServiceImpl)
          .refresh(new RefreshTokenValue("invalid-token"));

      mockMvc
          .perform(post("/api/v1/auth/refresh").cookie(new Cookie("refreshToken", "invalid-token")))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("無効なリフレッシュトークンです。"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class logout {
    @Test
    @Order(1)
    @DisplayName("正常系：ログアウト成功（refreshTokenあり）")
    void logout_with_token() throws Exception {
      doNothing().when(authServiceImpl).logout(new RefreshTokenValue("test-refresh-token"));

      mockMvc
          .perform(
              post("/api/v1/auth/logout").cookie(new Cookie("refreshToken", "test-refresh-token")))
          .andExpect(status().isNoContent())
          .andExpect(header().exists("Set-Cookie"));

      verify(authServiceImpl, times(1)).logout(new RefreshTokenValue("test-refresh-token"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ログアウト成功（refreshTokenなし）")
    void logout_without_token() throws Exception {
      mockMvc
          .perform(post("/api/v1/auth/logout"))
          .andExpect(status().isNoContent())
          .andExpect(header().exists("Set-Cookie"));

      verify(authServiceImpl, times(0)).logout(any(RefreshTokenValue.class));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ログアウト成功（refreshTokenが空）")
    void logout_empty_token() throws Exception {
      mockMvc
          .perform(post("/api/v1/auth/logout").cookie(new Cookie("refreshToken", "")))
          .andExpect(status().isNoContent())
          .andExpect(header().exists("Set-Cookie"));

      verify(authServiceImpl, times(0)).logout(any(RefreshTokenValue.class));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class passkeyOptions {
    @Test
    @Order(1)
    @DisplayName("正常系：パスキー認証オプションをPublicKeyCredentialRequestOptionsJSONの形式で返す")
    void passkeyOptions_success() throws Exception {
      doReturn(
              PasskeyAuthenticationOptionsModel.builder()
                  .challenge(new PasskeyChallenge("challenge-1"))
                  .rpId(new RelyingPartyId("localhost"))
                  .allowCredentialIds(List.of())
                  .timeoutMillis(new PasskeyTimeoutMillis(300_000L))
                  .build())
          .when(authServiceImpl)
          .createPasskeyAuthenticationOptions();

      mockMvc
          .perform(post("/api/v1/auth/passkey/options"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.challenge").value("challenge-1"))
          .andExpect(jsonPath("$.rpId").value("localhost"))
          .andExpect(jsonPath("$.timeout").value(300000))
          .andExpect(jsonPath("$.userVerification").value("required"))
          .andExpect(jsonPath("$.allowCredentials").isEmpty());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class passkeyLogin {
    private static final PasskeyResponseJson CREDENTIAL =
        new PasskeyResponseJson("{\"id\":\"credential-1\",\"type\":\"public-key\"}");

    @Test
    @Order(1)
    @DisplayName("正常系：パスキーログインに成功した場合、アクセストークンとリフレッシュトークンのcookieを返す")
    void passkeyLogin_success() throws Exception {
      doReturn(
              AuthTokenModel.builder()
                  .accessToken(new AccessToken("test-access-token"))
                  .refreshToken(new RefreshTokenValue("test-refresh-token"))
                  .expiresIn(new ExpiresIn(900L))
                  .build())
          .when(authServiceImpl)
          .loginWithPasskey(CREDENTIAL, TEST_IP_ADDRESS);
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();

      mockMvc
          .perform(
              post("/api/v1/auth/passkey/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("passkeyLogin_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").value("test-access-token"))
          .andExpect(jsonPath("$.expiresIn").value(900))
          .andExpect(
              header().string("Set-Cookie", containsString("refreshToken=test-refresh-token")))
          .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));

      verify(authServiceImpl, times(1)).loginWithPasskey(CREDENTIAL, TEST_IP_ADDRESS);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：認証レスポンスが空の場合、400を返す")
    void passkeyLogin_credential_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auth/passkey/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("passkeyLogin_badrequest_blank_credential.json")))
          .andExpect(status().isBadRequest());

      verify(authServiceImpl, times(0)).loginWithPasskey(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：パスキーの検証に失敗した場合、失敗理由を出さず401を返す")
    void passkeyLogin_verification_failed() throws Exception {
      doThrow(new PasskeyVerificationException("Unknown passkey credential"))
          .when(authServiceImpl)
          .loginWithPasskey(CREDENTIAL, TEST_IP_ADDRESS);

      mockMvc
          .perform(
              post("/api/v1/auth/passkey/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("passkeyLogin_success.json")))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("パスキーによる認証に失敗しました。"))
          .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：アカウントがロックされている場合、423を返す")
    void passkeyLogin_locked() throws Exception {
      doThrow(new LockedException("locked"))
          .when(authServiceImpl)
          .loginWithPasskey(CREDENTIAL, TEST_IP_ADDRESS);

      mockMvc
          .perform(
              post("/api/v1/auth/passkey/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("passkeyLogin_success.json")))
          .andExpect(status().isLocked());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：アカウントが無効の場合、401を返す")
    void passkeyLogin_disabled() throws Exception {
      doThrow(new DisabledException("disabled"))
          .when(authServiceImpl)
          .loginWithPasskey(CREDENTIAL, TEST_IP_ADDRESS);

      mockMvc
          .perform(
              post("/api/v1/auth/passkey/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("passkeyLogin_success.json")))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class recoveryOptions {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントIDとリカバリーコードで、新しいパスキーの登録オプションを返す")
    void recoveryOptions_success() throws Exception {
      doReturn(
              PasskeyRegistrationOptionsModel.builder()
                  .challenge(new PasskeyChallenge("challenge-1"))
                  .rpId(new RelyingPartyId("localhost"))
                  .rpName(new RelyingPartyName("WebGallery"))
                  .userHandle(new PasskeyUserHandle("handle-1"))
                  .accountId(new AccountId("testuser"))
                  .accountName(new AccountName("テストユーザー"))
                  .excludeCredentialIds(List.of(new CredentialId("credential-1")))
                  .timeoutMillis(new PasskeyTimeoutMillis(300_000L))
                  .build())
          .when(authServiceImpl)
          .createRecoveryOptions(
              new AccountId("testuser"), new RecoveryCode("ABCD-EFGH-JKMN-PQRS"));

      mockMvc
          .perform(
              post("/api/v1/auth/recovery/options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("recoveryOptions_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.challenge").value("challenge-1"))
          .andExpect(jsonPath("$.user.id").value("handle-1"))
          .andExpect(jsonPath("$.user.name").value("testuser"))
          .andExpect(jsonPath("$.excludeCredentials[0].id").value("credential-1"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：リカバリーコードが空の場合、400を返す")
    void recoveryOptions_recoveryCode_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auth/recovery/options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("recoveryOptions_badrequest_blank_recoveryCode.json")))
          .andExpect(status().isBadRequest());

      verify(authServiceImpl, times(0)).createRecoveryOptions(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：アカウントIDまたはリカバリーコードが正しくない場合、理由を区別せず401を返す")
    void recoveryOptions_invalid_code() throws Exception {
      doThrow(new InvalidRecoveryCodeException("Recovery code mismatch"))
          .when(authServiceImpl)
          .createRecoveryOptions(any(), any());

      mockMvc
          .perform(
              post("/api/v1/auth/recovery/options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("recoveryOptions_success.json")))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value("アカウントIDまたはリカバリーコードが正しくありません。"));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：アカウントがロックされている場合、423を返す")
    void recoveryOptions_locked() throws Exception {
      doThrow(new LockedException("locked"))
          .when(authServiceImpl)
          .createRecoveryOptions(any(), any());

      mockMvc
          .perform(
              post("/api/v1/auth/recovery/options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("recoveryOptions_success.json")))
          .andExpect(status().isLocked());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class recoveryLogin {
    private static final PasskeyResponseJson CREDENTIAL =
        new PasskeyResponseJson("{\"id\":\"credential-1\",\"type\":\"public-key\"}");

    @Test
    @Order(1)
    @DisplayName("正常系：新しいパスキーを登録してログインした場合、アクセストークンとリフレッシュトークンのcookieを返す")
    void recoveryLogin_success() throws Exception {
      doReturn(
              AuthTokenModel.builder()
                  .accessToken(new AccessToken("test-access-token"))
                  .refreshToken(new RefreshTokenValue("test-refresh-token"))
                  .expiresIn(new ExpiresIn(900L))
                  .build())
          .when(authServiceImpl)
          .loginWithRecoveryCode(CREDENTIAL, new PasskeyName("新しいiPhone"), TEST_IP_ADDRESS);
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();

      mockMvc
          .perform(
              post("/api/v1/auth/recovery/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("recoveryLogin_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").value("test-access-token"))
          .andExpect(
              header().string("Set-Cookie", containsString("refreshToken=test-refresh-token")));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：パスキー名が空の場合、400を返す")
    void recoveryLogin_passkeyName_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auth/recovery/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("recoveryLogin_badrequest_blank_passkeyName.json")))
          .andExpect(status().isBadRequest());

      verify(authServiceImpl, times(0)).loginWithRecoveryCode(any(), any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：登録レスポンスの検証に失敗した場合、401を返しcookieを設定しない")
    void recoveryLogin_verification_failed() throws Exception {
      doThrow(new PasskeyVerificationException("challenge not available"))
          .when(authServiceImpl)
          .loginWithRecoveryCode(any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/auth/recovery/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("recoveryLogin_success.json")))
          .andExpect(status().isUnauthorized())
          .andExpect(header().doesNotExist("Set-Cookie"));
    }
  }
}
