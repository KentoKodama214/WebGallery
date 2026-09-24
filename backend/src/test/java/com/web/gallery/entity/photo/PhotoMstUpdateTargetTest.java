package com.web.gallery.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsLocationPublic;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.model.photo.PhotoDeleteModel;
import com.web.gallery.model.photo.PhotoDetailModel;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoMstUpdateTargetTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void fromForUpdate_allFieldsPresent() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .imageFilePath(new ImageFilePath("1/1-abc.jpg"))
              .photoAt(new PhotoAt(OffsetDateTime.now()))
              .locationNo(new LocationNo(5L))
              .photoJapaneseTitle(new PhotoJapaneseTitle("タイトル"))
              .photoEnglishTitle(new PhotoEnglishTitle("title"))
              .caption(new Caption("caption"))
              .directionKbn(DirectionEnum.SQUARE)
              .exifData(
                  new ExifData(
                      new FocalLength(35),
                      new FValue(new BigDecimal("1.8")),
                      new ShutterSpeed(new BigDecimal("0.02")),
                      new Iso(200)))
              .isLocationPublic(new IsLocationPublic(true))
              .build();

      PhotoMstUpdateTarget actual = PhotoMstUpdateTarget.fromForUpdate(model);

      assertEquals(1L, actual.getUpdatedBy());
      assertFalse(actual.getIsDeleted());
      assertEquals(5L, actual.getLocationNo());
      assertEquals("タイトル", actual.getPhotoJapaneseTitle());
      assertEquals("title", actual.getPhotoEnglishTitle());
      assertEquals("caption", actual.getCaption());
      assertEquals(DirectionEnum.SQUARE, actual.getDirectionKbn());
      assertEquals(35, actual.getFocalLength());
      assertEquals(0, new BigDecimal("1.8").compareTo(actual.getFValue()));
      assertEquals(0, new BigDecimal("0.02").compareTo(actual.getShutterSpeed()));
      assertEquals(200, actual.getIso());
      assertTrue(actual.getIsLocationPublic());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未設定項目のみの場合、デフォルト値が反映されること")
    void fromForUpdate_allFieldsAbsent() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .imageFilePath(new ImageFilePath("1/1-abc.jpg"))
              .build();

      PhotoMstUpdateTarget actual = PhotoMstUpdateTarget.fromForUpdate(model);

      assertEquals(com.web.gallery.constant.Consts.MIN_OFFSET_DATE_TIME, actual.getPhotoAt());
      assertEquals(0L, actual.getLocationNo());
      assertEquals("", actual.getPhotoJapaneseTitle());
      assertEquals("", actual.getPhotoEnglishTitle());
      assertEquals("", actual.getCaption());
      assertEquals(DirectionEnum.NONE, actual.getDirectionKbn());
      assertEquals(0, actual.getFocalLength());
      assertEquals(0, BigDecimal.ZERO.compareTo(actual.getFValue()));
      assertEquals(0, BigDecimal.ZERO.compareTo(actual.getShutterSpeed()));
      assertEquals(0, actual.getIso());
      assertFalse(actual.getIsLocationPublic());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forDelete {
    @Test
    @Order(1)
    @DisplayName("正常系：削除フラグと更新者が設定されること")
    void forDelete_success() {
      PhotoDeleteModel model =
          PhotoDeleteModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .imageFilePath(new ImageFilePath("1/1-abc.jpg"))
              .build();

      PhotoMstUpdateTarget actual = PhotoMstUpdateTarget.forDelete(model);

      assertEquals(1L, actual.getUpdatedBy());
      assertTrue(actual.getIsDeleted());
    }
  }
}
