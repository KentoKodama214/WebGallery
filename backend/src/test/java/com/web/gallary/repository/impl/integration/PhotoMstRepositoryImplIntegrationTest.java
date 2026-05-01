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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.web.gallary.entity.PhotoMst;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.repository.impl.PhotoMstRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoMstRepositoryImplIntegrationTest {
	@Autowired
	private PhotoMstRepositoryImpl photoMstRepositoryImpl;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoMstRepositoryImplIntegrationTest.sql")
	class regist {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むPhotoDetailModelの登録")
		void regist_contain_null_parameter() throws RegistFailureException {
			String imageFilePath = "https://www.xxx.com/DSC14.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(4)
					.imageFilePath(imageFilePath)
					.build();
			
			photoMstRepositoryImpl.regist(photoDetailModel, imageFilePath, 4);
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=4", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(4, actualData.getFirst().getPhotoNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().plusHours(9));
			assertEquals(0, actualData.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/DSC14.jpg", actualData.getFirst().getImageFilePath());
			assertEquals("", actualData.getFirst().getPhotoJapaneseTitle());
			assertEquals("", actualData.getFirst().getPhotoEnglishTitle());
			assertEquals("", actualData.getFirst().getCaption());
			assertEquals(DirectionEnum.NONE, actualData.getFirst().getDirectionKbn());
			assertEquals(0, actualData.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getShutterSpeed()));
			assertEquals(0, actualData.getFirst().getIso());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelの登録")
		void regist_not_contain_null_parameter() throws RegistFailureException {
			String imageFilePath = "https://www.xxx.com/DSC14.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(4)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.locationNo(1)
					.imageFilePath(imageFilePath)
					.photoJapaneseTitle("タイトル14")
					.photoEnglishTitle("title14")
					.caption("キャプション14")
					.directionKbn(DirectionEnum.VERTICAL)
					.focalLength(24)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(100)
					.build();
			
			photoMstRepositoryImpl.regist(photoDetailModel, imageFilePath, 4);
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=4", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(4, actualData.getFirst().getPhotoNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().plusHours(9));
			assertEquals(1, actualData.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/DSC14.jpg", actualData.getFirst().getImageFilePath());
			assertEquals("タイトル14", actualData.getFirst().getPhotoJapaneseTitle());
			assertEquals("title14", actualData.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション14", actualData.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.getFirst().getDirectionKbn());
			assertEquals(24, actualData.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.getFirst().getShutterSpeed()));
			assertEquals(100, actualData.getFirst().getIso());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			String imageFilePath = "https://www.xxx.com/DSC11.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			assertThrows(RegistFailureException.class , () -> photoMstRepositoryImpl.regist(photoDetailModel, imageFilePath, 1));
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoMstRepositoryImplIntegrationTest.sql")
	class update {
		@Test
		@Order(1)
		@DisplayName("正常系：Nullのパラメータを含むPhotoDetailModelでの更新")
		void update_contain_null_parameter() throws UpdateFailureException {
			String imageFilePath = "https://www.xxx.com/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			photoMstRepositoryImpl.update(photoDetailModel);
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getPhotoNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().plusHours(9));
			assertEquals(0, actualData.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.getFirst().getImageFilePath());
			assertEquals("", actualData.getFirst().getPhotoJapaneseTitle());
			assertEquals("", actualData.getFirst().getPhotoEnglishTitle());
			assertEquals("", actualData.getFirst().getCaption());
			assertEquals(DirectionEnum.NONE, actualData.getFirst().getDirectionKbn());
			assertEquals(0, actualData.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getFValue()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getShutterSpeed()));
			assertEquals(0, actualData.getFirst().getIso());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelでの更新")
		void update_not_contain_null_parameter() throws UpdateFailureException {
			String imageFilePath = "https://www.xxx.com/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(1)
					.photoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
					.locationNo(1)
					.imageFilePath(imageFilePath)
					.photoJapaneseTitle("タイトル111")
					.photoEnglishTitle("title111")
					.caption("キャプション111")
					.directionKbn(DirectionEnum.HORIZONTAL)
					.focalLength(50)
					.fValue(BigDecimal.valueOf(8.0))
					.shutterSpeed(BigDecimal.valueOf(1))
					.iso(1000)
					.build();
			
			photoMstRepositoryImpl.update(photoDetailModel);
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getPhotoNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().plusHours(9));
			assertEquals(1, actualData.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.getFirst().getImageFilePath());
			assertEquals("タイトル111", actualData.getFirst().getPhotoJapaneseTitle());
			assertEquals("title111", actualData.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション111", actualData.getFirst().getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actualData.getFirst().getDirectionKbn());
			assertEquals(50, actualData.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.getFirst().getShutterSpeed()));
			assertEquals(1000, actualData.getFirst().getIso());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() {
			String imageFilePath = "https://www.xxx.com/DSC999.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.photoNo(9)
					.imageFilePath(imageFilePath)
					.build();
			
			assertThrows(UpdateFailureException.class, () -> photoMstRepositoryImpl.update(photoDetailModel));
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoMstRepositoryImplIntegrationTest.sql")
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void delete_success() throws UpdateFailureException {
			String imageFilePath = "https://www.xxx.com/DSC11.jpg";
			
			PhotoDeleteModel photoDeleteModel = PhotoDeleteModel.builder()
					.accountNo(1)
					.photoNo(1)
					.imageFilePath(imageFilePath)
					.build();
			
			photoMstRepositoryImpl.delete(photoDeleteModel);
			
			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(rs.getInt("account_no"))
							.photoNo(rs.getInt("photo_no"))
							.createdBy(rs.getInt("created_by"))
							.createdAt(rs.getObject("created_at", OffsetDateTime.class))
							.updatedBy(rs.getInt("updated_by"))
							.updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
							.isDeleted(rs.getBoolean("is_deleted"))
							.photoAt(rs.getObject("photo_at", OffsetDateTime.class))
							.locationNo(rs.getInt("location_no"))
							.imageFilePath(rs.getString("image_file_path"))
							.photoJapaneseTitle(rs.getString("photo_japanese_title"))
							.photoEnglishTitle(rs.getString("photo_english_title"))
							.caption(rs.getString("caption"))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(rs.getInt("focal_length"))
							.fValue(rs.getBigDecimal("f_value"))
							.shutterSpeed(rs.getBigDecimal("shutter_speed"))
							.iso(rs.getInt("iso"))
							.build());
			
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.size());
			assertEquals(1, actualData.getFirst().getAccountNo());
			assertEquals(1, actualData.getFirst().getPhotoNo());
			assertEquals(1, actualData.getFirst().getCreatedBy());
			assertEquals(1, actualData.getFirst().getUpdatedBy());
			assertTrue(actualData.getFirst().getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().plusHours(9));
			assertEquals(1, actualData.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/DSC11.jpg", actualData.getFirst().getImageFilePath());
			assertEquals("タイトル11", actualData.getFirst().getPhotoJapaneseTitle());
			assertEquals("title11", actualData.getFirst().getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.getFirst().getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.getFirst().getDirectionKbn());
			assertEquals(24, actualData.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.getFirst().getShutterSpeed()));
			assertEquals(100, actualData.getFirst().getIso());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void delete_UpdateFailureException() {
			String imageFilePath = "https://www.xxx.com/DSC11.jpg";
			
			PhotoDeleteModel photoDeleteModel = PhotoDeleteModel.builder()
					.accountNo(1)
					.photoNo(9)
					.imageFilePath(imageFilePath)
					.build();
			
			assertThrows(UpdateFailureException.class, () -> photoMstRepositoryImpl.delete(photoDeleteModel));
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoMstRepositoryImplIntegrationTest.sql")
	class getNewPhotoNo {
		@Test
		@Order(1)
		@DisplayName("正常系：getMaxPhotoNoがある場合")
		void getNewPhotoNo_getMaxPhotoNo_found() {
			assertEquals(4, photoMstRepositoryImpl.getNewPhotoNo(1));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：getMaxPhotoNoがない場合")
		void getNewPhotoNo_getMaxPhotoNo_not_found() {
			assertEquals(1, photoMstRepositoryImpl.getNewPhotoNo(9));
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoMstRepositoryImplIntegrationTest.sql")
	class isExistPhoto {
		@Test
		@Order(1)
		@DisplayName("正常系：画像ファイルパスに該当する写真が1つある場合")
		void isExistPhoto_photo_found() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC11.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
			
			assertTrue(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：画像ファイルパスに該当する写真が複数ある場合")
		void isExistPhoto_photos_found() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC22.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(2)
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
			
			assertTrue(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：画像ファイルパスに該当する写真があるが、削除済みの場合")
		void isExistPhoto_found_is_deleted() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC13.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
			
			assertFalse(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：画像ファイルパスに該当する写真がない場合")
		void isExistPhoto_not_found() {
			MultipartFile multipartFile = new MockMultipartFile(
					"file",
					"DSC99.jpg",
					"multipart/form-data",
					"sample image".getBytes()
			);
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(1)
					.imageFile(multipartFile)
					.imageFilePath("")
					.build();
			
			assertFalse(photoMstRepositoryImpl.isExistPhoto(photoDetailModel));
		}
		
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/PhotoMstRepositoryImplIntegrationTest.sql")
	class count {
		@Test
		@Order(1)
		@DisplayName("正常系")
		void count_success() {
			assertEquals(2, photoMstRepositoryImpl.count(1));
		}
	}
}