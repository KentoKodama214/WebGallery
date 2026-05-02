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

import com.web.gallary.entity.PhotoTagMst;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.mapper.PhotoTagMstMapper;
import com.web.gallary.model.PhotoTagDeleteModel;
import com.web.gallary.model.PhotoTagModel;

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
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(1).when(photoTagMstMapper).insert(photoTagMstCaptor.capture());
			
			photoTagMstRepositoryImpl.regist(photoTagModel);
			
			verify(photoTagMstMapper).insert(any(PhotoTagMst.class));
			PhotoTagMst photoTagMst = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMst.getAccountNo());
			assertEquals(1, photoTagMst.getPhotoNo());
			assertEquals(1, photoTagMst.getTagNo());
			assertEquals(1, photoTagMst.getCreatedBy());
			assertNull(photoTagMst.getCreatedAt());
			assertEquals("太陽", photoTagMst.getTagJapaneseName());
			assertEquals("sun", photoTagMst.getTagEnglishName());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			PhotoTagModel photoTagModel = PhotoTagModel.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doThrow(DuplicateKeyException.class).when(photoTagMstMapper).insert(photoTagMstCaptor.capture());
			
			assertThrows(RegistFailureException.class, () -> photoTagMstRepositoryImpl.regist(photoTagModel));
			
			verify(photoTagMstMapper).insert(any(PhotoTagMst.class));
			PhotoTagMst photoTagMst = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMst.getAccountNo());
			assertEquals(1, photoTagMst.getPhotoNo());
			assertEquals(1, photoTagMst.getTagNo());
			assertEquals(1, photoTagMst.getCreatedBy());
			assertNull(photoTagMst.getCreatedAt());
			assertEquals("太陽", photoTagMst.getTagJapaneseName());
			assertEquals("sun", photoTagMst.getTagEnglishName());
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
					.accountNo(1)
					.photoNo(1)
					.build();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(1).when(photoTagMstMapper).delete(photoTagMstCaptor.capture());
			
			photoTagMstRepositoryImpl.clear(photoTagDeleteModel);
			
			verify(photoTagMstMapper).delete(any(PhotoTagMst.class));
			PhotoTagMst photoTagMst = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMst.getAccountNo());
			assertEquals(1, photoTagMst.getPhotoNo());
			assertNull(photoTagMst.getTagNo());
			assertNull(photoTagMst.getCreatedBy());
			assertNull(photoTagMst.getCreatedAt());
			assertNull(photoTagMst.getTagJapaneseName());
			assertNull(photoTagMst.getTagEnglishName());
		}
	}
}