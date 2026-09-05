package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.dto.PhotoDeletionDto;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoMstCondition;
import com.web.gallery.entity.PhotoMstUpdateTarget;
import com.web.gallery.enumeration.DirectionEnum;
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

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PhotoMstMapperTest {
  @Autowired private PhotoMstMapper photoMstMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

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
      PhotoMstCondition photoMst = PhotoMstCondition.builder().accountNo(1L).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(3, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真番号でのcountで1件の場合")
    void count_by_photoNo() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().photoNo(1L).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(2, actual);
    }

    @Test
    @Order(3)
    @DisplayName("正常系：削除フラグでのcountで1件以上の場合")
    void count_by_isDeleted() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().isDeleted(true).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(3, actual);
    }

    @Test
    @Order(4)
    @DisplayName("正常系：撮影日時でのcountで1件の場合")
    void count_by_photoAt() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder()
              .photoAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
              .build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(5)
    @DisplayName("正常系：ロケーション番号でのcountで1件の場合")
    void count_by_locationNo() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().locationNo(1L).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(6)
    @DisplayName("正常系：画像ファイルパスでのcountで1件の場合")
    void count_by_imageFilePath() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().imageFilePath("https://www.xxx.com/DSC111.jpg").build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(7)
    @DisplayName("正常系：写真タイトル日本語でのcountで1件の場合")
    void count_by_photoJapaneseTitle() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().photoJapaneseTitle("タイトル11").build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(8)
    @DisplayName("正常系：写真タイトル英語でのcountで1件の場合")
    void count_by_photoEnglishTitle() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().photoEnglishTitle("title11").build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(9)
    @DisplayName("正常系：キャプションでのcountで1件の場合")
    void count_by_caption() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().caption("キャプション11").build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(10)
    @DisplayName("正常系：向き区分コードでのcountで1件の場合")
    void count_by_directionKbnCode() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().directionKbn(DirectionEnum.VERTICAL).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(11)
    @DisplayName("正常系：焦点距離でのcountで1件の場合")
    void count_by_focalLength() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().focalLength(24).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(12)
    @DisplayName("正常系：F値でのcountで1件の場合")
    void count_by_fValue() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().fValue(BigDecimal.valueOf(8.0)).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(13)
    @DisplayName("正常系：シャッタースピードでのcountで1件の場合")
    void count_by_shutterSpeed() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().shutterSpeed(BigDecimal.valueOf(1)).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(14)
    @DisplayName("正常系：ISOでのcountで1件の場合")
    void count_by_iso() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().iso(100).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(1, actual);
    }

    @Test
    @Order(15)
    @DisplayName("正常系：countで0件の場合")
    void count_not_found() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().accountNo(100L).build();
      Integer actual = photoMstMapper.count(photoMst);
      assertEquals(0, actual);
    }

    @Test
    @Order(16)
    @DisplayName("正常系：複数の条件でcountする場合")
    void count_some_conditions() {
      PhotoMstCondition photoMst = PhotoMstCondition.builder().accountNo(1L).photoNo(1L).build();
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
      PhotoMst insertPhotoMst =
          PhotoMst.builder()
              .accountNo(1L)
              .photoNo(4L)
              .createdBy(1L)
              .createdAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
              .updatedBy(1L)
              .updatedAt(OffsetDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)))
              .isDeleted(false)
              .photoAt(OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9)))
              .locationNo(6L)
              .imageFilePath("https://www.xxx.com/DSC666.jpg")
              .imageFileName("DSC666.jpg")
              .photoJapaneseTitle("")
              .photoEnglishTitle("")
              .caption("")
              .directionKbn(DirectionEnum.NONE)
              .focalLength(100)
              .fValue(BigDecimal.valueOf(2.8))
              .shutterSpeed(BigDecimal.valueOf(0.01))
              .iso(200)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = photoMstMapper.insert(insertPhotoMst);
      assertEquals(1, actualCount);

      List<PhotoMst> actualData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_mst WHERE account_no=1 and photo_no=4",
              (rs, rowNum) ->
                  PhotoMst.builder()
                      .accountNo(rs.getLong("account_no"))
                      .photoNo(rs.getLong("photo_no"))
                      .createdBy(rs.getLong("created_by"))
                      .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                      .updatedBy(rs.getLong("updated_by"))
                      .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                      .isDeleted(rs.getBoolean("is_deleted"))
                      .photoAt(rs.getObject("photo_at", OffsetDateTime.class))
                      .locationNo(rs.getLong("location_no"))
                      .imageFilePath(rs.getString("image_file_path"))
                      .imageFileName(rs.getString("image_file_name"))
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
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals(4L, actualData.getFirst().getPhotoNo());
      assertEquals(1L, actualData.getFirst().getCreatedBy());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.getFirst().getPhotoAt());
      assertEquals(6L, actualData.getFirst().getLocationNo());
      assertEquals("https://www.xxx.com/DSC666.jpg", actualData.getFirst().getImageFilePath());
      assertEquals("DSC666.jpg", actualData.getFirst().getImageFileName());
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
          "SELECT * FROM photo.photo_mst WHERE " + condition,
          (rs, rowNum) ->
              PhotoMst.builder()
                  .accountNo(rs.getLong("account_no"))
                  .photoNo(rs.getLong("photo_no"))
                  .createdBy(rs.getLong("created_by"))
                  .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                  .updatedBy(rs.getLong("updated_by"))
                  .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                  .isDeleted(rs.getBoolean("is_deleted"))
                  .photoAt(rs.getObject("photo_at", OffsetDateTime.class))
                  .locationNo(rs.getLong("location_no"))
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().accountNo(1L).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(3, actual);

      List<PhotoMst> actualData = getPhotoMstList("account_no=1");
      assertEquals(3, actualData.size());
      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().photoNo(1L).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(2, actual);

      List<PhotoMst> actualData =
          getPhotoMstList("photo_no=1").stream()
              .sorted(Comparator.comparing(PhotoMst::getAccountNo))
              .toList();
      assertEquals(2, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
      assertEquals("https://www.xxx.com/DSC111.jpg", actualData.get(0).getImageFilePath());
      assertEquals("タイトル11", actualData.get(0).getPhotoJapaneseTitle());
      assertEquals("title11", actualData.get(0).getPhotoEnglishTitle());
      assertEquals("キャプション11", actualData.get(0).getCaption());
      assertEquals(DirectionEnum.VERTICAL, actualData.get(0).getDirectionKbn());
      assertEquals(24, actualData.get(0).getFocalLength());
      assertEquals(0, BigDecimal.valueOf(8.0).compareTo(actualData.get(0).getFValue()));
      assertEquals(0, BigDecimal.valueOf(1).compareTo(actualData.get(0).getShutterSpeed()));
      assertEquals(1000, actualData.get(0).getIso());

      assertEquals(2L, actualData.get(1).getAccountNo());
      assertEquals(1L, actualData.get(1).getPhotoNo());
      assertEquals(1L, actualData.get(1).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(1).getCreatedAt());
      assertEquals(1L, actualData.get(1).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(1).getUpdatedAt());
      assertFalse(actualData.get(1).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(1).getPhotoAt());
      assertEquals(4L, actualData.get(1).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().isDeleted(true).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(3, actual);

      List<PhotoMst> actualData = getPhotoMstList("is_deleted=true");
      assertEquals(3, actualData.size());

      actualData = actualData.stream().filter(photoMst -> photoMst.getAccountNo() == 2).toList();
      assertEquals(2L, actualData.get(0).getAccountNo());
      assertEquals(3L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertTrue(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2022, 3, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(6L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder()
              .photoAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)))
              .build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("photo_at='2021-01-01 00:00:00.000 +0000'");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().locationNo(1L).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("location_no=1");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder().imageFilePath("https://www.xxx.com/DSC111.jpg").build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData =
          getPhotoMstList("image_file_path='https://www.xxx.com/DSC111.jpg'");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder().photoJapaneseTitle("タイトル11").build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("photo_japanese_title='タイトル11'");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder().photoEnglishTitle("title11").build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("photo_english_title='title11'");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().caption("キャプション11").build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("caption='キャプション11'");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder().directionKbn(DirectionEnum.VERTICAL).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("direction_kbn='vertical'");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().focalLength(24).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("focal_length=24");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder().fValue(BigDecimal.valueOf(8.0)).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("f_value=8.0");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder().shutterSpeed(BigDecimal.valueOf(1)).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("shutter_speed=1");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().iso(100).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("iso=1000");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
      PhotoMstCondition conditionPhotoMst = PhotoMstCondition.builder().accountNo(100L).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(0, actual);

      List<PhotoMst> actualData = getPhotoMstList("account_no=100");
      assertEquals(0, actualData.size());
    }

    @Test
    @Order(16)
    @DisplayName("正常系：複数の条件でupdateする場合")
    void update_some_conditions() {
      PhotoMstCondition conditionPhotoMst =
          PhotoMstCondition.builder().accountNo(1L).photoNo(1L).build();
      PhotoMstUpdateTarget targetPhotoMst = PhotoMstUpdateTarget.builder().iso(1000).build();
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actual = photoMstMapper.update(conditionPhotoMst, targetPhotoMst);
      assertEquals(1, actual);

      List<PhotoMst> actualData = getPhotoMstList("account_no=1 and photo_no=1");
      assertEquals(1, actualData.size());

      assertEquals(1L, actualData.get(0).getAccountNo());
      assertEquals(1L, actualData.get(0).getPhotoNo());
      assertEquals(1L, actualData.get(0).getCreatedBy());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getCreatedAt());
      assertEquals(1L, actualData.get(0).getUpdatedBy());
      assertEquals(transactionNow, actualData.get(0).getUpdatedAt());
      assertFalse(actualData.get(0).getIsDeleted());
      assertEquals(
          OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualData.get(0).getPhotoAt());
      assertEquals(1L, actualData.get(0).getLocationNo());
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
    @DisplayName("正常系：画像ファイル名に該当する写真が1つある場合")
    void isExistPhoto_photo_found() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().accountNo(1L).imageFileName("DSC111.jpg").build();
      assertTrue(photoMstMapper.isExistPhoto(photoMst));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：画像ファイル名に該当する写真が複数ある場合")
    void isExistPhoto_photos_found() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().accountNo(2L).imageFileName("DSC555.jpg").build();
      assertTrue(photoMstMapper.isExistPhoto(photoMst));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：画像ファイル名に該当する写真があるが、削除済みの場合")
    void isExistPhoto_found_is_deleted() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().accountNo(1L).imageFileName("DSC333.jpg").build();
      assertFalse(photoMstMapper.isExistPhoto(photoMst));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：画像ファイル名に該当する写真がない場合")
    void isExistPhoto_not_found() {
      PhotoMstCondition photoMst =
          PhotoMstCondition.builder().accountNo(1L).imageFileName("DSC999.jpg").build();
      assertFalse(photoMstMapper.isExistPhoto(photoMst));
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/PhotoMstMapperTest.sql")
  class deletePhotosByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号に該当する写真を物理削除し、削除した行を返す")
    void deletePhotosByAccountNo_found() {
      List<PhotoDeletionDto> actual = photoMstMapper.deletePhotosByAccountNo(1L);

      assertEquals(3, actual.size());
      List<PhotoDeletionDto> sorted =
          actual.stream().sorted((a, b) -> Long.compare(a.getPhotoNo(), b.getPhotoNo())).toList();
      assertEquals(1L, sorted.get(0).getPhotoNo());
      assertFalse(sorted.get(0).getIsDeleted());
      assertEquals(2L, sorted.get(1).getPhotoNo());
      assertTrue(sorted.get(1).getIsDeleted());
      assertEquals(3L, sorted.get(2).getPhotoNo());
      assertTrue(sorted.get(2).getIsDeleted());

      Integer remainCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst WHERE account_no=1", Integer.class);
      assertEquals(0, remainCount);

      // 他のアカウントの写真は残っていること
      Integer otherCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst WHERE account_no=2", Integer.class);
      assertEquals(3, otherCount);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：アカウント番号に該当する写真がない場合")
    void deletePhotosByAccountNo_not_found() {
      List<PhotoDeletionDto> actual = photoMstMapper.deletePhotosByAccountNo(100L);
      assertTrue(actual.isEmpty());
    }
  }
}
