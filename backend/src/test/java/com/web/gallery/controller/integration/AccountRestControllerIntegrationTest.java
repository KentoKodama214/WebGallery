package com.web.gallery.controller.integration;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

import com.web.gallery.AccountPrincipal;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.entity.Account;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountModel;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AccountRestControllerIntegrationTest {
	/** account_no=1（aaaaaaaa）のフィクスチャのパスワード平文と、そのBCryptハッシュ */
	private static final String ACCOUNT1_PASSWORD = "password01";
	private static final String ACCOUNT1_PASSWORD_HASH =
			"$2a$10$k19cLX6F2brrOLYp74GstejFJfeGjm52TMk..ELwoJeTcBcUnBVM2";

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
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント一覧を取得できる")
		void getAccountList_success() throws Exception {
			mockMvc.perform(
					get("/api/v1/accounts")
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(true))
				.andExpect(jsonPath("$.accountList.length()").value(3))
				.andExpect(jsonPath("$.accountList[0].accountId").value("aaaaaaaa"))
				.andExpect(jsonPath("$.accountList[0].accountName").value("AAAAAAAA"))
				.andExpect(jsonPath("$.accountList[1].accountId").value("bbbbbbbb"))
				.andExpect(jsonPath("$.accountList[1].accountName").value("BBBBBBBB"))
				.andExpect(jsonPath("$.accountList[2].accountId").value("cccccccc"))
				.andExpect(jsonPath("$.accountList[2].accountName").value("CCCCCCCC"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが0件の場合")
		@Sql("/sql/common/cleanup.sql")
		void getAccountList_empty() throws Exception {
			mockMvc.perform(
					get("/api/v1/accounts")
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.isLast").value(true))
				.andExpect(jsonPath("$.accountList.length()").value(0));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountRestControllerIntegrationTest.sql")
	class getAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：自分のアカウント詳細を取得できる")
		void getAccount_success() throws Exception {
			String accountId = "aaaaaaaa";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(accountId))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					get("/api/v1/accounts/" + accountId)
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.accountId").value("aaaaaaaa"))
				.andExpect(jsonPath("$.accountName").value("AAAAAAAA"))
				.andExpect(jsonPath("$.birthdate").value("1991-02-14"))
				.andExpect(jsonPath("$.sexKbn").value("none"))
				.andExpect(jsonPath("$.birthplacePrefectureKbnCode").value("none"))
				.andExpect(jsonPath("$.residentPrefectureKbnCode").value("none"))
				.andExpect(jsonPath("$.freeMemo").value(""));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：生年月日が最小日付の場合はnullで返却される")
		void getAccount_birthdate_min_date() throws Exception {
			String accountId = "bbbbbbbb";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId(accountId))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					get("/api/v1/accounts/" + accountId)
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.accountId").value("bbbbbbbb"))
				.andExpect(jsonPath("$.accountName").value("BBBBBBBB"))
				.andExpect(jsonPath("$.birthdate").isEmpty())
				.andExpect(jsonPath("$.sexKbn").value("man"))
				.andExpect(jsonPath("$.birthplacePrefectureKbnCode").value("none"))
				.andExpect(jsonPath("$.residentPrefectureKbnCode").value("none"))
				.andExpect(jsonPath("$.freeMemo").value(""));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：他人のアカウントIDを指定した場合はForbidden")
		void getAccount_forbidden() throws Exception {
			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					get("/api/v1/accounts/bbbbbbbb")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.value()))
				.andExpect(jsonPath("$.errorCode").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.getErrorCode()))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.NOT_AUTHORIZED_TO_EDIT_ACCOUNT.getErrorMessage()));
		}

		@Test
		@Order(4)
		@DisplayName("異常系：未認証の場合は403ではなく401で共通JSONエラーを返す")
		void getAccount_unauthenticated() throws Exception {
			mockMvc.perform(
					get("/api/v1/accounts/aaaaaaaa")
				)
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.httpStatus").value(HttpStatus.UNAUTHORIZED.value()))
				.andExpect(jsonPath("$.errorCode").value("E-A-0002"))
				.andExpect(jsonPath("$.errorMessage").isNotEmpty());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountRestControllerIntegrationTest.sql")
	@Sql("/sql/common/PrefectureKbnMst.sql")
	class register {
		private List<Account> getAccountList(String accountId) {
			return jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='" + accountId + "'", (rs, rowNum) ->
					Account.builder()
						.accountNo(rs.getLong("account_no"))
						.createdBy(rs.getLong("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getLong("updated_by"))
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

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(4L, actualData.getFirst().getAccountNo());
			assertEquals(0L, actualData.getFirst().getCreatedBy());
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
			assertEquals(0L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
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

		@Test
		@Order(4)
		@DisplayName("異常系：区分マスタに存在しない都道府県コードの場合、400を返す")
		void register_badrequest_invalid_prefecture() throws Exception {
			mockMvc.perform(
					post("/api/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("register_badrequest_invalid_prefecture.json"))
					.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));

			assertEquals(0, getAccountList("dddddddd").size());
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountRestControllerIntegrationTest.sql")
	class update {
		private List<Account> getAccountList(String accountId) {
			return jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='" + accountId + "'", (rs, rowNum) ->
					Account.builder()
						.accountNo(rs.getLong("account_no"))
						.createdBy(rs.getLong("created_by"))
						.createdAt(rs.getObject("created_at", OffsetDateTime.class))
						.updatedBy(rs.getLong("updated_by"))
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

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(accountId))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(1L, actual.getFirst().getAccountNo());
			assertEquals(1L, actual.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1L, actual.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actual.getFirst().getUpdatedAt());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertEquals(ACCOUNT1_PASSWORD_HASH, actual.getFirst().getPassword());
			// プロフィール更新のリクエストに含まれない項目は「変更なし」としてフィクスチャ値を維持する
			assertEquals(LocalDate.of(1991, 2, 14), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime());
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントID変更あり、パスワード変更なし")
		void update_change_accountID() throws Exception {
			String accountId = "aaaaaaaab";
			String accountName = "AAAAAAAA";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(1L, actual.getFirst().getAccountNo());
			assertEquals(1L, actual.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1L, actual.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actual.getFirst().getUpdatedAt());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertEquals(ACCOUNT1_PASSWORD_HASH, actual.getFirst().getPassword());
			// プロフィール更新のリクエストに含まれない項目は「変更なし」としてフィクスチャ値を維持する
			assertEquals(LocalDate.of(1991, 2, 14), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime());
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：アカウントID変更なし、パスワード変更あり")
		void update_change_password() throws Exception {
			String accountId = "aaaaaaaa";
			String accountName = "AAAAAAAA";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(accountId))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(1L, actual.getFirst().getAccountNo());
			assertEquals(1L, actual.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1L, actual.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actual.getFirst().getUpdatedAt());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertNotEquals(ACCOUNT1_PASSWORD_HASH, actual.getFirst().getPassword());
			// プロフィール更新のリクエストに含まれない項目は「変更なし」としてフィクスチャ値を維持する
			assertEquals(LocalDate.of(1991, 2, 14), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime());
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：アカウントID変更あり、パスワード変更あり")
		void update_change_accountId_and_password() throws Exception {
			String accountId = "aaaaaaaab";
			String accountName = "AAAAAAAA";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(1L, actual.getFirst().getAccountNo());
			assertEquals(1L, actual.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getCreatedAt());
			assertEquals(1L, actual.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actual.getFirst().getUpdatedAt());
			assertFalse(actual.getFirst().getIsDeleted());
			assertEquals(accountId, actual.getFirst().getAccountId());
			assertEquals(accountName, actual.getFirst().getAccountName());
			assertNotEquals(ACCOUNT1_PASSWORD_HASH, actual.getFirst().getPassword());
			// プロフィール更新のリクエストに含まれない項目は「変更なし」としてフィクスチャ値を維持する
			assertEquals(LocalDate.of(1991, 2, 14), actual.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actual.getFirst().getSexKbn());
			assertEquals("none", actual.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actual.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getFirst().getLastLoginDatetime());
			assertEquals(0, actual.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：アカウントID重複")
		void update_duplicate_accountId() throws Exception {
			String accountName = "AAAAAAAA";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

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

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

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

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

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

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

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

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(9L))
					.accountId(new AccountId(accountId))
					.accountName(new AccountName(accountName))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());

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
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.getErrorMessage()));
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/controller/AccountRestControllerDeleteAccountIntegrationTest.sql")
	class deleteAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント削除に成功する")
		void deleteAccount_success() throws Exception {
			String accountId = "aaaaaaaa";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId(accountId))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					delete("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"currentPassword\":\"" + ACCOUNT1_PASSWORD + "\"}")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isOk());

			// アカウントが削除されたことを確認
			Integer accountCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
			assertEquals(0, accountCount);

			// 写真マスタが削除されたことを確認
			Integer photoMstCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
			assertEquals(0, photoMstCount);

			// 写真タグが削除されたことを確認
			Integer photoTagCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_tag_mst where account_no=1", Integer.class);
			assertEquals(0, photoTagCount);

			// お気に入りが削除されたことを確認（自分が登録したもの＋自分の写真に対する他人のもの）
			Integer favoriteCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_favorite where account_no=1 or favorite_photo_account_no=1", Integer.class);
			assertEquals(0, favoriteCount);

			// account_no=2は残っていること
			Integer otherAccountCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.account where account_no=2", Integer.class);
			assertEquals(1, otherAccountCount);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：認証ユーザーと異なるアカウントIDの場合は403を返すこと")
		void deleteAccount_forbidden() throws Exception {
			String accountId = "aaaaaaaa";

			AccountModel sessionAccount = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();

			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					accountPrincipal, null, accountPrincipal.getAuthorities());

			mockMvc.perform(
					delete("/api/v1/accounts/" + accountId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"currentPassword\":\"" + ACCOUNT1_PASSWORD + "\"}")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().isForbidden());

			// アカウントは削除されていないことを確認
			Integer accountCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
			assertEquals(1, accountCount);
		}
	}
}
