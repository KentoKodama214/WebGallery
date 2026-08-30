package com.web.gallery.service.impl;

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

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.repository.impl.PhotoDetailRepositoryImpl;
import com.web.gallery.repository.impl.PhotoFavoriteRepositoryImpl;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoFavoriteServiceImplTest {
	@InjectMocks
	private PhotoFavoriteServiceImpl photoFavoriteServiceImpl;
	
	@Mock
	private PhotoFavoriteRepositoryImpl photoFavoriteRepositoryImpl;

	@Mock
	private PhotoDetailRepositoryImpl photoDetailRepositoryImpl;

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class addFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void addFavorite_success() throws GalleryException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(2L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			when(photoDetailRepositoryImpl.getPhotoDetail(any(PhotoDetailSearchModel.class)))
					.thenReturn(null);
			doNothing().when(photoFavoriteRepositoryImpl).regist(photoFavoriteModel);
			photoFavoriteServiceImpl.addFavorite(photoFavoriteModel);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void addFavorite_RegistFailureException() throws GalleryException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(2L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			when(photoDetailRepositoryImpl.getPhotoDetail(any(PhotoDetailSearchModel.class)))
					.thenReturn(null);
			doThrow(RegistFailureException.class).when(photoFavoriteRepositoryImpl).regist(photoFavoriteModel);
			assertThrows(RegistFailureException.class, () -> photoFavoriteServiceImpl.addFavorite(photoFavoriteModel));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：自分自身の写真をお気に入り登録しようとした場合はBadRequestExceptionをthrowする")
		void addFavorite_selfFavorite() {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			assertThrows(BadRequestException.class, () -> photoFavoriteServiceImpl.addFavorite(photoFavoriteModel));
			verifyNoInteractions(photoFavoriteRepositoryImpl);
		}

		@Test
		@Order(4)
		@DisplayName("異常系：対象の写真が存在しない場合はPhotoNotFoundExceptionをthrowする")
		void addFavorite_photoNotFound() throws GalleryException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(2L))
					.favoritePhotoNo(new PhotoNo(999L))
					.build();
			when(photoDetailRepositoryImpl.getPhotoDetail(any(PhotoDetailSearchModel.class)))
					.thenThrow(PhotoNotFoundException.class);
			assertThrows(PhotoNotFoundException.class, () -> photoFavoriteServiceImpl.addFavorite(photoFavoriteModel));
			verify(photoFavoriteRepositoryImpl, never()).regist(any(PhotoFavoriteModel.class));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteFavorite {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void deleteFavorite_success() throws GalleryException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			ArgumentCaptor<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteDeleteModel.class);
			doNothing().when(photoFavoriteRepositoryImpl).delete(photoFavoriteDeleteModelCaptor.capture());
			
			photoFavoriteServiceImpl.deleteFavorite(photoFavoriteModel);
			
			PhotoFavoriteDeleteModel photoFavoriteDeleteModel = photoFavoriteDeleteModelCaptor.getValue();
			assertEquals(new AccountNo(1L), photoFavoriteDeleteModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoFavoriteDeleteModel.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavoriteDeleteModel.getFavoritePhotoNo().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void deleteFavorite_UpdateFailureException() throws GalleryException {
			PhotoFavoriteModel photoFavoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			ArgumentCaptor<PhotoFavoriteDeleteModel> photoFavoriteDeleteModelCaptor = ArgumentCaptor.forClass(PhotoFavoriteDeleteModel.class);
			doThrow(UpdateFailureException.class).when(photoFavoriteRepositoryImpl).delete(photoFavoriteDeleteModelCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () ->photoFavoriteServiceImpl.deleteFavorite(photoFavoriteModel));
			
			PhotoFavoriteDeleteModel photoFavoriteDeleteModel = photoFavoriteDeleteModelCaptor.getValue();
			assertEquals(new AccountNo(1L), photoFavoriteDeleteModel.getAccountNo());
			assertEquals(new AccountNo(1L), photoFavoriteDeleteModel.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavoriteDeleteModel.getFavoritePhotoNo().value());
		}
	}
}