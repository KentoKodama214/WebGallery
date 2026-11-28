package com.web.gallery.presentation.controller.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.account.AccountModelList;
import com.web.gallery.application.model.account.AccountPageModel;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.SexEnum;
import com.web.gallery.domain.exception.RegistFailureException;
import com.web.gallery.domain.exception.UpdateFailureException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.BirthDate;
import com.web.gallery.domain.model.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.model.account.FreeMemo;
import com.web.gallery.domain.model.account.LoginFailureCount;
import com.web.gallery.domain.model.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import com.web.gallery.domain.model.auth.RelyingPartyName;
import com.web.gallery.infrastructure.security.SessionHelper;
import com.web.gallery.presentation.controller.common.CommonControllerAdvice;
import com.web.gallery.presentation.converter.account.AccountConverter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
public class AccountControllerTest {
  /** 登録レスポンス（json/controller/AccountControllerTest/regist_success.jsonと同じ値） */
  private static final PasskeyResponseJson REGIST_CREDENTIAL =
      new PasskeyResponseJson("{\"id\":\"credential-1\",\"type\":\"public-key\"}");

  @InjectMocks private AccountController accountController;

  @Mock private AccountService accountService;

  @Mock private SessionHelper sessionHelper;

  @Spy private AccountConverter accountConverter = new AccountConverter();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    JsonMapper jsonMapper = JsonMapper.builder().build();

    JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);

    mockMvc =
        MockMvcBuilders.standaloneSetup(accountController)
            .setMessageConverters(converter)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/AccountControllerTest/" + fileName)
            .getInputStream()
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント一覧を取得できること")
    void getAccountList_success() throws Exception {
      AccountModelList accountModels =
          AccountModelList.of(
              List.of(
                  AccountModel.builder()
                      .accountId(new AccountId("aaaaaaaa"))
                      .accountName(new AccountName("AAAAAAAA"))
                      .build(),
                  AccountModel.builder()
                      .accountId(new AccountId("bbbbbbbb"))
                      .accountName(new AccountName("BBBBBBBB"))
                      .build()));

      doReturn(AccountPageModel.of(accountModels, false))
          .when(accountService)
          .getAccountList(any());

      mockMvc
          .perform(get("/api/v1/accounts"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isLast").value(false))
          .andExpect(jsonPath("$.accountList[0].accountId").value("aaaaaaaa"))
          .andExpect(jsonPath("$.accountList[0].accountName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.accountList[1].accountId").value("bbbbbbbb"))
          .andExpect(jsonPath("$.accountList[1].accountName").value("BBBBBBBB"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが0件の場合は空リストを返すこと")
    void getAccountList_empty() throws Exception {
      doReturn(AccountPageModel.of(AccountModelList.empty(), true))
          .when(accountService)
          .getAccountList(any());

      mockMvc
          .perform(get("/api/v1/accounts"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isLast").value(true))
          .andExpect(jsonPath("$.accountList").isArray())
          .andExpect(jsonPath("$.accountList").isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：ページ番号が0以下。BadRequestExceptionをthrowする")
    void getAccountList_BadRequestException_pageNo_not_positive() throws Exception {
      mockMvc
          .perform(get("/api/v1/accounts").param("pageNo", "0"))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).getAccountList(any());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント情報を取得できること")
    void getAccount_success() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(accountId).when(sessionHelper).getAccountId();

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName("AAAAAAAA"))
              .birthdate(new BirthDate(LocalDate.of(2000, 1, 1)))
              .sexKbn(SexEnum.WOMAN)
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa"))
              .freeMemo(new FreeMemo("フリーメモ"))
              .build();

      doReturn(accountModel).when(accountService).getAccountById(new AccountId(accountId));

      mockMvc
          .perform(get("/api/v1/accounts/" + accountId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accountId").value(accountId))
          .andExpect(jsonPath("$.accountName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.birthdate").value("2000-01-01"))
          .andExpect(jsonPath("$.sexKbn").value("woman"))
          .andExpect(jsonPath("$.birthplacePrefectureKbnCode").value("Hokkaido"))
          .andExpect(jsonPath("$.residentPrefectureKbnCode").value("Okinawa"))
          .andExpect(jsonPath("$.freeMemo").value("フリーメモ"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：生年月日がMIN_LOCAL_DATEの場合はnullで返ること")
    void getAccount_birthdate_min() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(accountId).when(sessionHelper).getAccountId();

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId(accountId))
              .accountName(new AccountName("AAAAAAAA"))
              .birthdate(new BirthDate(Consts.MIN_LOCAL_DATE))
              .sexKbn(SexEnum.NONE)
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
              .freeMemo(new FreeMemo(""))
              .build();

      doReturn(accountModel).when(accountService).getAccountById(new AccountId(accountId));

      mockMvc
          .perform(get("/api/v1/accounts/" + accountId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accountId").value(accountId))
          .andExpect(jsonPath("$.accountName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.birthdate").isEmpty())
          .andExpect(jsonPath("$.sexKbn").value("none"))
          .andExpect(jsonPath("$.birthplacePrefectureKbnCode").value("none"))
          .andExpect(jsonPath("$.residentPrefectureKbnCode").value("none"))
          .andExpect(jsonPath("$.freeMemo").isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDの場合は403を返すこと")
    void getAccount_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc.perform(get("/api/v1/accounts/aaaaaaaa")).andExpect(status().isForbidden());

      verify(accountService, times(0)).getAccountById(any(AccountId.class));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class register {
    @Test
    @Order(1)
    @DisplayName("正常系")
    void register_regist_success() throws Exception {
      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(
              RecoveryCodeList.of(
                  List.of(
                      new RecoveryCode("ABCDEFGHJKMNPQRS"), new RecoveryCode("0123456789ABCDEF"))))
          .when(accountService)
          .registAccount(
              accountModelCaptor.capture(),
              eq(REGIST_CREDENTIAL),
              eq(new PasskeyName("MacBookのTouch ID")));

      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value(""))
          // リカバリーコードは4文字ごとにハイフンで区切った表示用の形で返す
          .andExpect(jsonPath("$.recoveryCodes[0]").value("ABCD-EFGH-JKMN-PQRS"))
          .andExpect(jsonPath("$.recoveryCodes[1]").value("0123-4567-89AB-CDEF"));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertNull(accountModel.getAccountNo());
      assertEquals("aaaaaaaa", accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertEquals(LocalDate.of(2000, 1, 1), accountModel.getBirthdate().value());
      assertEquals(SexEnum.WOMAN, accountModel.getSexKbn());
      assertEquals("Hokkaido", accountModel.getBirthplacePrefectureKbnCode().value());
      assertEquals("Okinawa", accountModel.getResidentPrefectureKbnCode().value());
      assertEquals("フリーメモ", accountModel.getFreeMemo().value());
      assertNull(accountModel.getAuthorityKbn());
      assertNull(accountModel.getLastLoginDatetime());
      assertEquals(new LoginFailureCount(0), accountModel.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：既に使われているアカウントIDの場合、409を返す")
    void register_exist_accountId() throws Exception {
      doThrow(ErrorEnum.DUPLICATE_ACCOUNT_ID.toException())
          .when(accountService)
          .registAccount(any(AccountModel.class), any(), any());

      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_ACCOUNT_ID.getErrorCode()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_accountId_is_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_blank_accountid.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：accountNameがDBカラム長（50文字）を超える場合、BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_accountName_too_long() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_accountname_too_long.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：accountIdが半角英数字以外を含む場合（@Patternパターン違反）、BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_accountId_pattern_violation() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_accountid_pattern.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：accountNameが全角スペースのみの場合（@Patternパターン違反）、BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_accountName_all_space() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_accountname_all_space.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(8)
    @DisplayName("異常系：birthdateが未来日付の場合（@Pastパターン違反）、BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_birthdate_future() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_birthdate_future.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(9)
    @DisplayName(
        "異常系：birthplacePrefectureKbnCodeが半角英数字以外を含む場合（@Patternパターン違反）、BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_birthplacePrefectureKbnCode_pattern_violation()
        throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_birthplaceprefecture_pattern.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(10)
    @DisplayName(
        "異常系：residentPrefectureKbnCodeがDBカラム長（20文字）を超える場合（@Sizeパターン違反）、BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_residentPrefectureKbnCode_too_long() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_residentprefecture_too_long.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(11)
    @DisplayName("異常系：freeMemoがDBカラム長（1000文字）を超える場合（@Sizeパターン違反）、BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_freeMemo_too_long() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_freememo_too_long.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class), any(), any());
    }

    @Test
    @Order(12)
    @DisplayName("異常系：RegistFailureExceptionをthrowする")
    void account_setting_RegistFailureException() throws Exception {
      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doThrow(new RegistFailureException(ErrorEnum.FAIL_TO_REGIST_ACCOUNT))
          .when(accountService)
          .registAccount(accountModelCaptor.capture(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isConflict());

      AccountModel accountModel = accountModelCaptor.getValue();
      assertNull(accountModel.getAccountNo());
      assertEquals("aaaaaaaa", accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertEquals(LocalDate.of(2000, 1, 1), accountModel.getBirthdate().value());
      assertEquals(SexEnum.WOMAN, accountModel.getSexKbn());
      assertEquals("Hokkaido", accountModel.getBirthplacePrefectureKbnCode().value());
      assertEquals("Okinawa", accountModel.getResidentPrefectureKbnCode().value());
      assertEquals("フリーメモ", accountModel.getFreeMemo().value());
      assertNull(accountModel.getAuthorityKbn());
      assertNull(accountModel.getLastLoginDatetime());
      assertEquals(new LoginFailureCount(0), accountModel.getLoginFailureCount());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントID変更なし")
    void update_not_change_accountID() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn(accountId).when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(false))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertEquals(LocalDate.of(2000, 1, 1), accountModel.getBirthdate().value());
      assertEquals(SexEnum.WOMAN, accountModel.getSexKbn());
      assertEquals("Hokkaido", accountModel.getBirthplacePrefectureKbnCode().value());
      assertEquals("Okinawa", accountModel.getResidentPrefectureKbnCode().value());
      assertEquals("フリーメモ", accountModel.getFreeMemo().value());
      assertNull(accountModel.getAuthorityKbn());
      assertNull(accountModel.getLastLoginDatetime());
      assertNull(accountModel.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントID変更あり")
    void update_change_accountID() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(true))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：アカウントID重複")
    void update_duplicate_accountId() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(true).when(accountService).updateAccount(accountModelCaptor.capture());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(true))
          .andExpect(jsonPath("$.isAccountIdChanged").value(true))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
    }

    @Test
    @Order(6)
    @DisplayName("正常系：パスワード関連のフィールドを含まないリクエストで更新できること")
    void update_no_password_field() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn(accountId).when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_field.json")))
          .andExpect(status().isOk());
      assertEquals(accountId, accountModelCaptor.getValue().getAccountId().value());
    }

    @Test
    @Order(7)
    @DisplayName("異常系：アカウントIDが空の場合、BadRequestExceptionをthrowする")
    void update_BadRequestException_account_id() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_badrequest_blank_accountid_no_password.json")))
          .andExpect(status().isBadRequest());

      verify(sessionHelper, times(0)).getAccountNo();
      verify(accountService, times(0)).updateAccount(any(AccountModel.class));
    }

    @Test
    @Order(10)
    @DisplayName("異常系：accountNameがDBカラム長（50文字）を超える場合、BadRequestExceptionをthrowする")
    void update_BadRequestException_accountName_too_long() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_badrequest_accountname_too_long.json")))
          .andExpect(status().isBadRequest());

      verify(sessionHelper, times(0)).getAccountNo();
      verify(accountService, times(0)).updateAccount(any(AccountModel.class));
    }

    @Test
    @Order(14)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void update_UpdateFailureException() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doThrow(UpdateFailureException.class)
          .when(accountService)
          .updateAccount(accountModelCaptor.capture());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_change.json")))
          .andExpect(status().isConflict());

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント削除に成功する")
    void deleteAccount_success() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(accountId).when(sessionHelper).getAccountId();
      doReturn(1L).when(sessionHelper).getAccountNo();
      doNothing()
          .when(accountService)
          .deleteAccount(
              eq(new AccountNo(1L)), eq(new AccountId(accountId)), any(PasskeyResponseJson.class));

      mockMvc
          .perform(
              post("/api/v1/accounts/" + accountId + "/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_success.json")))
          .andExpect(status().isOk());

      verify(accountService, times(1))
          .deleteAccount(
              eq(new AccountNo(1L)), eq(new AccountId(accountId)), any(PasskeyResponseJson.class));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：認証ユーザーと異なるアカウントIDの場合は403を返すこと")
    void deleteAccount_forbidden() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_success.json")))
          .andExpect(status().isForbidden());

      verify(accountService, times(0))
          .deleteAccount(
              any(AccountNo.class), any(AccountId.class), any(PasskeyResponseJson.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：本人確認の認証レスポンスが空の場合は400を返すこと")
    void deleteAccount_blank_credential() throws Exception {
      doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_badrequest_blank_credential.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0))
          .deleteAccount(
              any(AccountNo.class), any(AccountId.class), any(PasskeyResponseJson.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：本人確認の認証レスポンスが未指定の場合は400を返すこと")
    void deleteAccount_missing_credential() throws Exception {
      doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_badrequest_missing_credential.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0))
          .deleteAccount(
              any(AccountNo.class), any(AccountId.class), any(PasskeyResponseJson.class));
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class signupOptions {
    @Test
    @Order(1)
    @DisplayName("正常系：新規登録オプションをPublicKeyCredentialCreationOptionsJSONの形式で返す")
    void signupOptions_success() throws Exception {
      doReturn(
              PasskeyRegistrationOptionsModel.builder()
                  .challenge(new PasskeyChallenge("signup-challenge"))
                  .rpId(new RelyingPartyId("localhost"))
                  .rpName(new RelyingPartyName("WebGallery"))
                  .userHandle(new PasskeyUserHandle("handle-1"))
                  .accountId(new AccountId("aaaaaaaa"))
                  .accountName(new AccountName("AAAAAAAA"))
                  .excludeCredentialIds(List.of())
                  .timeoutMillis(new PasskeyTimeoutMillis(300_000L))
                  .build())
          .when(accountService)
          .createSignupOptions(new AccountId("aaaaaaaa"), new AccountName("AAAAAAAA"));

      mockMvc
          .perform(
              post("/api/v1/accounts/signup-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("signupOptions_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.challenge").value("signup-challenge"))
          .andExpect(jsonPath("$.user.id").value("handle-1"))
          .andExpect(jsonPath("$.user.name").value("aaaaaaaa"))
          .andExpect(jsonPath("$.user.displayName").value("AAAAAAAA"))
          .andExpect(jsonPath("$.authenticatorSelection.residentKey").value("required"))
          .andExpect(jsonPath("$.excludeCredentials").isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウントIDがパターン違反の場合、400を返す")
    void signupOptions_accountId_pattern_violation() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts/signup-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("signupOptions_badrequest_accountid_pattern.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).createSignupOptions(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：アカウントIDが既に使われている場合、409を返す")
    void signupOptions_exist_accountId() throws Exception {
      doThrow(ErrorEnum.DUPLICATE_ACCOUNT_ID.toException())
          .when(accountService)
          .createSignupOptions(any(), any());

      mockMvc
          .perform(
              post("/api/v1/accounts/signup-options")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("signupOptions_success.json")))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.errorCode").value(ErrorEnum.DUPLICATE_ACCOUNT_ID.getErrorCode()));
    }
  }
}
