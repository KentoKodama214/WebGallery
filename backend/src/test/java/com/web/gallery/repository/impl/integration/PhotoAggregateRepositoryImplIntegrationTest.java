package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.entity.photo.PhotoMst;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.photo.PhotoDetailModel;
import com.web.gallery.model.photo.PhotoTagModel;
import com.web.gallery.model.photo.PhotoTagModelList;
import com.web.gallery.repository.impl.PhotoAggregateRepositoryImpl;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class PhotoAggregateRepositoryImplIntegrationTest {
  @Autowired private PhotoAggregateRepositoryImpl photoAggregateRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  private PhotoTagModel buildTag(AccountNo accountNo, String japaneseName, String englishName) {
    return PhotoTagModel.builder()
        .accountNo(accountNo)
        .tagJapaneseName(new TagJapaneseName(japaneseName))
        .tagEnglishName(new TagEnglishName(englishName))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoAggregateRepositoryImplIntegrationTest.sql")
  class regist {
    @Test
    @Order(1)
    @DisplayName("正常系：写真マスタ・タグが登録されること")
    void regist_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo newPhotoNo = new PhotoNo(3L);
      ImageFilePath imageFilePath = new ImageFilePath("https://www.xxx.com/DSC333.jpg");
      MultipartFile multipartFile =
          new MockMultipartFile(
              "file", "DSC333.jpg", "multipart/form-data", "sample image".getBytes());
      PhotoTagModelList tags =
          PhotoTagModelList.of(
              List.of(buildTag(accountNo, "太陽", "sun"), buildTag(accountNo, "海", "sea")));
      PhotoDetailModel requestDetail =
          PhotoDetailModel.builder()
              .accountNo(accountNo)
              .imageFile(new ImageFile(multipartFile))
              .imageFilePath(new ImageFilePath(""))
              .photoTagModelList(tags)
              .build();
      Photo photo = Photo.forRegist(requestDetail, newPhotoNo, imageFilePath);

      photoAggregateRepositoryImpl.regist(photo);

      List<Map<String, Object>> photoMstRows =
          jdbcTemplate.queryForList(
              "SELECT * FROM photo.photo_mst WHERE account_no=1 AND photo_no=3");
      assertEquals(1, photoMstRows.size());
      assertEquals("https://www.xxx.com/DSC333.jpg", photoMstRows.get(0).get("image_file_path"));

      List<Map<String, Object>> photoTagRows =
          jdbcTemplate.queryForList(
              "SELECT * FROM photo.photo_tag_mst WHERE account_no=1 AND photo_no=3 ORDER BY tag_no");
      assertEquals(2, photoTagRows.size());
      assertEquals("太陽", photoTagRows.get(0).get("tag_japanese_name"));
      assertEquals(1L, ((Number) photoTagRows.get(0).get("tag_no")).longValue());
      assertEquals("海", photoTagRows.get(1).get("tag_japanese_name"));
      assertEquals(2L, ((Number) photoTagRows.get(1).get("tag_no")).longValue());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じファイル名の写真が既に存在する場合、FileDuplicateExceptionをthrowすること")
    void regist_duplicate() {
      AccountNo accountNo = new AccountNo(1L);
      MultipartFile multipartFile =
          new MockMultipartFile(
              "file", "DSC111.jpg", "multipart/form-data", "sample image".getBytes());
      PhotoDetailModel requestDetail =
          PhotoDetailModel.builder()
              .accountNo(accountNo)
              .imageFile(new ImageFile(multipartFile))
              .imageFilePath(new ImageFilePath(""))
              .build();
      Photo photo =
          Photo.forRegist(
              requestDetail, new PhotoNo(3L), new ImageFilePath("https://www.xxx.com/DSC111.jpg"));

      assertThrows(FileDuplicateException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      List<Map<String, Object>> photoMstRows =
          jdbcTemplate.queryForList(
              "SELECT * FROM photo.photo_mst WHERE account_no=1 AND photo_no=3");
      assertEquals(0, photoMstRows.size());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoAggregateRepositoryImplIntegrationTest.sql")
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：写真マスタが更新され、タグが差し替わること")
    void update_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(2L);
      PhotoTagModelList tags = PhotoTagModelList.of(List.of(buildTag(accountNo, "川", "river")));
      PhotoDetailModel requestDetail =
          PhotoDetailModel.builder()
              .accountNo(accountNo)
              .photoNo(photoNo)
              .imageFilePath(new ImageFilePath("https://www.xxx.com/DSC222.jpg"))
              .photoTagModelList(tags)
              .build();
      Photo photo = Photo.forUpdate(requestDetail);

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      photoAggregateRepositoryImpl.update(photo);

      List<Map<String, Object>> photoTagRows =
          jdbcTemplate.queryForList(
              "SELECT * FROM photo.photo_tag_mst WHERE account_no=1 AND photo_no=2 ORDER BY tag_no");
      assertEquals(1, photoTagRows.size());
      assertEquals("川", photoTagRows.get(0).get("tag_japanese_name"));
      assertEquals(1L, ((Number) photoTagRows.get(0).get("tag_no")).longValue());

      // 写真マスタ本体も更新されていることを確認する
      // （画像ファイル（image_file_path/image_file_name）は登録後に不変のため更新対象に含まれず、フィクスチャ値が維持される）
      List<PhotoMst> actualPhotoMstData =
          jdbcTemplate.query(
              "SELECT * FROM photo.photo_mst WHERE account_no=1 AND photo_no=2",
              (rs, rowNum) ->
                  PhotoMst.builder()
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
                      .isLocationPublic(rs.getBoolean("is_location_public"))
                      .build());
      assertEquals(1, actualPhotoMstData.size());
      assertEquals(1L, actualPhotoMstData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualPhotoMstData.getFirst().getUpdatedAt());
      assertFalse(actualPhotoMstData.getFirst().getIsDeleted());
      assertEquals(
          OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
          actualPhotoMstData.getFirst().getPhotoAt().plusHours(9));
      assertEquals(
          "https://www.xxx.com/DSC222.jpg", actualPhotoMstData.getFirst().getImageFilePath());
      assertEquals(0L, actualPhotoMstData.getFirst().getLocationNo());
      assertEquals("", actualPhotoMstData.getFirst().getPhotoJapaneseTitle());
      assertEquals("", actualPhotoMstData.getFirst().getPhotoEnglishTitle());
      assertEquals("", actualPhotoMstData.getFirst().getCaption());
      assertEquals(DirectionEnum.NONE, actualPhotoMstData.getFirst().getDirectionKbn());
      assertEquals(0, actualPhotoMstData.getFirst().getFocalLength());
      assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMstData.getFirst().getFValue()));
      assertEquals(0, BigDecimal.ZERO.compareTo(actualPhotoMstData.getFirst().getShutterSpeed()));
      assertEquals(0, actualPhotoMstData.getFirst().getIso());
      assertFalse(actualPhotoMstData.getFirst().getIsLocationPublic());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：削除済みの写真の場合、UpdateFailureExceptionをthrowすること")
    void update_alreadyDeleted_UpdateFailureException() {
      jdbcTemplate.update(
          "UPDATE photo.photo_mst SET is_deleted=true WHERE account_no=1 AND photo_no=2");

      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(2L);
      PhotoDetailModel requestDetail =
          PhotoDetailModel.builder()
              .accountNo(accountNo)
              .photoNo(photoNo)
              .imageFilePath(new ImageFilePath("https://www.xxx.com/DSC222.jpg"))
              .photoTagModelList(PhotoTagModelList.empty())
              .build();
      Photo photo = Photo.forUpdate(requestDetail);

      assertThrows(UpdateFailureException.class, () -> photoAggregateRepositoryImpl.update(photo));

      Integer tagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_tag_mst WHERE account_no=1 AND photo_no=2",
              Integer.class);
      assertEquals(1, tagCount);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/repository/PhotoAggregateRepositoryImplIntegrationTest.sql")
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：写真マスタが論理削除され、タグと全アカウントのお気に入りが削除されること")
    void delete_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(1L);
      Photo photo =
          Photo.forDelete(accountNo, photoNo, new ImageFilePath("https://www.xxx.com/DSC111.jpg"));

      photoAggregateRepositoryImpl.delete(photo);

      List<Map<String, Object>> photoMstRows =
          jdbcTemplate.queryForList(
              "SELECT * FROM photo.photo_mst WHERE account_no=1 AND photo_no=1");
      assertEquals(1, photoMstRows.size());
      assertEquals(true, photoMstRows.get(0).get("is_deleted"));

      Integer tagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_tag_mst WHERE account_no=1 AND photo_no=1",
              Integer.class);
      assertEquals(0, tagCount);

      Integer favoriteCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite WHERE favorite_photo_account_no=1 AND favorite_photo_no=1",
              Integer.class);
      assertEquals(0, favoriteCount);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：既に削除済みの写真を再度削除しようとした場合、PhotoNotFoundExceptionをthrowすること")
    void delete_alreadyDeleted_PhotoNotFoundException() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(1L);
      Photo photo =
          Photo.forDelete(accountNo, photoNo, new ImageFilePath("https://www.xxx.com/DSC111.jpg"));

      photoAggregateRepositoryImpl.delete(photo);

      assertThrows(PhotoNotFoundException.class, () -> photoAggregateRepositoryImpl.delete(photo));
    }
  }
}
