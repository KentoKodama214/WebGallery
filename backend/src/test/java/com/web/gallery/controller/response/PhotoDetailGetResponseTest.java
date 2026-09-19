package com.web.gallery.controller.response;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.IsLocationPublic;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoDetailGetResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class nullableFields {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目に値が設定されている場合、それぞれの値が設定されること")
    void from_withAllValues() {
      OffsetDateTime photoAt = OffsetDateTime.now();
      PhotoTagModel tagModel =
          PhotoTagModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .tagNo(new TagNo(1L))
              .tagJapaneseName(new TagJapaneseName("風景"))
              .tagEnglishName(new TagEnglishName("landscape"))
              .build();
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .isFavorite(new IsFavorite(true))
              .photoAt(new PhotoAt(photoAt))
              .locationNo(new LocationNo(1L))
              .geoLocation(
                  new GeoLocation(
                      new Address("東京都渋谷区"),
                      new Latitude(new BigDecimal("35.681236")),
                      new Longitude(new BigDecimal("139.767125"))))
              .locationName(new LocationName("渋谷駅"))
              .isLocationPublic(new IsLocationPublic(true))
              .imageFilePath(new ImageFilePath("path/to/image.jpg"))
              .photoJapaneseTitle(new PhotoJapaneseTitle("タイトル"))
              .photoEnglishTitle(new PhotoEnglishTitle("title"))
              .caption(new Caption("キャプション"))
              .directionKbn(DirectionEnum.VERTICAL)
              .exifData(
                  new ExifData(
                      new FocalLength(50),
                      new FValue(new BigDecimal("1.8")),
                      new ShutterSpeed(new BigDecimal("0.01")),
                      new Iso(100)))
              .photoTagModelList(PhotoTagModelList.of(List.of(tagModel)))
              .build();

      PhotoDetailGetResponse actual = PhotoDetailGetResponse.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(1L, actual.getPhotoNo());
      assertTrue(actual.getIsFavorite());
      assertEquals(photoAt, actual.getPhotoAt());
      assertEquals(1L, actual.getLocationNo());
      assertEquals("東京都渋谷区", actual.getAddress());
      assertEquals(new BigDecimal("35.681236"), actual.getLatitude());
      assertEquals(new BigDecimal("139.767125"), actual.getLongitude());
      assertEquals("渋谷駅", actual.getLocationName());
      assertTrue(actual.getIsLocationPublic());
      assertEquals("path/to/image.jpg", actual.getImageFilePath());
      assertEquals("タイトル", actual.getPhotoJapaneseTitle());
      assertEquals("title", actual.getPhotoEnglishTitle());
      assertEquals("キャプション", actual.getCaption());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertEquals(50, actual.getFocalLength());
      assertEquals(new BigDecimal("1.8"), actual.getFValue());
      assertEquals(new BigDecimal("0.01"), actual.getShutterSpeed());
      assertEquals(100, actual.getIso());
      assertEquals(1, actual.getPhotoTagList().size());
      assertEquals("風景", actual.getPhotoTagList().get(0).getTagJapaneseName());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：任意項目が未設定の場合、それぞれnullが設定されること")
    void from_withoutOptionalValues() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .imageFilePath(new ImageFilePath("path/to/image.jpg"))
              .build();

      PhotoDetailGetResponse actual = PhotoDetailGetResponse.from(model);

      assertNull(actual.getIsFavorite());
      assertNull(actual.getPhotoAt());
      assertNull(actual.getLocationNo());
      assertNull(actual.getAddress());
      assertNull(actual.getLatitude());
      assertNull(actual.getLongitude());
      assertNull(actual.getLocationName());
      assertNull(actual.getIsLocationPublic());
      assertNull(actual.getPhotoJapaneseTitle());
      assertNull(actual.getPhotoEnglishTitle());
      assertNull(actual.getCaption());
      assertNull(actual.getFocalLength());
      assertNull(actual.getFValue());
      assertNull(actual.getShutterSpeed());
      assertNull(actual.getIso());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class photoTagList {
    @Test
    @Order(1)
    @DisplayName("正常系：写真タグリストが未設定（null）の場合、空リストが設定されること")
    void photoTagList_null() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .imageFilePath(new ImageFilePath("path/to/image.jpg"))
              .photoTagModelList(null)
              .build();

      PhotoDetailGetResponse actual = PhotoDetailGetResponse.from(model);

      assertNotNull(actual.getPhotoTagList());
      assertTrue(actual.getPhotoTagList().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真タグリストが空の場合、空リストが設定されること")
    void photoTagList_empty() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .imageFilePath(new ImageFilePath("path/to/image.jpg"))
              .photoTagModelList(PhotoTagModelList.empty())
              .build();

      PhotoDetailGetResponse actual = PhotoDetailGetResponse.from(model);

      assertNotNull(actual.getPhotoTagList());
      assertTrue(actual.getPhotoTagList().isEmpty());
    }
  }
}
