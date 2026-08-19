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
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
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
		void regist_contain_null_parameter() throws RegistFailureException {
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
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.accountId(new AccountId(rs.getString("account_id")))
							.accountName(new AccountName(rs.getString("account_name")))
							.password(new Password(rs.getString("password")))
							.birthdate(new BirthDate(rs.getObject("birthdate", LocalDate.class)))
							.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
							.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code")))
							.residentPrefectureKbnCode(new ResidentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code")))
							.freeMemo(new FreeMemo(rs.getString("free_memo")))
							.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
							.lastLoginDatetime(new LastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class)))
							.loginFailureCount(new LoginFailureCount(rs.getInt("login_failure_count")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(0L), actualData.getFirst().getCreatedBy());
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(0L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("zzzzzzzz"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("ZZZZZZZZ"), actualData.getFirst().getAccountName());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelの登録")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/common/ResetAccountNoSeq.sql")
		void regist_not_contain_null_parameter() throws RegistFailureException {
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
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.accountId(new AccountId(rs.getString("account_id")))
							.accountName(new AccountName(rs.getString("account_name")))
							.password(new Password(rs.getString("password")))
							.birthdate(new BirthDate(rs.getObject("birthdate", LocalDate.class)))
							.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
							.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code")))
							.residentPrefectureKbnCode(new ResidentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code")))
							.freeMemo(new FreeMemo(rs.getString("free_memo")))
							.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
							.lastLoginDatetime(new LastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class)))
							.loginFailureCount(new LoginFailureCount(rs.getInt("login_failure_count")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(0L), actualData.getFirst().getCreatedBy());
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(0L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("zzzzzzzz"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("ZZZZZZZZ"), actualData.getFirst().getAccountName());
			assertEquals(new BirthDate(LocalDate.of(1991, 2, 14)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("Hokkaido"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("Okinawa"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo("フリーメモ"), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount().value());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void regist_RegistFailureException() throws RegistFailureException {
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
		void update_contain_null_parameter() throws UpdateFailureException {
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
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.accountId(new AccountId(rs.getString("account_id")))
							.accountName(new AccountName(rs.getString("account_name")))
							.password(new Password(rs.getString("password")))
							.birthdate(new BirthDate(rs.getObject("birthdate", LocalDate.class)))
							.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
							.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code")))
							.residentPrefectureKbnCode(new ResidentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code")))
							.freeMemo(new FreeMemo(rs.getString("free_memo")))
							.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
							.lastLoginDatetime(new LastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class)))
							.loginFailureCount(new LoginFailureCount(rs.getInt("login_failure_count")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("aaaaaaaa"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("AAAAAAAA"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password1"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
		void update_not_contain_null_parameter() throws UpdateFailureException {
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
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.accountId(new AccountId(rs.getString("account_id")))
							.accountName(new AccountName(rs.getString("account_name")))
							.password(new Password(rs.getString("password")))
							.birthdate(new BirthDate(rs.getObject("birthdate", LocalDate.class)))
							.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
							.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code")))
							.residentPrefectureKbnCode(new ResidentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code")))
							.freeMemo(new FreeMemo(rs.getString("free_memo")))
							.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
							.lastLoginDatetime(new LastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class)))
							.loginFailureCount(new LoginFailureCount(rs.getInt("login_failure_count")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("aaaaaaaa"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("AAAAAAAA"), actualData.getFirst().getAccountName());
			assertNotEquals(new Password("$2a$10$password1"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1991, 2, 14)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("Hokkaido"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("Okinawa"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo("フリーメモ"), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(2, actualData.getFirst().getLoginFailureCount().value());
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
					.accountNo(new AccountNo(8L))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			accountRepositoryImpl.updateLoginFailureCount(accountModel);

			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=8", (rs, rowNum) ->
						Account.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.accountId(new AccountId(rs.getString("account_id")))
							.accountName(new AccountName(rs.getString("account_name")))
							.password(new Password(rs.getString("password")))
							.birthdate(new BirthDate(rs.getObject("birthdate", LocalDate.class)))
							.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
							.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code")))
							.residentPrefectureKbnCode(new ResidentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code")))
							.freeMemo(new FreeMemo(rs.getString("free_memo")))
							.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
							.lastLoginDatetime(new LastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class)))
							.loginFailureCount(new LoginFailureCount(rs.getInt("login_failure_count")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(8L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(8L), actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(8L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("hhhhhhhh"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("HHHHHHHH"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password8"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
		void updateLoginFailureCount_not_contain_null_parameter() throws UpdateFailureException {
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
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.accountId(new AccountId(rs.getString("account_id")))
							.accountName(new AccountName(rs.getString("account_name")))
							.password(new Password(rs.getString("password")))
							.birthdate(new BirthDate(rs.getObject("birthdate", LocalDate.class)))
							.sexKbn(SexEnum.getOrDefault(rs.getString("sex_kbn")))
							.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode(rs.getString("birthplace_prefecture_kbn_code")))
							.residentPrefectureKbnCode(new ResidentPrefectureKbnCode(rs.getString("resident_prefecture_kbn_code")))
							.freeMemo(new FreeMemo(rs.getString("free_memo")))
							.authorityKbn(AuthorityEnum.getOrDefault(rs.getString("authority_kbn")))
							.lastLoginDatetime(new LastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class)))
							.loginFailureCount(new LoginFailureCount(rs.getInt("login_failure_count")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("aaaaaaaa"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("AAAAAAAA"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password1"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1991, 2, 14)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(2, actualData.getFirst().getLoginFailureCount().value());
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
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを2件以上取得")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/repository/AccountRepositoryImplIntegrationTest.sql")
		void getAccountList_found_some_accounts() {
			AccountModelList actual = accountRepositoryImpl.getAccountList();
			assertEquals(11, actual.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが0件")
		void getAccountList_not_found() {
			AccountModelList actual = accountRepositoryImpl.getAccountList();
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
			accountRepositoryImpl.delete(new AccountNo(1L));

			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=1", (rs, rowNum) ->
						Account.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.build());
			assertEquals(0, actualData.size());

			// 他のアカウントは残っていることを確認
			List<Account> otherData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=2", (rs, rowNum) ->
						Account.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.build());
			assertEquals(1, otherData.size());
		}
	}
}