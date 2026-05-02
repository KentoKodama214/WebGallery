package com.web.gallary.mapper;

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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.web.gallary.entity.PhotoMst;
import com.web.gallary.enumuration.DirectionEnum;

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
			PhotoMst photoMst = PhotoMst.builder().accountNo(1).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(3, actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのcountで1件の場合")
		void count_by_photoNo() {
			PhotoMst photoMst = PhotoMst.builder().photoNo(1).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(2, actual);
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：削除フラグでのcountで1件以上の場合")
		void count_by_isDeleted() {
			PhotoMst photoMst = PhotoMst.builder().isDeleted(true).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(3, actual);
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：撮影日時でのcountで1件の場合")
		void count_by_photoAt() {
			PhotoMst photoMst = PhotoMst.builder()
					.photoAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：ロケーション番号でのcountで1件の場合")
		void count_by_locationNo() {
			PhotoMst photoMst = PhotoMst.builder().locationNo(1).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：画像ファイルパスでのcountで1件の場合")
		void count_by_imageFilePath() {
			PhotoMst photoMst = PhotoMst.builder().imageFilePath("https://www.xxx.com/DSC111.jpg").build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：写真タイトル日本語でのcountで1件の場合")
		void count_by_photoJapaneseTitle() {
			PhotoMst photoMst = PhotoMst.builder().photoJapaneseTitle("タイトル11").build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：写真タイトル英語でのcountで1件の場合")
		void count_by_photoEnglishTitle() {
			PhotoMst photoMst = PhotoMst.builder().photoEnglishTitle("title11").build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：キャプションでのcountで1件の場合")
		void count_by_caption() {
			PhotoMst photoMst = PhotoMst.builder().caption("キャプション11").build();
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
			PhotoMst photoMst = PhotoMst.builder().focalLength(24).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：F値でのcountで1件の場合")
		void count_by_fValue() {
			PhotoMst photoMst = PhotoMst.builder().fValue(BigDecimal.valueOf(8.0)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：シャッタースピードでのcountで1件の場合")
		void count_by_shutterSpeed() {
			PhotoMst photoMst = PhotoMst.builder().shutterSpeed(BigDecimal.valueOf(1)).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：ISOでのcountで1件の場合")
		void count_by_iso() {
			PhotoMst photoMst = PhotoMst.builder().iso(100).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(1, actual);
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：countで0件の場合")
		void count_not_found() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(100).build();
			Integer actual = photoMstMapper.count(photoMst);
			assertEquals(0, actual);
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でcountする場合")
		void count_some_conditions() {
			PhotoMst photoMst = PhotoMst.builder().accountNo(1).photoNo(1).build();
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
					.accountNo(1)
					.photoNo(4)
					.createdBy(1)
					.createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.updatedBy(1)
					.updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
					.isDeleted(false)
					.photoAt(OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
					.locationNo(6)
					.imageFilePath("https://www.xxx.com/DSC666.jpg")
					.photoJapaneseTitle("")
					.photoEnglishTitle("")
					.caption("")
					.directionKbn(DirectionEnum.NONE)
					.focalLength(100)
					.fValue(BigDecimal.valueOf(2.8))
					.shutterSpeed(BigDecimal.valueOf(0.01))
					.iso(200)
					.build();
			
			Integer actualCount = photoMstMapper.insert(insertPhotoMst);
			assertEquals(1, actualCount);
			
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
			assertEquals(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.getFirst().getPhotoAt());
			assertEquals(6, actualData.getFirst().getLocationNo());
			assertEquals("https://www.xxx.com/DSC666.jpg", actualData.getFirst().getImageFilePath());
			assertEquals("", actualData.getFirst().getPhotoJapaneseTitle());
			assertEquals("", actualData.getFirst().getPhotoEnglishTitle());
			assertEquals("", actualData.getFirst().getCaption());
			assertEquals(DirectionEnum.NONE, actualData.getFirst().getDirectionKbn());
			assertEquals(100, actualData.getFirst().getFocalLength());
			assertEquals(0, BigDecimal.valueOf(2.8).compareTo(actualData.getFirst().getFValue()));
			assertEquals(0, BigDecimal.valueOf(0.01).compareTo(actualData.getFirst().getShutterSpeed()));
			assertEquals(200, actualData.getFirst().getIso());
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
		}
		
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号でのupdate")
		void update_by_accountNo() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().accountNo(1).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(3, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("account_no=1");
			assertEquals(3, actualData.size());
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：写真番号でのupdate")
		void update_by_photoNo() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().photoNo(1).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(2, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_no=1")
					.stream().sorted(Comparator.comparing(PhotoMst::getAccountNo)).toList();
			assertEquals(2, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
			
			assertEquals(2, actualData.get(1).getAccountNo());
			assertEquals(1, actualData.get(1).getPhotoNo());
			assertEquals(1, actualData.get(1).getCreatedBy());
			assertEquals(1, actualData.get(1).getUpdatedBy());
			assertFalse(actualData.get(1).getIsDeleted());
			assertEquals(OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(1).getPhotoAt());
			assertEquals(4, actualData.get(1).getLocationNo());
			assertEquals("https://www.xxx.com/DSC444.jpg", actualData.get(1).getImageFilePath());
			assertEquals("タイトル21", actualData.get(1).getPhotoJapaneseTitle());
			assertEquals("title21", actualData.get(1).getPhotoEnglishTitle());
			assertEquals("キャプション21", actualData.get(1).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actualData.get(1).getDirectionKbn());
			assertEquals(80, actualData.get(1).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(12.0).compareTo(actualData.get(1).getFValue()));
			assertEquals(0, BigDecimal.valueOf(5).compareTo(actualData.get(1).getShutterSpeed()));
			assertEquals(1000, actualData.get(1).getIso());
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：削除フラグでのupdate")
		void update_by_isDeleted() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().isDeleted(true).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(3, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("is_deleted=true");
			assertEquals(3, actualData.size());
			
			actualData = actualData.stream().filter(photoMst -> photoMst.getAccountNo() == 2).toList();
			assertEquals(2, actualData.get(0).getAccountNo());
			assertEquals(3, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertTrue(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2022, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(6, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC555.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル23", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title23", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション23", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.HORIZONTAL, actualData.get(0).getDirectionKbn());
			assertEquals(50, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(10.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(3).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：撮影日時でのupdate")
		void update_by_photoAt() {
			PhotoMst conditionPhotoMst = PhotoMst.builder()
					.photoAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_at='2021-01-01 00:00:00.000 +0000'");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(5)
		@DisplayName("正常系：ロケーション番号でのupdate")
		void update_by_locationNo() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().locationNo(1).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("location_no=1");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(6)
		@DisplayName("正常系：画像ファイルパスでのupdate")
		void update_by_imageFilePath() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().imageFilePath("https://www.xxx.com/DSC111.jpg").build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("image_file_path='https://www.xxx.com/DSC111.jpg'");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(7)
		@DisplayName("正常系：写真タイトル日本語でのupdate")
		void update_by_photoJapaneseTitle() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().photoJapaneseTitle("タイトル11").build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_japanese_title='タイトル11'");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(8)
		@DisplayName("正常系：写真タイトル英語でのupdate")
		void update_by_photoEnglishTitle() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().photoEnglishTitle("title11").build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("photo_english_title='title11'");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(9)
		@DisplayName("正常系：キャプションでのcountで1件の場合")
		void update_by_caption() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().caption("キャプション11").build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("caption='キャプション11'");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(10)
		@DisplayName("正常系：向き区分コードでのupdate")
		void update_by_directionKbnCode() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().directionKbn(DirectionEnum.VERTICAL).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("direction_kbn='vertical'");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(11)
		@DisplayName("正常系：焦点距離でのupdate")
		void update_by_focalLength() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().focalLength(24).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("focal_length=24");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(12)
		@DisplayName("正常系：F値でのupdate")
		void update_by_fValue() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().fValue(BigDecimal.valueOf(8.0)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("f_value=8.0");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(13)
		@DisplayName("正常系：シャッタースピードでのupdate")
		void update_by_shutterSpeed() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().shutterSpeed(BigDecimal.valueOf(1)).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("shutter_speed=1");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(14)
		@DisplayName("正常系：ISOでのupdate")
		void update_by_iso() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().iso(100).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("iso=1000");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
		
		@Test
		@Order(15)
		@DisplayName("正常系：updateで0件の場合")
		void update_not_found() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().accountNo(100).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(0, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("account_no=100");
			assertEquals(0, actualData.size());
		}
		
		@Test
		@Order(16)
		@DisplayName("正常系：複数の条件でupdateする場合")
		void update_some_conditions() {
			PhotoMst conditionPhotoMst = PhotoMst.builder().accountNo(1).photoNo(1).build();
			PhotoMst targetPhotoMst = PhotoMst.builder().iso(1000).build();
			Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
			assertEquals(1, actual);
			
			List<PhotoMst> actualData = getPhotoMstList("account_no=1 and photo_no=1");
			assertEquals(1, actualData.size());
			
			assertEquals(1, actualData.get(0).getAccountNo());
			assertEquals(1, actualData.get(0).getPhotoNo());
			assertEquals(1, actualData.get(0).getCreatedBy());
			assertEquals(1, actualData.get(0).getUpdatedBy());
			assertFalse(actualData.get(0).getIsDeleted());
			assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)), actualData.get(0).getPhotoAt());
			assertEquals(1, actualData.get(0).getLocationNo());
			assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
			assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
			assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
			assertEquals("キャプション11", actualData.get(0).getCaption());
			assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
			assertEquals(24, actualData.get(0).getFocalLength());
			assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
			assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
			assertEquals(1000, actualData.get(0).getIso());
		}
	}
	
	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class getMaxPhotoNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号に該当する写真がある場合")
		void getMaxPhotoNo_found() {
			Integer actual = photoMstMapper.getMaxPhotoNo(1);
			assertEquals(3, actual);
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：アカウント番号に該当する写真がない場合")
		void getMaxPhotoNo_not_found() {
			Integer actual = photoMstMapper.getMaxPhotoNo(3);
			assertNull(actual);
		}
	}
	
	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/mapper/PhotoMstMapperTest.sql")
	class isExistPhoto {
		@Test
		@Order(1)
		@DisplayName("正常系：画像ファイルパスに該当する写真が1つある場合")
		void isExistPhoto_photo_found() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(1)
					.imageFilePath("DSC111.jpg")
					.build();
			assertTrue(photoMstMapper.isExistPhoto(photoMst));
		}
		
		@Test
		@Order(2)
		@DisplayName("正常系：画像ファイルパスに該当する写真が複数ある場合")
		void isExistPhoto_photos_found() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(2)
					.imageFilePath("DSC555.jpg")
					.build();
			assertTrue(photoMstMapper.isExistPhoto(photoMst));
		}
		
		@Test
		@Order(3)
		@DisplayName("正常系：画像ファイルパスに該当する写真があるが、削除済みの場合")
		void isExistPhoto_found_is_deleted() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(1)
					.imageFilePath("DSC333.jpg")
					.build();
			assertFalse(photoMstMapper.isExistPhoto(photoMst));
		}
		
		@Test
		@Order(4)
		@DisplayName("正常系：画像ファイルパスに該当する写真がない場合")
		void isExistPhoto_not_found() {
			PhotoMst photoMst = PhotoMst.builder()
					.accountNo(1)
					.imageFilePath("DSC999.jpg")
					.build();
			assertFalse(photoMstMapper.isExistPhoto(photoMst));
		}
	}
}