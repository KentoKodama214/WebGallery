package com.web.gallary.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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

import com.web.gallary.AccountPrincipal;
import com.web.gallary.config.LoginConfig;
import com.web.gallary.entity.Account;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.AccountModel;
import com.web.gallary.repository.impl.AccountRepositoryImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
	@InjectMocks
	private AccountServiceImpl accountServiceImpl;
	
	@Mock
	private AccountRepositoryImpl accountRepositoryImpl;
	
	@Mock
	private AccountPrincipal accountPrincipal;
	
	@Mock
	private LoginConfig loginConfig;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class loadUserByUsername {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void loadUserByUsername_success() {
			String accountId = "aaaaaaaa";
			String password = "AAAAAAAA";
			Account account = Account.builder()
					.accountNo(1)
					.accountId(accountId)
					.loginFailureCount(0)
					.password(password)
					.build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(accountId);
			doReturn(3).when(loginConfig).getFailCount();

			UserDetails userDetails = accountServiceImpl.loadUserByUsername(accountId);
			assertEquals(accountId, userDetails.getUsername());
			assertEquals(password, userDetails.getPassword());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UsernameNotFoundExceptionをthrowする")
		void loadUserByUsername_UsernameNotFoundException() {
			String accountId = "aaaaaaaa";
			doReturn(null).when(accountRepositoryImpl).getByAccountId(accountId);
			assertThrows(UsernameNotFoundException.class, () -> accountServiceImpl.loadUserByUsername(accountId));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class registAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを新規登録")
		void registAccount_success() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder().accountId("aaaaaaaa").build();
			doReturn(false).when(accountRepositoryImpl).isExistAccount(null, "aaaaaaaa");
			doNothing().when(accountRepositoryImpl).regist(accountModel);
			assertTrue(accountServiceImpl.registAccount(accountModel));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが既に存在する")
		void registAccount_account_already_exist() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder().accountId("aaaaaaaa").build();
			doReturn(true).when(accountRepositoryImpl).isExistAccount(null, "aaaaaaaa");
			verify(accountRepositoryImpl,times(0)).regist(accountModel);
			assertFalse(accountServiceImpl.registAccount(accountModel));
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void registAccount_RegistFailureException() throws RegistFailureException {
			AccountModel accountModel = AccountModel.builder().accountId("aaaaaaaa").build();
			doReturn(false).when(accountRepositoryImpl).isExistAccount(null, "aaaaaaaa");
			doThrow(RegistFailureException.class).when(accountRepositoryImpl).regist(accountModel);
			assertThrows(RegistFailureException.class, () -> accountServiceImpl.registAccount(accountModel));
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class updateAccount {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントを更新")
		void updateAccount_success() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(1).accountId("aaaaaaaa").build();
			doReturn(false).when(accountRepositoryImpl).isExistAccount(1, "aaaaaaaa");
			doNothing().when(accountRepositoryImpl).update(accountModel);
			assertFalse(accountServiceImpl.updateAccount(accountModel));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが既に存在する")
		void updateAccount_account_already_exist() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(1).accountId("aaaaaaaa").build();
			doReturn(true).when(accountRepositoryImpl).isExistAccount(1, "aaaaaaaa");
			verify(accountRepositoryImpl,times(0)).update(accountModel);
			assertTrue(accountServiceImpl.updateAccount(accountModel));
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void updateAccount_UpdateFailureException() throws UpdateFailureException {
			AccountModel accountModel = AccountModel.builder().accountNo(1).accountId("aaaaaaaa").build();
			doReturn(false).when(accountRepositoryImpl).isExistAccount(1, "aaaaaaaa");
			doThrow(UpdateFailureException.class).when(accountRepositoryImpl).update(accountModel);
			assertThrows(UpdateFailureException.class, () -> accountServiceImpl.updateAccount(accountModel));
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountById {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void getAccountById_found() {
			Account expected = Account.builder().build();
			doReturn(expected).when(accountRepositoryImpl).getByAccountId("aaaaaaaa");
			assertEquals(expected, accountServiceImpl.getAccountById("aaaaaaaa"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合、nullを返す")
		void getAccountById_not_found() {
			doReturn(null).when(accountRepositoryImpl).getByAccountId("aaaaaaaa");
			assertNull(accountServiceImpl.getAccountById("aaaaaaaa"));
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountList {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウントが存在する場合")
		void getAccountList_found() {
			List<AccountModel> accountModelList = new ArrayList<AccountModel>();
			accountModelList.add(AccountModel.builder().accountId("cccccccc").build());
			accountModelList.add(AccountModel.builder().accountId("bbbbbbbb").build());
			accountModelList.add(AccountModel.builder().accountId("aaaaaaaa").build());
			
			doReturn(accountModelList).when(accountRepositoryImpl).getAccountList();
			
			List<AccountModel> actual = accountServiceImpl.getAccountList();
			assertEquals(accountModelList.size(), actual.size());
			assertEquals("aaaaaaaa", actual.get(0).getAccountId());
			assertEquals("bbbbbbbb", actual.get(1).getAccountId());
			assertEquals("cccccccc", actual.get(2).getAccountId());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void getAccountList_not_found() {
			List<AccountModel> accountModelList = new ArrayList<AccountModel>();
			
			doReturn(accountModelList).when(accountRepositoryImpl).getAccountList();
			
			List<AccountModel> actual = accountServiceImpl.getAccountList();
			assertEquals(0, actual.size());
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleAuthenticationSuccess {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handle_success() throws UpdateFailureException {
			String username = "aaaaaaaa";
			String password = "AAAAAAAA";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);
			
			Account account = Account.builder().accountNo(1).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(username);
			
			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doNothing().when(accountRepositoryImpl).updateLoginFailureCount(accountModelCaptor.capture());
			
			accountServiceImpl.handle(event);
			
			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertNull(accountModel.getAccountId());
			assertNull(accountModel.getAccountName());
			assertNull(accountModel.getPassword());
			assertNull(accountModel.getBirthdate());
			assertNull(accountModel.getSexKbn());
			assertNull(accountModel.getBirthplacePrefectureKbnCode());
			assertNull(accountModel.getResidentPrefectureKbnCode());
			assertNull(accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNotNull(accountModel.getLastLoginDatetime());
			assertEquals(0, accountModel.getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void handle_UpdateFailureException() throws UpdateFailureException {
			String username = "aaaaaaaa";
			String password = "AAAAAAAA";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);
			
			Account account = Account.builder().accountNo(1).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(username);
			
			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doThrow(UpdateFailureException.class).when(accountRepositoryImpl).updateLoginFailureCount(accountModelCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> accountServiceImpl.handle(event));
			
			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertNull(accountModel.getAccountId());
			assertNull(accountModel.getAccountName());
			assertNull(accountModel.getPassword());
			assertNull(accountModel.getBirthdate());
			assertNull(accountModel.getSexKbn());
			assertNull(accountModel.getBirthplacePrefectureKbnCode());
			assertNull(accountModel.getResidentPrefectureKbnCode());
			assertNull(accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNotNull(accountModel.getLastLoginDatetime());
			assertEquals(0, accountModel.getLoginFailureCount());
		}
	}
	
	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
			
			Account account = Account.builder().accountNo(1).loginFailureCount(1).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(username);
			
			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doNothing().when(accountRepositoryImpl).updateLoginFailureCount(accountModelCaptor.capture());
			
			accountServiceImpl.handle(event);
			
			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertNull(accountModel.getAccountId());
			assertNull(accountModel.getAccountName());
			assertNull(accountModel.getPassword());
			assertNull(accountModel.getBirthdate());
			assertNull(accountModel.getSexKbn());
			assertNull(accountModel.getBirthplacePrefectureKbnCode());
			assertNull(accountModel.getResidentPrefectureKbnCode());
			assertNull(accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNull(accountModel.getLastLoginDatetime());
			assertEquals(2, accountModel.getLoginFailureCount());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウントが存在しない場合")
		void handle_account_not_found() throws UpdateFailureException {
			String username = "aaaaaaaa";
			String password = "AAAAAAAA";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			
			String message = "Invalid username or password";
			BadCredentialsException exception = new BadCredentialsException(message);
			
			AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(authentication, exception);
			
			doReturn(null).when(accountRepositoryImpl).getByAccountId(username);
			
			accountServiceImpl.handle(event);
			verify(accountRepositoryImpl, times(0)).updateLoginFailureCount(any(AccountModel.class));
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void handle_UpdateFailureException() throws UpdateFailureException {
			String username = "aaaaaaaa";
			String password = "AAAAAAAA";
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			Authentication authentication = new UsernamePasswordAuthenticationToken(username, password, authorities);
			
			String message = "Invalid username or password";
			BadCredentialsException exception = new BadCredentialsException(message);
			
			AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(authentication, exception);
			
			Account account = Account.builder().accountNo(1).loginFailureCount(1).build();
			doReturn(account).when(accountRepositoryImpl).getByAccountId(username);
			
			ArgumentCaptor<AccountModel> accountModelCaptor = ArgumentCaptor.forClass(AccountModel.class);
			doThrow(UpdateFailureException.class).when(accountRepositoryImpl).updateLoginFailureCount(accountModelCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () ->accountServiceImpl.handle(event));
			
			AccountModel accountModel = accountModelCaptor.getValue();
			assertEquals(1, accountModel.getAccountNo());
			assertNull(accountModel.getAccountId());
			assertNull(accountModel.getAccountName());
			assertNull(accountModel.getPassword());
			assertNull(accountModel.getBirthdate());
			assertNull(accountModel.getSexKbn());
			assertNull(accountModel.getBirthplacePrefectureKbnCode());
			assertNull(accountModel.getResidentPrefectureKbnCode());
			assertNull(accountModel.getFreeMemo());
			assertNull(accountModel.getAuthorityKbn());
			assertNull(accountModel.getLastLoginDatetime());
			assertEquals(2, accountModel.getLoginFailureCount());
		}
	}
}