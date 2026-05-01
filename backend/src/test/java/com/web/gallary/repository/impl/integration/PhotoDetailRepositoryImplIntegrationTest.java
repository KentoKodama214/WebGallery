package com.web.gallary.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoGetModel;
import com.web.gallary.model.PhotoModel;
import com.web.gallary.repository.impl.PhotoDetailRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoDetailRepositoryImplIntegrationTest {
	@Autowired
	private PhotoDetailRepositoryImpl photoDetailRepositoryImpl;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoDetailRepositoryImplIntegrationTest.sql")
	class getPhotoList {
		@Test
		@Order(1)
		@DisplayName("正常系：写真が0件の場合")
		void getPhotoList_photo_not_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(1)
					.photoAccountNo(3)
					.build();
			List<PhotoModel> actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
			assertEquals(0, actual.size());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真が1件以上、写真タグが0件の場合")
		void getPhotoList_photoTag_not_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(1)
					.photoAccountNo(2)
					.build();
			
			List<PhotoModel> actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
			
			assertEquals(2, actual.size());
			assertEquals(2, actual.get(0).getAccountNo());
			assertEquals(1, actual.get(0).getPhotoNo());
			assertEquals(0, actual.get(0).getPhotoTagModelList().size());
			assertEquals(2, actual.get(1).getAccountNo());
			assertEquals(2, actual.get(1).getPhotoNo());
			assertEquals(0, actual.get(1).getPhotoTagModelList().size());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：写真が1件以上、写真タグが1件以上の場合")
		void getPhotoList_photoTag_found() {
			PhotoGetModel photoSelectModel = PhotoGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.build();
			
			List<PhotoModel> actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
			
			assertEquals(2, actual.size());
			
			assertEquals(1, actual.get(0).getAccountNo());
			assertEquals(1, actual.get(0).getPhotoNo());
			assertEquals(2, actual.get(0).getPhotoTagModelList().size());
			assertEquals(1, actual.get(0).getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(1, actual.get(0).getPhotoTagModelList().get(0).getPhotoNo());
			assertEquals(1, actual.get(0).getPhotoTagModelList().get(0).getTagNo());
			assertEquals("太陽", actual.get(0).getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sun", actual.get(0).getPhotoTagModelList().get(0).getTagEnglishName());
			assertEquals(1, actual.get(0).getPhotoTagModelList().get(1).getAccountNo());
			assertEquals(1, actual.get(0).getPhotoTagModelList().get(1).getPhotoNo());
			assertEquals(2, actual.get(0).getPhotoTagModelList().get(1).getTagNo());
			assertEquals("青空", actual.get(0).getPhotoTagModelList().get(1).getTagJapaneseName());
			assertEquals("bluesky", actual.get(0).getPhotoTagModelList().get(1).getTagEnglishName());
			
			assertEquals(1, actual.get(1).getAccountNo());
			assertEquals(2, actual.get(1).getPhotoNo());
			assertEquals(3, actual.get(1).getPhotoTagModelList().size());
			assertEquals(1, actual.get(1).getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(2, actual.get(1).getPhotoTagModelList().get(0).getPhotoNo());
			assertEquals(1, actual.get(1).getPhotoTagModelList().get(0).getTagNo());
			assertEquals("太陽", actual.get(1).getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sun", actual.get(1).getPhotoTagModelList().get(0).getTagEnglishName());
			assertEquals(1, actual.get(1).getPhotoTagModelList().get(1).getAccountNo());
			assertEquals(2, actual.get(1).getPhotoTagModelList().get(1).getPhotoNo());
			assertEquals(2, actual.get(1).getPhotoTagModelList().get(1).getTagNo());
			assertEquals("曇天", actual.get(1).getPhotoTagModelList().get(1).getTagJapaneseName());
			assertEquals("cloudy", actual.get(1).getPhotoTagModelList().get(1).getTagEnglishName());
			assertEquals(1, actual.get(1).getPhotoTagModelList().get(2).getAccountNo());
			assertEquals(2, actual.get(1).getPhotoTagModelList().get(2).getPhotoNo());
			assertEquals(3, actual.get(1).getPhotoTagModelList().get(2).getTagNo());
			assertEquals("花", actual.get(1).getPhotoTagModelList().get(2).getTagJapaneseName());
			assertEquals("flower", actual.get(1).getPhotoTagModelList().get(2).getTagEnglishName());
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoDetailRepositoryImplIntegrationTest.sql")
	class getPhotoDetail {
		@Test
		@Order(1)
		@DisplayName("正常系：写真のメタデータがデフォルト値、写真タグが0件の場合")
		void getPhotoDetail_photoTag_default_value_not_found() throws PhotoNotFoundException {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(1)
					.photoAccountNo(2)
					.photoNo(1)
					.build();
			
			PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel);
			
			assertEquals(2, actual.getAccountNo());
			assertEquals(1, actual.getPhotoNo());
			assertFalse(actual.getIsFavorite());
			assertEquals(OffsetDateTime.of(2022, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt());
			assertEquals(4, actual.getLocationNo());
			assertEquals("住所4", actual.getAddress());
			assertEquals(0, BigDecimal.valueOf(38.400).compareTo(actual.getLatitude()));
			assertEquals(0, BigDecimal.valueOf(115.400).compareTo(actual.getLongitude()));
			assertEquals("ロケーション4", actual.getLocationName());
			assertEquals("https://www.xxx.com/DSC444.jpg", actual.getImageFilePath());
			assertEquals("タイトル21", actual.getPhotoJapaneseTitle());
			assertEquals("title21", actual.getPhotoEnglishTitle());
			assertEquals("キャプション21", actual.getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actual.getDirectionKbn());
			assertEquals(80, actual.getFocalLength());
			assertEquals(0, BigDecimal.valueOf(12.0).compareTo(actual.getFValue()));
			assertEquals(0, BigDecimal.valueOf(5).compareTo(actual.getShutterSpeed()));
			assertEquals(800, actual.getIso());
			assertEquals(0, actual.getPhotoTagModelList().size());
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
			
			PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel);
			
			assertEquals(1, actual.getAccountNo());
			assertEquals(1, actual.getPhotoNo());
			assertTrue(actual.getIsFavorite());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actual.getPhotoAt());
			assertEquals(1, actual.getLocationNo());
			assertEquals("住所1", actual.getAddress());
			assertEquals(0, BigDecimal.valueOf(38.100).compareTo(actual.getLatitude()));
			assertEquals(0, BigDecimal.valueOf(115.100).compareTo(actual.getLongitude()));
			assertEquals("ロケーション1", actual.getLocationName());
			assertEquals("https://www.xxx.com/DSC111.jpg", actual.getImageFilePath());
			assertEquals("タイトル11", actual.getPhotoJapaneseTitle());
			assertEquals("title11", actual.getPhotoEnglishTitle());
			assertEquals("キャプション11", actual.getCaption());
			assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
			assertEquals(24, actual.getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actual.getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actual.getShutterSpeed()));
			assertEquals(100, actual.getIso());
			assertEquals(2, actual.getPhotoTagModelList().size());
			
			assertEquals(1, actual.getPhotoTagModelList().get(0).getAccountNo());
			assertEquals(1, actual.getPhotoTagModelList().get(0).getPhotoNo());
			assertEquals(1, actual.getPhotoTagModelList().get(0).getTagNo());
			assertEquals("太陽", actual.getPhotoTagModelList().get(0).getTagJapaneseName());
			assertEquals("sun", actual.getPhotoTagModelList().get(0).getTagEnglishName());
			assertEquals(1, actual.getPhotoTagModelList().get(1).getAccountNo());
			assertEquals(1, actual.getPhotoTagModelList().get(1).getPhotoNo());
			assertEquals(2, actual.getPhotoTagModelList().get(1).getTagNo());
			assertEquals("青空", actual.getPhotoTagModelList().get(1).getTagJapaneseName());
			assertEquals("bluesky", actual.getPhotoTagModelList().get(1).getTagEnglishName());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
		void getPhotoDetail_PhotoNotFoundException() {
			PhotoDetailGetModel photoDetailGetModel = PhotoDetailGetModel.builder()
					.accountNo(1)
					.photoAccountNo(1)
					.photoNo(99)
					.build();
			
			assertThrows(PhotoNotFoundException.class, () -> photoDetailRepositoryImpl.getPhotoDetail(photoDetailGetModel));
		}
	}
}