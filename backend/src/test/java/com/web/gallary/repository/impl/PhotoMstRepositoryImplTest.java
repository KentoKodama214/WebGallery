package com.web.gallary.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.web.gallary.entity.PhotoMst;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.mapper.PhotoMstMapper;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailModel;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoMstRepositoryImplTest {
	@InjectMocks
	private PhotoMstRepositoryImpl photoMstRepositoryImpl;
	
	@Mock
	private PhotoMstMapper photoMstMapper;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むPhotoDetailModelの登録")
		void regist_contain_null_parameter() throws RegistFailureException {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());
			
			photoMstRepositoryImpl.regist(photoDetailModel, imageFilePath, 1);
			
			verify(photoMstMapper).insert(any(PhotoMst.class));
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(1, photoMst.getAccountNo());
			assertEquals(1, photoMst.getPhotoNo());
			assertEquals(1, photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertEquals(1, photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertNull(photoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), photoMst.getPhotoAt());
			assertEquals(0, photoMst.getLocationNo());
			assertEquals(imageFilePath, photoMst.getImageFilePath());
			assertEquals("", photoMst.getPhotoJapaneseTitle());
			assertEquals("", photoMst.getPhotoEnglishTitle());
			assertEquals("", photoMst.getCaption());
			assertEquals(DirectionEnum.NONE, photoMst.getDirectionKbn());
			assertEquals(0, photoMst.getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getShutterSpeed()));
			assertEquals(0, photoMst.getIso());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelの登録")
		void regist_not_contain_null_parameter() throws RegistFailureException {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.locationNo(1)
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.photoJapaneseTitle("タイトル1")
					.photoEnglishTitle("title1")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.build();
			
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());
			
			photoMstRepositoryImpl.regist(photoDetailModel, imageFilePath, 1);
			
			verify(photoMstMapper).insert(any(PhotoMst.class));
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(1, photoMst.getAccountNo());
			assertEquals(1, photoMst.getPhotoNo());
			assertEquals(1, photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertEquals(1, photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertNull(photoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), photoMst.getPhotoAt());
			assertEquals(1, photoMst.getLocationNo());
			assertEquals(imageFilePath, photoMst.getImageFilePath());
			assertEquals("タイトル1", photoMst.getPhotoJapaneseTitle());
			assertEquals("title1", photoMst.getPhotoEnglishTitle());
			assertEquals("キャプション1", photoMst.getCaption());
			assertEquals(DirectionEnum.VERTICAL, photoMst.getDirectionKbn());
			assertEquals(24, photoMst.getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(photoMst.getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(photoMst.getShutterSpeed()));
			assertEquals(100, photoMst.getIso());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doThrow(DuplicateKeyException.class).when(photoMstMapper).insert(photoMstCaptor.capture());
			
			assertThrows(RegistFailureException.class, () -> photoMstRepositoryImpl.regist(photoDetailModel, imageFilePath, 1));
			
			verify(photoMstMapper).insert(any(PhotoMst.class));
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(1, photoMst.getAccountNo());
			assertEquals(1, photoMst.getPhotoNo());
			assertEquals(1, photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertEquals(1, photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertNull(photoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), photoMst.getPhotoAt());
			assertEquals(0, photoMst.getLocationNo());
			assertEquals(imageFilePath, photoMst.getImageFilePath());
			assertEquals("", photoMst.getPhotoJapaneseTitle());
			assertEquals("", photoMst.getPhotoEnglishTitle());
			assertEquals("", photoMst.getCaption());
			assertEquals(DirectionEnum.NONE, photoMst.getDirectionKbn());
			assertEquals(0, photoMst.getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getShutterSpeed()));
			assertEquals(0, photoMst.getIso());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class update {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むPhotoDetailModelでの更新")
		void update_contain_null_parameter() throws UpdateFailureException {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(1).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			photoMstRepositoryImpl.update(photoDetailModel);
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(1, cndPhotoMst.getAccountNo());
			assertEquals(1, cndPhotoMst.getPhotoNo());
			assertNull(cndPhotoMst.getCreatedBy());
			assertNull(cndPhotoMst.getCreatedAt());
			assertNull(cndPhotoMst.getUpdatedBy());
			assertNull(cndPhotoMst.getUpdatedAt());
			assertNull(cndPhotoMst.getIsDeleted());
			assertNull(cndPhotoMst.getPhotoAt());
			assertNull(cndPhotoMst.getLocationNo());
			assertNull(cndPhotoMst.getImageFilePath());
			assertNull(cndPhotoMst.getPhotoJapaneseTitle());
			assertNull(cndPhotoMst.getPhotoEnglishTitle());
			assertNull(cndPhotoMst.getCaption());
			assertNull(cndPhotoMst.getDirectionKbn());
			assertNull(cndPhotoMst.getFocalLength());
			assertNull(cndPhotoMst.getFValue());
			assertNull(cndPhotoMst.getShutterSpeed());
			assertNull(cndPhotoMst.getIso());
			
			PhotoMst targetPhotoMst = targetPhotoMstCaptor.getValue();
			assertNull(targetPhotoMst.getAccountNo());
			assertNull(targetPhotoMst.getPhotoNo());
			assertNull(targetPhotoMst.getCreatedBy());
			assertNull(targetPhotoMst.getCreatedAt());
			assertEquals(1, targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertFalse(targetPhotoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetPhotoMst.getPhotoAt());
			assertEquals(0, targetPhotoMst.getLocationNo());
			assertEquals(imageFilePath, targetPhotoMst.getImageFilePath());
			assertEquals("", targetPhotoMst.getPhotoJapaneseTitle());
			assertEquals("", targetPhotoMst.getPhotoEnglishTitle());
			assertEquals("", targetPhotoMst.getCaption());
			assertEquals(DirectionEnum.NONE, targetPhotoMst.getDirectionKbn());
			assertEquals(0, targetPhotoMst.getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getShutterSpeed()));
			assertEquals(0, targetPhotoMst.getIso());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelでの更新")
		void update_not_contain_null_parameter() throws UpdateFailureException {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.locationNo(1)
					.imageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg")
					.photoJapaneseTitle("タイトル1")
					.photoEnglishTitle("title1")
					.caption("キャプション1")
					.directionKbn(DirectionEnum.VERTICAL)
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(1).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			photoMstRepositoryImpl.update(photoDetailModel);
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(1, cndPhotoMst.getAccountNo());
			assertEquals(1, cndPhotoMst.getPhotoNo());
			assertNull(cndPhotoMst.getCreatedBy());
			assertNull(cndPhotoMst.getCreatedAt());
			assertNull(cndPhotoMst.getUpdatedBy());
			assertNull(cndPhotoMst.getUpdatedAt());
			assertNull(cndPhotoMst.getIsDeleted());
			assertNull(cndPhotoMst.getPhotoAt());
			assertNull(cndPhotoMst.getLocationNo());
			assertNull(cndPhotoMst.getImageFilePath());
			assertNull(cndPhotoMst.getPhotoJapaneseTitle());
			assertNull(cndPhotoMst.getPhotoEnglishTitle());
			assertNull(cndPhotoMst.getCaption());
			assertNull(cndPhotoMst.getDirectionKbn());
			assertNull(cndPhotoMst.getFocalLength());
			assertNull(cndPhotoMst.getFValue());
			assertNull(cndPhotoMst.getShutterSpeed());
			assertNull(cndPhotoMst.getIso());
			
			PhotoMst targetPhotoMst = targetPhotoMstCaptor.getValue();
			assertNull(targetPhotoMst.getAccountNo());
			assertNull(targetPhotoMst.getPhotoNo());
			assertNull(targetPhotoMst.getCreatedBy());
			assertNull(targetPhotoMst.getCreatedAt());
			assertEquals(1, targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertFalse(targetPhotoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), targetPhotoMst.getPhotoAt());
			assertEquals(1, targetPhotoMst.getLocationNo());
			assertEquals(imageFilePath, targetPhotoMst.getImageFilePath());
			assertEquals("タイトル1", targetPhotoMst.getPhotoJapaneseTitle());
			assertEquals("title1", targetPhotoMst.getPhotoEnglishTitle());
			assertEquals("キャプション1", targetPhotoMst.getCaption());
			assertEquals(DirectionEnum.VERTICAL, targetPhotoMst.getDirectionKbn());
			assertEquals(24, targetPhotoMst.getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(targetPhotoMst.getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(targetPhotoMst.getShutterSpeed()));
			assertEquals(100, targetPhotoMst.getIso());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(0).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> photoMstRepositoryImpl.update(photoDetailModel));
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(1, cndPhotoMst.getAccountNo());
			assertEquals(1, cndPhotoMst.getPhotoNo());
			assertNull(cndPhotoMst.getCreatedBy());
			assertNull(cndPhotoMst.getCreatedAt());
			assertNull(cndPhotoMst.getUpdatedBy());
			assertNull(cndPhotoMst.getUpdatedAt());
			assertNull(cndPhotoMst.getIsDeleted());
			assertNull(cndPhotoMst.getPhotoAt());
			assertNull(cndPhotoMst.getLocationNo());
			assertNull(cndPhotoMst.getImageFilePath());
			assertNull(cndPhotoMst.getPhotoJapaneseTitle());
			assertNull(cndPhotoMst.getPhotoEnglishTitle());
			assertNull(cndPhotoMst.getCaption());
			assertNull(cndPhotoMst.getDirectionKbn());
			assertNull(cndPhotoMst.getFocalLength());
			assertNull(cndPhotoMst.getFValue());
			assertNull(cndPhotoMst.getShutterSpeed());
			assertNull(cndPhotoMst.getIso());
			
			PhotoMst targetPhotoMst = targetPhotoMstCaptor.getValue();
			assertNull(targetPhotoMst.getAccountNo());
			assertNull(targetPhotoMst.getPhotoNo());
			assertNull(targetPhotoMst.getCreatedBy());
			assertNull(targetPhotoMst.getCreatedAt());
			assertEquals(1, targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertFalse(targetPhotoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetPhotoMst.getPhotoAt());
			assertEquals(0, targetPhotoMst.getLocationNo());
			assertEquals(imageFilePath, targetPhotoMst.getImageFilePath());
			assertEquals("", targetPhotoMst.getPhotoJapaneseTitle());
			assertEquals("", targetPhotoMst.getPhotoEnglishTitle());
			assertEquals("", targetPhotoMst.getCaption());
			assertEquals(DirectionEnum.NONE, targetPhotoMst.getDirectionKbn());
			assertEquals(0, targetPhotoMst.getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getShutterSpeed()));
			assertEquals(0, targetPhotoMst.getIso());
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void delete_success() throws UpdateFailureException {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDeleteModel photoDeleteModel = PhotoDeleteModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(1).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			photoMstRepositoryImpl.delete(photoDeleteModel);
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(1, cndPhotoMst.getAccountNo());
			assertEquals(1, cndPhotoMst.getPhotoNo());
			assertNull(cndPhotoMst.getCreatedBy());
			assertNull(cndPhotoMst.getCreatedAt());
			assertNull(cndPhotoMst.getUpdatedBy());
			assertNull(cndPhotoMst.getUpdatedAt());
			assertNull(cndPhotoMst.getIsDeleted());
			assertNull(cndPhotoMst.getPhotoAt());
			assertNull(cndPhotoMst.getLocationNo());
			assertNull(cndPhotoMst.getImageFilePath());
			assertNull(cndPhotoMst.getPhotoJapaneseTitle());
			assertNull(cndPhotoMst.getPhotoEnglishTitle());
			assertNull(cndPhotoMst.getCaption());
			assertNull(cndPhotoMst.getDirectionKbn());
			assertNull(cndPhotoMst.getFocalLength());
			assertNull(cndPhotoMst.getFValue());
			assertNull(cndPhotoMst.getShutterSpeed());
			assertNull(cndPhotoMst.getIso());
			
			PhotoMst targetPhotoMst = targetPhotoMstCaptor.getValue();
			assertNull(targetPhotoMst.getAccountNo());
			assertNull(targetPhotoMst.getPhotoNo());
			assertNull(targetPhotoMst.getCreatedBy());
			assertNull(targetPhotoMst.getCreatedAt());
			assertEquals(1, targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertTrue(targetPhotoMst.getIsDeleted());
			assertNull(targetPhotoMst.getPhotoAt());
			assertNull(targetPhotoMst.getLocationNo());
			assertNull(targetPhotoMst.getImageFilePath());
			assertNull(targetPhotoMst.getPhotoJapaneseTitle());
			assertNull(targetPhotoMst.getPhotoEnglishTitle());
			assertNull(targetPhotoMst.getCaption());
			assertNull(targetPhotoMst.getDirectionKbn());
			assertNull(targetPhotoMst.getFocalLength());
			assertNull(targetPhotoMst.getFValue());
			assertNull(targetPhotoMst.getShutterSpeed());
			assertNull(targetPhotoMst.getIso());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void delete_UpdateFailureException() {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDeleteModel photoDeleteModel = PhotoDeleteModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(0).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> photoMstRepositoryImpl.delete(photoDeleteModel));
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(1, cndPhotoMst.getAccountNo());
			assertEquals(1, cndPhotoMst.getPhotoNo());
			assertNull(cndPhotoMst.getCreatedBy());
			assertNull(cndPhotoMst.getCreatedAt());
			assertNull(cndPhotoMst.getUpdatedBy());
			assertNull(cndPhotoMst.getUpdatedAt());
			assertNull(cndPhotoMst.getIsDeleted());
			assertNull(cndPhotoMst.getPhotoAt());
			assertNull(cndPhotoMst.getLocationNo());
			assertNull(cndPhotoMst.getImageFilePath());
			assertNull(cndPhotoMst.getPhotoJapaneseTitle());
			assertNull(cndPhotoMst.getPhotoEnglishTitle());
			assertNull(cndPhotoMst.getCaption());
			assertNull(cndPhotoMst.getDirectionKbn());
			assertNull(cndPhotoMst.getFocalLength());
			assertNull(cndPhotoMst.getFValue());
			assertNull(cndPhotoMst.getShutterSpeed());
			assertNull(cndPhotoMst.getIso());
			
			PhotoMst targetPhotoMst = targetPhotoMstCaptor.getValue();
			assertNull(targetPhotoMst.getAccountNo());
			assertNull(targetPhotoMst.getPhotoNo());
			assertNull(targetPhotoMst.getCreatedBy());
			assertNull(targetPhotoMst.getCreatedAt());
			assertEquals(1, targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertTrue(targetPhotoMst.getIsDeleted());
			assertNull(targetPhotoMst.getPhotoAt());
			assertNull(targetPhotoMst.getLocationNo());
			assertNull(targetPhotoMst.getImageFilePath());
			assertNull(targetPhotoMst.getPhotoJapaneseTitle());
			assertNull(targetPhotoMst.getPhotoEnglishTitle());
			assertNull(targetPhotoMst.getCaption());
			assertNull(targetPhotoMst.getDirectionKbn());
			assertNull(targetPhotoMst.getFocalLength());
			assertNull(targetPhotoMst.getFValue());
			assertNull(targetPhotoMst.getShutterSpeed());
			assertNull(targetPhotoMst.getIso());
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getNewPhotoNo {
		@Test
		@Order(1)
		@DisplayName("正常系：getMaxPhotoNoがある場合")
		void getNewPhotoNo_getMaxPhotoNo_found() {
			doReturn(1).when(photoMstMapper).getMaxPhotoNo(1);
			assertEquals(2, photoMstRepositoryImpl.getNewPhotoNo(1));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：getMaxPhotoNoがない場合")
		void getNewPhotoNo_getMaxPhotoNo_not_found() {
			doReturn(null).when(photoMstMapper).getMaxPhotoNo(1);
			assertEquals(1, photoMstRepositoryImpl.getNewPhotoNo(1));
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class isExistPhoto {
		@Test
		@Order(1)
		@DisplayName("正常系：画像ファイルパスに該当する写真がない場合")
		void isExistPhoto_not_found() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
					
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(false).when(photoMstMapper).isExistPhoto(photoMstCaptor.capture());
			assertFalse(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
			
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(1, photoMst.getAccountNo());
			assertEquals("DSC111.jpg", photoMst.getImageFilePath());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：画像ファイルパスに該当する写真がある場合")
		void isExistPhoto_found() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC111.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
					
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(true).when(photoMstMapper).isExistPhoto(photoMstCaptor.capture());
			assertTrue(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
			
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(1, photoMst.getAccountNo());
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class count {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void count_success() {
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(3).when(photoMstMapper).count(photoMstCaptor.capture());
			
			assertEquals(3, photoMstRepositoryImpl.count(1));
			
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(1, photoMst.getAccountNo());
			assertNull(photoMst.getPhotoNo());
			assertNull(photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertNull(photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertFalse(photoMst.getIsDeleted());
			assertNull(photoMst.getPhotoAt());
			assertNull(photoMst.getLocationNo());
			assertNull(photoMst.getImageFilePath());
			assertNull(photoMst.getPhotoJapaneseTitle());
			assertNull(photoMst.getPhotoEnglishTitle());
			assertNull(photoMst.getCaption());
			assertNull(photoMst.getDirectionKbn());
			assertNull(photoMst.getFocalLength());
			assertNull(photoMst.getFValue());
			assertNull(photoMst.getShutterSpeed());
			assertNull(photoMst.getIso());
		}
	}
}