package com.web.gallary.mapper;

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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.SexEnum;

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
			Account account = Account.builder().accountNo(1).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().isDeleted(true).build();
			List<Account> actual = accountMapper.select(account);
		
			Account expectedAccount = Account.builder()
					.accountNo(9)
					.createdBy(9)
					.createdAt(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(9)
					.updatedAt(OffsetDateTime.of(2001, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(true)
					.accountId("iiiiiiii")
					.accountName("IIIIIIII")
					.password("$2a$10$password9")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.SPECIAL)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().accountId("aaaaaaaa").build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().accountName("AAAAAAAA").build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().password("$2a$10$password1").build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
					.accountNo(2)
					.createdBy(2)
					.createdAt(OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(2)
					.updatedAt(OffsetDateTime.of(2001, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.MAN)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().birthplacePrefectureKbnCode("Hokkaido").build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(3)
					.createdBy(3)
					.createdAt(OffsetDateTime.of(2000, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(3)
					.updatedAt(OffsetDateTime.of(2001, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("cccccccc")
					.accountName("CCCCCCCC")
					.password("$2a$10$password3")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().residentPrefectureKbnCode("Okinawa").build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(4)
					.createdBy(4)
					.createdAt(OffsetDateTime.of(2000, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(4)
					.updatedAt(OffsetDateTime.of(2001, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("dddddddd")
					.accountName("DDDDDDDD")
					.password("$2a$10$password4")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("Okinawa")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().freeMemo("フリーメモ").build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(5)
					.createdBy(5)
					.createdAt(OffsetDateTime.of(2000, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(5)
					.updatedAt(OffsetDateTime.of(2001, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("eeeeeeee")
					.accountName("EEEEEEEE")
					.password("$2a$10$password5")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("フリーメモ")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
					.accountNo(6)
					.createdBy(6)
					.createdAt(OffsetDateTime.of(2000, 1, 6, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(6)
					.updatedAt(OffsetDateTime.of(2001, 1, 6, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("ffffffff")
					.accountName("FFFFFFFF")
					.password("$2a$10$password6")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.MINI)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(7)
					.createdBy(7)
					.createdAt(OffsetDateTime.of(2000, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(7)
					.updatedAt(OffsetDateTime.of(2001, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("gggggggg")
					.accountName("GGGGGGGG")
					.password("$2a$10$password7")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
			Account account = Account.builder().loginFailureCount(2).build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount = Account.builder()
					.accountNo(8)
					.createdBy(8)
					.createdAt(OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(8)
					.updatedAt(OffsetDateTime.of(2001, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("hhhhhhhh")
					.accountName("HHHHHHHH")
					.password("$2a$10$password8")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(2)
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
			Account account = Account.builder().accountNo(99).build();
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
					.accountNo(9)
					.createdBy(9)
					.createdAt(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(9)
					.updatedAt(OffsetDateTime.of(2001, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(true)
					.accountId("iiiiiiii")
					.accountName("IIIIIIII")
					.password("$2a$10$password9")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.SPECIAL)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();
			
			Account expectedAccount2 = Account.builder()
					.accountNo(10)
					.createdBy(10)
					.createdAt(OffsetDateTime.of(2000, 1, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(10)
					.updatedAt(OffsetDateTime.of(2001, 1, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("jjjjjjjj")
					.accountName("JJJJJJJJ")
					.password("$2a$10$password10")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.SPECIAL)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
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
					.accountId("llllllll")
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Okinawa")
					.residentPrefectureKbnCode("Tokyo")
					.freeMemo("よろしく")
					.build();
			List<Account> actual = accountMapper.select(account);
			
			Account expectedAccount1 = Account.builder()
					.accountNo(12)
					.createdBy(12)
					.createdAt(OffsetDateTime.of(2000, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(12)
					.updatedAt(OffsetDateTime.of(2001, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("llllllll")
					.accountName("LLLLLLLL")
					.password("$2a$10$password12")
					.birthdate(LocalDate.of(1900, 1, 1))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Okinawa")
					.residentPrefectureKbnCode("Tokyo")
					.freeMemo("よろしく")
					.authorityKbn(AuthorityEnum.NORMAL)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(3)
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
			Account account = Account.builder().accountNo(1).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：削除フラグでのcount")
		void count_by_isDeleted() {
			Account account = Account.builder().isDeleted(true).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウントIDでのcount")
		void count_by_accountId() {
			Account account = Account.builder().accountId("aaaaaaaa").build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：アカウント名でのcount")
		void count_by_accountName() {
			Account account = Account.builder().accountName("AAAAAAAA").build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：パスワードでのcount")
		void count_by_password() {
			Account account = Account.builder().password("$2a$10$password1").build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：生年月日でのcount")
		void count_by_birthdate() {
			Account account = Account.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
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
			Account account = Account.builder().birthplacePrefectureKbnCode("Hokkaido").build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：在住都道府県区分コードでのcount")
		void count_by_residentPrefectureKbnCode() {
			Account account = Account.builder().residentPrefectureKbnCode("Okinawa").build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：フリーメモでのcount")
		void count_by_freeMemo() {
			Account account = Account.builder().freeMemo("フリーメモ").build();
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
					.lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：ログイン失敗回数でのcount")
		void count_by_loginFailureCount() {
			Account account = Account.builder().loginFailureCount(2).build();
			Integer actual = accountMapper.count(account);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：countで0件の場合")
		void count_not_found() {
			Account account = Account.builder().accountNo(99).build();
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
					.accountId("llllllll")
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Okinawa")
					.residentPrefectureKbnCode("Tokyo")
					.freeMemo("よろしく")
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
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("Okinawa")
					.freeMemo("フリーメモ")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
					.loginFailureCount(0)
					.build();
			
			Integer actualCount = accountMapper.insert(insertAccount);
			assertEquals(1, actualCount);
			
			List<Account> actualData = jdbcTemplate.query(
					"SELECT * FROM common.account", (rs, rowNum) ->
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
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals("Hokkaido", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
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
		@DisplayName("正常系：アカウント番号でのupdate")
		void update_by_accountNo() {
			Account conditionAccount = Account.builder().accountNo(1).build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_no=1");
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：削除フラグでのupdate")
		void update_by_isDeleted() {
			Account conditionAccount = Account.builder().isDeleted(true).build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("is_deleted=true");
			assertEquals(1, actualData.size());
			assertEquals(9, actualData.getFirst().getAccountNo());
			assertEquals(9, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(9, actualData.getFirst().getUpdatedBy());
			assertTrue(actualData.getFirst().getIsDeleted());
			assertEquals("iiiiiiii", actualData.getFirst().getAccountId());
			assertEquals("IIIIIIII", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password9", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.SPECIAL, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウントIDでのupdate")
		void update_by_accountId() {
			Account conditionAccount = Account.builder().accountId("aaaaaaaa").build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_id='aaaaaaaa'");
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：アカウント名でのupdate")
		void update_by_accountName() {
			Account conditionAccount = Account.builder().accountName("AAAAAAAA").build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_name='AAAAAAAA'");
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：パスワードでのupdate")
		void update_by_password() {
			Account conditionAccount = Account.builder().password("$2a$10$password1").build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("password='$2a$10$password1'");
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：生年月日でのupdate")
		void update_by_birthdate() {
			Account conditionAccount = Account.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("birthdate='1991-02-14'");
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：性別区分コードでのupdate")
		void update_by_sexKbnCode() {
			Account conditionAccount = Account.builder().sexKbn(SexEnum.MAN).build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("sex_kbn='man'");
			assertEquals(1, actualData.size());
			assertEquals(2, actualData.getFirst().getAccountNo());
			assertEquals(2, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(2, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("bbbbbbbb", actualData.getFirst().getAccountId());
			assertEquals("BBBBBBBB", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password2", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.MAN, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：出身都道府県区分コードでのupdate")
		void update_by_birthplacePrefectureKbnCode() {
			Account conditionAccount = Account.builder().birthplacePrefectureKbnCode("Hokkaido").build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("birthplace_prefecture_kbn_code='Hokkaido'");
			assertEquals(1, actualData.size());
			assertEquals(3, actualData.getFirst().getAccountNo());
			assertEquals(3, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(3, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("cccccccc", actualData.getFirst().getAccountId());
			assertEquals("CCCCCCCC", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password3", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("Hokkaido", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：在住都道府県区分コードでのupdate")
		void update_by_residentPrefectureKbnCode() {
			Account conditionAccount = Account.builder().residentPrefectureKbnCode("Okinawa").build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("resident_prefecture_kbn_code='Okinawa'");
			assertEquals(1, actualData.size());
			assertEquals(4, actualData.getFirst().getAccountNo());
			assertEquals(4, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(4, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("dddddddd", actualData.getFirst().getAccountId());
			assertEquals("DDDDDDDD", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password4", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：フリーメモでのupdate")
		void update_by_freeMemo() {
			Account conditionAccount = Account.builder().freeMemo("フリーメモ").build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("free_memo='フリーメモ'");
			assertEquals(1, actualData.size());
			assertEquals(5, actualData.getFirst().getAccountNo());
			assertEquals(5, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(5, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("eeeeeeee", actualData.getFirst().getAccountId());
			assertEquals("EEEEEEEE", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password5", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：権限区分コードでのupdate")
		void update_by_authorityKbnCode() {
			Account conditionAccount = Account.builder().authorityKbn(AuthorityEnum.MINI).build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("authority_kbn='mini-user'");
			assertEquals(1, actualData.size());
			assertEquals(6, actualData.getFirst().getAccountNo());
			assertEquals(6, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 6, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(6, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("ffffffff", actualData.getFirst().getAccountId());
			assertEquals("FFFFFFFF", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password6", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：最終ログイン日時でのupdate")
		void update_by_lastLoginDatetime() {
			Account conditionAccount = Account.builder()
					.lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.build();
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("last_login_datetime='2024-01-01 00:00:00.000 +0000'");
			assertEquals(1, actualData.size());
			assertEquals(7, actualData.getFirst().getAccountNo());
			assertEquals(7, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(7, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("gggggggg", actualData.getFirst().getAccountId());
			assertEquals("GGGGGGGG", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password7", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(1, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：ログイン失敗回数でのupdate")
		void update_by_loginFailureCounte() {
			Account conditionAccount = Account.builder().loginFailureCount(2).build();
			Account targetAccount = Account.builder().loginFailureCount(0).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_no=8");
			assertEquals(1, actualData.size());
			assertEquals(8, actualData.getFirst().getAccountNo());
			assertEquals(8, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(8, actualData.getFirst().getUpdatedBy());
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
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：更新対象のレコードなし")
		void update_not_found() {
			Account conditionAccount = Account.builder().accountNo(99).build();
			Account targetAccount = Account.builder().loginFailureCount(0).build();
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
			Account targetAccount = Account.builder().loginFailureCount(1).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(2, actual);
			
			List<Account> actualData = getAccountList("authority_kbn='special-user' order by account_no");
			assertEquals(2, actualData.size());
			assertEquals(9, actualData.get(0).getAccountNo());
			assertEquals(9, actualData.get(0).getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getCreatedAt());
			assertEquals(9, actualData.get(0).getUpdatedBy());
			assertTrue(actualData.get(0).getIsDeleted());
			assertEquals("iiiiiiii", actualData.get(0).getAccountId());
			assertEquals("IIIIIIII", actualData.get(0).getAccountName());
			assertEquals("$2a$10$password9", actualData.get(0).getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.get(0).getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.get(0).getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.get(0).getResidentPrefectureKbnCode());
			assertEquals("", actualData.get(0).getFreeMemo());
			assertEquals(AuthorityEnum.SPECIAL, actualData.get(0).getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getLastLoginDatetime());
			assertEquals(1, actualData.get(0).getLoginFailureCount());
			
			assertEquals(10, actualData.get(1).getAccountNo());
			assertEquals(10, actualData.get(1).getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(1).getCreatedAt());
			assertEquals(10, actualData.get(1).getUpdatedBy());
			assertFalse(actualData.get(1).getIsDeleted());
			assertEquals("jjjjjjjj", actualData.get(1).getAccountId());
			assertEquals("JJJJJJJJ", actualData.get(1).getAccountName());
			assertEquals("$2a$10$password10", actualData.get(1).getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.get(1).getBirthdate());
			assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
			assertEquals("none", actualData.get(1).getBirthplacePrefectureKbnCode());
			assertEquals("none", actualData.get(1).getResidentPrefectureKbnCode());
			assertEquals("", actualData.get(1).getFreeMemo());
			assertEquals(AuthorityEnum.SPECIAL, actualData.get(1).getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(1).getLastLoginDatetime());
			assertEquals(1, actualData.get(1).getLoginFailureCount());
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でupdateする場合")
		void update_some_conditions() {
			Account conditionAccount = Account.builder()
					.accountId("llllllll")
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Okinawa")
					.residentPrefectureKbnCode("Tokyo")
					.freeMemo("よろしく")
					.build();
			Account targetAccount = Account.builder().loginFailureCount(0).build();
			Integer actual = accountMapper.update(conditionAccount, targetAccount);
			assertEquals(1, actual);
			
			List<Account> actualData = getAccountList("account_id='llllllll'");
			assertEquals(1, actualData.size());
			assertEquals(12, actualData.getFirst().getAccountNo());
			assertEquals(12, actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt());
			assertEquals(12, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals("llllllll", actualData.getFirst().getAccountId());
			assertEquals("LLLLLLLL", actualData.getFirst().getAccountName());
			assertEquals("$2a$10$password12", actualData.getFirst().getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
			assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
			assertEquals("Okinawa", actualData.getFirst().getBirthplacePrefectureKbnCode());
			assertEquals("Tokyo", actualData.getFirst().getResidentPrefectureKbnCode());
			assertEquals("よろしく", actualData.getFirst().getFreeMemo());
			assertEquals(AuthorityEnum.NORMAL, actualData.getFirst().getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getLastLoginDatetime());
			assertEquals(0, actualData.getFirst().getLoginFailureCount());
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
		@DisplayName("正常系：アカウント番号でのdelete")
		void delete_by_accountNo() {
			Account deleteAccount = Account.builder().accountNo(1).build();
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
			Account deleteAccount = Account.builder().isDeleted(true).build();
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
			Account deleteAccount = Account.builder().accountId("aaaaaaaa").build();
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
			Account deleteAccount = Account.builder().accountName("AAAAAAAA").build();
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
			Account deleteAccount = Account.builder().password("$2a$10$password1").build();
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
			Account deleteAccount = Account.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
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
			Account deleteAccount = Account.builder().birthplacePrefectureKbnCode("Hokkaido").build();
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
			Account deleteAccount = Account.builder().residentPrefectureKbnCode("Okinawa").build();
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
			Account deleteAccount = Account.builder().freeMemo("フリーメモ").build();
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
					.lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
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
			Account deleteAccount = Account.builder().loginFailureCount(2).build();
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
			Account deleteAccount = Account.builder().accountNo(99).build();
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
					.accountId("llllllll")
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Okinawa")
					.residentPrefectureKbnCode("Tokyo")
					.freeMemo("よろしく")
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
			Account account = Account.builder().accountId("aaaaaaaa").build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertTrue(isExist);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントIDが一致するアカウントが存在しない")
		void isExistAccount_by_accountId_not_exist() {
			Account account = Account.builder().accountId("xxxxxxxx").build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertFalse(isExist);
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：アカウント番号以外で、アカウントIDが一致するアカウントが存在しない")
		void isExistAccount_by_accountId_and_accountNo_exists() {
			Account account = Account.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertFalse(isExist);
		}
		
		@Test
		@DisplayName("正常系：アカウント番号以外で、アカウントIDが一致するアカウントが存在する")
		void isExistAccount_by_accountId_and_accountNo_not_exist() {
			Account account = Account.builder()
					.accountNo(1)
					.accountId("bbbbbbbb")
					.build();
			Boolean isExist = accountMapper.isExistAccount(account);
			assertTrue(isExist);
		}
	}
}