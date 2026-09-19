package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.enumeration.AuthorityEnum;
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

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class SessionHelperTest {
  @InjectMocks private SessionHelper sessionHelper;

  @Mock private Authentication authentication;

  @Mock private AccountPrincipal accountPrincipal;

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
      doReturn(1L).when(accountPrincipal).getAccountNo();

      Long actual = sessionHelper.getAccountNo();
      assertEquals(Long.valueOf(1L), actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：セッションに存在せず、nullを返す")
    void getAccountNo_not_found() {
      doReturn(null).when(authentication).getPrincipal();

      Long actual = sessionHelper.getAccountNo();
      assertNull(actual);
      verify(accountPrincipal, times(0)).getAccountNo();
    }

    @Test
    @Order(3)
    @DisplayName("異常系：認証情報自体が存在しない場合、nullを返す")
    void getAccountNo_authenticationNull() {
      SecurityContextHolder.getContext().setAuthentication(null);

      Long actual = sessionHelper.getAccountNo();
      assertNull(actual);
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

    @Test
    @Order(3)
    @DisplayName("異常系：認証情報自体が存在しない場合、nullを返す")
    void getAccountId_authenticationNull() {
      SecurityContextHolder.getContext().setAuthentication(null);

      String actual = sessionHelper.getAccountId();
      assertNull(actual);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getAuthorityKbn {
    @Test
    @Order(1)
    @DisplayName("正常系：セッションに存在し、権限区分を返す")
    void getAuthorityKbn_found() {
      doReturn(accountPrincipal).when(authentication).getPrincipal();
      doReturn(AuthorityEnum.NORMAL).when(accountPrincipal).getAuthorityKbn();

      AuthorityEnum actual = sessionHelper.getAuthorityKbn();
      assertEquals(AuthorityEnum.NORMAL, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：セッションに存在せず、nullを返す")
    void getAuthorityKbn_not_found() {
      doReturn(null).when(authentication).getPrincipal();

      AuthorityEnum actual = sessionHelper.getAuthorityKbn();
      assertNull(actual);
      verify(accountPrincipal, times(0)).getAuthorityKbn();
    }

    @Test
    @Order(3)
    @DisplayName("異常系：認証情報自体が存在しない場合、nullを返す")
    void getAuthorityKbn_authenticationNull() {
      SecurityContextHolder.getContext().setAuthentication(null);

      AuthorityEnum actual = sessionHelper.getAuthorityKbn();
      assertNull(actual);
    }
  }
}
