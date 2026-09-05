package com.web.gallery.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.helper.SessionHelper;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
import com.web.gallery.model.AccountPageModel;
import com.web.gallery.service.AccountService;
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
public class AccountRestControllerTest {
  @InjectMocks private AccountRestController accountRestController;

  @Mock private AccountService accountService;

  @Mock private SessionHelper sessionHelper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    JsonMapper jsonMapper = JsonMapper.builder().build();

    JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);

    mockMvc =
        MockMvcBuilders.standaloneSetup(accountRestController)
            .setMessageConverters(converter)
            .setControllerAdvice(new CommonRestControllerAdvice())
            .build();
  }

  private String readJsonFile(String fileName) throws Exception {
    return new String(
        new ClassPathResource("json/controller/AccountRestControllerTest/" + fileName)
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
      doReturn(true).when(accountService).registAccount(accountModelCaptor.capture());

      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertNull(accountModel.getAccountNo());
      assertEquals("aaaaaaaa", accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertEquals("password01", accountModel.getPassword().value());
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
    @DisplayName("正常系：既に使われているアカウントIDの場合")
    void register_exist_accountId() throws Exception {
      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).registAccount(accountModelCaptor.capture());

      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_success.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isSuccess").value(false))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertNull(accountModel.getAccountNo());
      assertEquals("aaaaaaaa", accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertEquals("password01", accountModel.getPassword().value());
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
    @Order(3)
    @DisplayName("異常系：BadRequestExceptionをthrowする")
    void account_setting_BadRequestException_accountId_is_blank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/accounts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("regist_badrequest_blank_accountid.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0)).registAccount(any(AccountModel.class));
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

      verify(accountService, times(0)).registAccount(any(AccountModel.class));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：RegistFailureExceptionをthrowする")
    void account_setting_RegistFailureException() throws Exception {
      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doThrow(new RegistFailureException(ErrorEnum.FAIL_TO_REGIST_ACCOUNT))
          .when(accountService)
          .registAccount(accountModelCaptor.capture());

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
      assertEquals("password01", accountModel.getPassword().value());
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
    @DisplayName("正常系：アカウントID、パスワード変更なし")
    void update_not_change_accountID_and_password() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn(accountId).when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(false))
          .andExpect(jsonPath("$.isPasswordChanged").value(false))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertNull(accountModel.getPassword());
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
    @DisplayName("正常系：アカウントID変更あり、パスワード変更なし")
    void update_change_accountID() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(true))
          .andExpect(jsonPath("$.isPasswordChanged").value(false))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertNull(accountModel.getPassword());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウントID変更なし、パスワード変更あり")
    void update_change_password() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn(accountId).when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_with_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(false))
          .andExpect(jsonPath("$.isPasswordChanged").value(true))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertEquals("password01", accountModel.getPassword().value());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：アカウントID変更あり、パスワード変更あり")
    void update_change_accountId_and_password() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_with_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(false))
          .andExpect(jsonPath("$.isAccountIdChanged").value(true))
          .andExpect(jsonPath("$.isPasswordChanged").value(true))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("password01", accountModel.getPassword().value());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：アカウントID重複")
    void update_duplicate_accountId() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(true).when(accountService).updateAccount(accountModelCaptor.capture(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_change.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.httpStatus").value(200))
          .andExpect(jsonPath("$.isDuplicateAccountId").value(true))
          .andExpect(jsonPath("$.isAccountIdChanged").value(true))
          .andExpect(jsonPath("$.isPasswordChanged").value(false))
          .andExpect(jsonPath("$.message").value(""));

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertNull(accountModel.getPassword());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：パスワード変更なしで、パスワード以外のパラメータが不正")
    void update_BadRequestException_account_id() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_badrequest_blank_accountid_no_password.json")))
          .andExpect(status().isBadRequest());

      verify(sessionHelper, times(0)).getAccountNo();
      verify(accountService, times(0)).updateAccount(any(AccountModel.class), any());
    }

    @Test
    @Order(7)
    @DisplayName("異常系：パスワード変更ありで不正でなく、パスワード以外のパラメータが不正")
    void update_BadRequestException_account_id_with_change_password() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_badrequest_blank_accountid_with_password.json")))
          .andExpect(status().isBadRequest());

      verify(sessionHelper, times(0)).getAccountNo();
      verify(accountService, times(0)).updateAccount(any(AccountModel.class), any());
    }

    @Test
    @Order(8)
    @DisplayName("異常系：パスワード変更ありで、パスワードが不正")
    void update_BadRequestException_password() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_badrequest_short_password.json")))
          .andExpect(status().isBadRequest());

      verify(sessionHelper, times(0)).getAccountNo();
      verify(accountService, times(0)).updateAccount(any(AccountModel.class), any());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：accountNameがDBカラム長（50文字）を超える場合、BadRequestExceptionをthrowする")
    void update_BadRequestException_accountName_too_long() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/accounts/aaaaaaaa")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_badrequest_accountname_too_long.json")))
          .andExpect(status().isBadRequest());

      verify(sessionHelper, times(0)).getAccountNo();
      verify(accountService, times(0)).updateAccount(any(AccountModel.class), any());
    }

    @Test
    @Order(10)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void update_UpdateFailureException() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doThrow(UpdateFailureException.class)
          .when(accountService)
          .updateAccount(accountModelCaptor.capture(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_with_password_change.json")))
          .andExpect(status().isConflict());

      AccountModel accountModel = accountModelCaptor.getValue();
      assertEquals(1L, accountModel.getAccountNo().value());
      assertEquals(accountId, accountModel.getAccountId().value());
      assertEquals("AAAAAAAA", accountModel.getAccountName().value());
      assertEquals("password01", accountModel.getPassword().value());
    }

    @Test
    @Order(11)
    @DisplayName("正常系：newPasswordフィールド自体を省略（null）した場合も、パスワード変更なしとして扱われること")
    void update_no_password_field() throws Exception {
      String accountId = "aaaaaaaa";

      doReturn(1L).when(sessionHelper).getAccountNo();
      doReturn(accountId).when(sessionHelper).getAccountId();

      ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
      doReturn(false).when(accountService).updateAccount(accountModelCaptor.capture(), any());

      mockMvc
          .perform(
              put("/api/v1/accounts/" + accountId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("update_no_password_field.json")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isPasswordChanged").value(false));

      assertNull(accountModelCaptor.getValue().getPassword());
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
          .deleteAccount(eq(new AccountNo(1L)), eq(new AccountId(accountId)), any(Password.class));

      mockMvc
          .perform(
              post("/api/v1/accounts/" + accountId + "/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_success.json")))
          .andExpect(status().isOk());

      verify(accountService, times(1))
          .deleteAccount(eq(new AccountNo(1L)), eq(new AccountId(accountId)), any(Password.class));
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
          .deleteAccount(any(AccountNo.class), any(AccountId.class), any(Password.class));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：現在のパスワードが空の場合は400を返すこと")
    void deleteAccount_blank_currentPassword() throws Exception {
      doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_badrequest_blank_current_password.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0))
          .deleteAccount(any(AccountNo.class), any(AccountId.class), any(Password.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：現在のパスワードが未指定の場合は400を返すこと")
    void deleteAccount_missing_currentPassword() throws Exception {
      doReturn("aaaaaaaa").when(sessionHelper).getAccountId();

      mockMvc
          .perform(
              post("/api/v1/accounts/aaaaaaaa/deletion")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(readJsonFile("delete_badrequest_missing_current_password.json")))
          .andExpect(status().isBadRequest());

      verify(accountService, times(0))
          .deleteAccount(any(AccountNo.class), any(AccountId.class), any(Password.class));
    }
  }
}
