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
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoTagMstRepositoryImplTest {
	@InjectMocks
	private PhotoTagMstRepositoryImpl photoTagMstRepositoryImpl;
	
	@Mock
	private PhotoTagMstMapper photoTagMstMapper;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void regist_contain_null_parameter() throws RegistFailureException {
			PhotoTagModel photoTagModel = PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(1).when(photoTagMstMapper).insert(photoTagMstCaptor.capture());
			
			photoTagMstRepositoryImpl.regist(photoTagModel);
			
			verify(photoTagMstMapper).insert(any(PhotoTagMst.class));
			PhotoTagMst photoTagMst = photoTagMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoTagMst.getAccountNo());
			assertEquals(1L, photoTagMst.getPhotoNo().value());
			assertEquals(1L, photoTagMst.getTagNo().value());
			assertEquals(new CreatedBy(1L), photoTagMst.getCreatedBy());
			assertNull(photoTagMst.getCreatedAt());
			assertEquals("太陽", photoTagMst.getTagJapaneseName().value());
			assertEquals("sun", photoTagMst.getTagEnglishName().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			PhotoTagModel photoTagModel = PhotoTagModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.tagNo(new TagNo(1L))
					.tagJapaneseName(new TagJapaneseName("太陽"))
					.tagEnglishName(new TagEnglishName("sun"))
					.build();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doThrow(DuplicateKeyException.class).when(photoTagMstMapper).insert(photoTagMstCaptor.capture());
			
			assertThrows(RegistFailureException.class, () -> photoTagMstRepositoryImpl.regist(photoTagModel));
			
			verify(photoTagMstMapper).insert(any(PhotoTagMst.class));
			PhotoTagMst photoTagMst = photoTagMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoTagMst.getAccountNo());
			assertEquals(1L, photoTagMst.getPhotoNo().value());
			assertEquals(1L, photoTagMst.getTagNo().value());
			assertEquals(new CreatedBy(1L), photoTagMst.getCreatedBy());
			assertNull(photoTagMst.getCreatedAt());
			assertEquals("太陽", photoTagMst.getTagJapaneseName().value());
			assertEquals("sun", photoTagMst.getTagEnglishName().value());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class clear {
		@Test
		@Order(1)
		@DisplayName("正常系：")
		void clear_success() {
			PhotoTagDeleteModel photoTagDeleteModel = PhotoTagDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.build();
			
			ArgumentCaptor<PhotoTagMstCondition> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMstCondition.class);
			doReturn(1).when(photoTagMstMapper).delete(photoTagMstCaptor.capture());
			
			photoTagMstRepositoryImpl.clear(photoTagDeleteModel);
			
			verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
			PhotoTagMstCondition photoTagMst = photoTagMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoTagMst.getAccountNo());
			assertEquals(1L, photoTagMst.getPhotoNo().value());
			assertNull(photoTagMst.getTagNo());
			assertNull(photoTagMst.getTagJapaneseName());
			assertNull(photoTagMst.getTagEnglishName());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteByAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号で写真タグを全件削除する")
		void deleteByAccountNo_success() {
			ArgumentCaptor<PhotoTagMstCondition> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMstCondition.class);
			doReturn(1).when(photoTagMstMapper).delete(photoTagMstCaptor.capture());

			photoTagMstRepositoryImpl.deleteByAccountNo(new AccountNo(1L));

			verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
			PhotoTagMstCondition photoTagMst = photoTagMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoTagMst.getAccountNo());
			assertNull(photoTagMst.getPhotoNo());
			assertNull(photoTagMst.getTagNo());
		}
	}
}