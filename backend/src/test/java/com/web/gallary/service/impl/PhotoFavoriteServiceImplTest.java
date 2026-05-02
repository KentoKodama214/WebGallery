package com.web.gallary.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.test.context.ActiveProfiles;

import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoFavoriteDeleteModel;
import com.web.gallary.model.PhotoFavoriteModel;
import com.web.gallary.repository.impl.PhotoFavoriteRepositoryImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoFavoriteServiceImplTest {
	@InjectMocks
	private PhotoFavoriteServiceImpl photoFavoriteServiceImpl;
	
	@Mock
	private PhotoFavoriteRepositoryImpl photoFavoriteRepositoryImpl;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class addFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void addFavorite_success() throws RegistFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			doNothing().when(photoFavoriteRepositoryImpl).regist(photoFavoriteModel);
			photoFavoriteServiceImpl.addFavorite(photoFavoriteModel);
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void addFavorite_RegistFailureException() throws RegistFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			doThrow(RegistFailureException.class).when(photoFavoriteRepositoryImpl).regist(photoFavoriteModel);
			assertThrows(RegistFailureException.class, () -> photoFavoriteServiceImpl.addFavorite(photoFavoriteModel));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deleteFavorite_success() throws UpdateFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			ArgumentCaptor<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteDeleteModel.class);
			doNothing().when(photoFavoriteRepositoryImpl).delete(photoFavoriteDeleteModelCaptor.capture());
			
			photoFavoriteServiceImpl.deleteFavorite(photoFavoriteModel);
			
			PhotoFavoriteDeleteModel photoFavoriteDeleteModel = photoFavoriteDeleteModelCaptor.getValue();
			assertEquals(1, photoFavoriteDeleteModel.getAccountNo());
			assertEquals(1, photoFavoriteDeleteModel.getFavoritePhotoAccountNo());
			assertEquals(1, photoFavoriteDeleteModel.getFavoritePhotoNo());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deleteFavorite_UpdateFailureException() throws UpdateFailureException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			ArgumentCaptor<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteDeleteModel.class);
			doThrow(UpdateFailureException.class).when(photoFavoriteRepositoryImpl).delete(photoFavoriteDeleteModelCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () ->photoFavoriteServiceImpl.deleteFavorite(photoFavoriteModel));
			
			PhotoFavoriteDeleteModel photoFavoriteDeleteModel = photoFavoriteDeleteModelCaptor.getValue();
			assertEquals(1, photoFavoriteDeleteModel.getAccountNo());
			assertEquals(1, photoFavoriteDeleteModel.getFavoritePhotoAccountNo());
			assertEquals(1, photoFavoriteDeleteModel.getFavoritePhotoNo());
		}
	}
}