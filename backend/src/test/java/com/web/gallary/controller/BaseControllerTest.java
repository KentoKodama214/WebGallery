package com.web.gallary.controller;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class BaseControllerTest {
	@InjectMocks
	private BaseController baseController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".html");
		mockMvc = MockMvcBuilders.standaloneSetup(baseController)
				.setViewResolvers(viewResolver)
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class footer {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void footer_success() throws Exception {
			mockMvc.perform(get("/footer"))
				.andExpect(status().isOk())
				.andExpect(view().name("footer"));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class error {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void error_success() throws Exception {
			mockMvc.perform(get("/error"))
				.andExpect(status().isOk())
				.andExpect(view().name("error"));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class error_page {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むErrorRequest")
		void error_contain_null_parameter() throws Exception {
			mockMvc.perform(get("/error_page")
					.param("errorCode", "400")
					.param("httpStatus", "400"))
				.andExpect(status().isOk())
				.andExpect(view().name("error_page"))
				.andExpect(model().attribute("goBackPageUrl", "/"))
				.andExpect(model().attribute("errorMessage", "エラーが発生しました。システム管理者に問い合わせてください。"))
				.andExpect(model().attribute("errorCode", "400"))
				.andExpect(model().attribute("httpStatus", 400));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないErrorRequest")
		void error_not_contain_null_parameter() throws Exception {
			mockMvc.perform(get("/error_page")
					.param("errorCode", "400")
					.param("httpStatus", "400")
					.param("goBackPageUrl", "/login")
					.param("errorMessage", "パラメータが不正です。"))
				.andExpect(status().isOk())
				.andExpect(view().name("error_page"))
				.andExpect(model().attribute("goBackPageUrl", "/login"))
				.andExpect(model().attribute("errorMessage", "パラメータが不正です。"))
				.andExpect(model().attribute("errorCode", "400"))
				.andExpect(model().attribute("httpStatus", 400));
		}
	}
}
