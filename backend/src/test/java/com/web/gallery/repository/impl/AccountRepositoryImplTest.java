package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.web.gallery.dto.AccountDto;
import com.web.gallery.entity.Account;
import com.web.gallery.entity.AccountAuthority;
import com.web.gallery.entity.AccountCondition;
import com.web.gallery.entity.AccountUpdateTarget;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.mapper.AccountAuthorityMapper;
import com.web.gallery.mapper.AccountMapper;
import com.web.gallery.model.AccountGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
import com.web.gallery.model.AccountPageModel;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountRepositoryImplTest {
  @InjectMocks private AccountRepositoryImpl accountRepositoryImpl;

  @Mock private AccountMapper accountMapper;

  @Mock private AccountAuthorityMapper accountAuthorityMapper;

  @Mock private PasswordEncoder passwordEncoder;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが取得できた場合")
    void getByAccountNo_found() {
      AccountDto account = new AccountDto();
      account.setAccountNo(1L);
      account.setCreatedBy(1L);
      account.setCreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account.setUpdatedBy(1L);
      account.setUpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account.setIsDeleted(false);
      account.setAccountId("aaaaaaaa");
      account.setAccountName("AAAAAAAA");
      account.setPassword("$2a$10$password1");
      account.setBirthdate(LocalDate.of(1991, 2, 14));
      account.setSexKbn(SexEnum.NONE);
      account.setBirthplacePrefectureKbnCode("");
      account.setResidentPrefectureKbnCode("");
      account.setFreeMemo("");
      account.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      account.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account.setLoginFailureCount(0);

      List<AccountDto> accountList = new ArrayList<AccountDto>();
      accountList.add(account);

      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(accountList).when(accountMapper).select(accountCaptor.capture());

      AccountModel actual = accountRepositoryImpl.getByAccountNo(new AccountNo(1L));

      verify(accountMapper).select(any(AccountCondition.class));
      AccountCondition accountCapture = accountCaptor.getValue();
      assertEquals(1L, accountCapture.getAccountNo());

      assertNotNull(actual);
      assertEquals(account.getAccountNo(), actual.getAccountNo().value());
      assertEquals(account.getAccountId(), actual.getAccountId().value());
      assertEquals(account.getAccountName(), actual.getAccountName().value());
      assertEquals(account.getPassword(), actual.getPassword().value());
      assertEquals(account.getBirthdate(), actual.getBirthdate().value());
      assertEquals(account.getSexKbn(), actual.getSexKbn());
      assertEquals(
          account.getBirthplacePrefectureKbnCode(),
          actual.getBirthplacePrefectureKbnCode().value());
      assertEquals(
          account.getResidentPrefectureKbnCode(), actual.getResidentPrefectureKbnCode().value());
      assertEquals(account.getFreeMemo(), actual.getFreeMemo().value());
      assertEquals(account.getAuthorityKbn(), actual.getAuthorityKbn());
      assertEquals(account.getLastLoginDatetime(), actual.getLastLoginDatetime().value());
      assertEquals(account.getLoginFailureCount(), actual.getLoginFailureCount().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが取得できなかった場合")
    void getByAccountNo_not_found() {
      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(new ArrayList<AccountDto>()).when(accountMapper).select(accountCaptor.capture());

      AccountModel actual = accountRepositoryImpl.getByAccountNo(new AccountNo(1L));

      verify(accountMapper).select(any(AccountCondition.class));
      AccountCondition accountCapture = accountCaptor.getValue();
      assertEquals(1L, accountCapture.getAccountNo());

      assertNull(actual);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getByAccountId {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが取得できた場合")
    void getByAccountId_found() {
      AccountDto account = new AccountDto();
      account.setAccountNo(1L);
      account.setCreatedBy(1L);
      account.setCreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account.setUpdatedBy(1L);
      account.setUpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account.setIsDeleted(false);
      account.setAccountId("aaaaaaaa");
      account.setAccountName("AAAAAAAA");
      account.setPassword("$2a$10$password1");
      account.setBirthdate(LocalDate.of(1991, 2, 14));
      account.setSexKbn(SexEnum.NONE);
      account.setBirthplacePrefectureKbnCode("");
      account.setResidentPrefectureKbnCode("");
      account.setFreeMemo("");
      account.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      account.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account.setLoginFailureCount(0);

      List<AccountDto> accountList = new ArrayList<AccountDto>();
      accountList.add(account);

      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(accountList).when(accountMapper).select(accountCaptor.capture());

      AccountModel actual = accountRepositoryImpl.getByAccountId(new AccountId("aaaaaaaa"));

      verify(accountMapper).select(any(AccountCondition.class));
      AccountCondition accountCapture = accountCaptor.getValue();
      assertEquals("aaaaaaaa", accountCapture.getAccountId());

      assertNotNull(actual);
      assertEquals(account.getAccountNo(), actual.getAccountNo().value());
      assertEquals(account.getAccountId(), actual.getAccountId().value());
      assertEquals(account.getAccountName(), actual.getAccountName().value());
      assertEquals(account.getPassword(), actual.getPassword().value());
      assertEquals(account.getBirthdate(), actual.getBirthdate().value());
      assertEquals(account.getSexKbn(), actual.getSexKbn());
      assertEquals(
          account.getBirthplacePrefectureKbnCode(),
          actual.getBirthplacePrefectureKbnCode().value());
      assertEquals(
          account.getResidentPrefectureKbnCode(), actual.getResidentPrefectureKbnCode().value());
      assertEquals(account.getFreeMemo(), actual.getFreeMemo().value());
      assertEquals(account.getAuthorityKbn(), actual.getAuthorityKbn());
      assertEquals(account.getLastLoginDatetime(), actual.getLastLoginDatetime().value());
      assertEquals(account.getLoginFailureCount(), actual.getLoginFailureCount().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが取得できなかった場合")
    void getByAccountId_not_found() {
      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(new ArrayList<AccountDto>()).when(accountMapper).select(accountCaptor.capture());

      AccountModel actual = accountRepositoryImpl.getByAccountId(new AccountId("aaaaaaaa"));

      verify(accountMapper).select(any(AccountCondition.class));
      AccountCondition accountCapture = accountCaptor.getValue();
      assertEquals("aaaaaaaa", accountCapture.getAccountId());

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
    void regist_contain_null_parameter() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("aaaaaaaa"))
              .build();

      ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
      ArgumentCaptor<AccountAuthority> accountAuthorityCaptor =
          ArgumentCaptor.forClass(AccountAuthority.class);
      doReturn(1L).when(accountMapper).nextAccountNo();
      doReturn(1).when(accountMapper).insert(accountCaptor.capture());
      doReturn(1).when(accountAuthorityMapper).insert(accountAuthorityCaptor.capture());
      doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");

      accountRepositoryImpl.regist(accountModel);

      verify(accountMapper).insert(any(Account.class));
      Account accountCapture = accountCaptor.getValue();
      assertEquals(1L, accountCapture.getAccountNo());
      assertEquals(0L, accountCapture.getCreatedBy());
      assertEquals(null, accountCapture.getCreatedAt());
      assertEquals(0L, accountCapture.getUpdatedBy());
      assertEquals(null, accountCapture.getUpdatedAt());
      assertEquals(null, accountCapture.getIsDeleted());
      assertEquals("aaaaaaaa", accountCapture.getAccountId());
      assertEquals("AAAAAAAA", accountCapture.getAccountName());
      assertEquals("$2a$10$password1", accountCapture.getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), accountCapture.getBirthdate());
      assertEquals(SexEnum.NONE, accountCapture.getSexKbn());
      assertEquals("none", accountCapture.getBirthplacePrefectureKbnCode());
      assertEquals("none", accountCapture.getResidentPrefectureKbnCode());
      assertEquals("", accountCapture.getFreeMemo());
      assertEquals(
          OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)),
          accountCapture.getLastLoginDatetime());
      assertEquals(0, accountCapture.getLoginFailureCount());

      verify(accountAuthorityMapper).insert(any(AccountAuthority.class));
      AccountAuthority accountAuthorityCapture = accountAuthorityCaptor.getValue();
      assertEquals(1L, accountAuthorityCapture.getAccountNo());
      assertEquals(0L, accountAuthorityCapture.getCreatedBy());
      assertEquals(0L, accountAuthorityCapture.getUpdatedBy());
      assertEquals(AuthorityEnum.MINI, accountAuthorityCapture.getAuthorityKbn());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：Nullのパラメータを含まないAccountModelの登録")
    void regist_not_contain_null_parameter() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("aaaaaaaa"))
              .birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
              .sexKbn(SexEnum.WOMAN)
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa"))
              .freeMemo(new FreeMemo("フリーメモ"))
              .build();

      ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
      ArgumentCaptor<AccountAuthority> accountAuthorityCaptor =
          ArgumentCaptor.forClass(AccountAuthority.class);
      doReturn(1L).when(accountMapper).nextAccountNo();
      doReturn(1).when(accountMapper).insert(accountCaptor.capture());
      doReturn(1).when(accountAuthorityMapper).insert(accountAuthorityCaptor.capture());
      doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");

      accountRepositoryImpl.regist(accountModel);

      verify(accountMapper).insert(any(Account.class));
      Account accountCapture = accountCaptor.getValue();
      assertEquals(1L, accountCapture.getAccountNo());
      assertEquals(0L, accountCapture.getCreatedBy());
      assertEquals(null, accountCapture.getCreatedAt());
      assertEquals(0L, accountCapture.getUpdatedBy());
      assertEquals(null, accountCapture.getUpdatedAt());
      assertEquals(null, accountCapture.getIsDeleted());
      assertEquals("aaaaaaaa", accountCapture.getAccountId());
      assertEquals("AAAAAAAA", accountCapture.getAccountName());
      assertEquals("$2a$10$password1", accountCapture.getPassword());
      assertEquals(LocalDate.of(1991, 2, 14), accountCapture.getBirthdate());
      assertEquals(SexEnum.WOMAN, accountCapture.getSexKbn());
      assertEquals("Hokkaido", accountCapture.getBirthplacePrefectureKbnCode());
      assertEquals("Okinawa", accountCapture.getResidentPrefectureKbnCode());
      assertEquals("フリーメモ", accountCapture.getFreeMemo());
      assertEquals(
          OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)),
          accountCapture.getLastLoginDatetime());
      assertEquals(0, accountCapture.getLoginFailureCount());

      verify(accountAuthorityMapper).insert(any(AccountAuthority.class));
      AccountAuthority accountAuthorityCapture = accountAuthorityCaptor.getValue();
      assertEquals(1L, accountAuthorityCapture.getAccountNo());
      assertEquals(AuthorityEnum.MINI, accountAuthorityCapture.getAuthorityKbn());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：RegistFailureExceptionをthrowする")
    void regist_RegistFailureException() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("aaaaaaaa"))
              .build();

      ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
      doThrow(DuplicateKeyException.class).when(accountMapper).insert(accountCaptor.capture());
      doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");

      assertThrows(RegistFailureException.class, () -> accountRepositoryImpl.regist(accountModel));

      verify(accountMapper).insert(any(Account.class));
      Account accountCapture = accountCaptor.getValue();
      // accountMapper.nextAccountNo()はモック未スタブのため、Mockitoの既定値0Lが採番結果として使われる
      assertEquals(0L, accountCapture.getAccountNo());
      assertEquals(0L, accountCapture.getCreatedBy());
      assertEquals(null, accountCapture.getCreatedAt());
      assertEquals(0L, accountCapture.getUpdatedBy());
      assertEquals(null, accountCapture.getUpdatedAt());
      assertEquals(null, accountCapture.getIsDeleted());
      assertEquals("aaaaaaaa", accountCapture.getAccountId());
      assertEquals("AAAAAAAA", accountCapture.getAccountName());
      assertEquals("$2a$10$password1", accountCapture.getPassword());
      assertEquals(LocalDate.of(1900, 1, 1), accountCapture.getBirthdate());
      assertEquals(SexEnum.NONE, accountCapture.getSexKbn());
      assertEquals("none", accountCapture.getBirthplacePrefectureKbnCode());
      assertEquals("none", accountCapture.getResidentPrefectureKbnCode());
      assertEquals("", accountCapture.getFreeMemo());
      assertEquals(
          OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)),
          accountCapture.getLastLoginDatetime());
      assertEquals(0, accountCapture.getLoginFailureCount());

      verify(accountAuthorityMapper, never()).insert(any());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：Nullのパラメータを含むAccountModelでの更新")
    void update_contain_null_parameter() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .build();

      ArgumentCaptor<AccountCondition> cndAccountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor =
          ArgumentCaptor.forClass(AccountUpdateTarget.class);
      doReturn(1)
          .when(accountMapper)
          .update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

      accountRepositoryImpl.update(accountModel);

      verify(accountMapper).update(any(AccountCondition.class), any(AccountUpdateTarget.class));
      AccountCondition cndAccountCapture = cndAccountCaptor.getValue();
      assertEquals(1L, cndAccountCapture.getAccountNo());

      AccountUpdateTarget targetAccountCapture = targetAccountCaptor.getValue();
      assertEquals(null, targetAccountCapture.getUpdatedBy());
      assertEquals(null, targetAccountCapture.getIsDeleted());
      assertEquals("aaaaaaaa", targetAccountCapture.getAccountId());
      assertEquals("AAAAAAAA", targetAccountCapture.getAccountName());
      assertEquals(null, targetAccountCapture.getPassword());
      // 未指定項目は「変更なし」としてnullが渡る（センチネル値で上書きしない）
      assertEquals(null, targetAccountCapture.getBirthdate());
      assertEquals(null, targetAccountCapture.getSexKbn());
      assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getFreeMemo());
      assertEquals(null, targetAccountCapture.getLastLoginDatetime());
      assertEquals(null, targetAccountCapture.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
    void update_not_contain_null_parameter() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .password(new Password("aaaaaaaa"))
              .birthdate(new BirthDate(LocalDate.of(1991, 2, 14)))
              .sexKbn(SexEnum.WOMAN)
              .birthplacePrefectureKbnCode(new BirthplacePrefectureKbnCode("Hokkaido"))
              .residentPrefectureKbnCode(new ResidentPrefectureKbnCode("Okinawa"))
              .freeMemo(new FreeMemo("フリーメモ"))
              .lastLoginDatetime(
                  new LastLoginDatetime(
                      OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
              .loginFailureCount(new LoginFailureCount(2))
              .build();

      ArgumentCaptor<AccountCondition> cndAccountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor =
          ArgumentCaptor.forClass(AccountUpdateTarget.class);
      doReturn(1)
          .when(accountMapper)
          .update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
      doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");

      accountRepositoryImpl.update(accountModel);

      verify(accountMapper).update(any(AccountCondition.class), any(AccountUpdateTarget.class));
      AccountCondition cndAccountCapture = cndAccountCaptor.getValue();
      assertEquals(1L, cndAccountCapture.getAccountNo());

      AccountUpdateTarget targetAccountCapture = targetAccountCaptor.getValue();
      assertEquals(null, targetAccountCapture.getUpdatedBy());
      assertEquals(null, targetAccountCapture.getIsDeleted());
      assertEquals("aaaaaaaa", targetAccountCapture.getAccountId());
      assertEquals("AAAAAAAA", targetAccountCapture.getAccountName());
      assertEquals("$2a$10$password1", targetAccountCapture.getPassword());
      assertEquals(LocalDate.of(1991, 2, 14), targetAccountCapture.getBirthdate());
      assertEquals(SexEnum.WOMAN, targetAccountCapture.getSexKbn());
      assertEquals("Hokkaido", targetAccountCapture.getBirthplacePrefectureKbnCode());
      assertEquals("Okinawa", targetAccountCapture.getResidentPrefectureKbnCode());
      assertEquals("フリーメモ", targetAccountCapture.getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)),
          targetAccountCapture.getLastLoginDatetime());
      assertEquals(2, targetAccountCapture.getLoginFailureCount());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void update_UpdateFailureException() {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .build();

      ArgumentCaptor<AccountCondition> cndAccountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor =
          ArgumentCaptor.forClass(AccountUpdateTarget.class);
      doReturn(0)
          .when(accountMapper)
          .update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

      assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.update(accountModel));

      verify(accountMapper).update(any(AccountCondition.class), any(AccountUpdateTarget.class));
      AccountCondition cndAccountCapture = cndAccountCaptor.getValue();
      assertEquals(1L, cndAccountCapture.getAccountNo());

      AccountUpdateTarget targetAccountCapture = targetAccountCaptor.getValue();
      assertEquals(null, targetAccountCapture.getUpdatedBy());
      assertEquals(null, targetAccountCapture.getIsDeleted());
      assertEquals("aaaaaaaa", targetAccountCapture.getAccountId());
      assertEquals("AAAAAAAA", targetAccountCapture.getAccountName());
      assertEquals(null, targetAccountCapture.getPassword());
      // 未指定項目は「変更なし」としてnullが渡る（センチネル値で上書きしない）
      assertEquals(null, targetAccountCapture.getBirthdate());
      assertEquals(null, targetAccountCapture.getSexKbn());
      assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getFreeMemo());
      assertEquals(null, targetAccountCapture.getLastLoginDatetime());
      assertEquals(null, targetAccountCapture.getLoginFailureCount());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：DuplicateKeyException発生時にUpdateFailureExceptionをthrowする")
    void update_DuplicateKeyException() {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("aaaaaaaa"))
              .accountName(new AccountName("AAAAAAAA"))
              .build();

      ArgumentCaptor<AccountCondition> cndAccountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor =
          ArgumentCaptor.forClass(AccountUpdateTarget.class);
      doThrow(DuplicateKeyException.class)
          .when(accountMapper)
          .update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

      assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.update(accountModel));

      verify(accountMapper).update(any(AccountCondition.class), any(AccountUpdateTarget.class));
      AccountCondition cndAccountCapture = cndAccountCaptor.getValue();
      assertEquals(1L, cndAccountCapture.getAccountNo());

      AccountUpdateTarget targetAccountCapture = targetAccountCaptor.getValue();
      assertEquals(null, targetAccountCapture.getUpdatedBy());
      assertEquals(null, targetAccountCapture.getIsDeleted());
      assertEquals("aaaaaaaa", targetAccountCapture.getAccountId());
      assertEquals("AAAAAAAA", targetAccountCapture.getAccountName());
      assertEquals(null, targetAccountCapture.getPassword());
      // 未指定項目は「変更なし」としてnullが渡る（センチネル値で上書きしない）
      assertEquals(null, targetAccountCapture.getBirthdate());
      assertEquals(null, targetAccountCapture.getSexKbn());
      assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getFreeMemo());
      assertEquals(null, targetAccountCapture.getLastLoginDatetime());
      assertEquals(null, targetAccountCapture.getLoginFailureCount());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class updateLoginFailureCount {
    @Test
    @Order(1)
    @DisplayName("正常系：Nullのパラメータを含むAccountModelでの更新")
    void updateLoginFailureCount_contain_null_parameter() throws GalleryException {
      AccountModel accountModel = AccountModel.builder().accountNo(new AccountNo(1L)).build();

      ArgumentCaptor<AccountCondition> cndAccountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor =
          ArgumentCaptor.forClass(AccountUpdateTarget.class);
      doReturn(1)
          .when(accountMapper)
          .update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

      accountRepositoryImpl.updateLoginFailureCount(accountModel);

      verify(accountMapper).update(any(AccountCondition.class), any(AccountUpdateTarget.class));
      AccountCondition cndAccountCapture = cndAccountCaptor.getValue();
      assertEquals(1L, cndAccountCapture.getAccountNo());

      AccountUpdateTarget targetAccountCapture = targetAccountCaptor.getValue();
      assertEquals(null, targetAccountCapture.getUpdatedBy());
      assertEquals(null, targetAccountCapture.getIsDeleted());
      assertEquals(null, targetAccountCapture.getAccountId());
      assertEquals(null, targetAccountCapture.getAccountName());
      assertEquals(null, targetAccountCapture.getPassword());
      assertEquals(null, targetAccountCapture.getBirthdate());
      assertEquals(null, targetAccountCapture.getSexKbn());
      assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getFreeMemo());
      assertEquals(null, targetAccountCapture.getLastLoginDatetime());
      assertEquals(0, targetAccountCapture.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
    void updateLoginFailureCount_not_contain_null_parameter() throws GalleryException {
      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .lastLoginDatetime(
                  new LastLoginDatetime(
                      OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
              .loginFailureCount(new LoginFailureCount(2))
              .build();

      ArgumentCaptor<AccountCondition> cndAccountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor =
          ArgumentCaptor.forClass(AccountUpdateTarget.class);
      doReturn(1)
          .when(accountMapper)
          .update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

      accountRepositoryImpl.updateLoginFailureCount(accountModel);

      verify(accountMapper).update(any(AccountCondition.class), any(AccountUpdateTarget.class));
      AccountCondition cndAccountCapture = cndAccountCaptor.getValue();
      assertEquals(1L, cndAccountCapture.getAccountNo());

      AccountUpdateTarget targetAccountCapture = targetAccountCaptor.getValue();
      assertEquals(null, targetAccountCapture.getUpdatedBy());
      assertEquals(null, targetAccountCapture.getIsDeleted());
      assertEquals(null, targetAccountCapture.getAccountId());
      assertEquals(null, targetAccountCapture.getAccountName());
      assertEquals(null, targetAccountCapture.getPassword());
      assertEquals(null, targetAccountCapture.getBirthdate());
      assertEquals(null, targetAccountCapture.getSexKbn());
      assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getFreeMemo());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)),
          targetAccountCapture.getLastLoginDatetime());
      assertEquals(2, targetAccountCapture.getLoginFailureCount());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void updateLoginFailureCount_UpdateFailureException() {
      AccountModel accountModel = AccountModel.builder().accountNo(new AccountNo(1L)).build();

      ArgumentCaptor<AccountCondition> cndAccountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor =
          ArgumentCaptor.forClass(AccountUpdateTarget.class);
      doReturn(0)
          .when(accountMapper)
          .update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

      assertThrows(
          UpdateFailureException.class,
          () -> accountRepositoryImpl.updateLoginFailureCount(accountModel));

      verify(accountMapper).update(any(AccountCondition.class), any(AccountUpdateTarget.class));
      AccountCondition cndAccountCapture = cndAccountCaptor.getValue();
      assertEquals(1L, cndAccountCapture.getAccountNo());

      AccountUpdateTarget targetAccountCapture = targetAccountCaptor.getValue();
      assertEquals(null, targetAccountCapture.getUpdatedBy());
      assertEquals(null, targetAccountCapture.getIsDeleted());
      assertEquals(null, targetAccountCapture.getAccountId());
      assertEquals(null, targetAccountCapture.getAccountName());
      assertEquals(null, targetAccountCapture.getPassword());
      assertEquals(null, targetAccountCapture.getBirthdate());
      assertEquals(null, targetAccountCapture.getSexKbn());
      assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
      assertEquals(null, targetAccountCapture.getFreeMemo());
      assertEquals(null, targetAccountCapture.getLastLoginDatetime());
      assertEquals(0, targetAccountCapture.getLoginFailureCount());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class incrementLoginFailureCount {
    @Test
    @Order(1)
    @DisplayName("正常系：SQL側で原子的にインクリメントすること")
    void incrementLoginFailureCount_success() throws GalleryException {
      doReturn(1).when(accountMapper).incrementLoginFailureCount(1L);

      accountRepositoryImpl.incrementLoginFailureCount(new AccountNo(1L));

      verify(accountMapper).incrementLoginFailureCount(1L);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：UpdateFailureExceptionをthrowする")
    void incrementLoginFailureCount_UpdateFailureException() {
      doReturn(0).when(accountMapper).incrementLoginFailureCount(1L);

      assertThrows(
          UpdateFailureException.class,
          () -> accountRepositoryImpl.incrementLoginFailureCount(new AccountNo(1L)));

      verify(accountMapper).incrementLoginFailureCount(1L);
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントを物理削除する")
    void delete_success() {
      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(1).when(accountMapper).delete(accountCaptor.capture());

      accountRepositoryImpl.delete(new AccountNo(1L));

      verify(accountMapper, times(1)).delete(any(AccountCondition.class));
      AccountCondition accountCapture = accountCaptor.getValue();
      assertEquals(1L, accountCapture.getAccountNo());

      verify(accountAuthorityMapper, times(1)).delete(any());
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isExistAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントが存在する場合")
    void isExistAccount_true() {
      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(true).when(accountMapper).isExistAccount(accountCaptor.capture());

      assertTrue(
          accountRepositoryImpl.isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa")));
      verify(accountMapper, times(1)).isExistAccount(any(AccountCondition.class));

      AccountCondition accountCapture = accountCaptor.getValue();
      assertEquals(1L, accountCapture.getAccountNo());
      assertEquals("aaaaaaaa", accountCapture.getAccountId());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが存在しない場合")
    void isExistAccount_false() {
      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(false).when(accountMapper).isExistAccount(accountCaptor.capture());

      assertFalse(
          accountRepositoryImpl.isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa")));
      verify(accountMapper, times(1)).isExistAccount(any());

      AccountCondition accountCapture = accountCaptor.getValue();
      assertEquals(1L, accountCapture.getAccountNo());
      assertEquals("aaaaaaaa", accountCapture.getAccountId());
    }
  }

  @Nested
  @Order(9)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountList {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウントを2件以上取得")
    void getAccountList_found_some_accounts() {
      AccountDto account1 = new AccountDto();
      account1.setAccountNo(1L);
      account1.setCreatedBy(1L);
      account1.setCreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account1.setUpdatedBy(1L);
      account1.setUpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account1.setIsDeleted(false);
      account1.setAccountId("aaaaaaaa");
      account1.setAccountName("AAAAAAAA");
      account1.setPassword("$2a$10$password1");
      account1.setBirthdate(LocalDate.of(1991, 1, 1));
      account1.setSexKbn(SexEnum.MAN);
      account1.setBirthplacePrefectureKbnCode("Hokkaido");
      account1.setResidentPrefectureKbnCode("Aomori");
      account1.setFreeMemo("よろしく");
      account1.setAuthorityKbn(AuthorityEnum.MINI);
      account1.setLastLoginDatetime(
          OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account1.setLoginFailureCount(0);
      AccountDto account2 = new AccountDto();
      account2.setAccountNo(2L);
      account2.setCreatedBy(2L);
      account2.setCreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account2.setUpdatedBy(2L);
      account2.setUpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account2.setIsDeleted(false);
      account2.setAccountId("bbbbbbbb");
      account2.setAccountName("BBBBBBBB");
      account2.setPassword("$2a$10$password2");
      account2.setBirthdate(LocalDate.of(1991, 2, 1));
      account2.setSexKbn(SexEnum.WOMAN);
      account2.setBirthplacePrefectureKbnCode("Iwate");
      account2.setResidentPrefectureKbnCode("Okinawa");
      account2.setFreeMemo("お願いします");
      account2.setAuthorityKbn(AuthorityEnum.ADMINISTRATOR);
      account2.setLastLoginDatetime(
          OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      account2.setLoginFailureCount(1);

      List<AccountDto> accountList = new ArrayList<AccountDto>();
      accountList.add(account1);
      accountList.add(account2);

      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(accountList).when(accountMapper).selectList(accountCaptor.capture());

      AccountGetModel accountGetModel = AccountGetModel.builder().limit(100).offset(0).build();
      AccountPageModel actualPage = accountRepositoryImpl.getAccountList(accountGetModel);
      AccountModelList actual = actualPage.getAccountModelList();
      assertTrue(actualPage.getIsLast());

      AccountCondition account = accountCaptor.getValue();
      assertFalse(account.getIsDeleted());
      assertEquals(100, account.getLimit());
      assertEquals(0, account.getOffset());

      AccountModel actualAccountModel1 =
          actual.stream()
              .sorted(Comparator.comparing(m -> m.getAccountNo().value()))
              .toList()
              .getFirst();
      assertEquals(new AccountNo(1L), actualAccountModel1.getAccountNo());
      assertEquals(new AccountId("aaaaaaaa"), actualAccountModel1.getAccountId());
      assertEquals(new AccountName("AAAAAAAA"), actualAccountModel1.getAccountName());
      assertEquals(new Password("$2a$10$password1"), actualAccountModel1.getPassword());
      assertEquals(new BirthDate(LocalDate.of(1991, 1, 1)), actualAccountModel1.getBirthdate());
      assertEquals(SexEnum.MAN, actualAccountModel1.getSexKbn());
      assertEquals(
          new BirthplacePrefectureKbnCode("Hokkaido"),
          actualAccountModel1.getBirthplacePrefectureKbnCode());
      assertEquals(
          new ResidentPrefectureKbnCode("Aomori"),
          actualAccountModel1.getResidentPrefectureKbnCode());
      assertEquals(new FreeMemo("よろしく"), actualAccountModel1.getFreeMemo());
      assertEquals(AuthorityEnum.MINI, actualAccountModel1.getAuthorityKbn());
      assertEquals(
          new LastLoginDatetime(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))),
          actualAccountModel1.getLastLoginDatetime());
      assertEquals(new LoginFailureCount(0), actualAccountModel1.getLoginFailureCount());

      AccountModel actualAccountModel2 =
          actual.stream()
              .sorted(Comparator.comparing(m -> m.getAccountNo().value()))
              .toList()
              .getLast();
      assertEquals(new AccountNo(2L), actualAccountModel2.getAccountNo());
      assertEquals(new AccountId("bbbbbbbb"), actualAccountModel2.getAccountId());
      assertEquals(new AccountName("BBBBBBBB"), actualAccountModel2.getAccountName());
      assertEquals(new Password("$2a$10$password2"), actualAccountModel2.getPassword());
      assertEquals(new BirthDate(LocalDate.of(1991, 2, 1)), actualAccountModel2.getBirthdate());
      assertEquals(SexEnum.WOMAN, actualAccountModel2.getSexKbn());
      assertEquals(
          new BirthplacePrefectureKbnCode("Iwate"),
          actualAccountModel2.getBirthplacePrefectureKbnCode());
      assertEquals(
          new ResidentPrefectureKbnCode("Okinawa"),
          actualAccountModel2.getResidentPrefectureKbnCode());
      assertEquals(new FreeMemo("お願いします"), actualAccountModel2.getFreeMemo());
      assertEquals(AuthorityEnum.ADMINISTRATOR, actualAccountModel2.getAuthorityKbn());
      assertEquals(
          new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))),
          actualAccountModel2.getLastLoginDatetime());
      assertEquals(new LoginFailureCount(1), actualAccountModel2.getLoginFailureCount());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウントが0件")
    void getAccountList_not_found() {
      List<AccountDto> expected = new ArrayList<AccountDto>();

      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(expected).when(accountMapper).selectList(accountCaptor.capture());

      AccountGetModel accountGetModel = AccountGetModel.builder().limit(100).offset(0).build();
      AccountPageModel actualPage = accountRepositoryImpl.getAccountList(accountGetModel);
      assertEquals(expected.size(), actualPage.getAccountModelList().size());
      assertTrue(actualPage.getIsLast());

      AccountCondition account = accountCaptor.getValue();
      assertFalse(account.getIsDeleted());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：取得件数が上限に達した場合、最後のページでないと判定され、表示件数分に切り詰められること")
    void getAccountList_pagination_trims_when_more_results_exist() {
      AccountDto account1 = new AccountDto();
      account1.setAccountNo(1L);
      account1.setAccountId("aaaaaaaa");
      account1.setAccountName("AAAAAAAA");
      account1.setPassword("$2a$10$password1");
      account1.setIsDeleted(false);
      account1.setSexKbn(SexEnum.NONE);
      account1.setBirthplacePrefectureKbnCode("none");
      account1.setResidentPrefectureKbnCode("none");
      account1.setFreeMemo("");
      account1.setAuthorityKbn(AuthorityEnum.MINI);
      account1.setLoginFailureCount(0);
      AccountDto account2 = new AccountDto();
      account2.setAccountNo(2L);
      account2.setAccountId("bbbbbbbb");
      account2.setAccountName("BBBBBBBB");
      account2.setPassword("$2a$10$password2");
      account2.setIsDeleted(false);
      account2.setSexKbn(SexEnum.NONE);
      account2.setBirthplacePrefectureKbnCode("none");
      account2.setResidentPrefectureKbnCode("none");
      account2.setFreeMemo("");
      account2.setAuthorityKbn(AuthorityEnum.MINI);
      account2.setLoginFailureCount(0);

      List<AccountDto> accountList = new ArrayList<AccountDto>();
      accountList.add(account1);
      accountList.add(account2);

      doReturn(accountList).when(accountMapper).selectList(any(AccountCondition.class));

      // 1ページあたりの表示件数を1件と仮定し、limitはその1件多い2を指定する
      AccountGetModel accountGetModel = AccountGetModel.builder().limit(2).offset(0).build();
      AccountPageModel actual = accountRepositoryImpl.getAccountList(accountGetModel);

      assertFalse(actual.getIsLast());
      assertEquals(1, actual.getAccountModelList().size());
      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountModelList().get(0).getAccountId());
    }
  }

  @Nested
  @Order(10)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAccountListForAdmin {
    @Test
    @Order(1)
    @DisplayName("正常系：削除済みを含むアカウントを2件以上取得")
    void getAccountListForAdmin_found_some_accounts() {
      AccountDto account1 = new AccountDto();
      account1.setAccountNo(1L);
      account1.setAccountId("aaaaaaaa");
      account1.setAccountName("AAAAAAAA");
      account1.setPassword("$2a$10$password1");
      account1.setIsDeleted(false);
      account1.setSexKbn(SexEnum.NONE);
      account1.setBirthplacePrefectureKbnCode("none");
      account1.setResidentPrefectureKbnCode("none");
      account1.setFreeMemo("");
      account1.setAuthorityKbn(AuthorityEnum.MINI);
      account1.setLoginFailureCount(0);
      AccountDto account2 = new AccountDto();
      account2.setAccountNo(2L);
      account2.setAccountId("bbbbbbbb");
      account2.setAccountName("BBBBBBBB");
      account2.setPassword("$2a$10$password2");
      account2.setIsDeleted(true);
      account2.setSexKbn(SexEnum.NONE);
      account2.setBirthplacePrefectureKbnCode("none");
      account2.setResidentPrefectureKbnCode("none");
      account2.setFreeMemo("");
      account2.setAuthorityKbn(AuthorityEnum.MINI);
      account2.setLoginFailureCount(0);

      List<AccountDto> accountList = new ArrayList<AccountDto>();
      accountList.add(account1);
      accountList.add(account2);

      ArgumentCaptor<AccountCondition> accountCaptor =
          ArgumentCaptor.forClass(AccountCondition.class);
      doReturn(accountList).when(accountMapper).selectList(accountCaptor.capture());

      AccountGetModel accountGetModel = AccountGetModel.builder().limit(100).offset(0).build();
      AccountPageModel actualPage = accountRepositoryImpl.getAccountListForAdmin(accountGetModel);
      assertTrue(actualPage.getIsLast());
      assertEquals(2, actualPage.getAccountModelList().size());

      AccountCondition account = accountCaptor.getValue();
      assertNull(account.getIsDeleted());
      assertEquals(100, account.getLimit());
      assertEquals(0, account.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：取得件数が上限に達した場合、最後のページでないと判定され、表示件数分に切り詰められること")
    void getAccountListForAdmin_pagination_trims_when_more_results_exist() {
      AccountDto account1 = new AccountDto();
      account1.setAccountNo(1L);
      account1.setAccountId("aaaaaaaa");
      account1.setAccountName("AAAAAAAA");
      account1.setPassword("$2a$10$password1");
      account1.setIsDeleted(false);
      account1.setSexKbn(SexEnum.NONE);
      account1.setBirthplacePrefectureKbnCode("none");
      account1.setResidentPrefectureKbnCode("none");
      account1.setFreeMemo("");
      account1.setAuthorityKbn(AuthorityEnum.MINI);
      account1.setLoginFailureCount(0);
      AccountDto account2 = new AccountDto();
      account2.setAccountNo(2L);
      account2.setAccountId("bbbbbbbb");
      account2.setAccountName("BBBBBBBB");
      account2.setPassword("$2a$10$password2");
      account2.setIsDeleted(false);
      account2.setSexKbn(SexEnum.NONE);
      account2.setBirthplacePrefectureKbnCode("none");
      account2.setResidentPrefectureKbnCode("none");
      account2.setFreeMemo("");
      account2.setAuthorityKbn(AuthorityEnum.MINI);
      account2.setLoginFailureCount(0);

      List<AccountDto> accountList = new ArrayList<AccountDto>();
      accountList.add(account1);
      accountList.add(account2);

      doReturn(accountList).when(accountMapper).selectList(any(AccountCondition.class));

      // 1ページあたりの表示件数を1件と仮定し、limitはその1件多い2を指定する
      AccountGetModel accountGetModel = AccountGetModel.builder().limit(2).offset(0).build();
      AccountPageModel actual = accountRepositoryImpl.getAccountListForAdmin(accountGetModel);

      assertFalse(actual.getIsLast());
      assertEquals(1, actual.getAccountModelList().size());
      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountModelList().get(0).getAccountId());
    }
  }

  @Nested
  @Order(9)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class lockForUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：行ロックを取得すること")
    void lockForUpdate_success() {
      doReturn(1L).when(accountMapper).lockAccount(1L);

      accountRepositoryImpl.lockForUpdate(new AccountNo(1L));

      verify(accountMapper).lockAccount(1L);
    }
  }
}
