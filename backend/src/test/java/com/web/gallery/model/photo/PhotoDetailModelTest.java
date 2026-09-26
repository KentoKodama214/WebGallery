package com.web.gallery.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.photo.PhotoBulkSaveRequest;
import com.web.gallery.controller.request.photo.PhotoSaveRequest;
import com.web.gallery.controller.request.photo.PhotoTagSaveRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.enumeration.DirectionEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
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
class PhotoDetailModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromPhotoDetailDto {
    private PhotoDetailDto baseDto() {
      PhotoDetailDto dto = new PhotoDetailDto();
      dto.setAccountNo(1L);
      dto.setPhotoNo(1L);
      dto.setIsFavorite(true);
      dto.setPhotoAt(Consts.MIN_OFFSET_DATE_TIME);
      dto.setImageFilePath("path/to/image.jpg");
      dto.setDirectionKbn(DirectionEnum.HORIZONTAL);
      return dto;
    }

    @Test
    @Order(1)
    @DisplayName("正常系：任意項目が未設定の場合、nullまたはデフォルト値が設定されること")
    void from_optionalFieldsNull() {
      PhotoDetailDto dto = baseDto();

      PhotoDetailModel actual = PhotoDetailModel.from(dto, List.of());

      assertNull(actual.getPhotoAt(), "MIN_OFFSET_DATE_TIMEと同値の場合、未設定扱いになる");
      assertNull(actual.getLocationNo());
      assertNull(actual.getGeoLocation().address());
      assertNull(actual.getGeoLocation().latitude());
      assertNull(actual.getGeoLocation().longitude());
      assertNull(actual.getDisplayName());
      assertNull(actual.getIsLocationPublic());
      assertNull(actual.getPhotoJapaneseTitle());
      assertNull(actual.getPhotoEnglishTitle());
      assertNull(actual.getCaption());
      assertNull(actual.getExifData().focalLength());
      assertNull(actual.getExifData().fValue());
      assertNull(actual.getExifData().shutterSpeed());
      assertNull(actual.getExifData().iso());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：全項目が設定されている場合、そのまま設定されること")
    void from_allFieldsSet() {
      PhotoDetailDto dto = baseDto();
      OffsetDateTime photoAt = OffsetDateTime.now().withOffsetSameInstant(Consts.JST);
      dto.setPhotoAt(photoAt);
      dto.setLocationNo(1L);
      dto.setAddress("東京都渋谷区");
      dto.setLatitude(new BigDecimal("35.6812"));
      dto.setLongitude(new BigDecimal("139.7671"));
      dto.setDisplayName("渋谷スクランブル交差点");
      dto.setIsLocationPublic(true);
      dto.setPhotoJapaneseTitle("東京タワー");
      dto.setPhotoEnglishTitle("Tokyo Tower");
      dto.setCaption("夕暮れの東京タワー");
      dto.setFocalLength(50);
      dto.setFValue(new BigDecimal("2.8"));
      dto.setShutterSpeed(new BigDecimal("0.004"));
      dto.setIso(100);

      PhotoDetailModel actual = PhotoDetailModel.from(dto, List.of());

      assertNotNull(actual.getPhotoAt());
      assertEquals(1L, actual.getLocationNo().value());
      assertEquals("東京都渋谷区", actual.getGeoLocation().address().value());
      assertEquals(new BigDecimal("35.6812"), actual.getGeoLocation().latitude().value());
      assertEquals(new BigDecimal("139.7671"), actual.getGeoLocation().longitude().value());
      assertEquals("渋谷スクランブル交差点", actual.getDisplayName().value());
      assertTrue(actual.getIsLocationPublic().value());
      assertEquals("東京タワー", actual.getPhotoJapaneseTitle().value());
      assertEquals("Tokyo Tower", actual.getPhotoEnglishTitle().value());
      assertEquals("夕暮れの東京タワー", actual.getCaption().value());
      assertEquals(50, actual.getExifData().focalLength().value());
      assertEquals(new BigDecimal("2.8"), actual.getExifData().fValue().value());
      assertEquals(new BigDecimal("0.004"), actual.getExifData().shutterSpeed().value());
      assertEquals(100, actual.getExifData().iso().value());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：EXIF項目が0の場合、未設定扱いになること")
    void from_exifValuesZero() {
      PhotoDetailDto dto = baseDto();
      dto.setFocalLength(0);
      dto.setFValue(BigDecimal.ZERO);
      dto.setShutterSpeed(BigDecimal.ZERO);
      dto.setIso(0);

      PhotoDetailModel actual = PhotoDetailModel.from(dto, List.of());

      assertNull(actual.getExifData().focalLength());
      assertNull(actual.getExifData().fValue());
      assertNull(actual.getExifData().shutterSpeed());
      assertNull(actual.getExifData().iso());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromPhotoSaveRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：任意項目が未設定の場合、nullまたはデフォルト値が設定されること")
    void from_optionalFieldsNull() {
      PhotoSaveRequest request = new PhotoSaveRequest();
      request.setPhotoNo(null);
      request.setIsFavorite(null);
      request.setPhotoAt(null);
      request.setLocationNo(null);
      request.setAddress(null);
      request.setLatitude(null);
      request.setLongitude(null);
      request.setManagementName(null);
      request.setDisplayName(null);
      request.setIsLocationPublic(null);
      request.setImageFile(null);
      request.setImageFilePath(null);
      request.setPhotoJapaneseTitle("東京タワー");
      request.setPhotoEnglishTitle(null);
      request.setCaption(null);
      request.setDirectionKbn(DirectionEnum.HORIZONTAL);
      request.setPhotoTagRegistRequestList(null);

      PhotoDetailModel actual = PhotoDetailModel.from(request, new AccountNo(1L));

      assertNull(actual.getPhotoNo());
      assertNull(actual.getIsFavorite());
      assertNull(actual.getPhotoAt());
      assertNull(actual.getLocationNo());
      assertNull(actual.getGeoLocation().address());
      assertNull(actual.getGeoLocation().latitude());
      assertNull(actual.getGeoLocation().longitude());
      assertNull(actual.getManagementName());
      assertNull(actual.getDisplayName());
      assertNull(actual.getIsLocationPublic());
      assertNull(actual.getImageFile());
      assertEquals(Consts.STRING_EMPTY, actual.getImageFilePath().value());
      assertNull(actual.getPhotoEnglishTitle());
      assertNull(actual.getCaption());
      assertTrue(actual.getPhotoTagModelList().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：全項目が設定されている場合、そのまま設定されること")
    void from_allFieldsSet() throws Exception {
      MultipartFile imageFile =
          new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "dummy".getBytes());
      PhotoTagSaveRequest tagRequest = new PhotoTagSaveRequest();
      tagRequest.setTagJapaneseName("風景");
      tagRequest.setTagEnglishName("landscape");

      PhotoSaveRequest request = new PhotoSaveRequest();
      request.setPhotoNo(1L);
      request.setIsFavorite(true);
      request.setPhotoAt(LocalDateTime.of(2024, 1, 1, 12, 0));
      request.setLocationNo(1L);
      request.setAddress("東京都渋谷区");
      request.setLatitude(new BigDecimal("35.6812"));
      request.setLongitude(new BigDecimal("139.7671"));
      request.setManagementName("渋谷交差点_管理用");
      request.setDisplayName("渋谷スクランブル交差点");
      request.setIsLocationPublic(true);
      request.setImageFile(imageFile);
      request.setImageFilePath("path/to/image.jpg");
      request.setPhotoJapaneseTitle("東京タワー");
      request.setPhotoEnglishTitle("Tokyo Tower");
      request.setCaption("夕暮れの東京タワー");
      request.setDirectionKbn(DirectionEnum.HORIZONTAL);
      request.setPhotoTagRegistRequestList(List.of(tagRequest));

      PhotoDetailModel actual = PhotoDetailModel.from(request, new AccountNo(1L));

      assertEquals(1L, actual.getPhotoNo().value());
      assertTrue(actual.getIsFavorite().value());
      assertNotNull(actual.getPhotoAt());
      assertEquals(1L, actual.getLocationNo().value());
      assertEquals("東京都渋谷区", actual.getGeoLocation().address().value());
      assertEquals("渋谷交差点_管理用", actual.getManagementName().value());
      assertEquals("渋谷スクランブル交差点", actual.getDisplayName().value());
      assertNotNull(actual.getImageFile());
      assertEquals("Tokyo Tower", actual.getPhotoEnglishTitle().value());
      assertEquals("夕暮れの東京タワー", actual.getCaption().value());
      assertEquals(1, actual.getPhotoTagModelList().size());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromPhotoBulkSaveRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：任意項目が未設定の場合、nullまたはデフォルト値が設定されること")
    void from_optionalFieldsNull() {
      PhotoBulkSaveRequest request = new PhotoBulkSaveRequest();
      request.setImageFiles(
          List.of(
              new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "dummy".getBytes())));
      request.setPhotoAt(null);
      request.setLocationNo(null);
      request.setAddress(null);
      request.setLatitude(null);
      request.setLongitude(null);
      request.setManagementName(null);
      request.setDisplayName(null);
      request.setIsLocationPublic(null);
      request.setPhotoJapaneseTitle("東京タワー");
      request.setPhotoEnglishTitle(null);
      request.setCaption(null);
      request.setPhotoTagRegistRequestList(null);

      PhotoDetailModel actual =
          PhotoDetailModel.from(
              request, null, DirectionEnum.HORIZONTAL, ExifData.empty(), new AccountNo(1L));

      assertNull(actual.getPhotoAt());
      assertNull(actual.getLocationNo());
      assertNull(actual.getGeoLocation().address());
      assertNull(actual.getManagementName());
      assertNull(actual.getDisplayName());
      assertNull(actual.getIsLocationPublic());
      assertNull(actual.getImageFile());
      assertEquals(Consts.STRING_EMPTY, actual.getImageFilePath().value());
      assertNull(actual.getPhotoEnglishTitle());
      assertNull(actual.getCaption());
      assertTrue(actual.getPhotoTagModelList().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：全項目が設定されている場合、そのまま設定されること")
    void from_allFieldsSet() {
      MultipartFile imageFile =
          new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "dummy".getBytes());
      PhotoTagSaveRequest tagRequest = new PhotoTagSaveRequest();
      tagRequest.setTagJapaneseName("風景");
      tagRequest.setTagEnglishName("landscape");

      PhotoBulkSaveRequest request = new PhotoBulkSaveRequest();
      request.setImageFiles(List.of(imageFile));
      request.setPhotoAt(LocalDateTime.of(2024, 1, 1, 12, 0));
      request.setLocationNo(1L);
      request.setAddress("東京都渋谷区");
      request.setLatitude(new BigDecimal("35.6812"));
      request.setLongitude(new BigDecimal("139.7671"));
      request.setManagementName("渋谷交差点_管理用");
      request.setDisplayName("渋谷スクランブル交差点");
      request.setIsLocationPublic(true);
      request.setPhotoJapaneseTitle("東京タワー");
      request.setPhotoEnglishTitle("Tokyo Tower");
      request.setCaption("夕暮れの東京タワー");
      request.setPhotoTagRegistRequestList(List.of(tagRequest));

      PhotoDetailModel actual =
          PhotoDetailModel.from(
              request,
              imageFile,
              DirectionEnum.HORIZONTAL,
              ExifData.fromRawValues(50, new BigDecimal("2.8"), new BigDecimal("0.004"), 100),
              new AccountNo(1L));

      assertNotNull(actual.getPhotoAt());
      assertEquals(1L, actual.getLocationNo().value());
      assertEquals("東京都渋谷区", actual.getGeoLocation().address().value());
      assertEquals("渋谷交差点_管理用", actual.getManagementName().value());
      assertEquals("渋谷スクランブル交差点", actual.getDisplayName().value());
      assertNotNull(actual.getImageFile());
      assertEquals(Consts.STRING_EMPTY, actual.getImageFilePath().value());
      assertEquals("Tokyo Tower", actual.getPhotoEnglishTitle().value());
      assertEquals("夕暮れの東京タワー", actual.getCaption().value());
      assertEquals(DirectionEnum.HORIZONTAL, actual.getDirectionKbn());
      assertEquals(50, actual.getExifData().focalLength().value());
      assertEquals(1, actual.getPhotoTagModelList().size());
    }
  }
}
