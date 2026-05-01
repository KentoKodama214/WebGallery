package com.web.gallary.hepler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallary.AccountPrincipal;
import com.web.gallary.helper.SessionHelper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class SessionHelperTest {
	@InjectMocks
	private SessionHelper sessionHelper;

	@Mock
	private Authentication authentication;

	@Mock
	private AccountPrincipal accountPrincipal;
	
	@BeforeEach
	public void setUp() {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：セッションに存在し、アカウント番号を返す")
		void getAccountNo_found() {
			doReturn(accountPrincipal).when(authentication).getPrincipal();
			doReturn(1).when(accountPrincipal).getAccountNo();
			
			Integer actual = sessionHelper.getAccountNo();
			assertEquals(Integer.valueOf(1), actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：セッションに存在せず、nullを返す")
		void getAccountNo_not_found() {
			doReturn(null).when(authentication).getPrincipal();
			
			Integer actual = sessionHelper.getAccountNo();
			assertNull(actual);
			verify(accountPrincipal, times(0)).getAccountNo();
		}
	}
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getAccountId {
		@Test
		@Order(1)
		@DisplayName("正常系：セッションに存在し、アカウントIDを返す")
		void getAccountId_found() {
			doReturn(accountPrincipal).when(authentication).getPrincipal();
			doReturn("aaaaaaaa").when(accountPrincipal).getUsername();
			
			String actual = sessionHelper.getAccountId();
			assertEquals("aaaaaaaa", actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：セッションに存在せず、nullを返す")
		void getAccountId_not_found() {
			doReturn(null).when(authentication).getPrincipal();
			
			String actual = sessionHelper.getAccountId();
			assertNull(actual);
			verify(accountPrincipal, times(0)).getUsername();
		}
	}
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPassword {
		@Test
		@Order(1)
		@DisplayName("正常系：セッションに存在し、パスワードを返す")
		void getPassword_found() {
			doReturn(accountPrincipal).when(authentication).getPrincipal();
			doReturn("pasword").when(accountPrincipal).getPassword();
			
			String actual = sessionHelper.getPassword();
			assertEquals("pasword", actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：セッションに存在せず、nullを返す")
		void getPassword_not_found() {
			doReturn(null).when(authentication).getPrincipal();
			
			String actual = sessionHelper.getPassword();
			assertNull(actual);
			verify(accountPrincipal, times(0)).getPassword();
		}
	}
}