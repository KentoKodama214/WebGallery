package com.web.gallary.controller.integration;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class BaseControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	
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
				.andExpect(status().is3xxRedirection());
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
			mockMvc.perform(get("/error_page"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("goBackPageUrl", "/"))
				.andExpect(model().attribute("errorMessage", "エラーが発生しました。システム管理者に問い合わせてください。"))
				.andExpect(model().attributeDoesNotExist("errorCode"))
				.andExpect(model().attributeDoesNotExist("httpStatus"))
				.andExpect(view().name("error_page"));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないErrorRequest")
		void error_not_contain_null_parameter() throws Exception {
			mockMvc.perform(
					get("/error_page")
					.param("httpStatus", "400")
					.param("errorCode", "400")
					.param("errorMessage", "リクエスト不正です")
					.param("goBackPageUrl", "/login")
				)
				.andExpect(status().isOk())
				.andExpect(model().attribute("goBackPageUrl", "/login"))
				.andExpect(model().attribute("errorMessage", "リクエスト不正です"))
				.andExpect(model().attribute("errorCode", "400"))
				.andExpect(model().attribute("httpStatus", 400))
				.andExpect(view().name("error_page"));
		}
	}
}