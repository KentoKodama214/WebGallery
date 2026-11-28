package com.web.gallery.presentation.controller.account;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.RecoveryCode;
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
class RecoveryCodeControllerTest {
  private static final String SELF = "aaaaaaaa";
  private static final String OTHER = "bbbbbbbb";
  private static final AccountNo ACCOUNT_NO = new AccountNo(1L);

  /** 本人確認用の認証レスポンス（json/controller/RecoveryCodeControllerTest/*_success.jsonと同じ値） */
  private static final PasskeyResponseJson REAUTH_CREDENTIAL =
      new PasskeyResponseJson("{\"id\":\"credential-1\",\"type\":\"public-key\"}");

  @InjectMocks private RecoveryCodeController recoveryCodeController;

  @Mock private AccountService accountService;

  @Mock private SessionHelper sessionHelper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    lenient().doReturn(SELF).when(sessionHelper).getAccountId();
    lenient().doReturn(1L).when(sessionHelper).getAccountNo();
    mockMvc =
        MockMvcBuilders.standaloneSetup(recoveryCodeController)
            .setMessageConverters(new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/RecoveryCodeControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getRecoveryCodeStatus {
    @Test
    @Order(1)
    @DisplayName("正常系：未使用のリカバリーコードの件数と1回の発行数を返すこと（コード自体は返さない）")
    void getRecoveryCodeStatus_success() throws Exception {
      doReturn(7).when(accountService).getRecoveryCodeCount(ACCOUNT_NO);

      mockMvc
          .perform(get("/api/v1/accounts/" + SELF + "/recovery-codes"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.remainingCount").value(7))
          .andExpect(jsonPath("$.issueCount").value(10))
          .andExpect(jsonPath("$.recoveryCodes").doesNotExist());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：本人以外のアカウントIDの場合、403を返すこと")
    void getRecoveryCodeStatus_otherAccount() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts/" + OTHER + "/recovery-codes"))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode")
                  .value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.getErrorCode()));

      verify(accountService, times(0)).getRecoveryCodeCount(any());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class regenerateRecoveryCodes {
    @Test
    @Order(1)
    @DisplayName("正常系：本人確認の認証レスポンスを渡して再発行し、表示用の形でコードを返すこと")
    void regenerateRecoveryCodes_success() throws Exception {
      doReturn(
              RecoveryCodeList.of(
                  List.of(
                      new RecoveryCode("ABCDEFGHJKMNPQRS"), new RecoveryCode("0123456789ABCDEF"))))
          .when(accountService)
          .regenerateRecoveryCodes(ACCOUNT_NO, REAUTH_CREDENTIAL);

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/recovery-codes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regenerateRecoveryCodes_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.recoveryCodes[0]").value("ABCD-EFGH-JKMN-PQRS"))
          .andExpect(jsonPath("$.recoveryCodes[1]").value("0123-4567-89AB-CDEF"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：本人確認の認証レスポンスが空の場合、400を返すこと")
    void regenerateRecoveryCodes_credential_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/recovery-codes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      readJsonFile("regenerateRecoveryCodes_badrequest_blank_credential.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).regenerateRecoveryCodes(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：本人以外のアカウントIDの場合、403を返すこと")
    void regenerateRecoveryCodes_otherAccount() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/" + OTHER + "/recovery-codes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regenerateRecoveryCodes_success.json")))
          .andExpect(status().isForbidden());

      verify(accountService, times(0)).regenerateRecoveryCodes(any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：本人確認に失敗した場合、403を返すこと")
    void regenerateRecoveryCodes_reauthenticationFailed() throws Exception {
      doThrow(ErrorEnum.REAUTHENTICATION_FAILED.toException())
          .when(accountService)
          .regenerateRecoveryCodes(ACCOUNT_NO, REAUTH_CREDENTIAL);

      mockMvc
          .perform(
              post("/api/v1/accounts/" + SELF + "/recovery-codes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regenerateRecoveryCodes_success.json")))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.errorCode").value(ErrorEnum.REAUTHENTICATION_FAILED.getErrorCode()));
    }
  }
}
