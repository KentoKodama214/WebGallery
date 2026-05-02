package com.web.gallary.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallary.entity.PhotoFavorite;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.mapper.PhotoFavoriteMapper;
import com.web.gallary.model.PhotoFavoriteDeleteModel;
import com.web.gallary.model.PhotoFavoriteModel;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoFavoriteRepositoryImplTest {
	@InjectMocks
	private PhotoFavoriteRepositoryImpl photoFavoriteRepositoryImpl;
	
	@Mock
	private PhotoFavoriteMapper photoFavoriteMapper;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void regist_contain_null_parameter() throws RegistFailureException {
			PhotoFavoriteModel favoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			ArgumentCaptor<PhotoFavorite> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavorite.class);
			doReturn(1).when(photoFavoriteMapper).insert(photoFavoriteCaptor.capture());
			
			photoFavoriteRepositoryImpl.regist(favoriteModel);
			
			verify(photoFavoriteMapper).insert(any(PhotoFavorite.class));
			PhotoFavorite photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1, photoFavorite.getAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoNo());
			assertEquals(1, photoFavorite.getCreatedBy());
			assertNull(photoFavorite.getCreatedAt());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			PhotoFavoriteModel favoriteModel = PhotoFavoriteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			ArgumentCaptor<PhotoFavorite> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavorite.class);
			doThrow(DuplicateKeyException.class).when(photoFavoriteMapper).insert(photoFavoriteCaptor.capture());
			
			assertThrows(RegistFailureException.class, () -> photoFavoriteRepositoryImpl.regist(favoriteModel));
			
			verify(photoFavoriteMapper).insert(any(PhotoFavorite.class));
			PhotoFavorite photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1, photoFavorite.getAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoNo());
			assertEquals(1, photoFavorite.getCreatedBy());
			assertNull(photoFavorite.getCreatedAt());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void delete_contain_null_parameter() throws UpdateFailureException {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			ArgumentCaptor<PhotoFavorite> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavorite.class);
			doReturn(1).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());
			
			photoFavoriteRepositoryImpl.delete(favoriteDeleteModel);
			
			verify(photoFavoriteMapper).delete(any(PhotoFavorite.class));
			PhotoFavorite photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1, photoFavorite.getAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoNo());
			assertNull(photoFavorite.getCreatedBy());
			assertNull(photoFavorite.getCreatedAt());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void delete_UpdateFailureException() throws UpdateFailureException {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.accountNo(1)
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			ArgumentCaptor<PhotoFavorite> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavorite.class);
			doReturn(0).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> photoFavoriteRepositoryImpl.delete(favoriteDeleteModel));
			
			verify(photoFavoriteMapper).delete(any(PhotoFavorite.class));
			PhotoFavorite photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1, photoFavorite.getAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoNo());
			assertNull(photoFavorite.getCreatedBy());
			assertNull(photoFavorite.getCreatedAt());
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class clear {
		@Test
		@Order(1)
		@DisplayName("正常系：")
		void clear_success() {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.favoritePhotoAccountNo(1)
					.favoritePhotoNo(1)
					.build();
			
			ArgumentCaptor<PhotoFavorite> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavorite.class);
			doReturn(1).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());
			
			photoFavoriteRepositoryImpl.clear(favoriteDeleteModel);
			
			verify(photoFavoriteMapper).delete(any(PhotoFavorite.class));
			PhotoFavorite photoFavorite = photoFavoriteCaptor.getValue();
			assertNull(photoFavorite.getAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1, photoFavorite.getFavoritePhotoNo());
			assertNull(photoFavorite.getCreatedBy());
			assertNull(photoFavorite.getCreatedAt());
		}
	}
}