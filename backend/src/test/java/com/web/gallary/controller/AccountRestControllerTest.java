package com.web.gallary.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.web.gallary.controller.request.ErrorRequest;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.enumuration.SexEnum;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.AccountModel;
import com.web.gallary.service.impl.AccountServiceImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountRestControllerTest {
	@InjectMocks
	private AccountRestController accountRestController;

	@Mock
	private AccountServiceImpl accountServiceImpl;

	@Mock
	private SessionHelper sessionHelper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

		mockMvc = MockMvcBuilders.standaloneSetup(accountRestController)
				.setMessageConverters(converter)
				.setControllerAdvice(new CommonRestControllerAdvice(sessionHelper))
				.build();
	}

	private String readJsonFile(String fileName) throws Exception {
		return new String(
				new ClassPathResource("json/controller/AccountRestControllerTest/" + fileName).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class register {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void register_regist_success() throws Exception {
			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doReturn(true).when(accountServiceImpl).registAccount(accountModelCaptor.capture());

			mockMvc.perform(post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("regist_success.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value(""));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertNull(accountModel.getAccountNo());
			assertEquals("aaaaaaaa", accountModel.getAccountId());
			assertEquals("AAAAAAAA", accountModel.getAccountName());
			assertEquals("password01", accountModel.getPassword());
			assertEquals(LocalDate.of(2000, 1, 1), accountModel.getBirthdate());
			assertEquals(SexEnum.WOMAN, accountModel.getSexKbn());
			assertEquals("Hokkaido", accountModel.getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", accountModel.getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNull(accountModel.getLastLoginDatetime());
			assertEquals(0, accountModel.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：既に使われているアカウントIDの場合")
		void register_exist_accountId() throws Exception {
			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doReturn(false).when(accountServiceImpl).registAccount(accountModelCaptor.capture());

			mockMvc.perform(post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("regist_success.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(""));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertNull(accountModel.getAccountNo());
			assertEquals("aaaaaaaa", accountModel.getAccountId());
			assertEquals("AAAAAAAA", accountModel.getAccountName());
			assertEquals("password01", accountModel.getPassword());
			assertEquals(LocalDate.of(2000, 1, 1), accountModel.getBirthdate());
			assertEquals(SexEnum.WOMAN, accountModel.getSexKbn());
			assertEquals("Hokkaido", accountModel.getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", accountModel.getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNull(accountModel.getLastLoginDatetime());
			assertEquals(0, accountModel.getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void account_setting_BadRequestException_accountId_is_blank() throws Exception {
			mockMvc.perform(post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("regist_badrequest_blank_accountid.json")))
				.andExpect(status().isBadRequest());

			verify(accountServiceImpl, times(0)).registAccount(any(AccountModel.class));
		}

		@Test
		@Order(4)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void account_setting_RegistFailureException() throws Exception {
			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doThrow(new RegistFailureException(ErrorEnum.FAIL_TO_REGIST_ACCOUNT)).when(accountServiceImpl).registAccount(accountModelCaptor.capture());

			mockMvc.perform(post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("regist_success.json")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.goBackPageUrl").value("/register"));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertNull(accountModel.getAccountNo());
			assertEquals("aaaaaaaa", accountModel.getAccountId());
			assertEquals("AAAAAAAA", accountModel.getAccountName());
			assertEquals("password01", accountModel.getPassword());
			assertEquals(LocalDate.of(2000, 1, 1), accountModel.getBirthdate());
			assertEquals(SexEnum.WOMAN, accountModel.getSexKbn());
			assertEquals("Hokkaido", accountModel.getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", accountModel.getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNull(accountModel.getLastLoginDatetime());
			assertEquals(0, accountModel.getLoginFailureCount());
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class update {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントID、パスワード変更なし")
		void update_not_change_accountID_and_password() throws Exception {
			String accountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(accountId).when(sessionHelper).getAccountId();

			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doReturn(false).when(accountServiceImpl).updateAccount(accountModelCaptor.capture());

			mockMvc.perform(put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_no_password_change.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(false))
				.andExpect(jsonPath("$.isPasswordChanged").value(false))
				.andExpect(jsonPath("$.message").value(""));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertEquals(accountId, accountModel.getAccountId());
			assertEquals("AAAAAAAA", accountModel.getAccountName());
			assertNull(accountModel.getPassword());
			assertEquals(LocalDate.of(2000, 1, 1), accountModel.getBirthdate());
			assertEquals(SexEnum.WOMAN, accountModel.getSexKbn());
			assertEquals("Hokkaido", accountModel.getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", accountModel.getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNull(accountModel.getLastLoginDatetime());
			assertNull(accountModel.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントID変更あり、パスワード変更なし")
		void update_change_accountID() throws Exception {
			String accountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doReturn(false).when(accountServiceImpl).updateAccount(accountModelCaptor.capture());

			mockMvc.perform(put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_no_password_change.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(true))
				.andExpect(jsonPath("$.isPasswordChanged").value(false))
				.andExpect(jsonPath("$.message").value(""));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertEquals(accountId, accountModel.getAccountId());
			assertEquals("AAAAAAAA", accountModel.getAccountName());
			assertNull(accountModel.getPassword());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：アカウントID変更なし、パスワード変更あり")
		void update_change_password() throws Exception {
			String accountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn(accountId).when(sessionHelper).getAccountId();

			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doReturn(false).when(accountServiceImpl).updateAccount(accountModelCaptor.capture());

			mockMvc.perform(put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_with_password_change.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(false))
				.andExpect(jsonPath("$.isPasswordChanged").value(true))
				.andExpect(jsonPath("$.message").value(""));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertEquals(accountId, accountModel.getAccountId());
			assertEquals("AAAAAAAA", accountModel.getAccountName());
			assertEquals("password01", accountModel.getPassword());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：アカウントID変更あり、パスワード変更あり")
		void update_change_accountId_and_password() throws Exception {
			String accountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doReturn(false).when(accountServiceImpl).updateAccount(accountModelCaptor.capture());

			mockMvc.perform(put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_with_password_change.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(true))
				.andExpect(jsonPath("$.isPasswordChanged").value(true))
				.andExpect(jsonPath("$.message").value(""));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertEquals(accountId, accountModel.getAccountId());
			assertEquals("password01", accountModel.getPassword());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：アカウントID重複")
		void update_duplicate_accountId() throws Exception {
			String accountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();
			doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doReturn(true).when(accountServiceImpl).updateAccount(accountModelCaptor.capture());

			mockMvc.perform(put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_no_password_change.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(true))
				.andExpect(jsonPath("$.isAccountIdChanged").value(true))
				.andExpect(jsonPath("$.isPasswordChanged").value(false))
				.andExpect(jsonPath("$.message").value(""));

			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertEquals(accountId, accountModel.getAccountId());
			assertNull(accountModel.getPassword());
		}

		@Test
		@Order(6)
		@DisplayName("異常系：パスワード変更なしで、パスワード以外のパラメータが不正")
		void update_BadRequestException_account_id() throws Exception {
			mockMvc.perform(put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_badrequest_blank_accountid_no_password.json")))
				.andExpect(status().isBadRequest());

			verify(sessionHelper, times(0)).getAccountNo();
			verify(accountServiceImpl, times(0)).updateAccount(any(AccountModel.class));
		}

		@Test
		@Order(7)
		@DisplayName("異常系：パスワード変更ありで不正でなく、パスワード以外のパラメータが不正")
		void update_BadRequestException_account_id_with_change_password() throws Exception {
			mockMvc.perform(put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_badrequest_blank_accountid_with_password.json")))
				.andExpect(status().isBadRequest());

			verify(sessionHelper, times(0)).getAccountNo();
			verify(accountServiceImpl, times(0)).updateAccount(any(AccountModel.class));
		}

		@Test
		@Order(8)
		@DisplayName("異常系：パスワード変更ありで、パスワードが不正")
		void update_BadRequestException_password() throws Exception {
			mockMvc.perform(put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_badrequest_short_password.json")))
				.andExpect(status().isBadRequest());

			verify(sessionHelper, times(0)).getAccountNo();
			verify(accountServiceImpl, times(0)).updateAccount(any(AccountModel.class));
		}

		@Test
		@Order(9)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() throws Exception {
			String accountId = "aaaaaaaa";

			doReturn(1).when(sessionHelper).getAccountNo();

			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doThrow(UpdateFailureException.class).when(accountServiceImpl).updateAccount(accountModelCaptor.capture());

			mockMvc.perform(put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_with_password_change.json")))
				.andExpect(status().isConflict());

			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertEquals(accountId, accountModel.getAccountId());
			assertEquals("AAAAAAAA", accountModel.getAccountName());
			assertEquals("password01", accountModel.getPassword());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleInsertFailedException {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handleInsertFailedException_success() {
			RegistFailureException exception = new RegistFailureException(ErrorEnum.INVALID_INPUT);

			ResponseEntity<ErrorRequest> actual
				= accountRestController.handleInsertFailedException(exception);

			assertEquals(HttpStatus.CONFLICT, actual.getStatusCode());
			assertEquals(HttpStatus.CONFLICT.value(), actual.getBody().getHttpStatus());
			assertEquals(ErrorEnum.INVALID_INPUT.getErrorMessage(), actual.getBody().getErrorMessage());
			assertEquals("/register", actual.getBody().getGoBackPageUrl());
		}
	}
}
