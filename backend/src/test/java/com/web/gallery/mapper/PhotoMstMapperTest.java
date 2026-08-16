package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.enumeration.DirectionEnum;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PhotoMstMapperTest {
	@Autowired
	private PhotoMstMapper photoMstMapper;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class count {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのcountで1件の場合")
		void count_by_accountNo() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(new AccountNo(1L)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(3, actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのcountで1件の場合")
		void count_by_photoNo() {
			PhotoMst photoMst = PhotoMst.builder().photoNo(new PhotoNo(1L)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(2, actual);
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：削除フラグでのcountで1件以上の場合")
		void count_by_isDeleted() {
			PhotoMst photoMst = PhotoMst.builder().isDeleted(new IsDeleted(true)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(3, actual);
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：撮影日時でのcountで1件の場合")
		void count_by_photoAt() {
			PhotoMst photoMst = PhotoMst.builder()
					.photoAt(new PhotoAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：ロケーション番号でのcountで1件の場合")
		void count_by_locationNo() {
			PhotoMst photoMst = PhotoMst.builder().locationNo(new LocationNo(1L)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：画像ファイルパスでのcountで1件の場合")
		void count_by_imageFilePath() {
			PhotoMst photoMst = PhotoMst.builder().imageFilePath(new ImageFilePath("https://www.xxx.com/DSC111.jpg")).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：写真タイトル日本語でのcountで1件の場合")
		void count_by_photoJapaneseTitle() {
			PhotoMst photoMst = PhotoMst.builder().photoJapaneseTitle(new PhotoJapaneseTitle("タイトル11")).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：写真タイトル英語でのcountで1件の場合")
		void count_by_photoEnglishTitle() {
			PhotoMst photoMst = PhotoMst.builder().photoEnglishTitle(new PhotoEnglishTitle("title11")).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：キャプションでのcountで1件の場合")
		void count_by_caption() {
			PhotoMst photoMst = PhotoMst.builder().caption(new Caption("キャプション11")).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：向き区分コードでのcountで1件の場合")
		void count_by_directionKbnCode() {
			PhotoMst photoMst = PhotoMst.builder().directionKbn(DirectionEnum.VERTICAL).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：焦点距離でのcountで1件の場合")
		void count_by_focalLength() {
			PhotoMst photoMst = PhotoMst.builder().focalLength(new FocalLength(24)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：F値でのcountで1件の場合")
		void count_by_fValue() {
			PhotoMst photoMst = PhotoMst.builder().fValue(new FValue(BigDecimal.valueOf(8.0))).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：シャッタースピードでのcountで1件の場合")
		void count_by_shutterSpeed() {
			PhotoMst photoMst = PhotoMst.builder().shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(1))).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：ISOでのcountで1件の場合")
		void count_by_iso() {
			PhotoMst photoMst = PhotoMst.builder().iso(new Iso(100)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：countで0件の場合")
		void count_not_found() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(new AccountNo(100L)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(0, actual);
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でcountする場合")
		void count_some_conditions() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(new AccountNo(1L)).photoNo(new PhotoNo(1L)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
	}
	
	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class insert {
		@Test
		@Order(1)
		@DisplayName("正常系：登録成功")
		void insert_success() {
			PhotoMst insertPhotoMst = PhotoMst.builder()
					.accountNo(new AccountNo(1L))
					.photoNo(new PhotoNo(4L))
					.createdBy(new CreatedBy(1L))
					.createdAt(new CreatedAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
					.updatedBy(new UpdatedBy(1L))
					.updatedAt(new UpdatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))))
					.isDeleted(new IsDeleted(false))
					.photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9))))
					.locationNo(new LocationNo(6L))
					.imageFilePath(new ImageFilePath("https://www.xxx.com/DSC666.jpg"))
					.photoJapaneseTitle(new PhotoJapaneseTitle(""))
					.photoEnglishTitle(new PhotoEnglishTitle(""))
					.caption(new Caption(""))
					.directionKbn(DirectionEnum.NONE)
					.focalLength(new FocalLength(100))
					.fValue(new FValue(BigDecimal.valueOf(2.8)))
					.shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(0.01)))
					.iso(new Iso(200))
					.build();
			
			Integer actualCount = photoMstMapper.insert(insertPhotoMst);
			assertEquals(1, actualCount);
			
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
			assertEquals(new UpdatedBy(1L), actualData.getFirst().getUpdatedBy());
			assertFalse(actualData.getFirst().getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt().value());
			assertEquals(6L, actualData.getFirst().getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC666.jpg", actualData.getFirst().getImageFilePath().value());
			assertEquals("", actualData.getFirst().getPhotoJapaneseTitle().value());
			assertEquals("", actualData.getFirst().getPhotoEnglishTitle().value());
			assertEquals("", actualData.getFirst().getCaption().value());
			assertEquals(DirectionEnum.NONE, actualData.getFirst().getDirectionKbn());
			assertEquals(100, actualData.getFirst().getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.getFirst().getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.getFirst().getShutterSpeed().value()));
			assertEquals(200, actualData.getFirst().getIso().value());
		}
	}
	
	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class update {
		private List<PhotoMst> getPhotoMstList(String condition) {
			return jdbcTemplate.query(
					"SELECT * FROM photo.photo_mst WHERE " + condition, (rs, rowNum) ->
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
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのupdate")
		void update_by_accountNo() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().accountNo(new AccountNo(1L)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(3, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("account_no=1");
			assertEquals(3, actualData.size());
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのupdate")
		void update_by_photoNo() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().photoNo(new PhotoNo(1L)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(2, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_no=1")
					.stream().sorted(Comparator.comparing(p -> p.getAccountNo().value())).toList();
			assertEquals(2, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
			
			assertEquals(new AccountNo(2L), actualData.get(1).getAccountNo());
			assertEquals(1L, actualData.get(1).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(1).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(1).getUpdatedBy());
			assertFalse(actualData.get(1).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(1).getPhotoAt().value());
			assertEquals(4L, actualData.get(1).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC444.jpg", actualData.get(1).getImageFilePath().value());
			assertEquals("タイトル21", actualData.get(1).getPhotoJapaneseTitle().value());
			assertEquals("title21", actualData.get(1).getPhotoEnglishTitle().value());
			assertEquals("キャプション21", actualData.get(1).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actualData.get(1).getDirectionKbn());
			assertEquals(80, actualData.get(1).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(12.0).compareTo(actualData.get(1).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(5).compareTo(actualData.get(1).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(1).getIso().value());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：削除フラグでのupdate")
		void update_by_isDeleted() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().isDeleted(new IsDeleted(true)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(3, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("is_deleted=true");
			assertEquals(3, actualData.size());
			
			actualData = actualData.stream().filter(photoMst -> photoMst.getAccountNo().value() == 2).toList();
			assertEquals(new AccountNo(2L), actualData.get(0).getAccountNo());
			assertEquals(3L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertTrue(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2022, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(6L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC555.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル23", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title23", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション23", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.HORIZONTAL, actualData.get(0).getDirectionKbn());
			assertEquals(50, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(10.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(3).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：撮影日時でのupdate")
		void update_by_photoAt() {
			PhotoMst conditionPhotoMst = PhotoMst.builder()
					.photoAt(new PhotoAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_at='2021-01-01 00:00:00.000 +0000'");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：ロケーション番号でのupdate")
		void update_by_locationNo() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().locationNo(new LocationNo(1L)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("location_no=1");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：画像ファイルパスでのupdate")
		void update_by_imageFilePath() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().imageFilePath(new ImageFilePath("https://www.xxx.com/DSC111.jpg")).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("image_file_path='https://www.xxx.com/DSC111.jpg'");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：写真タイトル日本語でのupdate")
		void update_by_photoJapaneseTitle() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().photoJapaneseTitle(new PhotoJapaneseTitle("タイトル11")).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_japanese_title='タイトル11'");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：写真タイトル英語でのupdate")
		void update_by_photoEnglishTitle() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().photoEnglishTitle(new PhotoEnglishTitle("title11")).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_english_title='title11'");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：キャプションでのcountで1件の場合")
		void update_by_caption() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().caption(new Caption("キャプション11")).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("caption='キャプション11'");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：向き区分コードでのupdate")
		void update_by_directionKbnCode() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().directionKbn(DirectionEnum.VERTICAL).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("direction_kbn='vertical'");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：焦点距離でのupdate")
		void update_by_focalLength() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().focalLength(new FocalLength(24)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("focal_length=24");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：F値でのupdate")
		void update_by_fValue() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().fValue(new FValue(BigDecimal.valueOf(8.0))).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("f_value=8.0");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：シャッタースピードでのupdate")
		void update_by_shutterSpeed() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().shutterSpeed(new ShutterSpeed(BigDecimal.valueOf(1))).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("shutter_speed=1");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：ISOでのupdate")
		void update_by_iso() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().iso(new Iso(100)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("iso=1000");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：updateで0件の場合")
		void update_not_found() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().accountNo(new AccountNo(100L)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(0, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("account_no=100");
			assertEquals(0, actualData.size());
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でupdateする場合")
		void update_some_conditions() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().accountNo(new AccountNo(1L)).photoNo(new PhotoNo(1L)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(new Iso(1000)).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("account_no=1 and photo_no=1");
			assertEquals(1, actualData.size());
			
			assertEquals(new AccountNo(1L), actualData.get(0).getAccountNo());
			assertEquals(1L, actualData.get(0).getPhotoNo().value());
			assertEquals(new CreatedBy(1L), actualData.get(0).getCreatedBy());
			assertEquals(new UpdatedBy(1L), actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted().value());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt().value());
			assertEquals(1L, actualData.get(0).getLocationNo().value());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath().value());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle().value());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle().value());
			assertEquals("キャプション11", actualData.get(0).getCaption().value());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength().value());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue().value()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed().value()));
			assertEquals(1000, actualData.get(0).getIso().value());
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのdeleteで複数件削除される場合")
		void delete_by_accountNo() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(new AccountNo(1L)).build();
			Integer actual = photoMstMapper.delete(photoMst);
			assertEquals(3, actual);

			Integer remainCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst WHERE account_no=1", Integer.class);
			assertEquals(0, remainCount);

			// 他のアカウントの写真は残っていること
			Integer otherCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst WHERE account_no=2", Integer.class);
			assertEquals(3, otherCount);
		}

		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのdeleteで複数件削除される場合")
		void delete_by_photoNo() {
			PhotoMst photoMst = PhotoMst.builder().photoNo(new PhotoNo(1L)).build();
			Integer actual = photoMstMapper.delete(photoMst);
			assertEquals(2, actual);

			Integer remainCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst WHERE photo_no=1", Integer.class);
			assertEquals(0, remainCount);
		}

		@Test
		@Order(3)
		@DisplayName("正常系：アカウント番号と写真番号でのdeleteで1件削除される場合")
		void delete_by_accountNo_and_photoNo() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(new AccountNo(1L)).photoNo(new PhotoNo(1L)).build();
			Integer actual = photoMstMapper.delete(photoMst);
			assertEquals(1, actual);

			Integer remainCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst WHERE account_no=1", Integer.class);
			assertEquals(2, remainCount);
		}

		@Test
		@Order(4)
		@DisplayName("正常系：該当するレコードがない場合は0件")
		void delete_not_found() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(new AccountNo(100L)).build();
			Integer actual = photoMstMapper.delete(photoMst);
			assertEquals(0, actual);
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class getMaxPhotoNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号に該当する写真がある場合")
		void getMaxPhotoNo_found() {
			Long actual = photoMstMapper.getMaxPhotoNo(1L);
			assertEquals(3, actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウント番号に該当する写真がない場合")
		void getMaxPhotoNo_not_found() {
			Long actual = photoMstMapper.getMaxPhotoNo(3L);
			assertNull(actual);
		}
	}
	
	@Nested
	@Order(6)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class isExistPhoto {
		@Test
		@Order(1)
		@DisplayName("正常系：画像ファイルパスに該当する写真が1つある場合")
		void isExistPhoto_photo_found() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("DSC111.jpg"))
					.build();
			assertTrue(photoMstMapper.isExistPhoto(photoMst));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：画像ファイルパスに該当する写真が複数ある場合")
		void isExistPhoto_photos_found() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(new AccountNo(2L))
					.imageFilePath(new ImageFilePath("DSC555.jpg"))
					.build();
			assertTrue(photoMstMapper.isExistPhoto(photoMst));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：画像ファイルパスに該当する写真があるが、削除済みの場合")
		void isExistPhoto_found_is_deleted() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("DSC333.jpg"))
					.build();
			assertFalse(photoMstMapper.isExistPhoto(photoMst));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：画像ファイルパスに該当する写真がない場合")
		void isExistPhoto_not_found() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(new AccountNo(1L))
					.imageFilePath(new ImageFilePath("DSC999.jpg"))
					.build();
			assertFalse(photoMstMapper.isExistPhoto(photoMst));
		}
	}
}