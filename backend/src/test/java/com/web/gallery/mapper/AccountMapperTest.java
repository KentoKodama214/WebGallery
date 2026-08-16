package com.web.gallery.mapper;

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
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

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

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountMapperTest {
	@Autowired
	private AccountMapper accountMapper;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/AccountMapperTest.sql")
	class select {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのselectで1件の場合")
		void select_by_accountNo() {
			Account account = Account.builder().accountNo(new AccountNo(1L)).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(1L))
					.createdBy(new CreatedBy(1L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(1L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：削除フラグでのselectで1件の場合")
		void select_by_isDeleted() {
			Account account = Account.builder().isDeleted(new IsDeleted(true)).build();
			List<Account> actual = accountMapper.select(account);
		
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(9L))
					.createdBy(new CreatedBy(9L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(9L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(true))
					.accountId(new AccountId("iiiiiiii"))
					.accountName(new AccountName("IIIIIIII"))
					.password(new Password("$2a$10$password9"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.SPECIAL)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウントIDでのselectで1件の場合")
		void select_by_accountId() {
			Account account = Account.builder().accountId(new AccountId("aaaaaaaa")).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(1L))
					.createdBy(new CreatedBy(1L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(1L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：アカウント名でのselectで1件の場合")
		void select_by_accountName() {
			Account account = Account.builder().accountName(new AccountName("AAAAAAAA")).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(1L))
					.createdBy(new CreatedBy(1L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(1L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：パスワードでのselectで1件の場合")
		void select_by_password() {
			Account account = Account.builder().password(new Password("$2a$10$password1")).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(1L))
					.createdBy(new CreatedBy(1L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(1L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：生年月日でのselectで1件の場合")
		void select_by_birthdate() {
			Account account = Account.builder().birthdate(new BirthDate(LocalDate.of(1991, 2, 14))).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(1L))
					.createdBy(new CreatedBy(1L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(1L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：性別区分コードでのselectで1件の場合")
		void select_by_sexKbnCode() {
			Account account = Account.builder().sexKbn(SexEnum.MAN).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(2L))
					.createdBy(new CreatedBy(2L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(2L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("bbbbbbbb"))
					.accountName(new AccountName("BBBBBBBB"))
					.password(new Password("$2a$10$password2"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.MAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：出身都道府県区分コードでのselectで1件の場合")
		void select_by_birthplacePrefectureKbnCode() {
			Account account = Account.builder().birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido")).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(3L))
					.createdBy(new CreatedBy(3L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(3L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("cccccccc"))
					.accountName(new AccountName("CCCCCCCC"))
					.password(new Password("$2a$10$password3"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：在住都道府県区分コードでのselectで1件の場合")
		void select_by_residentPrefectureKbnCode() {
			Account account = Account.builder().residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa")).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(4L))
					.createdBy(new CreatedBy(4L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(4L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("dddddddd"))
					.accountName(new AccountName("DDDDDDDD"))
					.password(new Password("$2a$10$password4"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：フリーメモでのselectで1件の場合")
		void select_by_freeMemo() {
			Account account = Account.builder().freeMemo(new FreeMemo("フリーメモ")).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(5L))
					.createdBy(new CreatedBy(5L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(5L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("eeeeeeee"))
					.accountName(new AccountName("EEEEEEEE"))
					.password(new Password("$2a$10$password5"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo("フリーメモ"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：権限区分コードでのselectで1件の場合")
		void select_by_authorityKbnCode() {
			Account account = Account.builder().authorityKbn(AuthorityEnum.MINI).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(6L))
					.createdBy(new CreatedBy(6L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 6, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(6L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 6, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("ffffffff"))
					.accountName(new AccountName("FFFFFFFF"))
					.password(new Password("$2a$10$password6"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.MINI)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：最終ログイン日時でのselectで1件の場合")
		void select_by_lastLoginDatetime() {
			Account account = Account.builder().lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(7L))
					.createdBy(new CreatedBy(7L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(7L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("gggggggg"))
					.accountName(new AccountName("GGGGGGGG"))
					.password(new Password("$2a$10$password7"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：ログイン失敗回数でのselectで1件の場合")
		void select_by_loginFailureCount() {
			Account account = Account.builder().loginFailureCount(new LoginFailureCount(2)).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(new AccountNo(8L))
					.createdBy(new CreatedBy(8L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(8L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("hhhhhhhh"))
					.accountName(new AccountName("HHHHHHHH"))
					.password(new Password("$2a$10$password8"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(2))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：selectで0件の場合")
		void select_not_found() {
			Account account = Account.builder().accountNo(new AccountNo(99L)).build();
			List<Account> actual = accountMapper.select(account);
			List<Account> expected = new ArrayList<Account>();
			
			assertEquals(0, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：selectで2件以上の場合")
		void select_accounts() {
			Account account = Account.builder().authorityKbn(AuthorityEnum.SPECIAL).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount1 = Account.builder()
					.accountNo(new AccountNo(9L))
					.createdBy(new CreatedBy(9L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(9L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(true))
					.accountId(new AccountId("iiiiiiii"))
					.accountName(new AccountName("IIIIIIII"))
					.password(new Password("$2a$10$password9"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.SPECIAL)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			Account expectedAccount2 = Account.builder()
					.accountNo(new AccountNo(10L))
					.createdBy(new CreatedBy(10L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(10L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("jjjjjjjj"))
					.accountName(new AccountName("JJJJJJJJ"))
					.password(new Password("$2a$10$password10"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("none"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("none"))
					.freeMemo(new FreeMemo(""))
					.authorityKbn(AuthorityEnum.SPECIAL)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount1);
			expected.add(expectedAccount2);
			
			assertEquals(2, actual.size());
			assertEquals(expected, actual);
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でselectする場合")
		void select_some_conditions() {
			Account account = Account.builder()
					.accountId(new AccountId("llllllll"))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Okinawa"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
					.freeMemo(new FreeMemo("よろしく"))
					.build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount1 = Account.builder()
					.accountNo(new AccountNo(12L))
					.createdBy(new CreatedBy(12L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.updatedBy(new UpdatedBy(12L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("llllllll"))
					.accountName(new AccountName("LLLLLLLL"))
					.password(new Password("$2a$10$password12"))
					.birthdate(new BirthDate(LocalDate.of(1900, 1, 1)))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Okinawa"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
					.freeMemo(new FreeMemo("よろしく"))
					.authorityKbn(AuthorityEnum.NORMAL)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.loginFailureCount(new LoginFailureCount(3))
					.build();
			
			List<Account> expected = new ArrayList<Account>();
			expected.add(expectedAccount1);
			
			assertEquals(1, actual.size());
			assertEquals(expected, actual);
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/AccountMapperTest.sql")
	class count {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのcount")
		void count_by_accountNo() {
			Account account = Account.builder().accountNo(new AccountNo(1L)).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：削除フラグでのcount")
		void count_by_isDeleted() {
			Account account = Account.builder().isDeleted(new IsDeleted(true)).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウントIDでのcount")
		void count_by_accountId() {
			Account account = Account.builder().accountId(new AccountId("aaaaaaaa")).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：アカウント名でのcount")
		void count_by_accountName() {
			Account account = Account.builder().accountName(new AccountName("AAAAAAAA")).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：パスワードでのcount")
		void count_by_password() {
			Account account = Account.builder().password(new Password("$2a$10$password1")).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：生年月日でのcount")
		void count_by_birthdate() {
			Account account = Account.builder().birthdate(new BirthDate(LocalDate.of(1991, 2, 14))).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：性別区分コードでのcount")
		void count_by_sexKbnCode() {
			Account account = Account.builder().sexKbn(SexEnum.MAN).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：出身都道府県区分コードでのcount")
		void count_by_birthplacePrefectureKbnCode() {
			Account account = Account.builder().birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido")).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：在住都道府県区分コードでのcount")
		void count_by_residentPrefectureKbnCode() {
			Account account = Account.builder().residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa")).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：フリーメモでのcount")
		void count_by_freeMemo() {
			Account account = Account.builder().freeMemo(new FreeMemo("フリーメモ")).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：権限区分コードでのcount")
		void count_by_authorityKbnCode() {
			Account account = Account.builder().authorityKbn(AuthorityEnum.MINI).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：最終ログイン日時でのcount")
		void count_by_lastLoginDatetime() {
			Account account = Account.builder()
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：ログイン失敗回数でのcount")
		void count_by_loginFailureCount() {
			Account account = Account.builder().loginFailureCount(new LoginFailureCount(2)).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：countで0件の場合")
		void count_not_found() {
			Account account = Account.builder().accountNo(new AccountNo(99L)).build();
			Integer actual = accountMapper.count(account);
			assertEquals(0, actual);
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：countで2件以上の場合")
		void count_accounts() {
			Account account = Account.builder().authorityKbn(AuthorityEnum.SPECIAL).build();
			Integer actual = accountMapper.count(account);
			assertEquals(2, actual);
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でcountする場合")
		void count_some_conditions() {
			Account account = Account.builder()
					.accountId(new AccountId("llllllll"))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Okinawa"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
					.freeMemo(new FreeMemo("よろしく"))
					.build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/common/ResetAccountNoSeq.sql")
	class insert {
		@Test
		@Order(1)
		@DisplayName("正常系：登録成功")
		void insert_success() {
			Account insertAccount = Account.builder()
					.accountNo(new AccountNo(1L))
					.createdBy(new CreatedBy(1L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
					.updatedBy(new UpdatedBy(1L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
					.isDeleted(new IsDeleted(false))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("$2a$10$password1"))
					.birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa"))
					.freeMemo(new FreeMemo("フリーメモ"))
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9))))
					.loginFailureCount(new LoginFailureCount(0))
					.build();
			
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actualCount = accountMapper.insert(insertAccount);
			assertEquals(1, actualCount);

			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account", (rs, rowNum) ->
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
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("aaaaaaaa"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("AAAAAAAA"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password1"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1991, 2, 14)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("Hokkaido"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("Okinawa"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo("フリーメモ"), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(0), actualData.getFirst().getLoginFailureCount());
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/AccountMapperTest.sql")
	class update {
		private List<Account> getAccountList(String condition) {
			return jdbcTemplate.query(
					"SELECT * FROM common.account WHERE " + condition, (rs, rowNum) ->
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
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのupdate")
		void update_by_accountNo() {
			Account conditionAccount = Account.builder().accountNo(new AccountNo(1L)).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("account_no=1");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
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
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：削除フラグでのupdate")
		void update_by_isDeleted() {
			Account conditionAccount = Account.builder().isDeleted(new IsDeleted(true)).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("is_deleted=true");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(9L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(9L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(9L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertTrue(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("iiiiiiii"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("IIIIIIII"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password9"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.SPECIAL, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウントIDでのupdate")
		void update_by_accountId() {
			Account conditionAccount = Account.builder().accountId(new AccountId("aaaaaaaa")).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("account_id='aaaaaaaa'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
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
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：アカウント名でのupdate")
		void update_by_accountName() {
			Account conditionAccount = Account.builder().accountName(new AccountName("AAAAAAAA")).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("account_name='AAAAAAAA'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
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
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：パスワードでのupdate")
		void update_by_password() {
			Account conditionAccount = Account.builder().password(new Password("$2a$10$password1")).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("password='$2a$10$password1'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
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
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：生年月日でのupdate")
		void update_by_birthdate() {
			Account conditionAccount = Account.builder().birthdate(new BirthDate(LocalDate.of(1991, 2, 14))).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("birthdate='1991-02-14'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
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
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：性別区分コードでのupdate")
		void update_by_sexKbnCode() {
			Account conditionAccount = Account.builder().sexKbn(SexEnum.MAN).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("sex_kbn='man'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(2L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(2L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(2L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("bbbbbbbb"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("BBBBBBBB"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password2"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.MAN, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：出身都道府県区分コードでのupdate")
		void update_by_birthplacePrefectureKbnCode() {
			Account conditionAccount = Account.builder().birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido")).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("birthplace_prefecture_kbn_code='Hokkaido'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(3L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(3L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(3L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("cccccccc"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("CCCCCCCC"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password3"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("Hokkaido"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：在住都道府県区分コードでのupdate")
		void update_by_residentPrefectureKbnCode() {
			Account conditionAccount = Account.builder().residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa")).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("resident_prefecture_kbn_code='Okinawa'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(4L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(4L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(4L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("dddddddd"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("DDDDDDDD"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password4"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("Okinawa"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：フリーメモでのupdate")
		void update_by_freeMemo() {
			Account conditionAccount = Account.builder().freeMemo(new FreeMemo("フリーメモ")).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("free_memo='フリーメモ'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(5L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(5L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(5L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("eeeeeeee"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("EEEEEEEE"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password5"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo("フリーメモ"), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：権限区分コードでのupdate")
		void update_by_authorityKbnCode() {
			Account conditionAccount = Account.builder().authorityKbn(AuthorityEnum.MINI).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("authority_kbn='mini-user'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(6L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(6L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 6, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(6L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("ffffffff"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("FFFFFFFF"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password6"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：最終ログイン日時でのupdate")
		void update_by_lastLoginDatetime() {
			Account conditionAccount = Account.builder()
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("last_login_datetime='2024-01-01 00:00:00.000 +0000'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(7L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(7L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(7L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("gggggggg"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("GGGGGGGG"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password7"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：ログイン失敗回数でのupdate")
		void update_by_loginFailureCounte() {
			Account conditionAccount = Account.builder().loginFailureCount(new LoginFailureCount(2)).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(0)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("account_no=8");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(8L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(8L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
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
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(0), actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：更新対象のレコードなし")
		void update_not_found() {
			Account conditionAccount = Account.builder().accountNo(new AccountNo(99L)).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(0)).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(0, actual);
			
			List<Account> actualData = getAccountList("account_no=99");
			assertEquals(0, actualData.size());
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：2件以上updateの場合")
		void update_accounts() {
			Account conditionAccount = Account.builder().authorityKbn(AuthorityEnum.SPECIAL).build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(1)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(2, actual);

			List<Account> actualData = getAccountList("authority_kbn='special-user' order by account_no");
			assertEquals(2, actualData.size());
			assertEquals(new AccountNo(9L), actualData.get(0).getAccountNo());
			assertEquals(new CreatedBy(9L), actualData.get(0).getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.get(0).getCreatedAt());
			assertEquals(new UpdatedBy(9L), actualData.get(0).getUpdatedBy());
			assertEquals(transactionNow, actualData.get(0).getUpdatedAt().value());
			assertTrue(actualData.get(0).getIsDeleted().value());
			assertEquals(new AccountId("iiiiiiii"), actualData.get(0).getAccountId());
			assertEquals(new AccountName("IIIIIIII"), actualData.get(0).getAccountName());
			assertEquals(new Password("$2a$10$password9"), actualData.get(0).getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.get(0).getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.get(0).getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.get(0).getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.get(0).getFreeMemo());
			assertEquals(AuthorityEnum.SPECIAL, actualData.get(0).getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.get(0).getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.get(0).getLoginFailureCount());
			
			assertEquals(new AccountNo(10L), actualData.get(1).getAccountNo());
			assertEquals(new CreatedBy(10L), actualData.get(1).getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.get(1).getCreatedAt());
			assertEquals(new UpdatedBy(10L), actualData.get(1).getUpdatedBy());
			assertEquals(transactionNow, actualData.get(1).getUpdatedAt().value());
			assertFalse(actualData.get(1).getIsDeleted().value());
			assertEquals(new AccountId("jjjjjjjj"), actualData.get(1).getAccountId());
			assertEquals(new AccountName("JJJJJJJJ"), actualData.get(1).getAccountName());
			assertEquals(new Password("$2a$10$password10"), actualData.get(1).getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.get(1).getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("none"), actualData.get(1).getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("none"), actualData.get(1).getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo(""), actualData.get(1).getFreeMemo());
			assertEquals(AuthorityEnum.SPECIAL, actualData.get(1).getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.get(1).getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualData.get(1).getLoginFailureCount());
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でupdateする場合")
		void update_some_conditions() {
			Account conditionAccount = Account.builder()
					.accountId(new AccountId("llllllll"))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Okinawa"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
					.freeMemo(new FreeMemo("よろしく"))
					.build();
			Account targetAccount = Account.builder().loginFailureCount(new LoginFailureCount(0)).build();
			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);

			List<Account> actualData = getAccountList("account_id='llllllll'");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(12L), actualData.getFirst().getAccountNo());
			assertEquals(new CreatedBy(12L), actualData.getFirst().getCreatedBy());
			assertEquals(new CreatedAt(OffsetDateTime.of(2000, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getCreatedAt());
			assertEquals(new UpdatedBy(12L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(new AccountId("llllllll"), actualData.getFirst().getAccountId());
			assertEquals(new AccountName("LLLLLLLL"), actualData.getFirst().getAccountName());
			assertEquals(new Password("$2a$10$password12"), actualData.getFirst().getPassword());
			assertEquals(new BirthDate(LocalDate.of(1900, 1, 1)), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("Okinawa"), actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("Tokyo"), actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo("よろしく"), actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.NORMAL, actualData.getFirst().getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualData.getFirst().getLastLoginDatetime());
			assertEquals(new LoginFailureCount(0), actualData.getFirst().getLoginFailureCount());
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/AccountMapperTest.sql")
	class delete {
		private List<Account> getAccountList(String condition) {
			return jdbcTemplate.query(
					"SELECT * FROM common.account WHERE " + condition, (rs, rowNum) ->
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
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのdelete")
		void delete_by_accountNo() {
			Account deleteAccount = Account.builder().accountNo(new AccountNo(1L)).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_no=1");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("account_no<>1");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：削除フラグでのdelete")
		void delete_by_isDeleted() {
			Account deleteAccount = Account.builder().isDeleted(new IsDeleted(true)).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("is_deleted=true");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("is_deleted=false");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウントIDでのdelete")
		void delete_by_accountId() {
			Account deleteAccount = Account.builder().accountId(new AccountId("aaaaaaaa")).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_id='aaaaaaaa'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("account_id<>'aaaaaaaa'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：アカウント名でのdelete")
		void delete_by_accountName() {
			Account deleteAccount = Account.builder().accountName(new AccountName("AAAAAAAA")).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_name='AAAAAAAA'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("account_name<>'AAAAAAAA'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：パスワードでのdelete")
		void delete_by_password() {
			Account deleteAccount = Account.builder().password(new Password("$2a$10$password1")).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("password='$2a$10$password1'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("password<>'$2a$10$password1'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：生年月日でのdelete")
		void delete_by_birthdate() {
			Account deleteAccount = Account.builder().birthdate(new BirthDate(LocalDate.of(1991, 2, 14))).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("birthdate='1991-02-14'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("birthdate<>'1991-02-14'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：性別区分コードでのdelete")
		void delete_by_sexKbnCode() {
			Account deleteAccount = Account.builder().sexKbn(SexEnum.MAN).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("sex_kbn='man'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("sex_kbn<>'man'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：出身都道府県区分コードでのdelete")
		void delete_by_birthplacePrefectureKbnCode() {
			Account deleteAccount = Account.builder().birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido")).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("birthplace_prefecture_kbn_code='Hokkaido'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("birthplace_prefecture_kbn_code<>'Hokkaido'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：在住都道府県区分コードでのdelete")
		void delete_by_residentPrefectureKbnCode() {
			Account deleteAccount = Account.builder().residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa")).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("resident_prefecture_kbn_code='Okinawa'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("resident_prefecture_kbn_code<>'Okinawa'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：フリーメモでのdelete")
		void delete_by_freeMemo() {
			Account deleteAccount = Account.builder().freeMemo(new FreeMemo("フリーメモ")).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("free_memo='フリーメモ'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("free_memo<>'フリーメモ'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：権限区分コードでのdelete")
		void delete_by_authorityKbnCode() {
			Account deleteAccount = Account.builder().authorityKbn(AuthorityEnum.MINI).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("authority_kbn='mini-user'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("authority_kbn<>'mini-user'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：最終ログイン日時でのdelete")
		void delete_by_lastLoginDatetime() {
			Account deleteAccount = Account.builder()
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("last_login_datetime='2024-01-01 00:00:00.000 +0000'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("last_login_datetime<>'2024-01-01 00:00:00.000 +0000'");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：ログイン失敗回数でのdelete")
		void delete_by_loginFailureCount() {
			Account deleteAccount = Account.builder().loginFailureCount(new LoginFailureCount(2)).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("login_failure_count=2");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("login_failure_count<>2");
			assertEquals(11, actualRestData.size());
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：削除対象のレコードなし")
		void delete_not_found() {
			Account deleteAccount = Account.builder().accountNo(new AccountNo(99L)).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(0, actual);
			
			List<Account> actualData = getAccountList("account_no=99");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("account_no<>99");
			assertEquals(12, actualRestData.size());
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：2件以上deleteする場合")
		void delete_accounts() {
			Account deleteAccount = Account.builder().authorityKbn(AuthorityEnum.SPECIAL).build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(2, actual);
			
			List<Account> actualData = getAccountList("authority_kbn='special-user'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("authority_kbn<>'special-user'");
			assertEquals(10, actualRestData.size());
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でdeleteする場合")
		void delete_some_conditions() {
			Account deleteAccount = Account.builder()
					.accountId(new AccountId("llllllll"))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Okinawa"))
					.residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Tokyo"))
					.freeMemo(new FreeMemo("よろしく"))
					.build();
			Integer actual = accountMapper.delete(deleteAccount);
			assertEquals(1, actual);
			
			List<Account> actualData
				= getAccountList("account_id='llllllll'"
						+ " and sex_kbn='woman'"
						+ " and birthplace_prefecture_kbn_code='Okinawa'"
						+ " and resident_prefecture_kbn_code='Tokyo'"
						+ " and free_memo='よろしく'");
			assertEquals(0, actualData.size());
			
			List<Account> actualRestData = getAccountList("account_id<>'llllllll'"
					+ " and sex_kbn<>'woman'"
					+ " and birthplace_prefecture_kbn_code<>'Okinawa'"
					+ " and resident_prefecture_kbn_code<>'Tokyo'"
					+ " and free_memo<>'よろしく'");
			assertEquals(10, actualRestData.size());
		}
	}
	
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/AccountMapperTest.sql")
	class isExistAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントIDが一致するアカウントが存在する")
		void isExistAccount_by_accountId_exists() {
			Account account = Account.builder().accountId(new AccountId("aaaaaaaa")).build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertTrue(isExist);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントIDが一致するアカウントが存在しない")
		void isExistAccount_by_accountId_not_exist() {
			Account account = Account.builder().accountId(new AccountId("xxxxxxxx")).build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertFalse(isExist);
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウント番号以外で、アカウントIDが一致するアカウントが存在しない")
		void isExistAccount_by_accountId_and_accountNo_exists() {
			Account account = Account.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertFalse(isExist);
		}
		
		@Test
		@DisplayName("正常系：アカウント番号以外で、アカウントIDが一致するアカウントが存在する")
		void isExistAccount_by_accountId_and_accountNo_not_exist() {
			Account account = Account.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("bbbbbbbb"))
					.build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertTrue(isExist);
		}
	}
}