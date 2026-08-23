package com.web.gallery.repository.impl.integration;

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

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.repository.impl.PhotoMstRepositoryImpl;

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
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			photoMstRepositoryImpl.regist(photoDetailModel, new ImageFilePath(imageFilePath), new PhotoNo(4L));

			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=4", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(4L, actualData.getFirst().getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(0L, actualData.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC14.jpg", actualData.getFirst().getImageFilePath().value());
			assertEquals("", actualData.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("", actualData.getFirst().getPhotoEnglishTitle().value());
			assertEquals("", actualData.getFirst().getCaption().value());
			assertEquals(DirectionEnum.NONE, actualData.getFirst().getDirectionKbn());
			assertEquals(0, actualData.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getShutterSpeed().value()));
			assertEquals(0, actualData.getFirst().getIso().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelの登録")
		void regist_not_contain_null_parameter() throws RegistFailureException {
			String imageFilePath = "https://www.xxx.com/DSC14.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.locationNo(new LocationNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル14"))
					.photoEnglishTitle(new PhotoEnglishTitle("title14"))
					.caption(new Caption("キャプション14"))
					.directionKbn(DirectionEnum.VERTICAL)
					.exifData(new ExifData(new FocalLength(24), new FValue(BigDecimal.valueOf(2.8)), new ShutterSpeed(BigDecimal.valueOf(0.01)), new Iso(100)))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			photoMstRepositoryImpl.regist(photoDetailModel, new ImageFilePath(imageFilePath), new PhotoNo(4L));

			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=4", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(4L, actualData.getFirst().getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(transactionNow, actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(1L, actualData.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC14.jpg", actualData.getFirst().getImageFilePath().value());
			assertEquals("タイトル14", actualData.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title14", actualData.getFirst().getPhotoEnglishTitle().value());
			assertEquals("キャプション14", actualData.getFirst().getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.getFirst().getDirectionKbn());
			assertEquals(24, actualData.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.getFirst().getShutterSpeed().value()));
			assertEquals(100, actualData.getFirst().getIso().value());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：RegistFailureExceptionをthrowする")
		void regist_RegistFailureException() {
			String imageFilePath = "https://www.xxx.com/DSC11.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();
			
			assertThrows(RegistFailureException.class , () -> photoMstRepositoryImpl.regist(photoDetailModel, new ImageFilePath(imageFilePath), new PhotoNo(1L)));
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
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			photoMstRepositoryImpl.update(photoDetailModel);

			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(1L, actualData.getFirst().getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(0L, actualData.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.getFirst().getImageFilePath().value());
			assertEquals("", actualData.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("", actualData.getFirst().getPhotoEnglishTitle().value());
			assertEquals("", actualData.getFirst().getCaption().value());
			assertEquals(DirectionEnum.NONE, actualData.getFirst().getDirectionKbn());
			assertEquals(0, actualData.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.ZERO.compareTo(actualData.getFirst().getShutterSpeed().value()));
			assertEquals(0, actualData.getFirst().getIso().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：Nullのパラメータを含まないPhotoDetailModelでの更新")
		void update_not_contain_null_parameter() throws UpdateFailureException {
			String imageFilePath = "https://www.xxx.com/DSC111.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
					.locationNo(new LocationNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.photoJapaneseTitle(new PhotoJapaneseTitle("タイトル111"))
					.photoEnglishTitle(new PhotoEnglishTitle("title111"))
					.caption(new Caption("キャプション111"))
					.directionKbn(DirectionEnum.HORIZONTAL)
					.exifData(new ExifData(new FocalLength(50), new FValue(BigDecimal.valueOf(8.0)), new ShutterSpeed(BigDecimal.valueOf(1)), new Iso(1000)))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			photoMstRepositoryImpl.update(photoDetailModel);

			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(1L, actualData.getFirst().getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(1L, actualData.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.getFirst().getImageFilePath().value());
			assertEquals("タイトル111", actualData.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title111", actualData.getFirst().getPhotoEnglishTitle().value());
			assertEquals("キャプション111", actualData.getFirst().getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actualData.getFirst().getDirectionKbn());
			assertEquals(50, actualData.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.getFirst().getShutterSpeed().value()));
			assertEquals(1000, actualData.getFirst().getIso().value());
		}
		
		@Test
		@Order(3)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void update_UpdateFailureException() {
			String imageFilePath = "https://www.xxx.com/DSC999.jpg";
			
			PhotoDetailModel photoDetailModel = PhotoDetailModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(9L))
					.imageFilePath(new ImageFilePath(imageFilePath))
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
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(1L))
					.imageFilePath(new ImageFilePath(imageFilePath))
					.build();

			OffsetDateTime transactionNow = jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
			photoMstRepositoryImpl.delete(photoDeleteModel);

			List<PhotoMst> actualData = jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=1", (rs, rowNum) ->
						PhotoMst.builder()
							.accountNo(new AccountNo(rs.getLong("account_no")))
							.photoNo(new PhotoNo(rs.getLong("photo_no")))
							.createdBy(new CreatedBy(rs.getLong("created_by")))
							.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
							.updatedBy(new UpdatedBy(rs.getLong("updated_by")))
							.updatedAt(new UpdatedAt(rs.getObject("updated_at", OffsetDateTime.class)))
							.isDeleted(new IsDeleted(rs.getBoolean("is_deleted")))
							.photoAt(new PhotoAt(rs.getObject("photo_at", OffsetDateTime.class)))
							.locationNo(new LocationNo(rs.getLong("location_no")))
							.imageFilePath(new ImageFilePath(rs.getString("image_file_path")))
							.photoJapaneseTitle(new PhotoJapaneseTitle(rs.getString("photo_japanese_title")))
							.photoEnglishTitle(new PhotoEnglishTitle(rs.getString("photo_english_title")))
							.caption(new Caption(rs.getString("caption")))
							.directionKbn(DirectionEnum.getOrDefault(rs.getString("direction_kbn")))
							.focalLength(new FocalLength(rs.getInt("focal_length")))
							.fValue(new FValue(rs.getBigDecimal("f_value")))
							.shutterSpeed(new ShutterSpeed(rs.getBigDecimal("shutter_speed")))
							.iso(new Iso(rs.getInt("iso")))
							.build());

			assertEquals(1, actualData.size());
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(1L, actualData.getFirst().getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.getFirst().getCreatedBy());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getCreatedAt().value());
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertEquals(transactionNow, actualData.getFirst().getUpdatedAt().value());
			assertTrue(actualData.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().value().plusHours(9));
			assertEquals(1L, actualData.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC11.jpg", actualData.getFirst().getImageFilePath().value());
			assertEquals("タイトル11", actualData.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.getFirst().getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.getFirst().getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.getFirst().getDirectionKbn());
			assertEquals(24, actualData.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.getFirst().getShutterSpeed().value()));
			assertEquals(100, actualData.getFirst().getIso().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("異常系：UpdateFailureExceptionをthrowする")
		void delete_UpdateFailureException() {
			String imageFilePath = "https://www.xxx.com/DSC11.jpg";
			
			PhotoDeleteModel photoDeleteModel = PhotoDeleteModel.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(9L))
					.imageFilePath(new ImageFilePath(imageFilePath))
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
			assertEquals(new PhotoNo(4L), photoMstRepositoryImpl.getNewPhotoNo(new AccountNo(1L)));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：getMaxPhotoNoがない場合")
		void getNewPhotoNo_getMaxPhotoNo_not_found() {
			assertEquals(new PhotoNo(1L), photoMstRepositoryImpl.getNewPhotoNo(new AccountNo(9L)));
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
					.accountNo(new AccountNo(1L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
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
					.accountNo(new AccountNo(2L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
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
					.accountNo(new AccountNo(1L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
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
					.accountNo(new AccountNo(1L))
					.imageFile(new ImageFile(multipartFile))
					.imageFilePath(new ImageFilePath(""))
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
			assertEquals(2, photoMstRepositoryImpl.count(new AccountNo(1L)));
		}
	}
}