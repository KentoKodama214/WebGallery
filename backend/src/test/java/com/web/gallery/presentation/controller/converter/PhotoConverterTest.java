package com.web.gallery.presentation.controller.converter;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PhotoDeleteModel;
import com.web.gallery.application.model.photo.PhotoDetailModel;
import com.web.gallery.application.model.photo.PhotoFavoriteModel;
import com.web.gallery.application.model.photo.PhotoListGetModel;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.enumeration.SortPhotoEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.Referer;
import com.web.gallery.domain.model.photo.ExifData;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.presentation.controller.request.photo.PhotoBulkSaveRequest;
import com.web.gallery.presentation.controller.request.photo.PhotoDeleteRequest;
import com.web.gallery.presentation.controller.request.photo.PhotoFavoriteDeleteRequest;
import com.web.gallery.presentation.controller.request.photo.PhotoFavoriteRegistRequest;
import com.web.gallery.presentation.controller.request.photo.PhotoListRequest;
import com.web.gallery.presentation.controller.request.photo.PhotoSaveRequest;
import com.web.gallery.presentation.controller.request.photo.PhotoTagSaveRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
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
class PhotoConverterTest {

  private final PhotoConverter photoConverter = new PhotoConverter();

  private static final IpAddress IP_ADDRESS = new IpAddress("203.0.113.1");
  private static final Referer REFERER = new Referer("https://example.com/");

