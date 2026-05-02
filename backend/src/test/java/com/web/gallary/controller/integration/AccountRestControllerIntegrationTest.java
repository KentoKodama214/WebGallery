package com.web.gallary.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.AccountPrincipal;
import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.enumuration.SexEnum;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AccountRestControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String readJsonFile(String fileName) throws Exception {
		return new String(
				new ClassPathResource("json/controller/integration/AccountRestControllerIntegrationTest/" + fileName).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountRestControllerIntegrationTest.sql")
	class register {
		private List<Account> getAccountList(String accountId) {
			return jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='" + accountId + "'", (rs, rowNum) ->
					Account.builder()
						.accountNo(rs.getInt("account_no"))
						.createdBy(rs.getInt("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getInt("updated_by"))
						.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
						.isDeleted(rs.getBoolean("is_deleted"))
						.accountId(rs.getString("account_id"))
						.accountName(rs.getString("account_name"))
						.password(rs.getString("password"))
						.birthdate(rs.getObject("birthdate", LocalDate.class))
						.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
						.birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
						.residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
						.freeMemo(rs.getString("free_memo"))
						.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
						.lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
						.loginFailureCount(rs.getInt("login_failure_count"))
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

			mockMvc.perform(
					post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("register_regist_success.json"))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value(""));

			List<Account> actualData = getAccountList(accountId);

			assertEquals(1, actualData.size());
			assertEquals(4, actualData.getFirst().getAccountNo());
			assertEquals(0, actualData.getFirst().getCreatedBy());
			assertEquals(0, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals(accountId, actualData.getFirst().getAccountId());
			assertEquals(accountName, actualData.getFirst().getAccountName());
			assertEquals(birthDate, actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals(birthplacePrefectureKbnCode, actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(residentPrefectureKbnCode, actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(freeMemo, actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：既に使われているアカウントIDの場合")
		void register_exist_accountId() throws Exception {
			String accountId = "aaaaaaaa";

			mockMvc.perform(
					post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("register_exist_accountid.json"))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(""));

			List<Account> actualData = getAccountList(accountId);
			assertEquals(1, actualData.size());
			assertEquals(accountId, actualData.getFirst().getAccountId());
			assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void account_setting_BadRequestException_accountId_is_blank() throws Exception {
			mockMvc.perform(
					post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("register_badrequest_blank_accountid.json"))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.value()))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountRestControllerIntegrationTest.sql")
	class update {
		private List<Account> getAccountList(String accountId) {
			return jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='" + accountId + "'", (rs, rowNum) ->
					Account.builder()
						.accountNo(rs.getInt("account_no"))
						.createdBy(rs.getInt("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getInt("updated_by"))
						.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
						.isDeleted(rs.getBoolean("is_deleted"))
						.accountId(rs.getString("account_id"))
						.accountName(rs.getString("account_name"))
						.password(rs.getString("password"))
						.birthdate(rs.getObject("birthdate", LocalDate.class))
						.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
						.birthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code"))
						.residentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code"))
						.freeMemo(rs.getString("free_memo"))
						.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
						.lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
						.loginFailureCount(rs.getInt("login_failure_count"))
						.build());
		}

		@Test
		@Order(1)
		@DisplayName("正常系：アカウントID、パスワード変更なし")
		void update_not_change_accountID_and_password() throws Exception {
			String accountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(accountId)
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_not_change_accountid_and_password.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(false))
				.andExpect(jsonPath("$.isPasswordChanged").value(false))
				.andExpect(jsonPath("$.message").value(""));

			List<Account> actual = getAccountList(accountId);
			assertEquals(1, actual.size());
			assertEquals(1, actual.getFirst().getAccountNo());
			assertEquals(1, actual.getFirst().getCreatedBy());
			assertNotEquals(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1, actual.getFirst().getUpdatedBy());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertEquals("$2a$10$password1", actual.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントID変更あり、パスワード変更なし")
		void update_change_accountID() throws Exception {
			String accountId = "aaaaaaaab";
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_change_accountid.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(true))
				.andExpect(jsonPath("$.isPasswordChanged").value(false))
				.andExpect(jsonPath("$.message").value(""));

			List<Account> actual = getAccountList(accountId);
			assertEquals(1, actual.size());
			assertEquals(1, actual.getFirst().getAccountNo());
			assertEquals(1, actual.getFirst().getCreatedBy());
			assertNotEquals(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1, actual.getFirst().getUpdatedBy());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertEquals("$2a$10$password1", actual.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：アカウントID変更なし、パスワード変更あり")
		void update_change_password() throws Exception {
			String accountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(accountId)
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_change_password.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(false))
				.andExpect(jsonPath("$.isPasswordChanged").value(true))
				.andExpect(jsonPath("$.message").value(""));

			List<Account> actual = getAccountList(accountId);
			assertEquals(1, actual.size());
			assertEquals(1, actual.getFirst().getAccountNo());
			assertEquals(1, actual.getFirst().getCreatedBy());
			assertNotEquals(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1, actual.getFirst().getUpdatedBy());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertNotEquals("$2a$10$password1", actual.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：アカウントID変更あり、パスワード変更あり")
		void update_change_accountId_and_password() throws Exception {
			String accountId = "aaaaaaaab";
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_change_accountid_and_password.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(false))
				.andExpect(jsonPath("$.isAccountIdChanged").value(true))
				.andExpect(jsonPath("$.isPasswordChanged").value(true))
				.andExpect(jsonPath("$.message").value(""));

			List<Account> actual = getAccountList(accountId);
			assertEquals(1, actual.size());
			assertEquals(1, actual.getFirst().getAccountNo());
			assertEquals(1, actual.getFirst().getCreatedBy());
			assertNotEquals(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1, actual.getFirst().getUpdatedBy());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertNotEquals("$2a$10$password1", actual.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：アカウントID重複")
		void update_duplicate_accountId() throws Exception {
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_duplicate_accountid.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.isDuplicateAccountId").value(true))
				.andExpect(jsonPath("$.isAccountIdChanged").value(true))
				.andExpect(jsonPath("$.isPasswordChanged").value(false))
				.andExpect(jsonPath("$.message").value(""));
		}

		@Test
		@Order(6)
		@DisplayName("異常系：パスワード変更なしで、パスワード以外のパラメータが不正")
		void update_BadRequestException_account_id() throws Exception {
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_badrequest_account_id.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest());
		}

		@Test
		@Order(7)
		@DisplayName("異常系：パスワード変更ありで不正でなく、パスワード以外のパラメータが不正")
		void update_BadRequestException_account_id_with_change_password() throws Exception {
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/aaaaaaaa")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_badrequest_account_id_with_change_password.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest());
		}

		@Test
		@Order(8)
		@DisplayName("異常系：パスワード変更ありで、パスワードが不正")
		void update_BadRequestException_password() throws Exception {
			String accountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_badrequest_password.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isBadRequest());
		}

		@Test
		@Order(9)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() throws Exception {
			String accountId = "zzzzzzzz";
			String accountName = "AAAAAAAA";

			Account sessionAccount = Account.builder()
					.accountNo(9)
					.accountId(accountId)
					.accountName(accountName)
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);

			mockMvc.perform(
					put("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("update_update_failure.json"))
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.CONFLICT.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/zzzzzzzz/photo_list"));
		}
	}
}
