package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.entity.Account;
import com.web.gallery.enumuration.AuthorityEnum;
import com.web.gallery.enumuration.SexEnum;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.AccountModel;
import com.web.gallery.repository.impl.AccountRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class AccountRepositoryImplIntegrationTest {
	@Autowired
	private AccountRepositoryImpl accountRepositoryImpl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class getByAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが取得できた場合")
		void getByAccountNo_found() {
			AccountModel actual = accountRepositoryImpl.getByAccountNo(1L);

			assertEquals(1L, actual.getAccountNo());
			assertFalse(actual.getIsDeleted());
			assertEquals("aaaaaaaa", actual.getAccountId());
			assertEquals("AAAAAAAA", actual.getAccountName());
			assertEquals("$2a$10$password1", actual.getPassword());
			assertEquals(LocalDate.of(1991, 2, 14), actual.getBirthdate());
			assertEquals(SexEnum.NONE, actual.getSexKbn());
			assertEquals("none", actual.getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getResidentPrefectureKbnCode());
			assertEquals("", actual.getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getLastLoginDatetime());
			assertEquals(0, actual.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountNo_not_found() {
			AccountModel actual = accountRepositoryImpl.getByAccountNo(99L);
			assertNull(actual);
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class getByAccountId {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが取得できた場合")
		void getByAccountId_found() {
			AccountModel actual = accountRepositoryImpl.getByAccountId("aaaaaaaa");

			assertEquals(1L, actual.getAccountNo());
			assertFalse(actual.getIsDeleted());
			assertEquals("aaaaaaaa", actual.getAccountId());
			assertEquals("AAAAAAAA", actual.getAccountName());
			assertEquals("$2a$10$password1", actual.getPassword());
			assertEquals(LocalDate.of(1991, 2, 14), actual.getBirthdate());
			assertEquals(SexEnum.NONE, actual.getSexKbn());
			assertEquals("none", actual.getBirthplacePrefectureKbnCode());
			assertEquals("none", actual.getResidentPrefectureKbnCode());
			assertEquals("", actual.getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getLastLoginDatetime());
			assertEquals(0, actual.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountId_not_found() {
			AccountModel actual = accountRepositoryImpl.getByAccountId("zzzzzzzz");
			assertNull(actual);
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むAccountModelの登録")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/common/ResetAccountNoSeq.sql")
		void regist_contain_null_parameter() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId("zzzzzzzz")
					.accountName("ZZZZZZZZ")
					.password("zzzzzzzz")
					.build();
			
			accountRepositoryImpl.regist(accountModel);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='zzzzzzzz'", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1L, actualData.getFirst().getAccountNo());
			assertEquals(0L, actualData.getFirst().getCreatedBy());
			assertEquals(0L, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("zzzzzzzz", actualData.getFirst().getAccountId());
			assertEquals("ZZZZZZZZ", actualData.getFirst().getAccountName());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelの登録")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/common/ResetAccountNoSeq.sql")
		void regist_not_contain_null_parameter() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId("zzzzzzzz")
					.accountName("ZZZZZZZZ")
					.password("zzzzzzzz")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("Okinawa")
					.freeMemo("フリーメモ")
					.build();
			
			accountRepositoryImpl.regist(accountModel);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='zzzzzzzz'", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1L, actualData.getFirst().getAccountNo());
			assertEquals(0L, actualData.getFirst().getCreatedBy());
			assertEquals(0L, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("zzzzzzzz", actualData.getFirst().getAccountId());
			assertEquals("ZZZZZZZZ", actualData.getFirst().getAccountName());
			assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals("Hokkaido", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void regist_RegistFailureException() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("aaaaaaaa")
					.build();
			
			assertThrows(RegistFailureException.class, () -> accountRepositoryImpl.regist(accountModel));
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class update {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むAccountModelでの更新")
		void update_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1L)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.build();
			
			accountRepositoryImpl.update(accountModel);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='aaaaaaaa'", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1L, actualData.getFirst().getAccountNo());
			assertEquals(1L, actualData.getFirst().getCreatedBy());
			assertEquals(1L, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("aaaaaaaa", actualData.getFirst().getAccountId());
			assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password1", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
		void update_not_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1L)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("aaaaaaaa")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("Okinawa")
					.freeMemo("フリーメモ")
					.lastLoginDatetime(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.loginFailureCount(2)
					.build();
			
			accountRepositoryImpl.update(accountModel);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='aaaaaaaa'", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1L, actualData.getFirst().getAccountNo());
			assertEquals(1L, actualData.getFirst().getCreatedBy());
			assertEquals(1L, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("aaaaaaaa", actualData.getFirst().getAccountId());
			assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
			assertNotEquals("$2a$10$password1", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals("Hokkaido", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(2, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(13L)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.build();
			
			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.update(accountModel));
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class updateLoginFailureCount {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むAccountModelでの更新")
		void updateLoginFailureCount_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(8L)
					.build();
			
			accountRepositoryImpl.updateLoginFailureCount(accountModel);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=8", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(8L, actualData.getFirst().getAccountNo());
			assertEquals(8L, actualData.getFirst().getCreatedBy());
			assertEquals(8L, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("hhhhhhhh", actualData.getFirst().getAccountId());
			assertEquals("HHHHHHHH", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password8", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
		void updateLoginFailureCount_not_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1L)
					.lastLoginDatetime(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.loginFailureCount(2)
					.build();
			
			accountRepositoryImpl.updateLoginFailureCount(accountModel);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=1", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1L, actualData.getFirst().getAccountNo());
			assertEquals(1L, actualData.getFirst().getCreatedBy());
			assertEquals(1L, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("aaaaaaaa", actualData.getFirst().getAccountId());
			assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password1", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(2, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void updateLoginFailureCount_UpdateFailureException() {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(13L)
					.loginFailureCount(2)
					.build();
			
			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.updateLoginFailureCount(accountModel));
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class isExistAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void isExistAccount_true() {
			assertTrue(accountRepositoryImpl.isExistAccount(2L, "aaaaaaaa"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void isExistAccount_false() {
			assertFalse(accountRepositoryImpl.isExistAccount(1L, "zzzzzzzz"));
		}
	}
	
	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを2件以上取得")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void getAccountList_found_some_accounts() {
			List<AccountModel> actual = accountRepositoryImpl.getAccountList();
			assertEquals(11, actual.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが0件")
		void getAccountList_not_found() {
			List<AccountModel> actual = accountRepositoryImpl.getAccountList();
			assertEquals(0, actual.size());
		}
	}

	@Nested
	@Order(8)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを物理削除する")
		void delete_success() {
			accountRepositoryImpl.delete(1L);

			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=1", (rs, rowNum) ->
						Account.builder()
							.accountNo(rs.getLong("account_no"))
							.build());
			assertEquals(0, actualData.size());

			// 他のアカウントは残っていることを確認
			List<Account> otherData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=2", (rs, rowNum) ->
						Account.builder()
							.accountNo(rs.getLong("account_no"))
							.build());
			assertEquals(1, otherData.size());
		}
	}
}