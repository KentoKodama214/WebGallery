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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.BadRequestException;
import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.ForbiddenAccountException;
import com.web.gallary.exception.PhotoNotAdditableException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CommonRestControllerAdviceTest {
	@InjectMocks
	private CommonRestControllerAdvice commonRestControllerAdvice;

	@Mock
	private SessionHelper sessionHelper;

	private MockMvc mockMvc;

	@RestController
	static class TestRestController extends PhotoFavoriteController {
		TestRestController() {
			super(null, null);
		}

		@GetMapping("/test/bad_request")
		public String throwBadRequestException() throws BadRequestException {
			throw new BadRequestException(ErrorEnum.INVALID_INPUT);
		}

		@GetMapping("/test/forbidden")
		public String throwForbiddenAccountException() throws ForbiddenAccountException {
			throw new ForbiddenAccountException(ErrorEnum.INVALID_INPUT);
		}

		@GetMapping("/test/file_duplicate")
		public String throwFileDuplicateException() throws FileDuplicateException {
			throw new FileDuplicateException(ErrorEnum.INVALID_INPUT);
		}

		@GetMapping("/test/photo_not_additable")
		public String throwPhotoNotAdditableException() throws PhotoNotAdditableException {
			throw new PhotoNotAdditableException(ErrorEnum.INVALID_INPUT);
		}

		@GetMapping("/test/regist_failure")
		public String throwRegistFailureException() throws RegistFailureException {
			throw new RegistFailureException(ErrorEnum.INVALID_INPUT);
		}

		@GetMapping("/test/update_failure")
		public String throwUpdateFailureException() throws UpdateFailureException {
			throw new UpdateFailureException(ErrorEnum.INVALID_INPUT);
		}
	}

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestRestController())
				.setControllerAdvice(commonRestControllerAdvice)
				.build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleBadRequestException {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handleBadRequestException_not_login_user() throws Exception {
			mockMvc.perform(get("/test/bad_request"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.httpStatus").value(400))
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.message").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleFileForbiddenAccountException {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void handleFileForbiddenAccountException_not_login_user() throws Exception {
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/forbidden"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.httpStatus").value(403))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void handleFileForbiddenAccountException_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/forbidden"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.httpStatus").value(403))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/" + accountId + "/photo_list"));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleFileDuplicateException {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void handleFileDuplicateException_not_login_user() throws Exception {
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/file_duplicate"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void handleFileDuplicateException_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/file_duplicate"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/" + accountId + "/photo_list"));
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handlePhotoNotAdditableException {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void handlePhotoNotAdditableException_not_login_user() throws Exception {
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/photo_not_additable"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.httpStatus").value(400))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void handlePhotoNotAdditableException_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/photo_not_additable"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.httpStatus").value(400))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/" + accountId + "/photo_list"));
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleInsertFailedException {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void handleInsertFailedException_not_login_user() throws Exception {
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/regist_failure"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void handleInsertFailedException_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/regist_failure"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/" + accountId + "/photo_list"));
		}
	}

	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleUpdateFailureException {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void handleUpdateFailureException_not_login_user() throws Exception {
			doReturn(null).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/update_failure"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/login"));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void handleUpdateFailureException_login_user() throws Exception {
			String accountId = "aaaaaaaa";
			doReturn(accountId).when(sessionHelper).getAccountId();

			mockMvc.perform(get("/test/update_failure"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()))
				.andExpect(jsonPath("$.goBackPageUrl").value("/photo/" + accountId + "/photo_list"));
		}
	}

	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getGoBackPageUrl {
		@Test
		@Order(1)
		@DisplayName("正常系：非ログインユーザーの場合")
		void getGoBackPageUrl_not_login_user() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method goBackPageUrl = CommonRestControllerAdvice.class.getDeclaredMethod("getGoBackPageUrl");
			goBackPageUrl.setAccessible(true);
			doReturn(null).when(sessionHelper).getAccountId();
			assertEquals("/login", (String) goBackPageUrl.invoke(commonRestControllerAdvice));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログインユーザーの場合")
		void getGoBackPageUrl_login_user() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method goBackPageUrl = CommonRestControllerAdvice.class.getDeclaredMethod("getGoBackPageUrl");
			goBackPageUrl.setAccessible(true);
			doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
			assertEquals("/photo/aaaaaaaa/photo_list", (String) goBackPageUrl.invoke(commonRestControllerAdvice));
		}
	}
}
