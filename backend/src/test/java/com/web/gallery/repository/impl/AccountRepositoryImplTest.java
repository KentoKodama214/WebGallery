package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.entity.Account;
import com.web.gallery.entity.AccountCondition;
import com.web.gallery.entity.AccountUpdateTarget;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.mapper.AccountMapper;
import com.web.gallery.model.AccountGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
import com.web.gallery.model.AccountPageModel;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountRepositoryImplTest {
	@InjectMocks
	private AccountRepositoryImpl accountRepositoryImpl;
	
	@Mock
	private AccountMapper accountMapper;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getByAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが取得できた場合")
		void getByAccountNo_found() {
			Account account = Account.builder()
					.accountNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1L)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("")
					.residentPrefectureKbnCode("")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();

			List<Account> accountList = new ArrayList<Account>();
			accountList.add(account);

			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
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
			assertEquals(account.getBirthplacePrefectureKbnCode(), actual.getBirthplacePrefectureKbnCode().value());
			assertEquals(account.getResidentPrefectureKbnCode(), actual.getResidentPrefectureKbnCode().value());
			assertEquals(account.getFreeMemo(), actual.getFreeMemo().value());
			assertEquals(account.getAuthorityKbn(), actual.getAuthorityKbn());
			assertEquals(account.getLastLoginDatetime(), actual.getLastLoginDatetime().value());
			assertEquals(account.getLoginFailureCount(), actual.getLoginFailureCount().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountNo_not_found() {
			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			doReturn(new ArrayList<Account>()).when(accountMapper).select(accountCaptor.capture());

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
			Account account = Account.builder()
					.accountNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1L)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("")
					.residentPrefectureKbnCode("")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();

			List<Account> accountList = new ArrayList<Account>();
			accountList.add(account);

			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
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
			assertEquals(account.getBirthplacePrefectureKbnCode(), actual.getBirthplacePrefectureKbnCode().value());
			assertEquals(account.getResidentPrefectureKbnCode(), actual.getResidentPrefectureKbnCode().value());
			assertEquals(account.getFreeMemo(), actual.getFreeMemo().value());
			assertEquals(account.getAuthorityKbn(), actual.getAuthorityKbn());
			assertEquals(account.getLastLoginDatetime(), actual.getLastLoginDatetime().value());
			assertEquals(account.getLoginFailureCount(), actual.getLoginFailureCount().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountId_not_found() {
			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			doReturn(new ArrayList<Account>()).when(accountMapper).select(accountCaptor.capture());

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
			AccountModel accountModel = AccountModel.builder()
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.password(new Password("aaaaaaaa"))
					.build();

			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(1).when(accountMapper).insert(accountCaptor.capture());
			doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");

			accountRepositoryImpl.regist(accountModel);

			verify(accountMapper).insert(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals(null, accountCapture.getAccountNo());
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
			assertEquals(AuthorityEnum.MINI, accountCapture.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), accountCapture.getLastLoginDatetime());
			assertEquals(0, accountCapture.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelの登録")
		void regist_not_contain_null_parameter() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
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
			doReturn(1).when(accountMapper).insert(accountCaptor.capture());
			doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");

			accountRepositoryImpl.regist(accountModel);

			verify(accountMapper).insert(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals(null, accountCapture.getAccountNo());
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
			assertEquals(AuthorityEnum.MINI, accountCapture.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), accountCapture.getLastLoginDatetime());
			assertEquals(0, accountCapture.getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() throws GalleryException {
			AccountModel accountModel = AccountModel.builder()
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
			assertEquals(null, accountCapture.getAccountNo());
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
			assertEquals(AuthorityEnum.MINI, accountCapture.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), accountCapture.getLastLoginDatetime());
			assertEquals(0, accountCapture.getLoginFailureCount());
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
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.build();

			ArgumentCaptor<AccountCondition> cndAccountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor = ArgumentCaptor.forClass(AccountUpdateTarget.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

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
			assertEquals(LocalDate.of(1900, 1, 1), targetAccountCapture.getBirthdate());
			assertEquals(SexEnum.NONE, targetAccountCapture.getSexKbn());
			assertEquals("none", targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals("none", targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals("", targetAccountCapture.getFreeMemo());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetAccountCapture.getLastLoginDatetime());
			assertEquals(0, targetAccountCapture.getLoginFailureCount());
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

			ArgumentCaptor<AccountCondition> cndAccountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor = ArgumentCaptor.forClass(AccountUpdateTarget.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
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
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetAccountCapture.getLastLoginDatetime());
			assertEquals(2, targetAccountCapture.getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("aaaaaaaa"))
					.accountName(new AccountName("AAAAAAAA"))
					.build();

			ArgumentCaptor<AccountCondition> cndAccountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor = ArgumentCaptor.forClass(AccountUpdateTarget.class);
			doReturn(0).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

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
			assertEquals(LocalDate.of(1900, 1, 1), targetAccountCapture.getBirthdate());
			assertEquals(SexEnum.NONE, targetAccountCapture.getSexKbn());
			assertEquals("none", targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals("none", targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals("", targetAccountCapture.getFreeMemo());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetAccountCapture.getLastLoginDatetime());
			assertEquals(0, targetAccountCapture.getLoginFailureCount());
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
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.build();

			ArgumentCaptor<AccountCondition> cndAccountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor = ArgumentCaptor.forClass(AccountUpdateTarget.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

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
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
					.loginFailureCount(new LoginFailureCount(2))
					.build();

			ArgumentCaptor<AccountCondition> cndAccountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor = ArgumentCaptor.forClass(AccountUpdateTarget.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

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
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetAccountCapture.getLastLoginDatetime());
			assertEquals(2, targetAccountCapture.getLoginFailureCount());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void updateLoginFailureCount_UpdateFailureException() {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.build();

			ArgumentCaptor<AccountCondition> cndAccountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			ArgumentCaptor<AccountUpdateTarget> targetAccountCaptor = ArgumentCaptor.forClass(AccountUpdateTarget.class);
			doReturn(0).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());

			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.updateLoginFailureCount(accountModel));

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
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを物理削除する")
		void delete_success() {
			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			doReturn(1).when(accountMapper).delete(accountCaptor.capture());

			accountRepositoryImpl.delete(new AccountNo(1L));

			verify(accountMapper, times(1)).delete(any(AccountCondition.class));
			AccountCondition accountCapture = accountCaptor.getValue();
			assertEquals(1L, accountCapture.getAccountNo());
		}
	}

	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class isExistAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void isExistAccount_true() {
			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			doReturn(true).when(accountMapper).isExistAccount(accountCaptor.capture());

			assertTrue(accountRepositoryImpl.isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa")));
			verify(accountMapper, times(1)).isExistAccount(any(AccountCondition.class));

			AccountCondition accountCapture = accountCaptor.getValue();
			assertEquals(1L, accountCapture.getAccountNo());
			assertEquals("aaaaaaaa", accountCapture.getAccountId());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void isExistAccount_false() {
			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			doReturn(false).when(accountMapper).isExistAccount(accountCaptor.capture());

			assertFalse(accountRepositoryImpl.isExistAccount(new AccountNo(1L), new AccountId("aaaaaaaa")));
			verify(accountMapper, times(1)).isExistAccount(any());

			AccountCondition accountCapture = accountCaptor.getValue();
			assertEquals(1L, accountCapture.getAccountNo());
			assertEquals("aaaaaaaa", accountCapture.getAccountId());
		}
	}

	@Nested
	@Order(8)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを2件以上取得")
		void getAccountList_found_some_accounts() {
			Account account1 = Account.builder()
					.accountNo(1L)
					.createdBy(1L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1L)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.birthdate(LocalDate.of(1991, 1, 1))
					.sexKbn(SexEnum.MAN)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("Aomori")
					.freeMemo("よろしく")
					.authorityKbn(AuthorityEnum.MINI)
					.lastLoginDatetime(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();
			Account account2 = Account.builder()
					.accountNo(2L)
					.createdBy(2L)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(2L)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.isDeleted(false)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.birthdate(LocalDate.of(1991, 2, 1))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Iwate")
					.residentPrefectureKbnCode("Okinawa")
					.freeMemo("お願いします")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(1)
					.build();

			List<Account> accountList = new ArrayList<Account>();
			accountList.add(account1);
			accountList.add(account2);

			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			doReturn(accountList).when(accountMapper).select(accountCaptor.capture());

			AccountGetModel accountGetModel = AccountGetModel.builder().limit(100).offset(0).build();
			AccountPageModel actualPage = accountRepositoryImpl.getAccountList(accountGetModel);
			AccountModelList actual = actualPage.getAccountModelList();
			assertTrue(actualPage.getIsLast());

			AccountCondition account = accountCaptor.getValue();
			assertFalse(account.getIsDeleted());
			assertEquals(100, account.getLimit());
			assertEquals(0, account.getOffset());

			AccountModel actualAccountModel1 = actual.stream().sorted(Comparator.comparing(m -> m.getAccountNo().value())).toList().getFirst();
			assertEquals(new AccountNo(1L), actualAccountModel1.getAccountNo());
			assertEquals(new AccountId("aaaaaaaa"), actualAccountModel1.getAccountId());
			assertEquals(new AccountName("AAAAAAAA"), actualAccountModel1.getAccountName());
			assertEquals(new Password("$2a$10$password1"), actualAccountModel1.getPassword());
			assertEquals(new BirthDate(LocalDate.of(1991, 1, 1)), actualAccountModel1.getBirthdate());
			assertEquals(SexEnum.MAN, actualAccountModel1.getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("Hokkaido"), actualAccountModel1.getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("Aomori"), actualAccountModel1.getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo("よろしく"), actualAccountModel1.getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualAccountModel1.getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualAccountModel1.getLastLoginDatetime());
			assertEquals(new LoginFailureCount(0), actualAccountModel1.getLoginFailureCount());

			AccountModel actualAccountModel2 = actual.stream().sorted(Comparator.comparing(m -> m.getAccountNo().value())).toList().getLast();
			assertEquals(new AccountNo(2L), actualAccountModel2.getAccountNo());
			assertEquals(new AccountId("bbbbbbbb"), actualAccountModel2.getAccountId());
			assertEquals(new AccountName("BBBBBBBB"), actualAccountModel2.getAccountName());
			assertEquals(new Password("$2a$10$password2"), actualAccountModel2.getPassword());
			assertEquals(new BirthDate(LocalDate.of(1991, 2, 1)), actualAccountModel2.getBirthdate());
			assertEquals(SexEnum.WOMAN, actualAccountModel2.getSexKbn());
			assertEquals(new BirthplacePrefectureKbnCode("Iwate"), actualAccountModel2.getBirthplacePrefectureKbnCode());
			assertEquals(new ResidentPrefectureKbnCode("Okinawa"), actualAccountModel2.getResidentPrefectureKbnCode());
			assertEquals(new FreeMemo("お願いします"), actualAccountModel2.getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualAccountModel2.getAuthorityKbn());
			assertEquals(new LastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))), actualAccountModel2.getLastLoginDatetime());
			assertEquals(new LoginFailureCount(1), actualAccountModel2.getLoginFailureCount());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが0件")
		void getAccountList_not_found() {
			List<Account> expected = new ArrayList<Account>();

			ArgumentCaptor<AccountCondition> accountCaptor = ArgumentCaptor.forClass(AccountCondition.class);
			doReturn(expected).when(accountMapper).select(accountCaptor.capture());

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
			Account account1 = Account.builder()
					.accountNo(1L)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.isDeleted(false)
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.MINI)
					.loginFailureCount(0)
					.build();
			Account account2 = Account.builder()
					.accountNo(2L)
					.accountId("bbbbbbbb")
					.accountName("BBBBBBBB")
					.password("$2a$10$password2")
					.isDeleted(false)
					.sexKbn(SexEnum.NONE)
					.birthplacePrefectureKbnCode("none")
					.residentPrefectureKbnCode("none")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.MINI)
					.loginFailureCount(0)
					.build();

			List<Account> accountList = new ArrayList<Account>();
			accountList.add(account1);
			accountList.add(account2);

			doReturn(accountList).when(accountMapper).select(any(AccountCondition.class));

			// 1ページあたりの表示件数を1件と仮定し、limitはその1件多い2を指定する
			AccountGetModel accountGetModel = AccountGetModel.builder().limit(2).offset(0).build();
			AccountPageModel actual = accountRepositoryImpl.getAccountList(accountGetModel);

			assertFalse(actual.getIsLast());
			assertEquals(1, actual.getAccountModelList().size());
			assertEquals(new AccountId("aaaaaaaa"), actual.getAccountModelList().get(0).getAccountId());
		}
	}
}