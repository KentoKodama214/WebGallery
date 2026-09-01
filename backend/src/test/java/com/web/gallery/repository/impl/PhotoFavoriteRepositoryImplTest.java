package com.web.gallery.repository.impl;

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

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.entity.PhotoFavorite;
import com.web.gallery.entity.PhotoFavoriteCondition;
import com.web.gallery.exception.FavoriteNotFoundException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;

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
		void regist_contain_null_parameter() throws GalleryException {
			PhotoFavoriteModel favoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			
			ArgumentCaptor<PhotoFavorite> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavorite.class);
			doReturn(1).when(photoFavoriteMapper).insert(photoFavoriteCaptor.capture());
			
			photoFavoriteRepositoryImpl.regist(favoriteModel);
			
			verify(photoFavoriteMapper).insert(any(PhotoFavorite.class));
			PhotoFavorite photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1L, photoFavorite.getAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoNo());
			assertEquals(1L, photoFavorite.getCreatedBy());
			assertNull(photoFavorite.getCreatedAt());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			PhotoFavoriteModel favoriteModel = PhotoFavoriteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			
			ArgumentCaptor<PhotoFavorite> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavorite.class);
			doThrow(DuplicateKeyException.class).when(photoFavoriteMapper).insert(photoFavoriteCaptor.capture());
			
			assertThrows(RegistFailureException.class, () -> photoFavoriteRepositoryImpl.regist(favoriteModel));
			
			verify(photoFavoriteMapper).insert(any(PhotoFavorite.class));
			PhotoFavorite photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1L, photoFavorite.getAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoNo());
			assertEquals(1L, photoFavorite.getCreatedBy());
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
		void delete_contain_null_parameter() throws GalleryException {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			
			ArgumentCaptor<PhotoFavoriteCondition> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
			doReturn(1).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());
			
			photoFavoriteRepositoryImpl.delete(favoriteDeleteModel);
			
			verify(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
			PhotoFavoriteCondition photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1L, photoFavorite.getAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoNo());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：対象のお気に入りが存在しない場合、FavoriteNotFoundExceptionをthrowする")
		void delete_FavoriteNotFoundException() throws GalleryException {
			PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();
			
			ArgumentCaptor<PhotoFavoriteCondition> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
			doReturn(0).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());
			
			assertThrows(FavoriteNotFoundException.class, () -> photoFavoriteRepositoryImpl.delete(favoriteDeleteModel));
			
			verify(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
			PhotoFavoriteCondition photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1L, photoFavorite.getAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoNo());
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
					.favoritePhotoAccountNo(new AccountNo(1L))
					.favoritePhotoNo(new PhotoNo(1L))
					.build();

			ArgumentCaptor<PhotoFavoriteCondition> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
			doReturn(1).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());

			photoFavoriteRepositoryImpl.clear(favoriteDeleteModel);
			
			verify(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
			PhotoFavoriteCondition photoFavorite = photoFavoriteCaptor.getValue();
			assertNull(photoFavorite.getAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoNo());
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteByAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号で自分が登録したお気に入りを全件削除する")
		void deleteByAccountNo_success() {
			ArgumentCaptor<PhotoFavoriteCondition> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
			doReturn(1).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());

			photoFavoriteRepositoryImpl.deleteByAccountNo(new AccountNo(1L));

			verify(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
			PhotoFavoriteCondition photoFavorite = photoFavoriteCaptor.getValue();
			assertEquals(1L, photoFavorite.getAccountNo());
			assertNull(photoFavorite.getFavoritePhotoAccountNo());
			assertNull(photoFavorite.getFavoritePhotoNo());
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteByFavoritePhotoAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号で自分の写真に対する他人のお気に入りを全件削除する")
		void deleteByFavoritePhotoAccountNo_success() {
			ArgumentCaptor<PhotoFavoriteCondition> photoFavoriteCaptor = ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
			doReturn(1).when(photoFavoriteMapper).delete(photoFavoriteCaptor.capture());

			photoFavoriteRepositoryImpl.deleteByFavoritePhotoAccountNo(new AccountNo(1L));

			verify(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
			PhotoFavoriteCondition photoFavorite = photoFavoriteCaptor.getValue();
			assertNull(photoFavorite.getAccountNo());
			assertEquals(1L, photoFavorite.getFavoritePhotoAccountNo());
			assertNull(photoFavorite.getFavoritePhotoNo());
		}
	}
}