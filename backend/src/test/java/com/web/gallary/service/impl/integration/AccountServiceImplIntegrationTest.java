package com.web.gallary.service.impl.integration;

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

import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.SexEnum;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.AccountModel;
import com.web.gallary.service.impl.AccountServiceImpl;

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
					.accountId("mmmmmmmm")
					.accountName("MMMMMMMM")
					.password("mmmmmmmm")
					.build();
			assertTrue(accountServiceImpl.registAccount(accountModel));
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_id='mmmmmmmm'", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(0, actualData.getFirst().getCreatedBy());
			assertEquals(0, actualData.getFirst().getUpdatedBy());
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
		void registAccount_account_already_exist() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("aaaaaaaa")
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
			AccountModel accountModel = AccountModel.builder().accountNo(1).accountId("zzzzzzzz").build();
			assertFalse(accountServiceImpl.updateAccount(accountModel));
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=1", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
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
		void updateAccount_account_already_exist() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(1).accountId("bbbbbbbb").build();
			assertTrue(accountServiceImpl.updateAccount(accountModel));
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account where account_no=1", (rs, rowNum) ->
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
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
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
		void updateAccount_UpdateFailureException() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(99).accountId("zzzzzzzz").build();
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
			Account actual = accountServiceImpl.getAccountById("aaaaaaaa");
			
			assertEquals(1, actual.getAccountNo());
			assertEquals(1, actual.getCreatedBy());
			assertEquals(1, actual.getUpdatedBy());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getLastLoginDatetime().plusHours(9));
			assertEquals(0, actual.getLoginFailureCount());
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
			assertEquals("aaaaaaaa", actual.get(0).getAccountId());
			assertEquals("bbbbbbbb", actual.get(1).getAccountId());
			assertEquals("cccccccc", actual.get(2).getAccountId());
			assertEquals("dddddddd", actual.get(3).getAccountId());
			assertEquals("eeeeeeee", actual.get(4).getAccountId());
			assertEquals("ffffffff", actual.get(5).getAccountId());
			assertEquals("gggggggg", actual.get(6).getAccountId());
			assertEquals("hhhhhhhh", actual.get(7).getAccountId());
			assertEquals("jjjjjjjj", actual.get(8).getAccountId());
			assertEquals("kkkkkkkk", actual.get(9).getAccountId());
			assertEquals("llllllll", actual.get(10).getAccountId());
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
			
			assertEquals(1, actualData.size());
			assertEquals(11, actualData.getFirst().getAccountNo());
			assertEquals(11, actualData.getFirst().getCreatedBy());
			assertEquals(11, actualData.getFirst().getUpdatedBy());
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
			assertNotEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime().plusHours(9));
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}
	}
	
	@Nested
	@Order(7)
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
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
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
			
			assertEquals(0, actualData.size());
		}
	}
}