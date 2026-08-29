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

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.entity.Account;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.AccountGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
import com.web.gallery.model.AccountPageModel;
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
			AccountModel actual = accountRepositoryImpl.getByAccountNo(new AccountNo(1L));

			assertEquals(new AccountNo(1L), actual.getAccountNo());
			assertEquals(new IsDeleted(false), actual.getIsDeleted());
			assertEquals(new AccountId("aaaaaaaa"), actual.getAccountId());
			assertEquals(new AccountName("AAAAAAAA"), actual.getAccountName());
			assertEquals(new Password("$2a$10$password1"), actual.getPassword());
			assertEquals(new BirthDate(LocalDate.of(1991, 2, 14)), actual.getBirthdate());
			assertEquals(SexEnum.NONE, actual.getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actual.getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actual.getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actual.getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actual.getLastLoginDatetime());
			assertEquals(new LoginFailureCount(0), actual.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountNo_not_found() {
			AccountModel actual = accountRepositoryImpl.getByAccountNo(new AccountNo(99L));
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
			AccountModel actual = accountRepositoryImpl.getByAccountId(new AccountId("aaaaaaaa"));

			assertEquals(new AccountNo(1L), actual.getAccountNo());
			assertEquals(new IsDeleted(false), actual.getIsDeleted());
			assertEquals(new AccountId("aaaaaaaa"), actual.getAccountId());
			assertEquals(new AccountName("AAAAAAAA"), actual.getAccountName());
			assertEquals(new Password("$2a$10$password1"), actual.getPassword());
			assertEquals(new BirthDate(LocalDate.of(1991, 2, 14)), actual.getBirthdate());
			assertEquals(SexEnum.NONE, actual.getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actual.getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actual.getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actual.getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actual.getLastLoginDatetime());
			assertEquals(new LoginFailureCount(0), actual.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountId_not_found() {
			AccountModel actual = accountRepositoryImpl.getByAccountId(new AccountId("zzzzzzzz"));
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
		void regist_contain_null_parameter() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountId(new AccountId("zzzzzzzz"))
					.accountName(new AccountName("ZZZZZZZZ"))
					.password(new Password("zzzzzzzz"))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
			assertEquals(0L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
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
		void regist_not_contain_null_parameter() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountId(new AccountId("zzzzzzzz"))
					.accountName(new AccountName("ZZZZZZZZ"))
					.password(new Password("zzzzzzzz"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa"))
					.freeMemo(new FreeMemo("フリーメモ"))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
			assertEquals(0L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
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
		void regist_RegistFailureException() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("aaaaaaaa"))
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
		void update_contain_null_parameter() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(1L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
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
		void update_not_contain_null_parameter() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("aaaaaaaa"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa"))
					.freeMemo(new FreeMemo("フリーメモ"))
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
					.loginFailureCount(new LoginFailureCount(2))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(1L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
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
					.accountNo(new AccountNo(13L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.build();

			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.update(accountModel));
		}

		@Test
		@Order(4)
		@DisplayName("異常系：account_idの一意制約違反（DuplicateKeyException）発生時にUpdateFailureExceptionをthrowする")
		void update_DuplicateKeyException() {
			// account_no=2を、既に別アカウント（account_no=1）が使用しているaccount_idに更新しようとする
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(2L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("BBBBBBBB"))
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
		void updateLoginFailureCount_contain_null_parameter() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(8L))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(8L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
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
		void updateLoginFailureCount_not_contain_null_parameter() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
					.loginFailureCount(new LoginFailureCount(2))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
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
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(1L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
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
					.accountNo(new AccountNo(13L))
					.loginFailureCount(new LoginFailureCount(2))
					.build();
			
			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.updateLoginFailureCount(accountModel));
		}
	}

	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class incrementLoginFailureCount {
		@Test
		@Order(1)
		@DisplayName("正常系：SQL側で原子的にインクリメントすること")
		void incrementLoginFailureCount_success() throws GalleryException {
			accountRepositoryImpl.incrementLoginFailureCount(new AccountNo(1L));

			Integer actual = jdbcTemplate.queryForObject(
					"SELECT login_failure_count FROM common.account WHERE account_no=1", Integer.class);
			assertEquals(1, actual);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void incrementLoginFailureCount_UpdateFailureException() {
			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.incrementLoginFailureCount(new AccountNo(13L)));
		}
	}

	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class isExistAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void isExistAccount_true() {
			assertTrue(accountRepositoryImpl.isExistAccount(new AccountNo(2L), new AccountId("aaaaaaaa")));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void isExistAccount_false() {
			assertFalse(accountRepositoryImpl.isExistAccount(new AccountNo(1L), new AccountId("zzzzzzzz")));
		}
	}
	
	@Nested
	@Order(8)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：limitを十分大きくした場合、アカウントを全件取得できること")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void getAccountList_found_some_accounts() {
			AccountGetModel accountGetModel = AccountGetModel.builder().limit(100).offset(0).build();
			AccountPageModel actual = accountRepositoryImpl.getAccountList(accountGetModel);
			assertEquals(11, actual.getAccountModelList().size());
			assertTrue(actual.getIsLast());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが0件")
		void getAccountList_not_found() {
			AccountGetModel accountGetModel = AccountGetModel.builder().limit(100).offset(0).build();
			AccountPageModel actual = accountRepositoryImpl.getAccountList(accountGetModel);
			assertEquals(0, actual.getAccountModelList().size());
			assertTrue(actual.getIsLast());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：1ページあたりの表示件数（5件）に切り詰められ、最後のページでないと判定されること（1ページ目）")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void getAccountList_pagination_firstPage() {
			// 1ページあたりの表示件数を5件と仮定し、limitはその1件多い6を指定する
			AccountGetModel accountGetModel = AccountGetModel.builder().limit(6).offset(0).build();
			AccountPageModel actual = accountRepositoryImpl.getAccountList(accountGetModel);

			assertFalse(actual.getIsLast());
			assertEquals(5, actual.getAccountModelList().size());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：2ページ目も表示件数分取得でき、まだ最後のページでないと判定されること")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void getAccountList_pagination_secondPage() {
			AccountGetModel accountGetModel = AccountGetModel.builder().limit(6).offset(5).build();
			AccountPageModel actual = accountRepositoryImpl.getAccountList(accountGetModel);

			assertFalse(actual.getIsLast());
			assertEquals(5, actual.getAccountModelList().size());
		}

		@Test
		@Order(5)
		@DisplayName("正常系：残り件数が表示件数未満の場合、最後のページと判定されること（3ページ目）")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void getAccountList_pagination_lastPage() {
			// 11件中、1・2ページ目で10件取得済みのため、3ページ目は残り1件のみ
			AccountGetModel accountGetModel = AccountGetModel.builder().limit(6).offset(10).build();
			AccountPageModel actual = accountRepositoryImpl.getAccountList(accountGetModel);

			assertTrue(actual.getIsLast());
			assertEquals(1, actual.getAccountModelList().size());
		}
	}

	@Nested
	@Order(9)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを物理削除する")
		void delete_success() {
			accountRepositoryImpl.delete(new AccountNo(1L));

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

	@Nested
	@Order(9)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
	class lockForUpdate {
		@Test
		@Order(1)
		@DisplayName("正常系：行ロックを取得できること")
		void lockForUpdate_success() {
			assertDoesNotThrow(() -> accountRepositoryImpl.lockForUpdate(new AccountNo(1L)));
		}
	}
}