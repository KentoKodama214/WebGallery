package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsLocationPublic;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.model.PhotoDetailModel;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
class PhotoMstTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void fromForRegist_allFieldsPresent() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .imageFilePath(new ImageFilePath("dummy/path.jpg"))
              .photoAt(new PhotoAt(OffsetDateTime.now()))
              .locationNo(new LocationNo(5L))
              .photoJapaneseTitle(new PhotoJapaneseTitle("タイトル"))
              .photoEnglishTitle(new PhotoEnglishTitle("title"))
              .caption(new Caption("caption"))
              .directionKbn(DirectionEnum.VERTICAL)
              .exifData(
                  new ExifData(
                      new FocalLength(50),
                      new FValue(new BigDecimal("2.8")),
                      new ShutterSpeed(new BigDecimal("0.01")),
                      new Iso(100)))
              .isLocationPublic(new IsLocationPublic(true))
              .imageFile(
                  new ImageFile(
                      new MockMultipartFile(
                          "file", "original.jpg", "image/jpeg", new byte[] {1, 2, 3})))
              .build();

      PhotoMst actual = PhotoMst.fromForRegist(model, "1/1-abc.jpg", 1L);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(1L, actual.getPhotoNo());
      assertEquals("1/1-abc.jpg", actual.getImageFilePath());
      assertEquals("original.jpg", actual.getImageFileName());
      assertEquals("タイトル", actual.getPhotoJapaneseTitle());
      assertEquals("title", actual.getPhotoEnglishTitle());
      assertEquals("caption", actual.getCaption());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertEquals(50, actual.getFocalLength());
      assertEquals(0, new BigDecimal("2.8").compareTo(actual.getFValue()));
      assertEquals(0, new BigDecimal("0.01").compareTo(actual.getShutterSpeed()));
      assertEquals(100, actual.getIso());
      assertTrue(actual.getIsLocationPublic());
      assertEquals(5L, actual.getLocationNo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未設定項目のみの場合、デフォルト値が反映されること")
    void fromForRegist_allFieldsAbsent() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .imageFilePath(new ImageFilePath("1/1-xyz.jpg"))
              .build();

      PhotoMst actual = PhotoMst.fromForRegist(model, "1/1-xyz.jpg", 1L);

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
      assertEquals("1-xyz.jpg", actual.getImageFileName());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：画像ファイルの元ファイル名が取得できない場合、オブジェクトキーの末尾を使うこと")
    void fromForRegist_imageFileWithoutOriginalFilename() {
      MultipartFile multipartFile = mock(MultipartFile.class);
      doReturn(null).when(multipartFile).getOriginalFilename();
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .imageFilePath(new ImageFilePath("1/1-noname.jpg"))
              .imageFile(new ImageFile(multipartFile))
              .build();

      PhotoMst actual = PhotoMst.fromForRegist(model, "1/1-noname.jpg", 1L);

      assertEquals("1-noname.jpg", actual.getImageFileName());
    }
  }
}
