package com.web.gallary.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.web.gallary.helper.SessionHelper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {
	@InjectMocks
	private LoginController loginController;

	@Mock
	private SessionHelper sessionHelper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".html");
		mockMvc = MockMvcBuilders.standaloneSetup(loginController)
				.setViewResolvers(viewResolver)
				.build();
	}

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
				.andExpect(view().name("login"))
				.andExpect(model().attributeDoesNotExist());
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
			doReturn(null).when(sessionHelper).getAccountId();
			mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void success_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();
			mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/photo/" + accountId + "/photo_list"));
		}
	}
}
