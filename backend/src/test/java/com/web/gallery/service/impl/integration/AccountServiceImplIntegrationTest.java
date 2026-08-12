package com.web.gallery.service.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

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
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.entity.Account;
import com.web.gallery.enumuration.AuthorityEnum;
import com.web.gallery.enumuration.SexEnum;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.AccountModel;
import com.web.gallery.service.impl.AccountServiceImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class AccountServiceImplIntegrationTest {
	@Autowired
	private AccountServiceImpl accountServiceImpl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
	class loadUserByUsername {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void loadUserByUsername_success() {
			UserDetails userDetails = accountServiceImpl.loadUserByUsername("aaaaaaaa");
			assertEquals("aaaaaaaa", userDetails.getUsername());
			assertEquals("$2a$10$password1", userDetails.getPassword());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UsernameNotFoundExceptionをthrowする")
		void loadUserByUsername_UsernameNotFoundException() {
			assertThrows(UsernameNotFoundException.class, () -> accountServiceImpl.loadUserByUsername("zzzzzzzz"));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class registAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを新規登録")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/common/ResetAccountNoSeq.sql")
		void registAccount_success() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId(new AccountId("mmmmmmmm"))
					.accountName(new AccountName("MMMMMMMM"))
					.password(new Password("mmmmmmmm"))
					.build();
			assertTrue(accountServiceImpl.registAccount(accountModel));
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='mmmmmmmm'", (rs, rowNum) ->
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
			assertEquals(1L, actualData.getFirst().getAccountNo().value());
			assertEquals(0L, actualData.getFirst().getCreatedBy().value());
			assertEquals(0L, actualData.getFirst().getUpdatedBy().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals("mmmmmmmm", actualData.getFirst().getAccountId().value());
			assertEquals("MMMMMMMM", actualData.getFirst().getAccountName().value());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate().value());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode().value());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode().value());
			assertEquals("", actualData.getFirst().getFreeMemo().value());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(new LoginFailureCount(0), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが既に存在する")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
		void registAccount_account_already_exist() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("aaaaaaaa"))
					.build();
			assertFalse(accountServiceImpl.registAccount(accountModel));
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
	class updateAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを更新")
		void updateAccount_success() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(new AccountNo(1L)).accountId(new AccountId("zzzzzzzz")).build();
			assertFalse(accountServiceImpl.updateAccount(accountModel));
			
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
			assertEquals(1L, actualData.getFirst().getAccountNo().value());
			assertEquals(1L, actualData.getFirst().getCreatedBy().value());
			assertEquals(1L, actualData.getFirst().getUpdatedBy().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals("zzzzzzzz", actualData.getFirst().getAccountId().value());
			assertEquals("AAAAAAAA", actualData.getFirst().getAccountName().value());
			assertEquals("$2a$10$password1", actualData.getFirst().getPassword().value());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate().value());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode().value());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode().value());
			assertEquals("", actualData.getFirst().getFreeMemo().value());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(new LoginFailureCount(0), actualData.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが既に存在する")
		void updateAccount_account_already_exist() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(new AccountNo(1L)).accountId(new AccountId("bbbbbbbb")).build();
			assertTrue(accountServiceImpl.updateAccount(accountModel));
			
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
			assertEquals(1L, actualData.getFirst().getAccountNo().value());
			assertEquals(1L, actualData.getFirst().getCreatedBy().value());
			assertEquals(1L, actualData.getFirst().getUpdatedBy().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals("aaaaaaaa", actualData.getFirst().getAccountId().value());
			assertEquals("AAAAAAAA", actualData.getFirst().getAccountName().value());
			assertEquals("$2a$10$password1", actualData.getFirst().getPassword().value());
			assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate().value());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode().value());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode().value());
			assertEquals("", actualData.getFirst().getFreeMemo().value());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(new LoginFailureCount(0), actualData.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void updateAccount_UpdateFailureException() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(new AccountNo(99L)).accountId(new AccountId("zzzzzzzz")).build();
			assertThrows(UpdateFailureException.class, () -> accountServiceImpl.updateAccount(accountModel));
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
	class getAccountById {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void getAccountById_found() {
			AccountModel actual = accountServiceImpl.getAccountById("aaaaaaaa");

			assertEquals(1L, actual.getAccountNo().value());
			assertEquals("aaaaaaaa", actual.getAccountId().value());
			assertEquals("AAAAAAAA", actual.getAccountName().value());
			assertEquals("$2a$10$password1", actual.getPassword().value());
			assertEquals(LocalDate.of(1991, 2, 14), actual.getBirthdate().value());
			assertEquals(SexEnum.NONE, actual.getSexKbn());
			assertEquals("none", actual.getBirthplacePrefectureKbnCode().value());
			assertEquals("none", actual.getResidentPrefectureKbnCode().value());
			assertEquals("", actual.getFreeMemo().value());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getLastLoginDatetime().value().plusHours(9));
			assertEquals(new LoginFailureCount(0), actual.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合、nullを返す")
		void getAccountById_not_found() {
			assertNull(accountServiceImpl.getAccountById("zzzzzzzz"));
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
		void getAccountList_found() {
			List<AccountModel> actual = accountServiceImpl.getAccountList();
			
			assertEquals(11, actual.size());
			assertEquals("aaaaaaaa", actual.get(0).getAccountId().value());
			assertEquals("bbbbbbbb", actual.get(1).getAccountId().value());
			assertEquals("cccccccc", actual.get(2).getAccountId().value());
			assertEquals("dddddddd", actual.get(3).getAccountId().value());
			assertEquals("eeeeeeee", actual.get(4).getAccountId().value());
			assertEquals("ffffffff", actual.get(5).getAccountId().value());
			assertEquals("gggggggg", actual.get(6).getAccountId().value());
			assertEquals("hhhhhhhh", actual.get(7).getAccountId().value());
			assertEquals("jjjjjjjj", actual.get(8).getAccountId().value());
			assertEquals("kkkkkkkk", actual.get(9).getAccountId().value());
			assertEquals("llllllll", actual.get(10).getAccountId().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void getAccountList_not_found() {
			List<AccountModel> actual = accountServiceImpl.getAccountList();
			assertEquals(0, actual.size());
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/AccountServiceImplDeleteAccountIntegrationTest.sql")
	class deleteAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントと関連データがすべて物理削除されること")
		void deleteAccount_success() {
			accountServiceImpl.deleteAccount(1L, "aaaaaaaa");

			// アカウントが削除されたことを確認
			List<Account> accountData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=1", (rs, rowNum) ->
						Account.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.build());
			assertEquals(0, accountData.size());

			// account_no=2のアカウントは残っていること
			List<Account> otherAccountData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=2", (rs, rowNum) ->
						Account.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.build());
			assertEquals(1, otherAccountData.size());

			// 写真マスタが削除されたことを確認
			Integer photoMstCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
			assertEquals(0, photoMstCount);

			// account_no=2の写真マスタは残っていること
			Integer otherPhotoMstCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst where account_no=2", Integer.class);
			assertEquals(1, otherPhotoMstCount);

			// 写真タグが削除されたことを確認
			Integer photoTagCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_tag_mst where account_no=1", Integer.class);
			assertEquals(0, photoTagCount);

			// 自分が登録したお気に入りが削除されたことを確認
			Integer favoriteByAccount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_favorite where account_no=1", Integer.class);
			assertEquals(0, favoriteByAccount);

			// 他人が自分の写真に対して登録したお気に入りが削除されたことを確認
			Integer favoriteForAccount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_favorite where favorite_photo_account_no=1", Integer.class);
			assertEquals(0, favoriteForAccount);

			// account_no=2が自分の写真をお気に入りにしたレコードは残っていること
			Integer otherFavoriteCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_favorite where account_no=2 and favorite_photo_account_no=2", Integer.class);
			assertEquals(1, otherFavoriteCount);
		}
	}

	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
	class handleAuthenticationSuccess {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handle_success() throws UpdateFailureException {
			String username = "kkkkkkkk";
			String password = "KKKKKKKK";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);
			
			accountServiceImpl.handle(event);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=11", (rs, rowNum) ->
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
			assertEquals(11L, actualData.getFirst().getAccountNo().value());
			assertEquals(11L, actualData.getFirst().getCreatedBy().value());
			assertEquals(11L, actualData.getFirst().getUpdatedBy().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals("kkkkkkkk", actualData.getFirst().getAccountId().value());
			assertEquals("KKKKKKKK", actualData.getFirst().getAccountName().value());
			assertEquals("$2a$10$password11", actualData.getFirst().getPassword().value());
			assertEquals(LocalDate.of(1990, 1, 1), actualData.getFirst().getBirthdate().value());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals("Okinawa", actualData.getFirst().getBirthplacePrefectureKbnCode().value());
			assertEquals("Tokyo", actualData.getFirst().getResidentPrefectureKbnCode().value());
			assertEquals("よろしく", actualData.getFirst().getFreeMemo().value());
			assertEquals(AuthorityEnum.NORMAL, actualData.getFirst().getAuthorityKbn());
			assertNotEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(new LoginFailureCount(0), actualData.getFirst().getLoginFailureCount());
		}
	}
	
	@Nested
	@Order(8)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
	class handleAuthenticationFailureBadCredentials {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void handle_account_found() throws UpdateFailureException {
			String username = "aaaaaaaa";
			String password = "AAAAAAAA";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			
			String message = "Invalid username or password";
			BadCredentialsException exception = new BadCredentialsException(message);
			
			AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(authentication, exception);
			
			accountServiceImpl.handle(event);
			
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
			assertEquals(1L, actualData.getFirst().getAccountNo().value());
			assertEquals(1L, actualData.getFirst().getCreatedBy().value());
			assertEquals(1L, actualData.getFirst().getUpdatedBy().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals("aaaaaaaa", actualData.getFirst().getAccountId().value());
			assertEquals("AAAAAAAA", actualData.getFirst().getAccountName().value());
			assertEquals("$2a$10$password1", actualData.getFirst().getPassword().value());
			assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate().value());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode().value());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode().value());
			assertEquals("", actualData.getFirst().getFreeMemo().value());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().value().plusHours(9));
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void handle_account_not_found() throws UpdateFailureException {
			String username = "zzzzzzzz";
			String password = "ZZZZZZZZ";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			
			String message = "Invalid username or password";
			BadCredentialsException exception = new BadCredentialsException(message);
			
			AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(authentication, exception);
			
			accountServiceImpl.handle(event);
			
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
			
			assertEquals(0, actualData.size());
		}
	}
}