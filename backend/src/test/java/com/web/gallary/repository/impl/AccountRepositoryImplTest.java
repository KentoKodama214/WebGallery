package com.web.gallary.repository.impl;

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

import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.SexEnum;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.mapper.AccountMapper;
import com.web.gallary.model.AccountModel;

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
					.birthplacePrefectureKbnCode("")
					.residentPrefectureKbnCode("")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();

			List<Account> accountList = new ArrayList<Account>();
			accountList.add(account);
			
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(accountList).when(accountMapper).select(accountCaptor.capture());
			
			Account actual = accountRepositoryImpl.getByAccountNo(1);

			verify(accountMapper).select(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals(1, accountCapture.getAccountNo());

			assertNotNull(actual);
			assertEquals(accountList.getFirst(), actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountNo_not_found() {
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(new ArrayList<Account>()).when(accountMapper).select(accountCaptor.capture());
			
			Account actual = accountRepositoryImpl.getByAccountNo(1);
			
			verify(accountMapper).select(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals(1, accountCapture.getAccountNo());
			
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
					.birthplacePrefectureKbnCode("")
					.residentPrefectureKbnCode("")
					.freeMemo("")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.lastLoginDatetime(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.loginFailureCount(0)
					.build();

			List<Account> accountList = new ArrayList<Account>();
			accountList.add(account);
			
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(accountList).when(accountMapper).select(accountCaptor.capture());
			
			Account actual = accountRepositoryImpl.getByAccountId("aaaaaaaa");

			verify(accountMapper).select(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals("aaaaaaaa", accountCapture.getAccountId());

			assertNotNull(actual);
			assertEquals(accountList.getFirst(), actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが取得できなかった場合")
		void getByAccountId_not_found() {
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(new ArrayList<Account>()).when(accountMapper).select(accountCaptor.capture());
			
			Account actual = accountRepositoryImpl.getByAccountId("aaaaaaaa");
			
			verify(accountMapper).select(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
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
		void regist_contain_null_parameter() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("aaaaaaaa")
					.build();
			
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(1).when(accountMapper).insert(accountCaptor.capture());
			doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");
			
			accountRepositoryImpl.regist(accountModel);
			
			verify(accountMapper).insert(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals(null, accountCapture.getAccountNo());
			assertEquals(0, accountCapture.getCreatedBy());
			assertEquals(null, accountCapture.getCreatedAt());
			assertEquals(0, accountCapture.getUpdatedBy());
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
		void regist_not_contain_null_parameter() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("aaaaaaaa")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("Okinawa")
					.freeMemo("フリーメモ")
					.build();
			
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(1).when(accountMapper).insert(accountCaptor.capture());
			doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");
			
			accountRepositoryImpl.regist(accountModel);
			
			verify(accountMapper).insert(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals(null, accountCapture.getAccountNo());
			assertEquals(0, accountCapture.getCreatedBy());
			assertEquals(null, accountCapture.getCreatedAt());
			assertEquals(0, accountCapture.getUpdatedBy());
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
		void regist_RegistFailureException() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("aaaaaaaa")
					.build();
			
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doThrow(DuplicateKeyException.class).when(accountMapper).insert(accountCaptor.capture());
			doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");
			
			assertThrows(RegistFailureException.class, () -> accountRepositoryImpl.regist(accountModel));
			
			verify(accountMapper).insert(any(Account.class));
			Account accountCapture = accountCaptor.getValue();
			assertEquals(null, accountCapture.getAccountNo());
			assertEquals(0, accountCapture.getCreatedBy());
			assertEquals(null, accountCapture.getCreatedAt());
			assertEquals(0, accountCapture.getUpdatedBy());
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
		void update_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.build();
			
			ArgumentCaptor<Account> cndAccountCaptor = ArgumentCaptor.forClass(Account.class);
			ArgumentCaptor<Account> targetAccountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
			
			accountRepositoryImpl.update(accountModel);
			
			verify(accountMapper).update(any(Account.class), any(Account.class));
			Account cndAccountCapture = cndAccountCaptor.getValue();
			assertEquals(1, cndAccountCapture.getAccountNo());
			
			Account targetAccountCapture = targetAccountCaptor.getValue();
			assertEquals(null, targetAccountCapture.getCreatedBy());
			assertEquals(null, targetAccountCapture.getCreatedAt());
			assertEquals(null, targetAccountCapture.getUpdatedBy());
			assertEquals(null, targetAccountCapture.getUpdatedAt());
			assertEquals(null, targetAccountCapture.getIsDeleted());
			assertEquals("aaaaaaaa", targetAccountCapture.getAccountId());
			assertEquals("AAAAAAAA", targetAccountCapture.getAccountName());
			assertEquals(null, targetAccountCapture.getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), targetAccountCapture.getBirthdate());
			assertEquals(SexEnum.NONE, targetAccountCapture.getSexKbn());
			assertEquals("none", targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals("none", targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals("", targetAccountCapture.getFreeMemo());
			assertEquals(null, targetAccountCapture.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetAccountCapture.getLastLoginDatetime());
			assertEquals(0, targetAccountCapture.getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
		void update_not_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.password("aaaaaaaa")
					.birthdate(LocalDate.of(1991, 2, 14))
					.sexKbn(SexEnum.WOMAN)
					.birthplacePrefectureKbnCode("Hokkaido")
					.residentPrefectureKbnCode("Okinawa")
					.freeMemo("フリーメモ")
					.lastLoginDatetime(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.loginFailureCount(2)
					.build();
			
			ArgumentCaptor<Account> cndAccountCaptor = ArgumentCaptor.forClass(Account.class);
			ArgumentCaptor<Account> targetAccountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
			doReturn("$2a$10$password1").when(passwordEncoder).encode("aaaaaaaa");
			
			accountRepositoryImpl.update(accountModel);
			
			verify(accountMapper).update(any(Account.class), any(Account.class));
			Account cndAccountCapture = cndAccountCaptor.getValue();
			assertEquals(1, cndAccountCapture.getAccountNo());
			
			Account targetAccountCapture = targetAccountCaptor.getValue();
			assertEquals(null, targetAccountCapture.getCreatedBy());
			assertEquals(null, targetAccountCapture.getCreatedAt());
			assertEquals(null, targetAccountCapture.getUpdatedBy());
			assertEquals(null, targetAccountCapture.getUpdatedAt());
			assertEquals(null, targetAccountCapture.getIsDeleted());
			assertEquals("aaaaaaaa", targetAccountCapture.getAccountId());
			assertEquals("AAAAAAAA", targetAccountCapture.getAccountName());
			assertEquals("$2a$10$password1", targetAccountCapture.getPassword());
			assertEquals(LocalDate.of(1991, 2, 14), targetAccountCapture.getBirthdate());
			assertEquals(SexEnum.WOMAN, targetAccountCapture.getSexKbn());
			assertEquals("Hokkaido", targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals("フリーメモ", targetAccountCapture.getFreeMemo());
			assertEquals(null, targetAccountCapture.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetAccountCapture.getLastLoginDatetime());
			assertEquals(2, targetAccountCapture.getLoginFailureCount());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1)
					.accountId("aaaaaaaa")
					.accountName("AAAAAAAA")
					.build();
			
			ArgumentCaptor<Account> cndAccountCaptor = ArgumentCaptor.forClass(Account.class);
			ArgumentCaptor<Account> targetAccountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(0).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.update(accountModel));
			
			verify(accountMapper).update(any(Account.class), any(Account.class));
			Account cndAccountCapture = cndAccountCaptor.getValue();
			assertEquals(1, cndAccountCapture.getAccountNo());
			
			Account targetAccountCapture = targetAccountCaptor.getValue();
			assertEquals(null, targetAccountCapture.getCreatedBy());
			assertEquals(null, targetAccountCapture.getCreatedAt());
			assertEquals(null, targetAccountCapture.getUpdatedBy());
			assertEquals(null, targetAccountCapture.getUpdatedAt());
			assertEquals(null, targetAccountCapture.getIsDeleted());
			assertEquals("aaaaaaaa", targetAccountCapture.getAccountId());
			assertEquals("AAAAAAAA", targetAccountCapture.getAccountName());
			assertEquals(null, targetAccountCapture.getPassword());
			assertEquals(LocalDate.of(1900, 1, 1), targetAccountCapture.getBirthdate());
			assertEquals(SexEnum.NONE, targetAccountCapture.getSexKbn());
			assertEquals("none", targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals("none", targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals("", targetAccountCapture.getFreeMemo());
			assertEquals(null, targetAccountCapture.getAuthorityKbn());
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
		void updateLoginFailureCount_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1)
					.build();
			
			ArgumentCaptor<Account> cndAccountCaptor = ArgumentCaptor.forClass(Account.class);
			ArgumentCaptor<Account> targetAccountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
			
			accountRepositoryImpl.updateLoginFailureCount(accountModel);
			
			verify(accountMapper).update(any(Account.class), any(Account.class));
			Account cndAccountCapture = cndAccountCaptor.getValue();
			assertEquals(cndAccountCapture.getAccountNo(), 1);
			
			Account targetAccountCapture = targetAccountCaptor.getValue();
			assertEquals(null, targetAccountCapture.getCreatedBy());
			assertEquals(null, targetAccountCapture.getCreatedAt());
			assertEquals(null, targetAccountCapture.getUpdatedBy());
			assertEquals(null, targetAccountCapture.getUpdatedAt());
			assertEquals(null, targetAccountCapture.getIsDeleted());
			assertEquals(null, targetAccountCapture.getAccountId());
			assertEquals(null, targetAccountCapture.getAccountName());
			assertEquals(null, targetAccountCapture.getPassword());
			assertEquals(null, targetAccountCapture.getBirthdate());
			assertEquals(null, targetAccountCapture.getSexKbn());
			assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals(null, targetAccountCapture.getFreeMemo());
			assertEquals(null, targetAccountCapture.getAuthorityKbn());
			assertEquals(null, targetAccountCapture.getLastLoginDatetime());
			assertEquals(0, targetAccountCapture.getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないAccountModelでの更新")
		void updateLoginFailureCount_not_contain_null_parameter() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1)
					.lastLoginDatetime(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.loginFailureCount(2)
					.build();
			
			ArgumentCaptor<Account> cndAccountCaptor = ArgumentCaptor.forClass(Account.class);
			ArgumentCaptor<Account> targetAccountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(1).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
			
			accountRepositoryImpl.updateLoginFailureCount(accountModel);
			
			verify(accountMapper).update(any(Account.class), any(Account.class));
			Account cndAccountCapture = cndAccountCaptor.getValue();
			assertEquals(cndAccountCapture.getAccountNo(), 1);
			
			Account targetAccountCapture = targetAccountCaptor.getValue();
			assertEquals(null, targetAccountCapture.getCreatedBy());
			assertEquals(null, targetAccountCapture.getCreatedAt());
			assertEquals(null, targetAccountCapture.getUpdatedBy());
			assertEquals(null, targetAccountCapture.getUpdatedAt());
			assertEquals(null, targetAccountCapture.getIsDeleted());
			assertEquals(null, targetAccountCapture.getAccountId());
			assertEquals(null, targetAccountCapture.getAccountName());
			assertEquals(null, targetAccountCapture.getPassword());
			assertEquals(null, targetAccountCapture.getBirthdate());
			assertEquals(null, targetAccountCapture.getSexKbn());
			assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals(null, targetAccountCapture.getFreeMemo());
			assertEquals(null, targetAccountCapture.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetAccountCapture.getLastLoginDatetime());
			assertEquals(2, targetAccountCapture.getLoginFailureCount());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void updateLoginFailureCount_UpdateFailureException() {
			AccountModel accountModel = AccountModel.builder()
					.accountNo(1)
					.build();
			
			ArgumentCaptor<Account> cndAccountCaptor = ArgumentCaptor.forClass(Account.class);
			ArgumentCaptor<Account> targetAccountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(0).when(accountMapper).update(cndAccountCaptor.capture(), targetAccountCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> accountRepositoryImpl.updateLoginFailureCount(accountModel));
			
			verify(accountMapper).update(any(Account.class), any(Account.class));
			Account cndAccountCapture = cndAccountCaptor.getValue();
			assertEquals(cndAccountCapture.getAccountNo(), 1);
			
			Account targetAccountCapture = targetAccountCaptor.getValue();
			assertEquals(null, targetAccountCapture.getCreatedBy());
			assertEquals(null, targetAccountCapture.getCreatedAt());
			assertEquals(null, targetAccountCapture.getUpdatedBy());
			assertEquals(null, targetAccountCapture.getUpdatedAt());
			assertEquals(null, targetAccountCapture.getIsDeleted());
			assertEquals(null, targetAccountCapture.getAccountId());
			assertEquals(null, targetAccountCapture.getAccountName());
			assertEquals(null, targetAccountCapture.getPassword());
			assertEquals(null, targetAccountCapture.getBirthdate());
			assertEquals(null, targetAccountCapture.getSexKbn());
			assertEquals(null, targetAccountCapture.getBirthplacePrefectureKbnCode());
			assertEquals(null, targetAccountCapture.getResidentPrefectureKbnCode());
			assertEquals(null, targetAccountCapture.getFreeMemo());
			assertEquals(null, targetAccountCapture.getAuthorityKbn());
			assertEquals(null, targetAccountCapture.getLastLoginDatetime());
			assertEquals(0, targetAccountCapture.getLoginFailureCount());
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class isExistAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void isExistAccount_true() {
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(true).when(accountMapper).isExistAccount(accountCaptor.capture());
			
			assertTrue(accountRepositoryImpl.isExistAccount(1, "aaaaaaaa"));
			verify(accountMapper, times(1)).isExistAccount(any(Account.class));
			
			Account accountCapture = accountCaptor.getValue();
			assertEquals(1, accountCapture.getAccountNo());
			assertEquals("aaaaaaaa", accountCapture.getAccountId());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void isExistAccount_false() {
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(false).when(accountMapper).isExistAccount(accountCaptor.capture());
			
			assertFalse(accountRepositoryImpl.isExistAccount(1, "aaaaaaaa"));
			verify(accountMapper, times(1)).isExistAccount(any());
			
			Account accountCapture = accountCaptor.getValue();
			assertEquals(1, accountCapture.getAccountNo());
			assertEquals("aaaaaaaa", accountCapture.getAccountId());
		}
	}
	
	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを2件以上取得")
		void getAccountList_found_some_accounts() {
			Account account1 = Account.builder()
					.accountNo(1)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(1)
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
					.accountNo(2)
					.createdBy(2)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.updatedBy(2)
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
			
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(accountList).when(accountMapper).select(accountCaptor.capture());

			List<AccountModel> actual = accountRepositoryImpl.getAccountList();
			
			Account account = accountCaptor.getValue();
			assertFalse(account.getIsDeleted());
			
			AccountModel actualAccountModel1 = actual.stream().sorted(Comparator.comparing(AccountModel::getAccountNo)).toList().getFirst();
			assertEquals(1, actualAccountModel1.getAccountNo());
			assertEquals("aaaaaaaa", actualAccountModel1.getAccountId());
			assertEquals("AAAAAAAA", actualAccountModel1.getAccountName());
			assertEquals("$2a$10$password1", actualAccountModel1.getPassword());
			assertEquals(LocalDate.of(1991, 1, 1), actualAccountModel1.getBirthdate());
			assertEquals(SexEnum.MAN, actualAccountModel1.getSexKbn());
			assertEquals("Hokkaido", actualAccountModel1.getBirthplacePrefectureKbnCode());
			assertEquals("Aomori", actualAccountModel1.getResidentPrefectureKbnCode());
			assertEquals("よろしく", actualAccountModel1.getFreeMemo());
			assertEquals(AuthorityEnum.MINI, actualAccountModel1.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualAccountModel1.getLastLoginDatetime());
			assertEquals(0, actualAccountModel1.getLoginFailureCount());
			
			AccountModel actualAccountModel2 = actual.stream().sorted(Comparator.comparing(AccountModel::getAccountNo)).toList().getLast();
			assertEquals(2, actualAccountModel2.getAccountNo());
			assertEquals("bbbbbbbb", actualAccountModel2.getAccountId());
			assertEquals("BBBBBBBB", actualAccountModel2.getAccountName());
			assertEquals("$2a$10$password2", actualAccountModel2.getPassword());
			assertEquals(LocalDate.of(1991, 2, 1), actualAccountModel2.getBirthdate());
			assertEquals(SexEnum.WOMAN, actualAccountModel2.getSexKbn());
			assertEquals("Iwate", actualAccountModel2.getBirthplacePrefectureKbnCode());
			assertEquals("Okinawa", actualAccountModel2.getResidentPrefectureKbnCode());
			assertEquals("お願いします", actualAccountModel2.getFreeMemo());
			assertEquals(AuthorityEnum.ADMINISTRATOR, actualAccountModel2.getAuthorityKbn());
			assertEquals(OffsetDateTime.of(2002, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualAccountModel2.getLastLoginDatetime());
			assertEquals(1, actualAccountModel2.getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが0件")
		void getAccountList_not_found() {
			List<Account> expected = new ArrayList<Account>();
			
			ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
			doReturn(expected).when(accountMapper).select(accountCaptor.capture());

			List<AccountModel> actual = accountRepositoryImpl.getAccountList();
			assertEquals(expected, actual);
			
			Account account = accountCaptor.getValue();
			assertFalse(account.getIsDeleted());
		}
	}
}