  private PhotoListRequest basePhotoListRequest() {
    PhotoListRequest request = new PhotoListRequest();
    request.setDirectionKbn(DirectionEnum.VERTICAL);
    request.setIsFavorite(true);
    request.setTagList("太陽 海");
    request.setSortBy(SortPhotoEnum.SEASON);
    request.setPageNo(2);
    request.setSearchExecuted(true);
    request.setLogInitialView(true);
    return request;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPhotoListGetModel {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void toPhotoListGetModel_allFieldsSet() {
      PhotoListRequest request = basePhotoListRequest();

      PhotoListGetModel actual =
          photoConverter.toPhotoListGetModel(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountId("aaaaaaaa"), actual.getPhotoAccountId());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertTrue(actual.getIsFavoriteOnly().value());
      assertEquals(2, actual.getTagList().size());
      assertEquals("太陽", actual.getTagList().get(0));
      assertEquals("海", actual.getTagList().get(1));
      assertEquals(SortPhotoEnum.SEASON, actual.getSortBy());
      assertEquals(2, actual.getPageNo());
      assertTrue(actual.getSearchExecuted());
      assertTrue(actual.getLogInitialView());
      assertEquals(IP_ADDRESS, actual.getIpAddress());
      assertEquals(REFERER, actual.getReferer());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ログイン中のアカウント番号が未設定（null）の場合、nullが設定されること")
    void toPhotoListGetModel_accountNoNull() {
      PhotoListRequest request = basePhotoListRequest();

      PhotoListGetModel actual =
          photoConverter.toPhotoListGetModel(request, null, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertNull(actual.getAccountNo());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：お気に入りのみフィルタ・検索実行・初回閲覧ログの各フラグが未設定の場合、falseが設定されること")
    void toPhotoListGetModel_optionalFlagsNull() {
      PhotoListRequest request = basePhotoListRequest();
      request.setIsFavorite(null);
      request.setSearchExecuted(null);
      request.setLogInitialView(null);

      PhotoListGetModel actual =
          photoConverter.toPhotoListGetModel(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertFalse(actual.getIsFavoriteOnly().value());
      assertFalse(actual.getSearchExecuted());
      assertFalse(actual.getLogInitialView());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：タグリストが未設定（null）の場合、空リストが設定されること")
    void toPhotoListGetModel_tagListNull() {
      PhotoListRequest request = basePhotoListRequest();
      request.setTagList(null);

      PhotoListGetModel actual =
          photoConverter.toPhotoListGetModel(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertTrue(actual.getTagList().isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：タグリストの全角スペース区切りが半角スペースとして分割されること")
    void toPhotoListGetModel_tagListFullSpaceSeparator() {
      PhotoListRequest request = basePhotoListRequest();
      request.setTagList("太陽　海");

      PhotoListGetModel actual =
          photoConverter.toPhotoListGetModel(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(2, actual.getTagList().size());
      assertEquals("太陽", actual.getTagList().get(0));
      assertEquals("海", actual.getTagList().get(1));
    }

    @Test
    @Order(6)
    @DisplayName("正常系：連続した空白による空文字トークンが除外されること")
    void toPhotoListGetModel_tagListIgnoresBlankTokens() {
      PhotoListRequest request = basePhotoListRequest();
      request.setTagList("太陽" + "　".repeat(5) + "海");

      PhotoListGetModel actual =
          photoConverter.toPhotoListGetModel(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(2, actual.getTagList().size());
    }

    @Test
    @Order(7)
    @DisplayName("正常系：タグ指定数が上限（20件）を超える場合、上限件数までに切り詰められること")
    void toPhotoListGetModel_tagListExceedsMaxSize() {
      PhotoListRequest request = basePhotoListRequest();
      request.setTagList(String.join(" ", Collections.nCopies(25, "tag")));

      PhotoListGetModel actual =
          photoConverter.toPhotoListGetModel(request, 1L, "aaaaaaaa", IP_ADDRESS, REFERER);

      assertEquals(20, actual.getTagList().size());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPhotoDeleteModel {
    @Test
    @Order(1)
    @DisplayName("正常系：リクエストとアカウント番号からModelが生成されること")
    void toPhotoDeleteModel_success() {
      PhotoDeleteRequest request = new PhotoDeleteRequest();
      request.setPhotoNo(1L);
      request.setImageFilePath("1/1-abc.jpg");

      PhotoDeleteModel actual = photoConverter.toPhotoDeleteModel(request, new AccountNo(2L));

      assertEquals(2L, actual.getAccountNo().value());
      assertEquals(1L, actual.getPhotoNo().value());
      assertEquals("1/1-abc.jpg", actual.getImageFilePath().value());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPhotoFavoriteModelForRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：登録リクエストの各項目が値オブジェクトに変換されて設定されること")
    void toPhotoFavoriteModel_success() {
      PhotoFavoriteRegistRequest request = new PhotoFavoriteRegistRequest();
      request.setFavoritePhotoAccountNo(2L);
      request.setFavoritePhotoNo(3L);

      PhotoFavoriteModel actual = photoConverter.toPhotoFavoriteModel(request, 1L);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountNo(2L), actual.getFavoritePhotoAccountNo());
      assertEquals(new PhotoNo(3L), actual.getFavoritePhotoNo());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPhotoFavoriteModelForDelete {
    @Test
    @Order(1)
    @DisplayName("正常系：解除リクエストの各項目が値オブジェクトに変換されて設定されること")
    void toPhotoFavoriteModel_success() {
      PhotoFavoriteDeleteRequest request = new PhotoFavoriteDeleteRequest();
      request.setFavoritePhotoAccountNo(2L);
      request.setFavoritePhotoNo(3L);

      PhotoFavoriteModel actual = photoConverter.toPhotoFavoriteModel(request, 1L);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountNo(2L), actual.getFavoritePhotoAccountNo());
      assertEquals(new PhotoNo(3L), actual.getFavoritePhotoNo());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPhotoDetailModelForRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：タグ登録リクエストが値オブジェクトへ変換されてそのまま反映されること")
    void toPhotoDetailModelForRegist_tagAllFieldsSet() {
      PhotoTagSaveRequest tagRequest = new PhotoTagSaveRequest();
      tagRequest.setTagJapaneseName("風景");
      tagRequest.setTagEnglishName("landscape");
      PhotoSaveRequest request = new PhotoSaveRequest();
      request.setImageFilePath("1/1-abc.jpg");
      request.setPhotoTagRegistRequestList(List.of(tagRequest));

      PhotoDetailModel actual =
          photoConverter.toPhotoDetailModelForRegist(request, new AccountNo(1L));

      assertEquals(1, actual.getPhotoTagModelList().size());
      assertEquals(new AccountNo(1L), actual.getPhotoTagModelList().get(0).getAccountNo());
      assertEquals("風景", actual.getPhotoTagModelList().get(0).getTagJapaneseName().value());
      assertEquals("landscape", actual.getPhotoTagModelList().get(0).getTagEnglishName().value());
      assertNull(actual.getPhotoTagModelList().get(0).getPhotoNo());
      assertNull(actual.getPhotoTagModelList().get(0).getTagNo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：タグ登録リクエストの英語名が未設定の場合、空文字が設定されること")
    void toPhotoDetailModelForRegist_tagEnglishNameNull() {
      PhotoTagSaveRequest tagRequest = new PhotoTagSaveRequest();
      tagRequest.setTagJapaneseName("風景");
      tagRequest.setTagEnglishName(null);
      PhotoSaveRequest request = new PhotoSaveRequest();
      request.setImageFilePath("1/1-abc.jpg");
      request.setPhotoTagRegistRequestList(List.of(tagRequest));

      PhotoDetailModel actual =
          photoConverter.toPhotoDetailModelForRegist(request, new AccountNo(1L));

      assertEquals("", actual.getPhotoTagModelList().get(0).getTagEnglishName().value());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：任意項目が未設定の場合、nullまたはデフォルト値が設定されること")
    void toPhotoDetailModelForRegist_optionalFieldsNull() {
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

      PhotoDetailModel actual =
          photoConverter.toPhotoDetailModelForRegist(request, new AccountNo(1L));

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
    @Order(4)
    @DisplayName("正常系：全項目が設定されている場合、そのまま設定されること")
    void toPhotoDetailModelForRegist_allFieldsSet() {
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

      PhotoDetailModel actual =
          photoConverter.toPhotoDetailModelForRegist(request, new AccountNo(1L));

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
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toPhotoDetailModelForBulkRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：任意項目が未設定の場合、nullまたはデフォルト値が設定されること")
    void toPhotoDetailModelForBulkRegist_optionalFieldsNull() {
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
          photoConverter.toPhotoDetailModelForBulkRegist(
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
    void toPhotoDetailModelForBulkRegist_allFieldsSet() {
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
          photoConverter.toPhotoDetailModelForBulkRegist(
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
