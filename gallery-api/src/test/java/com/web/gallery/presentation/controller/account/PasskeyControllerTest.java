package com.web.gallery.presentation.controller.account;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.Password;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import com.web.gallery.domain.model.auth.RelyingPartyName;
import com.web.gallery.domain.model.auth.SignCount;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.presentation.controller.common.CommonControllerAdvice;
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
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PasskeyControllerTest {
  private static final String SELF = "aaaaaaaa";
  private static final String OTHER = "bbbbbbbb";
  private static final AccountNo ACCOUNT_NO = new AccountNo(1L);

  @InjectMocks private PasskeyController passkeyController;

  @Mock private AccountService accountService;

  @Mock private SessionHelper sessionHelper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    lenient().doReturn(SELF).when(sessionHelper).getAccountId();
    lenient().doReturn(1L).when(sessionHelper).getAccountNo();
    mockMvc =
        MockMvcBuilders.standaloneSetup(passkeyController)
            .setMessageConverters(new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/PasskeyControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  private static PasskeyCredentialModel passkey(long passkeyNo, String name) {
    return PasskeyCredentialModel.builder()
        .passkeyNo(new PasskeyNo(passkeyNo))
        .accountNo(ACCOUNT_NO)
        .credentialId(new CredentialId("credential-" + passkeyNo))
        .attestedCredentialData(new AttestedCredentialData(new byte[] {1}))
        .signCount(new SignCount(0L))
        .transports(new PasskeyTransports("internal"))
        .isBackupEligible(new IsBackupEligible(true))
        .isBackedUp(new IsBackedUp(true))
        .isUvInitialized(new IsUvInitialized(true))
        .passkeyName(new PasskeyName(name))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPasskeys {
    @Test
    @Order(1)
    @DisplayName("正常系：本人のパスキー一覧と登録上限を返し、公開鍵等の技術的な値は返さないこと")
    void getPasskeys_success() throws Exception {
      doReturn(
              PasskeyCredentialModelList.of(List.of(passkey(1L, "MacBook"), passkey(2L, "iPhone"))))
          .when(accountService)
          .getPasskeyList(ACCOUNT_NO);

      mockMvc
          .perform(get("/api/v1/accounts/" + SELF + "/passkeys"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.passkeys[0].passkeyNo").value(1))
          .andExpect(jsonPath("$.passkeys[0].passkeyName").value("MacBook"))
          .andExpect(jsonPath("$.passkeys[0].isSynced").value(true))
          .andExpect(jsonPath("$.passkeys[1].passkeyName").value("iPhone"))
          .andExpect(jsonPath("$.passkeys[0].credentialId").doesNotExist())
          .andExpect(jsonPath("$.passkeys[0].attestedCredentialData").doesNotExist())
          .andExpect(jsonPath("$.maxCount").value(10));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：本人以外のアカウントIDの場合、403を返すこと")
    void getPasskeys_otherAccount() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts/" + OTHER + "/passkeys"))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.getErrorCode()));

      verify(accountService, times(0)).getPasskeyList(any());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class createRegistrationOptions {
    @Test
    @Order(1)
    @DisplayName("正常系：現在のパスワードを渡して登録オプションを発行し、PublicKeyCredentialCreationOptionsJSONの形式で返すこと")
    void createRegistrationOptions_success() throws Exception {
      doReturn(
              PasskeyRegistrationOptionsModel.builder()
                  .challenge(new PasskeyChallenge("challenge-1"))
                  .rpId(new RelyingPartyId("localhost"))
                  .rpName(new RelyingPartyName("WebGallery"))
                  .userHandle(new PasskeyUserHandle("handle-1"))
                  .accountId(new AccountId(SELF))
                  .accountName(new AccountName("テストユーザー"))
                  .excludeCredentialIds(List.of(new CredentialId("credential-1")))
                  .timeoutMillis(new PasskeyTimeoutMillis(300_000L))
                  .build())
          .when(accountService)
          .createPasskeyRegistrationOptions(ACCOUNT_NO, new Password("password01"));

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys/registration-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("createRegistrationOptions_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.rp.id").value("localhost"))
          .andExpect(jsonPath("$.rp.name").value("WebGallery"))
          .andExpect(jsonPath("$.user.id").value("handle-1"))
          .andExpect(jsonPath("$.user.name").value(SELF))
          .andExpect(jsonPath("$.user.displayName").value("テストユーザー"))
          .andExpect(jsonPath("$.challenge").value("challenge-1"))
          .andExpect(jsonPath("$.pubKeyCredParams[0].alg").value(-7))
          .andExpect(jsonPath("$.excludeCredentials[0].id").value("credential-1"))
          .andExpect(jsonPath("$.authenticatorSelection.residentKey").value("required"))
          .andExpect(jsonPath("$.authenticatorSelection.userVerification").value("required"))
          .andExpect(jsonPath("$.attestation").value("none"))
          .andExpect(jsonPath("$.timeout").value(300000));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：現在のパスワードが空の場合、400を返すこと")
    void createRegistrationOptions_password_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys/registration-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      readJsonFile("createRegistrationOptions_badrequest_blank_password.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).createPasskeyRegistrationOptions(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：本人以外のアカウントIDの場合、403を返すこと")
    void createRegistrationOptions_otherAccount() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + OTHER + "/passkeys/registration-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("createRegistrationOptions_success.json")))
          .andExpect(status().isForbidden());

      verify(accountService, times(0)).createPasskeyRegistrationOptions(any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：現在のパスワードが一致しない場合、403を返すこと")
    void createRegistrationOptions_passwordMismatch() throws Exception {
      doThrow(ErrorEnum.CURRENT_PASSWORD_MISMATCH.toException())
          .when(accountService)
          .createPasskeyRegistrationOptions(ACCOUNT_NO, new Password("password01"));

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys/registration-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("createRegistrationOptions_success.json")))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.CURRENT_PASSWORD_MISMATCH.getErrorCode()));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：パスキーの登録数が上限に達している場合、400を返すこと")
    void createRegistrationOptions_reachedLimit() throws Exception {
      doThrow(ErrorEnum.REACHED_PASSKEY_LIMIT.toException())
          .when(accountService)
          .createPasskeyRegistrationOptions(ACCOUNT_NO, new Password("password01"));

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys/registration-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("createRegistrationOptions_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(ErrorEnum.REACHED_PASSKEY_LIMIT.getErrorMessage()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class registPasskey {
    private static final PasskeyResponseJson CREDENTIAL =
        new PasskeyResponseJson("{\"id\":\"credential-1\",\"type\":\"public-key\"}");

    @Test
    @Order(1)
    @DisplayName("正常系：前後の空白を除いたパスキー名で登録し、登録したパスキーを返すこと")
    void registPasskey_success() throws Exception {
      doReturn(passkey(5L, "MacBookのTouch ID"))
          .when(accountService)
          .registerPasskey(ACCOUNT_NO, CREDENTIAL, new PasskeyName("MacBookのTouch ID"));

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("registPasskey_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.passkeyNo").value(5))
          .andExpect(jsonPath("$.passkeyName").value("MacBookのTouch ID"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：登録レスポンスが空の場合、400を返すこと")
    void registPasskey_credential_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("registPasskey_badrequest_blank_credential.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registerPasskey(any(), any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：パスキー名が50文字を超える場合、400を返すこと")
    void registPasskey_name_tooLong() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("registPasskey_badrequest_name_too_long.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registerPasskey(any(), any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：本人以外のアカウントIDの場合、403を返すこと")
    void registPasskey_otherAccount() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + OTHER + "/passkeys")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("registPasskey_success.json")))
          .andExpect(status().isForbidden());

      verify(accountService, times(0)).registerPasskey(any(), any(), any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：登録レスポンスの検証に失敗した場合、400を返すこと")
    void registPasskey_invalid() throws Exception {
      doThrow(ErrorEnum.INVALID_PASSKEY_REGISTRATION.toException())
          .when(accountService)
          .registerPasskey(any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("registPasskey_success.json")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(ErrorEnum.INVALID_PASSKEY_REGISTRATION.getErrorMessage()));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：登録済みのパスキーの場合、409を返すこと")
    void registPasskey_duplicate() throws Exception {
      doThrow(ErrorEnum.DUPLICATE_PASSKEY.toException())
          .when(accountService)
          .registerPasskey(any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("registPasskey_success.json")))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_PASSKEY.getErrorCode()));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updatePasskey {
    @Test
    @Order(1)
    @DisplayName("正常系：パスキー名を変更すること")
    void updatePasskey_success() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/" + SELF + "/passkeys/5")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("updatePasskey_success.json")))
          .andExpect(status().isOk());

      verify(accountService, times(1))
          .renamePasskey(ACCOUNT_NO, new PasskeyNo(5L), new PasskeyName("iPhoneのFace ID"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：パスキー名が空白のみの場合、400を返すこと")
    void updatePasskey_name_blank() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/" + SELF + "/passkeys/5")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("updatePasskey_badrequest_blank_name.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).renamePasskey(any(), any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：本人以外のアカウントIDの場合、403を返すこと")
    void updatePasskey_otherAccount() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/" + OTHER + "/passkeys/5")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("updatePasskey_success.json")))
          .andExpect(status().isForbidden());

      verify(accountService, times(0)).renamePasskey(any(), any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：パスキーが存在しない（他人のパスキーを含む）場合、404を返すこと")
    void updatePasskey_notFound() throws Exception {
      doThrow(ErrorEnum.PASSKEY_NOT_FOUND.toException())
          .when(accountService)
          .renamePasskey(any(), any(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + SELF + "/passkeys/99")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("updatePasskey_success.json")))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.PASSKEY_NOT_FOUND.getErrorCode()));
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deletePasskey {
    @Test
    @Order(1)
    @DisplayName("正常系：現在のパスワードを渡してパスキーを削除すること")
    void deletePasskey_success() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys/5/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("deletePasskey_success.json")))
          .andExpect(status().isOk());

      verify(accountService, times(1))
          .deletePasskey(ACCOUNT_NO, new PasskeyNo(5L), new Password("password01"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：現在のパスワードが空の場合、400を返すこと")
    void deletePasskey_password_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys/5/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("deletePasskey_badrequest_blank_password.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).deletePasskey(any(), any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：本人以外のアカウントIDの場合、403を返すこと")
    void deletePasskey_otherAccount() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + OTHER + "/passkeys/5/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("deletePasskey_success.json")))
          .andExpect(status().isForbidden());

      verify(accountService, times(0)).deletePasskey(any(), any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：パスキーが存在しない場合、404を返すこと")
    void deletePasskey_notFound() throws Exception {
      doThrow(ErrorEnum.PASSKEY_NOT_FOUND.toException())
          .when(accountService)
          .deletePasskey(any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/passkeys/99/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("deletePasskey_success.json")))
          .andExpect(status().isNotFound());
    }
  }
}
