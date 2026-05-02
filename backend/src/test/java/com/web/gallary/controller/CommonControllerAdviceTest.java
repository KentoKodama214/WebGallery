package com.web.gallary.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.bind.annotation.GetMapping;

import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.ForbiddenAccountException;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.helper.SessionHelper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CommonControllerAdviceTest {
	@InjectMocks
	private CommonControllerAdvice commonControllerAdvice;

	@Mock
	private SessionHelper sessionHelper;

	private MockMvc mockMvc;

	@Controller
	static class TestController extends BaseController {
		@GetMapping("/test/forbidden")
		public String throwForbiddenAccountException() throws ForbiddenAccountException {
			throw new ForbiddenAccountException(ErrorEnum.INVALID_INPUT);
		}

		@GetMapping("/test/photo_not_found")
		public String throwPhotoNotFoundException() throws PhotoNotFoundException {
			throw new PhotoNotFoundException(ErrorEnum.FAIL_TO_REGIST_PHOTO);
		}
	}

	@BeforeEach
	void setUp() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".html");
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(commonControllerAdvice)
				.setViewResolvers(viewResolver)
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleFileForbiddenAccountException {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void handleFileForbiddenAccountException_not_login_user() throws Exception {
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/forbidden"))
				.andExpect(status().isForbidden())
				.andExpect(view().name("error_page"))
				.andExpect(model().attribute("httpStatus", HttpStatus.FORBIDDEN.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.INVALID_INPUT.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void handleFileForbiddenAccountException_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/forbidden"))
				.andExpect(status().isForbidden())
				.andExpect(view().name("error_page"))
				.andExpect(model().attribute("httpStatus", HttpStatus.FORBIDDEN.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.INVALID_INPUT.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/photo/" + accountId + "/photo_list"));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handlePhotoNotFoundException {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void handlePhotoNotFoundException_not_login_user() throws Exception {
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/photo_not_found"))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("error_page"))
				.andExpect(model().attribute("httpStatus", HttpStatus.BAD_REQUEST.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.FAIL_TO_REGIST_PHOTO.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.FAIL_TO_REGIST_PHOTO.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void handlePhotoNotFoundException_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/photo_not_found"))
				.andExpect(status().isBadRequest())
				.andExpect(view().name("error_page"))
				.andExpect(model().attribute("httpStatus", HttpStatus.BAD_REQUEST.value()))
				.andExpect(model().attribute("errorCode", ErrorEnum.FAIL_TO_REGIST_PHOTO.getErrorCode()))
				.andExpect(model().attribute("errorMessage", ErrorEnum.FAIL_TO_REGIST_PHOTO.getErrorMessage()))
				.andExpect(model().attribute("goBackPageUrl", "/photo/" + accountId + "/photo_list"));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getGoBackPageUrl {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void getGoBackPageUrl_not_login_user() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method goBackPageUrl = CommonControllerAdvice.class.getDeclaredMethod("getGoBackPageUrl");
			goBackPageUrl.setAccessible(true);
			doReturn(null).when(sessionHelper).getAccountId();
			assertEquals("/login", (String) goBackPageUrl.invoke(commonControllerAdvice));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void getGoBackPageUrl_login_user() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method goBackPageUrl = CommonControllerAdvice.class.getDeclaredMethod("getGoBackPageUrl");
			goBackPageUrl.setAccessible(true);
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			assertEquals("/photo/aaaaaaaa/photo_list", (String) goBackPageUrl.invoke(commonControllerAdvice));
		}
	}
}
