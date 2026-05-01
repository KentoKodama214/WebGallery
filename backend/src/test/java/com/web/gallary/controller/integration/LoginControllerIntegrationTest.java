package com.web.gallary.controller.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.AccountPrincipal;
import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.AuthorityEnum;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class LoginControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class login {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void login_success() throws Exception {
			mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class success {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void success_not_login_user() throws Exception {
			mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void success_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			Account sessionAccount = Account.builder()
					.accountNo(1)
					.accountId(accountId)
					.accountName("AAAAAAAA")
					.password("$2a$10$password1")
					.authorityKbn(AuthorityEnum.ADMINISTRATOR)
					.build();
			
			AccountPrincipal accountPrincipal = new AccountPrincipal(sessionAccount, 0);
			Authentication authentication = new UsernamePasswordAuthenticationToken(accountPrincipal, null);
			
			mockMvc.perform(
					get("/")
					.with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
					.with(csrf())
				)
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/photo/" + accountId + "/photo_list"));
		}
	}
}