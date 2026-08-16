package com.web.gallery.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.ForbiddenAccountException;
import com.web.gallery.exception.PhotoNotAdditableException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CommonRestControllerAdviceTest {
	@InjectMocks
	private CommonRestControllerAdvice commonRestControllerAdvice;

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
		@DisplayName("正常系")
		void handleFileForbiddenAccountException_success() throws Exception {
			mockMvc.perform(get("/test/forbidden"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.httpStatus").value(403))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleFileDuplicateException {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handleFileDuplicateException_success() throws Exception {
			mockMvc.perform(get("/test/file_duplicate"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handlePhotoNotAdditableException {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handlePhotoNotAdditableException_success() throws Exception {
			mockMvc.perform(get("/test/photo_not_additable"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.httpStatus").value(400))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleInsertFailedException {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handleInsertFailedException_success() throws Exception {
			mockMvc.perform(get("/test/regist_failure"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
	}

	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class handleUpdateFailureException {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void handleUpdateFailureException_success() throws Exception {
			mockMvc.perform(get("/test/update_failure"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.httpStatus").value(409))
				.andExpect(jsonPath("$.errorMessage").value(ErrorEnum.INVALID_INPUT.getErrorMessage()));
		}
	}
}
