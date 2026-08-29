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
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.AccountListGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountPageModel;
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
		void registAccount_success() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
					.accountId(new AccountId("mmmmmmmm"))
					.accountName(new AccountName("MMMMMMMM"))
					.password(new Password("mmmmmmmm"))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			assertTrue(accountServiceImpl.registAccount(accountModel));

			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='mmmmmmmm'", (rs, rowNum) ->
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
			assertEquals("mmmmmmmm", actualData.getFirst().getAccountId());
			assertEquals("MMMMMMMM", actualData.getFirst().getAccountName());
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
		@DisplayName("正常系：アカウントが既に存在する")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
		void registAccount_account_already_exist() throws GalleryException {
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
		void updateAccount_success() throws GalleryException {
			AccountModel accountModel = AccountModel.builder().accountNo(new AccountNo(1L)).accountId(new AccountId("zzzzzzzz")).build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			assertFalse(accountServiceImpl.updateAccount(accountModel));

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
			assertEquals("zzzzzzzz", actualData.getFirst().getAccountId());
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
		@DisplayName("正常系：アカウントが既に存在する")
		void updateAccount_account_already_exist() throws GalleryException {
			AccountModel accountModel = AccountModel.builder().accountNo(new AccountNo(1L)).accountId(new AccountId("bbbbbbbb")).build();
			assertTrue(accountServiceImpl.updateAccount(accountModel));

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
			assertEquals(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getUpdatedAt());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void updateAccount_UpdateFailureException() throws GalleryException {
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
			AccountModel actual = accountServiceImpl.getAccountById(new AccountId("aaaaaaaa"));

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
			assertNull(accountServiceImpl.getAccountById(new AccountId("zzzzzzzz")));
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合（1ページ目、5件に切り詰められ最後のページでないこと）")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
		void getAccountList_found() {
			AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
			AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);

			assertFalse(actual.getIsLast());
			assertEquals(5, actual.getAccountModelList().size());
			assertEquals("aaaaaaaa", actual.getAccountModelList().get(0).getAccountId().value());
			assertEquals("bbbbbbbb", actual.getAccountModelList().get(1).getAccountId().value());
			assertEquals("cccccccc", actual.getAccountModelList().get(2).getAccountId().value());
			assertEquals("dddddddd", actual.getAccountModelList().get(3).getAccountId().value());
			assertEquals("eeeeeeee", actual.getAccountModelList().get(4).getAccountId().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在する場合（2ページ目、まだ最後のページでないこと）")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
		void getAccountList_found_secondPage() {
			AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(2).build();
			AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);

			assertFalse(actual.getIsLast());
			assertEquals(5, actual.getAccountModelList().size());
			assertEquals("ffffffff", actual.getAccountModelList().get(0).getAccountId().value());
			assertEquals("gggggggg", actual.getAccountModelList().get(1).getAccountId().value());
			assertEquals("hhhhhhhh", actual.getAccountModelList().get(2).getAccountId().value());
			assertEquals("jjjjjjjj", actual.getAccountModelList().get(3).getAccountId().value());
			assertEquals("kkkkkkkk", actual.getAccountModelList().get(4).getAccountId().value());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：アカウントが存在する場合（3ページ目、残り1件で最後のページと判定されること）")
		@Sql("/sql/common/cleanup.sql")
		@Sql("/sql/service/AccountServiceImplIntegrationTest.sql")
		void getAccountList_found_lastPage() {
			AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(3).build();
			AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);

			assertTrue(actual.getIsLast());
			assertEquals(1, actual.getAccountModelList().size());
			assertEquals("llllllll", actual.getAccountModelList().get(0).getAccountId().value());
		}

		@Test
		@Order(4)
		@DisplayName("正常系：アカウントが存在しない場合")
		void getAccountList_not_found() {
			AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();
			AccountPageModel actual = accountServiceImpl.getAccountList(accountListGetModel);
			assertEquals(0, actual.getAccountModelList().size());
			assertTrue(actual.getIsLast());
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
			accountServiceImpl.deleteAccount(new AccountNo(1L), new AccountId("aaaaaaaa"));

			// アカウントが削除されたことを確認
			List<Account> accountData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=1", (rs, rowNum) ->
						Account.builder()
							.accountNo(rs.getLong("account_no"))
							.build());
			assertEquals(0, accountData.size());

			// account_no=2のアカウントは残っていること
			List<Account> otherAccountData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=2", (rs, rowNum) ->
						Account.builder()
							.accountNo(rs.getLong("account_no"))
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

			// リフレッシュトークンが失効・削除されたことを確認
			Integer refreshTokenCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.refresh_token where account_no=1", Integer.class);
			assertEquals(0, refreshTokenCount);

			// account_no=2の有効なリフレッシュトークンは残っていること
			Integer otherRefreshTokenCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.refresh_token where account_no=2 and is_revoked=false", Integer.class);
			assertEquals(1, otherRefreshTokenCount);
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
		void handle_success() throws GalleryException {
			String username = "kkkkkkkk";
			String password = "KKKKKKKK";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			OffsetDateTime beforeLogin = OffsetDateTime.now();
			accountServiceImpl.handle(event);
			OffsetDateTime afterLogin = OffsetDateTime.now();

			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=11", (rs, rowNum) ->
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
			assertEquals(11L, actualData.getFirst().getAccountNo());
			assertEquals(11L, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 11, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(11L, actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("kkkkkkkk", actualData.getFirst().getAccountId());
			assertEquals("KKKKKKKK", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password11", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1990, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals("Okinawa", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("Tokyo", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("よろしく", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.NORMAL, actualData.getFirst().getAuthorityKbn());
			assertFalse(actualData.getFirst().getLastLoginDatetime().isBefore(beforeLogin));
			assertFalse(actualData.getFirst().getLastLoginDatetime().isAfter(afterLogin));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
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
		void handle_account_found() throws GalleryException {
			String username = "aaaaaaaa";
			String password = "AAAAAAAA";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			
			String message = "Invalid username or password";
			BadCredentialsException exception = new BadCredentialsException(message);
			
			AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(authentication, exception);

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			accountServiceImpl.handle(event);

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
			assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void handle_account_not_found() throws GalleryException {
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
			
			assertEquals(0, actualData.size());
		}
	}
}