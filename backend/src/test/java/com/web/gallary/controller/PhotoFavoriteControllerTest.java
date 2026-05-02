package com.web.gallary.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.helper.SessionHelper;
import com.web.gallary.model.PhotoFavoriteModel;
import com.web.gallary.service.impl.PhotoFavoriteServiceImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoFavoriteControllerTest {
	@InjectMocks
	private PhotoFavoriteController photoFavoriteController;

	@Mock
	private PhotoFavoriteServiceImpl photoFavoriteServiceImpl;

	@Mock
	private SessionHelper sessionHelper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(photoFavoriteController)
				.setControllerAdvice(new CommonRestControllerAdvice(sessionHelper))
				.build();
	}

	private String readJsonFile(String fileName) throws Exception {
		return new String(
				new ClassPathResource("json/controller/PhotoFavoriteControllerTest/" + fileName).getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class addFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void addFavorite_success() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoFavoriteModel> photoFavoriteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteModel.class);
			doNothing().when(photoFavoriteServiceImpl).addFavorite(photoFavoriteModelCaptor.capture());

			mockMvc.perform(post("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("add_favorite.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("お気に入りに追加しました。"));

			PhotoFavoriteModel photoFavoriteModel = photoFavoriteModelCaptor.getValue();
			assertEquals(1, photoFavoriteModel.getAccountNo());
			assertEquals(2, photoFavoriteModel.getFavoritePhotoAccountNo());
			assertEquals(3, photoFavoriteModel.getFavoritePhotoNo());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void addFavorite_BadRequestException() throws Exception {
			mockMvc.perform(post("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("add_favorite_badrequest.json")))
				.andExpect(status().isBadRequest());

			verify(sessionHelper, times(0)).getAccountNo();
			verify(photoFavoriteServiceImpl, times(0)).addFavorite(any(PhotoFavoriteModel.class));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void addFavorite_RegistFailureException() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoFavoriteModel> photoFavoriteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteModel.class);
			doThrow(RegistFailureException.class).when(photoFavoriteServiceImpl).addFavorite(photoFavoriteModelCaptor.capture());

			mockMvc.perform(post("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("add_favorite.json")))
				.andExpect(status().isConflict());

			PhotoFavoriteModel photoFavoriteModel = photoFavoriteModelCaptor.getValue();
			assertEquals(1, photoFavoriteModel.getAccountNo());
			assertEquals(2, photoFavoriteModel.getFavoritePhotoAccountNo());
			assertEquals(3, photoFavoriteModel.getFavoritePhotoNo());
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deleteFavorite_success() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoFavoriteModel> photoFavoriteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteModel.class);
			doNothing().when(photoFavoriteServiceImpl).deleteFavorite(photoFavoriteModelCaptor.capture());

			mockMvc.perform(delete("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_favorite.json")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpStatus").value(200))
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.message").value("お気に入りを解除しました。"));

			PhotoFavoriteModel photoFavoriteModel = photoFavoriteModelCaptor.getValue();
			assertEquals(1, photoFavoriteModel.getAccountNo());
			assertEquals(2, photoFavoriteModel.getFavoritePhotoAccountNo());
			assertEquals(3, photoFavoriteModel.getFavoritePhotoNo());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：BadRequestExceptionをthrowする")
		void deleteFavorite_BadRequestException() throws Exception {
			mockMvc.perform(delete("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_favorite_badrequest.json")))
				.andExpect(status().isBadRequest());

			verify(sessionHelper, times(0)).getAccountNo();
			verify(photoFavoriteServiceImpl, times(0)).deleteFavorite(any(PhotoFavoriteModel.class));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deleteFavorite_UpdateFailureException() throws Exception {
			doReturn(1).when(sessionHelper).getAccountNo();

			ArgumentCaptor<PhotoFavoriteModel> photoFavoriteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteModel.class);
			doThrow(UpdateFailureException.class).when(photoFavoriteServiceImpl).deleteFavorite(photoFavoriteModelCaptor.capture());

			mockMvc.perform(delete("/api/v1/photos/favorites")
					.contentType(MediaType.APPLICATION_JSON)
					.content(readJsonFile("delete_favorite.json")))
				.andExpect(status().isConflict());

			PhotoFavoriteModel photoFavoriteModel = photoFavoriteModelCaptor.getValue();
			assertEquals(1, photoFavoriteModel.getAccountNo());
			assertEquals(2, photoFavoriteModel.getFavoritePhotoAccountNo());
			assertEquals(3, photoFavoriteModel.getFavoritePhotoNo());
		}
	}
}
