package com.web.gallary.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

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

import com.web.gallary.dto.PhotoDetailDto;
import com.web.gallary.dto.PhotoDetailGetDto;
import com.web.gallary.dto.PhotoDto;
import com.web.gallary.dto.PhotoListGetDto;
import com.web.gallary.entity.PhotoTagMst;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.mapper.PhotoDetailMapper;
import com.web.gallary.mapper.PhotoTagMstMapper;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoGetModel;
import com.web.gallary.model.PhotoModel;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoDetailRepositoryImplTest {
	@InjectMocks
	private PhotoDetailRepositoryImpl photoDetailRepositoryImpl;
	
	@Mock
	private PhotoTagMstMapper photoTagMstMapper;
	
	@Mock
	private PhotoDetailMapper photoDetailMapper;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoList {
		@Test
		@Order(1)
		@DisplayName("正常系：写真が0件の場合")
		void getPhotoList_photo_not_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.build();
			
			List<PhotoDto> photoDtoList = new ArrayList<PhotoDto>();
			
			ArgumentCaptor<PhotoListGetDto> photoListGetDtoCaptor = ArgumentCaptor.forClass(PhotoListGetDto.class);
			doReturn(photoDtoList).when(photoDetailMapper).getPhotoList(photoListGetDtoCaptor.capture());
			
			List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());
			
			List<PhotoModel> expected = new ArrayList<PhotoModel>();
			
			List<PhotoModel> actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
			
			assertEquals(expected, actual);
			
			PhotoListGetDto photoListGetDtoCapture = photoListGetDtoCaptor.getValue();
			assertEquals(1, photoListGetDtoCapture.getAccountNo());
			assertEquals(1, photoListGetDtoCapture.getPhotoAccountNo());
			
			PhotoTagMst photoTagMstCapture = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMstCapture.getAccountNo());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真が1件以上、写真タグが0件の場合")
		void getPhotoList_photoTag_not_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.build();
			
			List<PhotoDto> photoDtoList = new ArrayList<PhotoDto>();
			PhotoDto photoDto1 = new PhotoDto();
			photoDto1.setAccountNo(1);
			photoDto1.setPhotoNo(1);
			photoDto1.setFavoriteCount(1);
			photoDto1.setIsFavorite(false);
			photoDto1.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
			photoDto1.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
			photoDto1.setCaption("キャプション1");
			photoDto1.setDirectionKbn(DirectionEnum.VERTICAL);
			photoDtoList.add(photoDto1);
			
			PhotoDto photoDto2 = new PhotoDto();
			photoDto2.setAccountNo(1);
			photoDto2.setPhotoNo(2);
			photoDto2.setFavoriteCount(2);
			photoDto2.setIsFavorite(true);
			photoDto2.setPhotoAt(OffsetDateTime.of(2000, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
			photoDto2.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg");
			photoDto2.setCaption("キャプション2");
			photoDto2.setDirectionKbn(DirectionEnum.HORIZONTAL);
			photoDtoList.add(photoDto2);
			
			ArgumentCaptor<PhotoListGetDto> photoListGetDtoCaptor = ArgumentCaptor.forClass(PhotoListGetDto.class);
			doReturn(photoDtoList).when(photoDetailMapper).getPhotoList(photoListGetDtoCaptor.capture());
			
			List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());
			
			List<PhotoModel> actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
			
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(1, actual.get(0).getPhotoNo());
			assertEquals(1, actual.get(0).getFavoriteCount());
			assertFalse(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション1", actual.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.get(0).getDirectionKbn());
			
			assertEquals(1, actual.get(1).getAccountNo());
			assertEquals(2, actual.get(1).getPhotoNo());
			assertEquals(2, actual.get(1).getFavoriteCount());
			assertTrue(actual.get(1).getIsFavorite());
			assertEquals(OffsetDateTime.of(2000, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(1).getPhotoAt());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC222.jpg", actual.get(1).getImageFilePath());
			assertEquals("キャプション2", actual.get(1).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(1).getDirectionKbn());
			
			PhotoListGetDto photoListGetDtoCapture = photoListGetDtoCaptor.getValue();
			assertEquals(1, photoListGetDtoCapture.getAccountNo());
			assertEquals(1, photoListGetDtoCapture.getPhotoAccountNo());
			
			PhotoTagMst photoTagMstCapture = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMstCapture.getAccountNo());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：写真が1件以上、写真タグが1件以上の場合")
		void getPhotoList_photoTag_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.build();
			
			List<PhotoDto> photoDtoList = new ArrayList<PhotoDto>();
			PhotoDto photoDto1 = new PhotoDto();
			photoDto1.setAccountNo(1);
			photoDto1.setPhotoNo(1);
			photoDto1.setFavoriteCount(1);
			photoDto1.setIsFavorite(false);
			photoDto1.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
			photoDto1.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
			photoDto1.setCaption("キャプション1");
			photoDto1.setDirectionKbn(DirectionEnum.VERTICAL);
			photoDtoList.add(photoDto1);
			
			PhotoDto photoDto2 = new PhotoDto();
			photoDto2.setAccountNo(1);
			photoDto2.setPhotoNo(2);
			photoDto2.setFavoriteCount(2);
			photoDto2.setIsFavorite(true);
			photoDto2.setPhotoAt(OffsetDateTime.of(2000, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
			photoDto2.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg");
			photoDto2.setCaption("キャプション2");
			photoDto2.setDirectionKbn(DirectionEnum.HORIZONTAL);
			photoDtoList.add(photoDto2);
			
			ArgumentCaptor<PhotoListGetDto> photoListGetDtoCaptor = ArgumentCaptor.forClass(PhotoListGetDto.class);
			doReturn(photoDtoList).when(photoDetailMapper).getPhotoList(photoListGetDtoCaptor.capture());
			
			List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();
			photoTagMstList.add(PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagMstList.add(PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(1)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());
			
			List<PhotoModel> actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
			
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(1, actual.get(0).getPhotoNo());
			assertEquals(1, actual.get(0).getFavoriteCount());
			assertFalse(actual.get(0).getIsFavorite());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(0).getPhotoAt());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.get(0).getImageFilePath());
			assertEquals("キャプション1", actual.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.get(0).getDirectionKbn());
			assertEquals(1, actual.get(0).getPhotoTagModelList().size());
			assertEquals(1, actual.get(0).getPhotoTagModelList().get(0).getTagNo());
			assertEquals("太陽", actual.get(0).getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sun", actual.get(0).getPhotoTagModelList().get(0).getTagEnglishName());
			
			assertEquals(1, actual.get(1).getAccountNo());
			assertEquals(2, actual.get(1).getPhotoNo());
			assertEquals(2, actual.get(1).getFavoriteCount());
			assertTrue(actual.get(1).getIsFavorite());
			assertEquals(OffsetDateTime.of(2000, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.get(1).getPhotoAt());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC222.jpg", actual.get(1).getImageFilePath());
			assertEquals("キャプション2", actual.get(1).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.get(1).getDirectionKbn());
			assertEquals(1, actual.get(1).getPhotoTagModelList().size());
			assertEquals(1, actual.get(1).getPhotoTagModelList().get(0).getTagNo());
			assertEquals("海", actual.get(1).getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sea", actual.get(1).getPhotoTagModelList().get(0).getTagEnglishName());
			
			PhotoListGetDto photoListGetDtoCapture = photoListGetDtoCaptor.getValue();
			assertEquals(1, photoListGetDtoCapture.getAccountNo());
			assertEquals(1, photoListGetDtoCapture.getPhotoAccountNo());
			
			PhotoTagMst photoTagMstCapture = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMstCapture.getAccountNo());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class getPhotoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系：写真のメタデータがデフォルト値、写真タグが0件の場合")
		void getPhotoDetail_photoTag_default_value_not_found() throws PhotoNotFoundException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.photoNo(1)
					.build();
			
			PhotoDetailDto photoDetailDto = new PhotoDetailDto();
			photoDetailDto.setAccountNo(1);
			photoDetailDto.setPhotoNo(1);
			photoDetailDto.setIsFavorite(false);
			photoDetailDto.setPhotoAt(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)));
			photoDetailDto.setLocationNo(null);
			photoDetailDto.setAddress(null);
			photoDetailDto.setLatitude(null);
			photoDetailDto.setLongitude(null);
			photoDetailDto.setLocationName(null);
			photoDetailDto.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
			photoDetailDto.setPhotoJapaneseTitle("");
			photoDetailDto.setPhotoEnglishTitle("");
			photoDetailDto.setCaption("");
			photoDetailDto.setDirectionKbn(DirectionEnum.VERTICAL);
			photoDetailDto.setFocalLength(0);
			photoDetailDto.setFValue(BigDecimal.ZERO);
			photoDetailDto.setShutterSpeed(BigDecimal.ZERO);
			photoDetailDto.setIso(0);
			
			ArgumentCaptor<PhotoDetailGetDto> photoDetailGetDtoCaptor = ArgumentCaptor.forClass(PhotoDetailGetDto.class);
			doReturn(photoDetailDto).when(photoDetailMapper).getPhotoDetail(photoDetailGetDtoCaptor.capture());
			
			List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());
			
			PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel);
			
			assertEquals(1, actual.getAccountNo());
			assertEquals(1, actual.getPhotoNo());
			assertFalse(actual.getIsFavorite());
			assertNull(actual.getPhotoAt());
			assertNull(actual.getLocationNo());
			assertNull(actual.getAddress());
			assertNull(actual.getLatitude());
			assertNull(actual.getLongitude());
			assertNull(actual.getLocationName());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.getImageFilePath());
			assertEquals("", actual.getPhotoJapaneseTitle());
			assertEquals("", actual.getPhotoEnglishTitle());
			assertEquals("", actual.getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
			assertNull(actual.getFocalLength());
			assertNull(actual.getFValue());
			assertNull(actual.getShutterSpeed());
			assertNull(actual.getIso());
			assertEquals(0, actual.getPhotoTagModelList().size());
			
			PhotoDetailGetDto photoDetailGetDtoCapture = photoDetailGetDtoCaptor.getValue();
			assertEquals(1, photoDetailGetDtoCapture.getAccountNo());
			assertEquals(1, photoDetailGetDtoCapture.getPhotoAccountNo());
			assertEquals(1, photoDetailGetDtoCapture.getPhotoNo());
			
			PhotoTagMst photoTagMstCapture = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMstCapture.getAccountNo());
			assertEquals(1, photoTagMstCapture.getPhotoNo());
			assertNull(photoTagMstCapture.getTagNo());
			assertNull(photoTagMstCapture.getTagJapaneseName());
			assertNull(photoTagMstCapture.getTagEnglishName());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真のメタデータがデフォルト値でない場、写真タグが1件以上の場合")
		void getPhotoDetail_not_default_value_photoTag_found() throws PhotoNotFoundException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.photoNo(1)
					.build();
			
			PhotoDetailDto photoDetailDto = new PhotoDetailDto();
			photoDetailDto.setAccountNo(1);
			photoDetailDto.setPhotoNo(1);
			photoDetailDto.setIsFavorite(false);
			photoDetailDto.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
			photoDetailDto.setLocationNo(1);
			photoDetailDto.setAddress("住所");
			photoDetailDto.setLatitude(BigDecimal.valueOf(38.000));
			photoDetailDto.setLongitude(BigDecimal.valueOf(115.000));
			photoDetailDto.setLocationName("富士山");
			photoDetailDto.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
			photoDetailDto.setPhotoJapaneseTitle("タイトル");
			photoDetailDto.setPhotoEnglishTitle("title");
			photoDetailDto.setCaption("キャプション");
			photoDetailDto.setDirectionKbn(DirectionEnum.VERTICAL);
			photoDetailDto.setFocalLength(24);
			photoDetailDto.setFValue(BigDecimal.valueOf(2.8));
			photoDetailDto.setShutterSpeed(BigDecimal.valueOf(0.01));
			photoDetailDto.setIso(100);
			
			ArgumentCaptor<PhotoDetailGetDto> photoDetailGetDtoCaptor = ArgumentCaptor.forClass(PhotoDetailGetDto.class);
			doReturn(photoDetailDto).when(photoDetailMapper).getPhotoDetail(photoDetailGetDtoCaptor.capture());
			
			List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();
			photoTagMstList.add(PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(1)
					.tagNo(1)
					.tagJapaneseName("太陽")
					.tagEnglishName("sun")
					.build());
			photoTagMstList.add(PhotoTagMst.builder()
					.accountNo(1)
					.photoNo(2)
					.tagNo(1)
					.tagJapaneseName("海")
					.tagEnglishName("sea")
					.build());
			
			ArgumentCaptor<PhotoTagMst> photoTagMstCaptor = ArgumentCaptor.forClass(PhotoTagMst.class);
			doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());
			
			PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel);
			
			assertEquals(1, actual.getAccountNo());
			assertEquals(1, actual.getPhotoNo());
			assertFalse(actual.getIsFavorite());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt());
			assertEquals(1, actual.getLocationNo());
			assertEquals("住所", actual.getAddress());
			assertEquals(0, BigDecimal.valueOf(38.000).compareTo(actual.getLatitude()));
			assertEquals(0, BigDecimal.valueOf(115.000).compareTo(actual.getLongitude()));
			assertEquals("富士山", actual.getLocationName());
			assertEquals("https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.getImageFilePath());
			assertEquals("タイトル", actual.getPhotoJapaneseTitle());
			assertEquals("title", actual.getPhotoEnglishTitle());
			assertEquals("キャプション", actual.getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
			assertEquals(2, actual.getPhotoTagModelList().size());
			assertEquals(1, actual.getPhotoTagModelList().get(0).getTagNo());
			assertEquals("太陽", actual.getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sun", actual.getPhotoTagModelList().get(0).getTagEnglishName());
			assertEquals("海", actual.getPhotoTagModelList().get(1).getTagJapaneseName());
			assertEquals("sea", actual.getPhotoTagModelList().get(1).getTagEnglishName());
			
			PhotoDetailGetDto photoDetailGetDtoCapture = photoDetailGetDtoCaptor.getValue();
			assertEquals(1, photoDetailGetDtoCapture.getAccountNo());
			assertEquals(1, photoDetailGetDtoCapture.getPhotoAccountNo());
			assertEquals(1, photoDetailGetDtoCapture.getPhotoNo());
			
			PhotoTagMst photoTagMstCapture = photoTagMstCaptor.getValue();
			assertEquals(1, photoTagMstCapture.getAccountNo());
			assertEquals(1, photoTagMstCapture.getPhotoNo());
			assertNull(photoTagMstCapture.getTagNo());
			assertNull(photoTagMstCapture.getTagJapaneseName());
			assertNull(photoTagMstCapture.getTagEnglishName());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.photoNo(1)
					.build();
			
			ArgumentCaptor<PhotoDetailGetDto> photoDetailGetDtoCaptor = ArgumentCaptor.forClass(PhotoDetailGetDto.class);
			doReturn(null).when(photoDetailMapper).getPhotoDetail(photoDetailGetDtoCaptor.capture());
			
			assertThrows(PhotoNotFoundException.class, () -> photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel));
			verify(photoTagMstMapper, times(0)).select(any(PhotoTagMst.class));
		}
	}
}