package com.web.gallery.repository.impl;

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

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDetailModel;

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
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();
			
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());
			
			photoMstRepositoryImpl.regist(photoDetailModel, new ImageFilePath(imageFilePath), new PhotoNo(1L));
			
			verify(photoMstMapper).insert(any(PhotoMst.class));
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoMst.getAccountNo());
			assertEquals(1L, photoMst.getPhotoNo().value());
			assertEquals(new CreatedBy(1L), photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertEquals(new UpdatedBy(1L), photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertNull(photoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), photoMst.getPhotoAt().value());
			assertEquals(0L, photoMst.getLocationNo().value());
			assertEquals(imageFilePath, photoMst.getImageFilePath().value());
			assertEquals("", photoMst.getPhotoJapaneseTitle().value());
			assertEquals("", photoMst.getPhotoEnglishTitle().value());
			assertEquals("", photoMst.getCaption().value());
			assertEquals(DirectionEnum.NONE, photoMst.getDirectionKbn());
			assertEquals(0, photoMst.getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getShutterSpeed().value()));
			assertEquals(0, photoMst.getIso().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelの登録")
		void regist_not_contain_null_parameter() throws RegistFailureException {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.locationNo(new LocationNo(1L))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル1"))
					.photoEnglishTitle(new PhotoEnglishTitle("title1"))
					.caption(new Caption("キャプション1"))
					.directionKbn(DirectionEnum.VERTICAL)
					.focalLength(new FocalLength(24))
					.fValue(new FValue(BigDecimal.valueOf(2.8)))
					.shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(0.01)))
					.iso(new Iso(100))
					.build();
			
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());
			
			photoMstRepositoryImpl.regist(photoDetailModel, new ImageFilePath(imageFilePath), new PhotoNo(1L));
			
			verify(photoMstMapper).insert(any(PhotoMst.class));
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoMst.getAccountNo());
			assertEquals(1L, photoMst.getPhotoNo().value());
			assertEquals(new CreatedBy(1L), photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertEquals(new UpdatedBy(1L), photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertNull(photoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), photoMst.getPhotoAt().value());
			assertEquals(1L, photoMst.getLocationNo().value());
			assertEquals(imageFilePath, photoMst.getImageFilePath().value());
			assertEquals("タイトル1", photoMst.getPhotoJapaneseTitle().value());
			assertEquals("title1", photoMst.getPhotoEnglishTitle().value());
			assertEquals("キャプション1", photoMst.getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, photoMst.getDirectionKbn());
			assertEquals(24, photoMst.getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(photoMst.getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(photoMst.getShutterSpeed().value()));
			assertEquals(100, photoMst.getIso().value());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();
			
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doThrow(DuplicateKeyException.class).when(photoMstMapper).insert(photoMstCaptor.capture());
			
			assertThrows(RegistFailureException.class, () -> photoMstRepositoryImpl.regist(photoDetailModel, new ImageFilePath(imageFilePath), new PhotoNo(1L)));
			
			verify(photoMstMapper).insert(any(PhotoMst.class));
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoMst.getAccountNo());
			assertEquals(1L, photoMst.getPhotoNo().value());
			assertEquals(new CreatedBy(1L), photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertEquals(new UpdatedBy(1L), photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertNull(photoMst.getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), photoMst.getPhotoAt().value());
			assertEquals(0L, photoMst.getLocationNo().value());
			assertEquals(imageFilePath, photoMst.getImageFilePath().value());
			assertEquals("", photoMst.getPhotoJapaneseTitle().value());
			assertEquals("", photoMst.getPhotoEnglishTitle().value());
			assertEquals("", photoMst.getCaption().value());
			assertEquals(DirectionEnum.NONE, photoMst.getDirectionKbn());
			assertEquals(0, photoMst.getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(photoMst.getShutterSpeed().value()));
			assertEquals(0, photoMst.getIso().value());
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
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(1).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			photoMstRepositoryImpl.update(photoDetailModel);
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), cndPhotoMst.getAccountNo());
			assertEquals(1L, cndPhotoMst.getPhotoNo().value());
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
			assertEquals(new UpdatedBy(1L), targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertFalse(targetPhotoMst.getIsDeleted().value());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetPhotoMst.getPhotoAt().value());
			assertEquals(0L, targetPhotoMst.getLocationNo().value());
			assertEquals(imageFilePath, targetPhotoMst.getImageFilePath().value());
			assertEquals("", targetPhotoMst.getPhotoJapaneseTitle().value());
			assertEquals("", targetPhotoMst.getPhotoEnglishTitle().value());
			assertEquals("", targetPhotoMst.getCaption().value());
			assertEquals(DirectionEnum.NONE, targetPhotoMst.getDirectionKbn());
			assertEquals(0, targetPhotoMst.getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getShutterSpeed().value()));
			assertEquals(0, targetPhotoMst.getIso().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelでの更新")
		void update_not_contain_null_parameter() throws UpdateFailureException {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.locationNo(new LocationNo(1L))
					.imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル1"))
					.photoEnglishTitle(new PhotoEnglishTitle("title1"))
					.caption(new Caption("キャプション1"))
					.directionKbn(DirectionEnum.VERTICAL)
					.focalLength(new FocalLength(24))
					.fValue(new FValue(BigDecimal.valueOf(2.8)))
					.shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(0.01)))
					.iso(new Iso(100))
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(1).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			photoMstRepositoryImpl.update(photoDetailModel);
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), cndPhotoMst.getAccountNo());
			assertEquals(1L, cndPhotoMst.getPhotoNo().value());
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
			assertEquals(new UpdatedBy(1L), targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertFalse(targetPhotoMst.getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), targetPhotoMst.getPhotoAt().value());
			assertEquals(1L, targetPhotoMst.getLocationNo().value());
			assertEquals(imageFilePath, targetPhotoMst.getImageFilePath().value());
			assertEquals("タイトル1", targetPhotoMst.getPhotoJapaneseTitle().value());
			assertEquals("title1", targetPhotoMst.getPhotoEnglishTitle().value());
			assertEquals("キャプション1", targetPhotoMst.getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, targetPhotoMst.getDirectionKbn());
			assertEquals(24, targetPhotoMst.getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(targetPhotoMst.getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(targetPhotoMst.getShutterSpeed().value()));
			assertEquals(100, targetPhotoMst.getIso().value());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() {
			String imageFilePath = "https://localhost:8080/image/aaaaaaaa/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(0).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> photoMstRepositoryImpl.update(photoDetailModel));
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), cndPhotoMst.getAccountNo());
			assertEquals(1L, cndPhotoMst.getPhotoNo().value());
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
			assertEquals(new UpdatedBy(1L), targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertFalse(targetPhotoMst.getIsDeleted().value());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)), targetPhotoMst.getPhotoAt().value());
			assertEquals(0L, targetPhotoMst.getLocationNo().value());
			assertEquals(imageFilePath, targetPhotoMst.getImageFilePath().value());
			assertEquals("", targetPhotoMst.getPhotoJapaneseTitle().value());
			assertEquals("", targetPhotoMst.getPhotoEnglishTitle().value());
			assertEquals("", targetPhotoMst.getCaption().value());
			assertEquals(DirectionEnum.NONE, targetPhotoMst.getDirectionKbn());
			assertEquals(0, targetPhotoMst.getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(targetPhotoMst.getShutterSpeed().value()));
			assertEquals(0, targetPhotoMst.getIso().value());
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
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(1).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			photoMstRepositoryImpl.delete(photoDeleteModel);
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), cndPhotoMst.getAccountNo());
			assertEquals(1L, cndPhotoMst.getPhotoNo().value());
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
			assertEquals(new UpdatedBy(1L), targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertTrue(targetPhotoMst.getIsDeleted().value());
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
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();
			
			ArgumentCaptor<PhotoMst> cndPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			ArgumentCaptor<PhotoMst> targetPhotoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			
			doReturn(0).when(photoMstMapper).update(cndPhotoMstCaptor.capture(), targetPhotoMstCaptor.capture());
			
			assertThrows(UpdateFailureException.class, () -> photoMstRepositoryImpl.delete(photoDeleteModel));
			
			verify(photoMstMapper).update(any(PhotoMst.class), any(PhotoMst.class));
			PhotoMst cndPhotoMst = cndPhotoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), cndPhotoMst.getAccountNo());
			assertEquals(1L, cndPhotoMst.getPhotoNo().value());
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
			assertEquals(new UpdatedBy(1L), targetPhotoMst.getUpdatedBy());
			assertNull(targetPhotoMst.getUpdatedAt());
			assertTrue(targetPhotoMst.getIsDeleted().value());
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
			doReturn(1L).when(photoMstMapper).getMaxPhotoNo(1L);
			assertEquals(new PhotoNo(2L), photoMstRepositoryImpl.getNewPhotoNo(new AccountNo(1L)));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：getMaxPhotoNoがない場合")
		void getNewPhotoNo_getMaxPhotoNo_not_found() {
			doReturn(null).when(photoMstMapper).getMaxPhotoNo(1L);
			assertEquals(new PhotoNo(1L), photoMstRepositoryImpl.getNewPhotoNo(new AccountNo(1L)));
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
					.accountNo(new AccountNo(1L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.build();
					
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(false).when(photoMstMapper).isExistPhoto(photoMstCaptor.capture());
			assertFalse(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
			
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoMst.getAccountNo());
			assertEquals("DSC111.jpg", photoMst.getImageFilePath().value());
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
					.accountNo(new AccountNo(1L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
					.build();
					
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(true).when(photoMstMapper).isExistPhoto(photoMstCaptor.capture());
			assertTrue(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
			
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoMst.getAccountNo());
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
			
			assertEquals(3, photoMstRepositoryImpl.count(new AccountNo(1L)));
			
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoMst.getAccountNo());
			assertNull(photoMst.getPhotoNo());
			assertNull(photoMst.getCreatedBy());
			assertNull(photoMst.getCreatedAt());
			assertNull(photoMst.getUpdatedBy());
			assertNull(photoMst.getUpdatedAt());
			assertFalse(photoMst.getIsDeleted().value());
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

	@Nested
	@Order(7)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteByAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号で写真マスタを物理削除する")
		void deleteByAccountNo_success() {
			ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
			doReturn(1).when(photoMstMapper).delete(photoMstCaptor.capture());

			photoMstRepositoryImpl.deleteByAccountNo(new AccountNo(1L));

			verify(photoMstMapper).delete(any(PhotoMst.class));
			PhotoMst photoMst = photoMstCaptor.getValue();
			assertEquals(new AccountNo(1L), photoMst.getAccountNo());
			assertNull(photoMst.getPhotoNo());
		}
	}
}