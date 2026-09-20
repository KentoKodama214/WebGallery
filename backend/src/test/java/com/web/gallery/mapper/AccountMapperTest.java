package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.dto.AccountDto;
import com.web.gallery.entity.account.Account;
import com.web.gallery.entity.account.AccountCondition;
import com.web.gallery.entity.account.AccountUpdateTarget;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountMapperTest {
  @Autowired private AccountMapper accountMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

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
      AccountCondition account = AccountCondition.builder().accountNo(1L).build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(1L);
      expectedAccount.setCreatedBy(1L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(1L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("aaaaaaaa");
      expectedAccount.setAccountName("AAAAAAAA");
      expectedAccount.setPassword("$2a$10$password1");
      expectedAccount.setBirthdate(LocalDate.of(1991, 2, 14));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：削除フラグでのselectで1件の場合")
    void select_by_isDeleted() {
      AccountCondition account = AccountCondition.builder().isDeleted(true).build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(9L);
      expectedAccount.setCreatedBy(9L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(9L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(true);
      expectedAccount.setAccountId("iiiiiiii");
      expectedAccount.setAccountName("IIIIIIII");
      expectedAccount.setPassword("$2a$10$password9");
      expectedAccount.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.SPECIAL);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウントIDでのselectで1件の場合")
    void select_by_accountId() {
      AccountCondition account = AccountCondition.builder().accountId("aaaaaaaa").build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(1L);
      expectedAccount.setCreatedBy(1L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(1L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("aaaaaaaa");
      expectedAccount.setAccountName("AAAAAAAA");
      expectedAccount.setPassword("$2a$10$password1");
      expectedAccount.setBirthdate(LocalDate.of(1991, 2, 14));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(4)
    @DisplayName("正常系：アカウント名でのselectで1件の場合")
    void select_by_accountName() {
      AccountCondition account = AccountCondition.builder().accountName("AAAAAAAA").build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(1L);
      expectedAccount.setCreatedBy(1L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(1L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("aaaaaaaa");
      expectedAccount.setAccountName("AAAAAAAA");
      expectedAccount.setPassword("$2a$10$password1");
      expectedAccount.setBirthdate(LocalDate.of(1991, 2, 14));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(5)
    @DisplayName("正常系：パスワードでのselectで1件の場合")
    void select_by_password() {
      AccountCondition account = AccountCondition.builder().password("$2a$10$password1").build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(1L);
      expectedAccount.setCreatedBy(1L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(1L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("aaaaaaaa");
      expectedAccount.setAccountName("AAAAAAAA");
      expectedAccount.setPassword("$2a$10$password1");
      expectedAccount.setBirthdate(LocalDate.of(1991, 2, 14));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(6)
    @DisplayName("正常系：生年月日でのselectで1件の場合")
    void select_by_birthdate() {
      AccountCondition account =
          AccountCondition.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(1L);
      expectedAccount.setCreatedBy(1L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(1L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("aaaaaaaa");
      expectedAccount.setAccountName("AAAAAAAA");
      expectedAccount.setPassword("$2a$10$password1");
      expectedAccount.setBirthdate(LocalDate.of(1991, 2, 14));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(7)
    @DisplayName("正常系：性別区分コードでのselectで1件の場合")
    void select_by_sexKbnCode() {
      AccountCondition account = AccountCondition.builder().sexKbn(SexEnum.MAN).build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(2L);
      expectedAccount.setCreatedBy(2L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(2L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("bbbbbbbb");
      expectedAccount.setAccountName("BBBBBBBB");
      expectedAccount.setPassword("$2a$10$password2");
      expectedAccount.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount.setSexKbn(SexEnum.MAN);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(8)
    @DisplayName("正常系：出身都道府県区分コードでのselectで1件の場合")
    void select_by_birthplacePrefectureKbnCode() {
      AccountCondition account =
          AccountCondition.builder().birthplacePrefectureKbnCode("Hokkaido").build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(3L);
      expectedAccount.setCreatedBy(3L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(3L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("cccccccc");
      expectedAccount.setAccountName("CCCCCCCC");
      expectedAccount.setPassword("$2a$10$password3");
      expectedAccount.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("Hokkaido");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(9)
    @DisplayName("正常系：在住都道府県区分コードでのselectで1件の場合")
    void select_by_residentPrefectureKbnCode() {
      AccountCondition account =
          AccountCondition.builder().residentPrefectureKbnCode("Okinawa").build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(4L);
      expectedAccount.setCreatedBy(4L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(4L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("dddddddd");
      expectedAccount.setAccountName("DDDDDDDD");
      expectedAccount.setPassword("$2a$10$password4");
      expectedAccount.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("Okinawa");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(10)
    @DisplayName("正常系：フリーメモでのselectで1件の場合")
    void select_by_freeMemo() {
      AccountCondition account = AccountCondition.builder().freeMemo("フリーメモ").build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(5L);
      expectedAccount.setCreatedBy(5L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(5L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("eeeeeeee");
      expectedAccount.setAccountName("EEEEEEEE");
      expectedAccount.setPassword("$2a$10$password5");
      expectedAccount.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("フリーメモ");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(12)
    @DisplayName("正常系：最終ログイン日時でのselectで1件の場合")
    void select_by_lastLoginDatetime() {
      AccountCondition account =
          AccountCondition.builder()
              .lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
              .build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(7L);
      expectedAccount.setCreatedBy(7L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(7L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("gggggggg");
      expectedAccount.setAccountName("GGGGGGGG");
      expectedAccount.setPassword("$2a$10$password7");
      expectedAccount.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(0);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(13)
    @DisplayName("正常系：ログイン失敗回数でのselectで1件の場合")
    void select_by_loginFailureCount() {
      AccountCondition account = AccountCondition.builder().loginFailureCount(2).build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount = new AccountDto();
      expectedAccount.setAccountNo(8L);
      expectedAccount.setCreatedBy(8L);
      expectedAccount.setCreatedAt(
          OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setUpdatedBy(8L);
      expectedAccount.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setIsDeleted(false);
      expectedAccount.setAccountId("hhhhhhhh");
      expectedAccount.setAccountName("HHHHHHHH");
      expectedAccount.setPassword("$2a$10$password8");
      expectedAccount.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount.setSexKbn(SexEnum.NONE);
      expectedAccount.setBirthplacePrefectureKbnCode("none");
      expectedAccount.setResidentPrefectureKbnCode("none");
      expectedAccount.setFreeMemo("");
      expectedAccount.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      expectedAccount.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount.setLoginFailureCount(2);
      expectedAccount.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(14)
    @DisplayName("正常系：selectで0件の場合")
    void select_not_found() {
      AccountCondition account = AccountCondition.builder().accountNo(99L).build();
      List<AccountDto> actual = accountMapper.select(account);
      List<AccountDto> expected = new ArrayList<AccountDto>();

      assertEquals(0, actual.size());
      assertEquals(expected, actual);
    }

    @Test
    @Order(16)
    @DisplayName("正常系：複数の条件でselectする場合")
    void select_some_conditions() {
      AccountCondition account =
          AccountCondition.builder()
              .accountId("llllllll")
              .sexKbn(SexEnum.WOMAN)
              .birthplacePrefectureKbnCode("Okinawa")
              .residentPrefectureKbnCode("Tokyo")
              .freeMemo("よろしく")
              .build();
      List<AccountDto> actual = accountMapper.select(account);

      AccountDto expectedAccount1 = new AccountDto();
      expectedAccount1.setAccountNo(12L);
      expectedAccount1.setCreatedBy(12L);
      expectedAccount1.setCreatedAt(
          OffsetDateTime.of(2000, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount1.setUpdatedBy(12L);
      expectedAccount1.setUpdatedAt(
          OffsetDateTime.of(2001, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount1.setIsDeleted(false);
      expectedAccount1.setAccountId("llllllll");
      expectedAccount1.setAccountName("LLLLLLLL");
      expectedAccount1.setPassword("$2a$10$password12");
      expectedAccount1.setBirthdate(LocalDate.of(1900, 1, 1));
      expectedAccount1.setSexKbn(SexEnum.WOMAN);
      expectedAccount1.setBirthplacePrefectureKbnCode("Okinawa");
      expectedAccount1.setResidentPrefectureKbnCode("Tokyo");
      expectedAccount1.setFreeMemo("よろしく");
      expectedAccount1.setAuthorityKbn(AuthorityEnum.NORMAL);
      expectedAccount1.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      expectedAccount1.setLoginFailureCount(3);
      expectedAccount1.setIsAdminLocked(false);

      List<AccountDto> expected = new ArrayList<AccountDto>();
      expected.add(expectedAccount1);

      assertEquals(1, actual.size());
      assertEquals(expected, actual);
    }
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/AccountMapperTest.sql")
  class selectList {
    @Test
    @Order(1)
    @DisplayName("正常系：一覧取得ではパスワードハッシュを射影せず、is_deleted・limit・offsetが適用されること")
    void selectList_excludes_password_and_applies_paging() {
      AccountCondition condition =
          AccountCondition.builder().isDeleted(false).limit(2).offset(0).build();

      List<AccountDto> actual = accountMapper.selectList(condition);

      assertEquals(2, actual.size());
      // パスワードハッシュはDBから取得しない
      assertNull(actual.get(0).getPassword());
      assertNull(actual.get(1).getPassword());
      // account_id ASC でソートされ、offset/limit が適用される
      assertEquals("aaaaaaaa", actual.get(0).getAccountId());
      assertEquals("bbbbbbbb", actual.get(1).getAccountId());
      // 削除済みアカウントは含まれない
      assertFalse(actual.get(0).getIsDeleted());
      assertFalse(actual.get(1).getIsDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当0件の場合は空リストを返すこと")
    void selectList_not_found() {
      AccountCondition condition =
          AccountCondition.builder().accountNo(99L).limit(5).offset(0).build();

      assertEquals(0, accountMapper.selectList(condition).size());
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
      AccountCondition account = AccountCondition.builder().accountNo(1L).build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：削除フラグでのcount")
    void count_by_isDeleted() {
      AccountCondition account = AccountCondition.builder().isDeleted(true).build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウントIDでのcount")
    void count_by_accountId() {
      AccountCondition account = AccountCondition.builder().accountId("aaaaaaaa").build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(4)
    @DisplayName("正常系：アカウント名でのcount")
    void count_by_accountName() {
      AccountCondition account = AccountCondition.builder().accountName("AAAAAAAA").build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(5)
    @DisplayName("正常系：パスワードでのcount")
    void count_by_password() {
      AccountCondition account = AccountCondition.builder().password("$2a$10$password1").build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(6)
    @DisplayName("正常系：生年月日でのcount")
    void count_by_birthdate() {
      AccountCondition account =
          AccountCondition.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(7)
    @DisplayName("正常系：性別区分コードでのcount")
    void count_by_sexKbnCode() {
      AccountCondition account = AccountCondition.builder().sexKbn(SexEnum.MAN).build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(8)
    @DisplayName("正常系：出身都道府県区分コードでのcount")
    void count_by_birthplacePrefectureKbnCode() {
      AccountCondition account =
          AccountCondition.builder().birthplacePrefectureKbnCode("Hokkaido").build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(9)
    @DisplayName("正常系：在住都道府県区分コードでのcount")
    void count_by_residentPrefectureKbnCode() {
      AccountCondition account =
          AccountCondition.builder().residentPrefectureKbnCode("Okinawa").build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(10)
    @DisplayName("正常系：フリーメモでのcount")
    void count_by_freeMemo() {
      AccountCondition account = AccountCondition.builder().freeMemo("フリーメモ").build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(12)
    @DisplayName("正常系：最終ログイン日時でのcount")
    void count_by_lastLoginDatetime() {
      AccountCondition account =
          AccountCondition.builder()
              .lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
              .build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(13)
    @DisplayName("正常系：ログイン失敗回数でのcount")
    void count_by_loginFailureCount() {
      AccountCondition account = AccountCondition.builder().loginFailureCount(2).build();
      Integer actual = accountMapper.count(account);
      assertEquals(1, actual);
    }

    @Test
    @Order(14)
    @DisplayName("正常系：countで0件の場合")
    void count_not_found() {
      AccountCondition account = AccountCondition.builder().accountNo(99L).build();
      Integer actual = accountMapper.count(account);
      assertEquals(0, actual);
    }

    @Test
    @Order(15)
    @DisplayName("正常系：countで2件以上の場合")
    void count_accounts() {
      AccountCondition account = AccountCondition.builder().loginFailureCount(3).build();
      Integer actual = accountMapper.count(account);
      assertEquals(2, actual);
    }

    @Test
    @Order(16)
    @DisplayName("正常系：複数の条件でcountする場合")
    void count_some_conditions() {
      AccountCondition account =
          AccountCondition.builder()
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
      Account insertAccount =
          Account.builder()
              .accountNo(1L)
              .createdBy(1L)
              .createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
              .updatedBy(1L)
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
              .lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
              .loginFailureCount(0)
              .isAdminLocked(false)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = accountMapper.insert(insertAccount);
      assertEquals(1, actualCount);

      List<Account> actualData =
          jdbcTemplate.query(
              "SELECT * FROM common.account",
              (rs, rowNum) ->
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
                      .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                      .loginFailureCount(rs.getInt("login_failure_count"))
                      .isAdminLocked(rs.getBoolean("is_admin_locked"))
                      .build());

      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("aaaaaaaa", actualData.getFirst().getAccountId());
      assertEquals("AAAAAAAA", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password1", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1991, 2, 14), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
      assertEquals("Hokkaido", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("Okinawa", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("フリーメモ", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
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
          "SELECT * FROM common.account WHERE " + condition,
          (rs, rowNum) ->
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
                  .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                  .loginFailureCount(rs.getInt("login_failure_count"))
                  .isAdminLocked(rs.getBoolean("is_admin_locked"))
                  .build());
    }

    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号でのupdate")
    void update_by_accountNo() {
      AccountCondition conditionAccount = AccountCondition.builder().accountNo(1L).build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("account_no=1");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
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
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：削除フラグでのupdate")
    void update_by_isDeleted() {
      AccountCondition conditionAccount = AccountCondition.builder().isDeleted(true).build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("is_deleted=true");
      assertEquals(1, actualData.size());
      assertEquals(9L, actualData.getFirst().getAccountNo());
      assertEquals(9L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 9, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(9L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertTrue(actualData.getFirst().getIsDeleted());
      assertEquals("iiiiiiii", actualData.getFirst().getAccountId());
      assertEquals("IIIIIIII", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password9", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウントIDでのupdate")
    void update_by_accountId() {
      AccountCondition conditionAccount = AccountCondition.builder().accountId("aaaaaaaa").build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("account_id='aaaaaaaa'");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
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
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：アカウント名でのupdate")
    void update_by_accountName() {
      AccountCondition conditionAccount =
          AccountCondition.builder().accountName("AAAAAAAA").build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("account_name='AAAAAAAA'");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
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
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：パスワードでのupdate")
    void update_by_password() {
      AccountCondition conditionAccount =
          AccountCondition.builder().password("$2a$10$password1").build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("password='$2a$10$password1'");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
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
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(6)
    @DisplayName("正常系：生年月日でのupdate")
    void update_by_birthdate() {
      AccountCondition conditionAccount =
          AccountCondition.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("birthdate='1991-02-14'");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
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
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(7)
    @DisplayName("正常系：性別区分コードでのupdate")
    void update_by_sexKbnCode() {
      AccountCondition conditionAccount = AccountCondition.builder().sexKbn(SexEnum.MAN).build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("sex_kbn='man'");
      assertEquals(1, actualData.size());
      assertEquals(2L, actualData.getFirst().getAccountNo());
      assertEquals(2L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(2L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("bbbbbbbb", actualData.getFirst().getAccountId());
      assertEquals("BBBBBBBB", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password2", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.MAN, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(8)
    @DisplayName("正常系：出身都道府県区分コードでのupdate")
    void update_by_birthplacePrefectureKbnCode() {
      AccountCondition conditionAccount =
          AccountCondition.builder().birthplacePrefectureKbnCode("Hokkaido").build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("birthplace_prefecture_kbn_code='Hokkaido'");
      assertEquals(1, actualData.size());
      assertEquals(3L, actualData.getFirst().getAccountNo());
      assertEquals(3L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 3, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(3L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("cccccccc", actualData.getFirst().getAccountId());
      assertEquals("CCCCCCCC", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password3", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("Hokkaido", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(9)
    @DisplayName("正常系：在住都道府県区分コードでのupdate")
    void update_by_residentPrefectureKbnCode() {
      AccountCondition conditionAccount =
          AccountCondition.builder().residentPrefectureKbnCode("Okinawa").build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("resident_prefecture_kbn_code='Okinawa'");
      assertEquals(1, actualData.size());
      assertEquals(4L, actualData.getFirst().getAccountNo());
      assertEquals(4L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 4, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(4L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("dddddddd", actualData.getFirst().getAccountId());
      assertEquals("DDDDDDDD", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password4", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("Okinawa", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(10)
    @DisplayName("正常系：フリーメモでのupdate")
    void update_by_freeMemo() {
      AccountCondition conditionAccount = AccountCondition.builder().freeMemo("フリーメモ").build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("free_memo='フリーメモ'");
      assertEquals(1, actualData.size());
      assertEquals(5L, actualData.getFirst().getAccountNo());
      assertEquals(5L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 5, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(5L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("eeeeeeee", actualData.getFirst().getAccountId());
      assertEquals("EEEEEEEE", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password5", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("フリーメモ", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(12)
    @DisplayName("正常系：最終ログイン日時でのupdate")
    void update_by_lastLoginDatetime() {
      AccountCondition conditionAccount =
          AccountCondition.builder()
              .lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
              .build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(1).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData =
          getAccountList("last_login_datetime='2024-01-01 00:00:00.000 +0000'");
      assertEquals(1, actualData.size());
      assertEquals(7L, actualData.getFirst().getAccountNo());
      assertEquals(7L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 7, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(7L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("gggggggg", actualData.getFirst().getAccountId());
      assertEquals("GGGGGGGG", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password7", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.NONE, actualData.getFirst().getSexKbn());
      assertEquals("none", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("none", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(1, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(13)
    @DisplayName("正常系：ログイン失敗回数でのupdate")
    void update_by_loginFailureCounte() {
      AccountCondition conditionAccount = AccountCondition.builder().loginFailureCount(2).build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(0).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("account_no=8");
      assertEquals(1, actualData.size());
      assertEquals(8L, actualData.getFirst().getAccountNo());
      assertEquals(8L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 8, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
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
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(0, actualData.getFirst().getLoginFailureCount());
    }

    @Test
    @Order(14)
    @DisplayName("正常系：更新対象のレコードなし")
    void update_not_found() {
      AccountCondition conditionAccount = AccountCondition.builder().accountNo(99L).build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(0).build();
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(0, actual);

      List<Account> actualData = getAccountList("account_no=99");
      assertEquals(0, actualData.size());
    }

    @Test
    @Order(16)
    @DisplayName("正常系：複数の条件でupdateする場合")
    void update_some_conditions() {
      AccountCondition conditionAccount =
          AccountCondition.builder()
              .accountId("llllllll")
              .sexKbn(SexEnum.WOMAN)
              .birthplacePrefectureKbnCode("Okinawa")
              .residentPrefectureKbnCode("Tokyo")
              .freeMemo("よろしく")
              .build();
      AccountUpdateTarget targetAccount =
          AccountUpdateTarget.builder().loginFailureCount(0).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = accountMapper.update(conditionAccount, targetAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("account_id='llllllll'");
      assertEquals(1, actualData.size());
      assertEquals(12L, actualData.getFirst().getAccountNo());
      assertEquals(12L, actualData.getFirst().getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getCreatedAt());
      assertEquals(12L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals("llllllll", actualData.getFirst().getAccountId());
      assertEquals("LLLLLLLL", actualData.getFirst().getAccountName());
      assertEquals("$2a$10$password12", actualData.getFirst().getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), actualData.getFirst().getBirthdate());
      assertEquals(SexEnum.WOMAN, actualData.getFirst().getSexKbn());
      assertEquals("Okinawa", actualData.getFirst().getBirthplacePrefectureKbnCode());
      assertEquals("Tokyo", actualData.getFirst().getResidentPrefectureKbnCode());
      assertEquals("よろしく", actualData.getFirst().getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getLastLoginDatetime());
      assertEquals(0, actualData.getFirst().getLoginFailureCount());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/AccountMapperTest.sql")
  class delete {
    @BeforeEach
    void clearAccountAuthority() {
      // account.delete()はaccount_authorityとの外部キー制約に抵触するため、
      // AccountAuthorityMapperを介した削除（AccountRepositoryImpl等の実運用フロー）を経ずに
      // Mapperを直接呼び出す本テストでは、事前にaccount_authorityを削除しておく
      jdbcTemplate.update("DELETE FROM common.account_authority");
    }

    private List<Account> getAccountList(String condition) {
      return jdbcTemplate.query(
          "SELECT * FROM common.account WHERE " + condition,
          (rs, rowNum) ->
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
                  .lastLoginDatetime(rs.getObject("last_login_datetime", OffsetDateTime.class))
                  .loginFailureCount(rs.getInt("login_failure_count"))
                  .isAdminLocked(rs.getBoolean("is_admin_locked"))
                  .build());
    }

    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号でのdelete")
    void delete_by_accountNo() {
      AccountCondition deleteAccount = AccountCondition.builder().accountNo(1L).build();
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
      AccountCondition deleteAccount = AccountCondition.builder().isDeleted(true).build();
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
      AccountCondition deleteAccount = AccountCondition.builder().accountId("aaaaaaaa").build();
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
      AccountCondition deleteAccount = AccountCondition.builder().accountName("AAAAAAAA").build();
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
      AccountCondition deleteAccount =
          AccountCondition.builder().password("$2a$10$password1").build();
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
      AccountCondition deleteAccount =
          AccountCondition.builder().birthdate(LocalDate.of(1991, 2, 14)).build();
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
      AccountCondition deleteAccount = AccountCondition.builder().sexKbn(SexEnum.MAN).build();
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
      AccountCondition deleteAccount =
          AccountCondition.builder().birthplacePrefectureKbnCode("Hokkaido").build();
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
      AccountCondition deleteAccount =
          AccountCondition.builder().residentPrefectureKbnCode("Okinawa").build();
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
      AccountCondition deleteAccount = AccountCondition.builder().freeMemo("フリーメモ").build();
      Integer actual = accountMapper.delete(deleteAccount);
      assertEquals(1, actual);

      List<Account> actualData = getAccountList("free_memo='フリーメモ'");
      assertEquals(0, actualData.size());

      List<Account> actualRestData = getAccountList("free_memo<>'フリーメモ'");
      assertEquals(11, actualRestData.size());
    }

    @Test
    @Order(12)
    @DisplayName("正常系：最終ログイン日時でのdelete")
    void delete_by_lastLoginDatetime() {
      AccountCondition deleteAccount =
          AccountCondition.builder()
              .lastLoginDatetime(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
              .build();
      Integer actual = accountMapper.delete(deleteAccount);
      assertEquals(1, actual);

      List<Account> actualData =
          getAccountList("last_login_datetime='2024-01-01 00:00:00.000 +0000'");
      assertEquals(0, actualData.size());

      List<Account> actualRestData =
          getAccountList("last_login_datetime<>'2024-01-01 00:00:00.000 +0000'");
      assertEquals(11, actualRestData.size());
    }

    @Test
    @Order(13)
    @DisplayName("正常系：ログイン失敗回数でのdelete")
    void delete_by_loginFailureCount() {
      AccountCondition deleteAccount = AccountCondition.builder().loginFailureCount(2).build();
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
      AccountCondition deleteAccount = AccountCondition.builder().accountNo(99L).build();
      Integer actual = accountMapper.delete(deleteAccount);
      assertEquals(0, actual);

      List<Account> actualData = getAccountList("account_no=99");
      assertEquals(0, actualData.size());

      List<Account> actualRestData = getAccountList("account_no<>99");
      assertEquals(12, actualRestData.size());
    }

    @Test
    @Order(16)
    @DisplayName("正常系：複数の条件でdeleteする場合")
    void delete_some_conditions() {
      AccountCondition deleteAccount =
          AccountCondition.builder()
              .accountId("llllllll")
              .sexKbn(SexEnum.WOMAN)
              .birthplacePrefectureKbnCode("Okinawa")
              .residentPrefectureKbnCode("Tokyo")
              .freeMemo("よろしく")
              .build();
      Integer actual = accountMapper.delete(deleteAccount);
      assertEquals(1, actual);

      List<Account> actualData =
          getAccountList(
              "account_id='llllllll'"
                  + " and sex_kbn='woman'"
                  + " and birthplace_prefecture_kbn_code='Okinawa'"
                  + " and resident_prefecture_kbn_code='Tokyo'"
                  + " and free_memo='よろしく'");
      assertEquals(0, actualData.size());

      List<Account> actualRestData =
          getAccountList(
              "account_id<>'llllllll'"
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
      AccountCondition account = AccountCondition.builder().accountId("aaaaaaaa").build();
      Boolean isExist = accountMapper.isExistAccount(account);
      assertTrue(isExist);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントIDが一致するアカウントが存在しない")
    void isExistAccount_by_accountId_not_exist() {
      AccountCondition account = AccountCondition.builder().accountId("xxxxxxxx").build();
      Boolean isExist = accountMapper.isExistAccount(account);
      assertFalse(isExist);
    }

    @Test
    @Order(3)
    @DisplayName("正常系：アカウント番号以外で、アカウントIDが一致するアカウントが存在しない")
    void isExistAccount_by_accountId_and_accountNo_exists() {
      AccountCondition account =
          AccountCondition.builder().accountNo(1L).accountId("aaaaaaaa").build();
      Boolean isExist = accountMapper.isExistAccount(account);
      assertFalse(isExist);
    }

    @Test
    @DisplayName("正常系：アカウント番号以外で、アカウントIDが一致するアカウントが存在する")
    void isExistAccount_by_accountId_and_accountNo_not_exist() {
      AccountCondition account =
          AccountCondition.builder().accountNo(1L).accountId("bbbbbbbb").build();
      Boolean isExist = accountMapper.isExistAccount(account);
      assertTrue(isExist);
    }
  }
}
