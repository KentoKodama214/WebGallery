package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.dto.PhotoDetailGetDto;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.dto.PhotoListGetDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.mapper.PhotoDetailMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoPageModel;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoDetailRepositoryImplTest {
  @InjectMocks private PhotoDetailRepositoryImpl photoDetailRepositoryImpl;

  @Mock private PhotoTagMstMapper photoTagMstMapper;

  @Mock private PhotoDetailMapper photoDetailMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPhotoList {
    @Test
    @Order(1)
    @DisplayName("正常系：写真が0件の場合")
    void getPhotoList_photo_not_found() {
      PhotoGetModel photoSelectModel =
          PhotoGetModel.builder()
              .accountNo(new AccountNo(1L))
              .photoAccountNo(new AccountNo(1L))
              .directionKbn(DirectionEnum.NONE)
              .isFavoriteOnly(new IsFavoriteOnly(false))
              .tagList(List.of())
              .sortBy(SortPhotoEnum.PHOTO_AT)
              .limit(6)
              .offset(0)
              .build();

      List<PhotoDto> photoDtoList = new ArrayList<PhotoDto>();

      ArgumentCaptor<PhotoListGetDto> photoListGetDtoCaptor =
          ArgumentCaptor.forClass(PhotoListGetDto.class);
      doReturn(photoDtoList).when(photoDetailMapper).getPhotoList(photoListGetDtoCaptor.capture());

      PhotoPageModel actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

      assertTrue(actual.getPhotoModelList().isEmpty());
      assertTrue(actual.getIsLast());

      PhotoListGetDto photoListGetDtoCapture = photoListGetDtoCaptor.getValue();
      assertEquals(1L, photoListGetDtoCapture.getAccountNo());
      assertEquals(1L, photoListGetDtoCapture.getPhotoAccountNo());
      assertNull(photoListGetDtoCapture.getDirectionKbn());
      assertFalse(photoListGetDtoCapture.getIsFavoriteOnly());
      assertEquals(List.of(), photoListGetDtoCapture.getTagList());
      assertEquals("PHOTO_AT", photoListGetDtoCapture.getSortBy());
      assertEquals(6, photoListGetDtoCapture.getLimit());
      assertEquals(0, photoListGetDtoCapture.getOffset());

      verify(photoTagMstMapper, times(0)).select(any(PhotoTagMstCondition.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真が1件以上、写真タグが0件の場合")
    void getPhotoList_photoTag_not_found() {
      PhotoGetModel photoSelectModel =
          PhotoGetModel.builder()
              .accountNo(new AccountNo(1L))
              .photoAccountNo(new AccountNo(1L))
              .directionKbn(DirectionEnum.NONE)
              .isFavoriteOnly(new IsFavoriteOnly(false))
              .tagList(List.of())
              .sortBy(SortPhotoEnum.PHOTO_AT)
              .limit(6)
              .offset(0)
              .build();

      List<PhotoDto> photoDtoList = new ArrayList<PhotoDto>();
      PhotoDto photoDto1 = new PhotoDto();
      photoDto1.setAccountNo(1L);
      photoDto1.setPhotoNo(1L);
      photoDto1.setFavoriteCount(1);
      photoDto1.setIsFavorite(false);
      photoDto1.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      photoDto1.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
      photoDto1.setCaption("キャプション1");
      photoDto1.setDirectionKbn(DirectionEnum.VERTICAL);
      photoDtoList.add(photoDto1);

      PhotoDto photoDto2 = new PhotoDto();
      photoDto2.setAccountNo(1L);
      photoDto2.setPhotoNo(2L);
      photoDto2.setFavoriteCount(2);
      photoDto2.setIsFavorite(true);
      photoDto2.setPhotoAt(OffsetDateTime.of(2000, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      photoDto2.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg");
      photoDto2.setCaption("キャプション2");
      photoDto2.setDirectionKbn(DirectionEnum.HORIZONTAL);
      photoDtoList.add(photoDto2);

      ArgumentCaptor<PhotoListGetDto> photoListGetDtoCaptor =
          ArgumentCaptor.forClass(PhotoListGetDto.class);
      doReturn(photoDtoList).when(photoDetailMapper).getPhotoList(photoListGetDtoCaptor.capture());

      List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();

      ArgumentCaptor<PhotoTagMstCondition> photoTagMstCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());

      PhotoPageModel actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
      assertTrue(actual.getIsLast());

      assertEquals(new AccountNo(1L), actual.getPhotoModelList().get(0).getAccountNo());
      assertEquals(1L, actual.getPhotoModelList().get(0).getPhotoNo().value());
      assertEquals(1, actual.getPhotoModelList().get(0).getFavoriteCount().value());
      assertFalse(actual.getPhotoModelList().get(0).getIsFavorite().value());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, Consts.JST),
          actual.getPhotoModelList().get(0).getPhotoAt().value());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC111.jpg",
          actual.getPhotoModelList().get(0).getImageFilePath().value());
      assertEquals("キャプション1", actual.getPhotoModelList().get(0).getCaption().value());
      assertEquals(DirectionEnum.VERTICAL, actual.getPhotoModelList().get(0).getDirectionKbn());

      assertEquals(new AccountNo(1L), actual.getPhotoModelList().get(1).getAccountNo());
      assertEquals(2L, actual.getPhotoModelList().get(1).getPhotoNo().value());
      assertEquals(2, actual.getPhotoModelList().get(1).getFavoriteCount().value());
      assertTrue(actual.getPhotoModelList().get(1).getIsFavorite().value());
      assertEquals(
          OffsetDateTime.of(2000, 2, 1, 9, 0, 0, 0, Consts.JST),
          actual.getPhotoModelList().get(1).getPhotoAt().value());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC222.jpg",
          actual.getPhotoModelList().get(1).getImageFilePath().value());
      assertEquals("キャプション2", actual.getPhotoModelList().get(1).getCaption().value());
      assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoModelList().get(1).getDirectionKbn());

      PhotoListGetDto photoListGetDtoCapture = photoListGetDtoCaptor.getValue();
      assertEquals(1L, photoListGetDtoCapture.getAccountNo());
      assertEquals(1L, photoListGetDtoCapture.getPhotoAccountNo());
      assertNull(photoListGetDtoCapture.getDirectionKbn());
      assertFalse(photoListGetDtoCapture.getIsFavoriteOnly());
      assertEquals(List.of(), photoListGetDtoCapture.getTagList());
      assertEquals("PHOTO_AT", photoListGetDtoCapture.getSortBy());

      PhotoTagMstCondition photoTagMstCapture = photoTagMstCaptor.getValue();
      assertEquals(1L, photoTagMstCapture.getAccountNo());
      assertEquals(List.of(1L, 2L), photoTagMstCapture.getPhotoNoList());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：写真が1件以上、写真タグが1件以上の場合")
    void getPhotoList_photoTag_found() {
      PhotoGetModel photoSelectModel =
          PhotoGetModel.builder()
              .accountNo(new AccountNo(1L))
              .photoAccountNo(new AccountNo(1L))
              .directionKbn(DirectionEnum.NONE)
              .isFavoriteOnly(new IsFavoriteOnly(false))
              .tagList(List.of())
              .sortBy(SortPhotoEnum.PHOTO_AT)
              .limit(6)
              .offset(0)
              .build();

      List<PhotoDto> photoDtoList = new ArrayList<PhotoDto>();
      PhotoDto photoDto1 = new PhotoDto();
      photoDto1.setAccountNo(1L);
      photoDto1.setPhotoNo(1L);
      photoDto1.setFavoriteCount(1);
      photoDto1.setIsFavorite(false);
      photoDto1.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      photoDto1.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
      photoDto1.setCaption("キャプション1");
      photoDto1.setDirectionKbn(DirectionEnum.VERTICAL);
      photoDtoList.add(photoDto1);

      PhotoDto photoDto2 = new PhotoDto();
      photoDto2.setAccountNo(1L);
      photoDto2.setPhotoNo(2L);
      photoDto2.setFavoriteCount(2);
      photoDto2.setIsFavorite(true);
      photoDto2.setPhotoAt(OffsetDateTime.of(2000, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      photoDto2.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg");
      photoDto2.setCaption("キャプション2");
      photoDto2.setDirectionKbn(DirectionEnum.HORIZONTAL);
      photoDtoList.add(photoDto2);

      ArgumentCaptor<PhotoListGetDto> photoListGetDtoCaptor =
          ArgumentCaptor.forClass(PhotoListGetDto.class);
      doReturn(photoDtoList).when(photoDetailMapper).getPhotoList(photoListGetDtoCaptor.capture());

      List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();
      photoTagMstList.add(
          PhotoTagMst.builder()
              .accountNo(1L)
              .photoNo(1L)
              .tagNo(1L)
              .tagJapaneseName("太陽")
              .tagEnglishName("sun")
              .build());
      photoTagMstList.add(
          PhotoTagMst.builder()
              .accountNo(1L)
              .photoNo(2L)
              .tagNo(1L)
              .tagJapaneseName("海")
              .tagEnglishName("sea")
              .build());

      ArgumentCaptor<PhotoTagMstCondition> photoTagMstCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());

      PhotoPageModel actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);
      assertTrue(actual.getIsLast());

      assertEquals(new AccountNo(1L), actual.getPhotoModelList().get(0).getAccountNo());
      assertEquals(1L, actual.getPhotoModelList().get(0).getPhotoNo().value());
      assertEquals(1, actual.getPhotoModelList().get(0).getFavoriteCount().value());
      assertFalse(actual.getPhotoModelList().get(0).getIsFavorite().value());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, Consts.JST),
          actual.getPhotoModelList().get(0).getPhotoAt().value());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC111.jpg",
          actual.getPhotoModelList().get(0).getImageFilePath().value());
      assertEquals("キャプション1", actual.getPhotoModelList().get(0).getCaption().value());
      assertEquals(DirectionEnum.VERTICAL, actual.getPhotoModelList().get(0).getDirectionKbn());
      assertEquals(1, actual.getPhotoModelList().get(0).getPhotoTagModelList().size());
      assertEquals(
          1L, actual.getPhotoModelList().get(0).getPhotoTagModelList().get(0).getTagNo().value());
      assertEquals(
          "太陽",
          actual
              .getPhotoModelList()
              .get(0)
              .getPhotoTagModelList()
              .get(0)
              .getTagJapaneseName()
              .value());
      assertEquals(
          "sun",
          actual
              .getPhotoModelList()
              .get(0)
              .getPhotoTagModelList()
              .get(0)
              .getTagEnglishName()
              .value());

      assertEquals(new AccountNo(1L), actual.getPhotoModelList().get(1).getAccountNo());
      assertEquals(2L, actual.getPhotoModelList().get(1).getPhotoNo().value());
      assertEquals(2, actual.getPhotoModelList().get(1).getFavoriteCount().value());
      assertTrue(actual.getPhotoModelList().get(1).getIsFavorite().value());
      assertEquals(
          OffsetDateTime.of(2000, 2, 1, 9, 0, 0, 0, Consts.JST),
          actual.getPhotoModelList().get(1).getPhotoAt().value());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC222.jpg",
          actual.getPhotoModelList().get(1).getImageFilePath().value());
      assertEquals("キャプション2", actual.getPhotoModelList().get(1).getCaption().value());
      assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoModelList().get(1).getDirectionKbn());
      assertEquals(1, actual.getPhotoModelList().get(1).getPhotoTagModelList().size());
      assertEquals(
          1L, actual.getPhotoModelList().get(1).getPhotoTagModelList().get(0).getTagNo().value());
      assertEquals(
          "海",
          actual
              .getPhotoModelList()
              .get(1)
              .getPhotoTagModelList()
              .get(0)
              .getTagJapaneseName()
              .value());
      assertEquals(
          "sea",
          actual
              .getPhotoModelList()
              .get(1)
              .getPhotoTagModelList()
              .get(0)
              .getTagEnglishName()
              .value());

      PhotoListGetDto photoListGetDtoCapture = photoListGetDtoCaptor.getValue();
      assertEquals(1L, photoListGetDtoCapture.getAccountNo());
      assertEquals(1L, photoListGetDtoCapture.getPhotoAccountNo());
      assertNull(photoListGetDtoCapture.getDirectionKbn());
      assertFalse(photoListGetDtoCapture.getIsFavoriteOnly());
      assertEquals(List.of(), photoListGetDtoCapture.getTagList());
      assertEquals("PHOTO_AT", photoListGetDtoCapture.getSortBy());

      PhotoTagMstCondition photoTagMstCapture = photoTagMstCaptor.getValue();
      assertEquals(1L, photoTagMstCapture.getAccountNo());
      assertEquals(List.of(1L, 2L), photoTagMstCapture.getPhotoNoList());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：取得件数が上限を超える場合、表示件数分に切り詰められ、最後のページでないと判定されること")
    void getPhotoList_pagination_trims_when_more_results_exist() {
      // 1ページあたりの表示件数を1件と仮定し、limitはその1件多い2を指定する
      PhotoGetModel photoSelectModel =
          PhotoGetModel.builder()
              .accountNo(new AccountNo(1L))
              .photoAccountNo(new AccountNo(1L))
              .directionKbn(DirectionEnum.NONE)
              .isFavoriteOnly(new IsFavoriteOnly(false))
              .tagList(List.of())
              .sortBy(SortPhotoEnum.PHOTO_AT)
              .limit(2)
              .offset(0)
              .build();

      List<PhotoDto> photoDtoList = new ArrayList<PhotoDto>();
      PhotoDto photoDto1 = new PhotoDto();
      photoDto1.setAccountNo(1L);
      photoDto1.setPhotoNo(1L);
      photoDto1.setFavoriteCount(0);
      photoDto1.setIsFavorite(false);
      photoDto1.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      photoDto1.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
      photoDto1.setCaption("キャプション1");
      photoDto1.setDirectionKbn(DirectionEnum.VERTICAL);
      photoDtoList.add(photoDto1);

      PhotoDto photoDto2 = new PhotoDto();
      photoDto2.setAccountNo(1L);
      photoDto2.setPhotoNo(2L);
      photoDto2.setFavoriteCount(0);
      photoDto2.setIsFavorite(false);
      photoDto2.setPhotoAt(OffsetDateTime.of(2000, 2, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      photoDto2.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg");
      photoDto2.setCaption("キャプション2");
      photoDto2.setDirectionKbn(DirectionEnum.HORIZONTAL);
      photoDtoList.add(photoDto2);

      doReturn(photoDtoList).when(photoDetailMapper).getPhotoList(any(PhotoListGetDto.class));

      ArgumentCaptor<PhotoTagMstCondition> photoTagMstCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(new ArrayList<PhotoTagMst>())
          .when(photoTagMstMapper)
          .select(photoTagMstCaptor.capture());

      PhotoPageModel actual = photoDetailRepositoryImpl.getPhotoList(photoSelectModel);

      // limit(2)件取得できたため、まだ後続のページが存在すると判定されること
      assertFalse(actual.getIsLast());
      // 表示件数（limit - 1 = 1件）に切り詰められること
      assertEquals(1, actual.getPhotoModelList().size());
      assertEquals(1L, actual.getPhotoModelList().get(0).getPhotoNo().value());

      // タグ取得の絞り込み対象も、切り詰め後の写真番号のみになること
      assertEquals(List.of(1L), photoTagMstCaptor.getValue().getPhotoNoList());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPhotoDetail {
    @Test
    @Order(1)
    @DisplayName("正常系：写真のメタデータがデフォルト値、写真タグが0件の場合")
    void getPhotoDetail_photoTag_default_value_not_found() throws GalleryException {
      PhotoDetailSearchModel photoDetailSearchModel =
          PhotoDetailSearchModel.builder()
              .accountNo(new AccountNo(1L))
              .photoAccountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .build();

      PhotoDetailDto photoDetailDto = new PhotoDetailDto();
      photoDetailDto.setAccountNo(1L);
      photoDetailDto.setPhotoNo(1L);
      photoDetailDto.setIsFavorite(false);
      photoDetailDto.setPhotoAt(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)));
      photoDetailDto.setLocationNo(0L);
      photoDetailDto.setAddress(null);
      photoDetailDto.setLatitude(null);
      photoDetailDto.setLongitude(null);
      photoDetailDto.setLocationName(null);
      photoDetailDto.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
      photoDetailDto.setPhotoJapaneseTitle("");
      photoDetailDto.setPhotoEnglishTitle("");
      photoDetailDto.setCaption("");
      photoDetailDto.setDirectionKbn(DirectionEnum.VERTICAL);
      photoDetailDto.setFocalLength(0);
      photoDetailDto.setFValue(BigDecimal.ZERO);
      photoDetailDto.setShutterSpeed(BigDecimal.ZERO);
      photoDetailDto.setIso(0);

      ArgumentCaptor<PhotoDetailGetDto> photoDetailGetDtoCaptor =
          ArgumentCaptor.forClass(PhotoDetailGetDto.class);
      doReturn(photoDetailDto)
          .when(photoDetailMapper)
          .getPhotoDetail(photoDetailGetDtoCaptor.capture());

      List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();

      ArgumentCaptor<PhotoTagMstCondition> photoTagMstCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());

      PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailSearchModel);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(1L, actual.getPhotoNo().value());
      assertFalse(actual.getIsFavorite().value());
      assertNull(actual.getPhotoAt());
      assertEquals(0L, actual.getLocationNo().value());
      assertNull(actual.getGeoLocation().address());
      assertNull(actual.getGeoLocation().latitude());
      assertNull(actual.getGeoLocation().longitude());
      assertNull(actual.getLocationName());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.getImageFilePath().value());
      assertEquals("", actual.getPhotoJapaneseTitle().value());
      assertEquals("", actual.getPhotoEnglishTitle().value());
      assertEquals("", actual.getCaption().value());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertNull(actual.getExifData().focalLength());
      assertNull(actual.getExifData().fValue());
      assertNull(actual.getExifData().shutterSpeed());
      assertNull(actual.getExifData().iso());
      assertEquals(0, actual.getPhotoTagModelList().size());

      PhotoDetailGetDto photoDetailGetDtoCapture = photoDetailGetDtoCaptor.getValue();
      assertEquals(1L, photoDetailGetDtoCapture.getAccountNo());
      assertEquals(1L, photoDetailGetDtoCapture.getPhotoAccountNo());
      assertEquals(1L, photoDetailGetDtoCapture.getPhotoNo());

      PhotoTagMstCondition photoTagMstCapture = photoTagMstCaptor.getValue();
      assertEquals(1L, photoTagMstCapture.getAccountNo());
      assertEquals(1L, photoTagMstCapture.getPhotoNo());
      assertNull(photoTagMstCapture.getTagNo());
      assertNull(photoTagMstCapture.getTagJapaneseName());
      assertNull(photoTagMstCapture.getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真のメタデータがデフォルト値でない場、写真タグが1件以上の場合")
    void getPhotoDetail_not_default_value_photoTag_found() throws GalleryException {
      PhotoDetailSearchModel photoDetailSearchModel =
          PhotoDetailSearchModel.builder()
              .accountNo(new AccountNo(1L))
              .photoAccountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .build();

      PhotoDetailDto photoDetailDto = new PhotoDetailDto();
      photoDetailDto.setAccountNo(1L);
      photoDetailDto.setPhotoNo(1L);
      photoDetailDto.setIsFavorite(false);
      photoDetailDto.setPhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0)));
      photoDetailDto.setLocationNo(1L);
      photoDetailDto.setAddress("住所");
      photoDetailDto.setLatitude(BigDecimal.valueOf(38.000));
      photoDetailDto.setLongitude(BigDecimal.valueOf(115.000));
      photoDetailDto.setLocationName("富士山");
      photoDetailDto.setImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg");
      photoDetailDto.setPhotoJapaneseTitle("タイトル");
      photoDetailDto.setPhotoEnglishTitle("title");
      photoDetailDto.setCaption("キャプション");
      photoDetailDto.setDirectionKbn(DirectionEnum.VERTICAL);
      photoDetailDto.setFocalLength(24);
      photoDetailDto.setFValue(BigDecimal.valueOf(2.8));
      photoDetailDto.setShutterSpeed(BigDecimal.valueOf(0.01));
      photoDetailDto.setIso(100);

      ArgumentCaptor<PhotoDetailGetDto> photoDetailGetDtoCaptor =
          ArgumentCaptor.forClass(PhotoDetailGetDto.class);
      doReturn(photoDetailDto)
          .when(photoDetailMapper)
          .getPhotoDetail(photoDetailGetDtoCaptor.capture());

      List<PhotoTagMst> photoTagMstList = new ArrayList<PhotoTagMst>();
      photoTagMstList.add(
          PhotoTagMst.builder()
              .accountNo(1L)
              .photoNo(1L)
              .tagNo(1L)
              .tagJapaneseName("太陽")
              .tagEnglishName("sun")
              .build());
      photoTagMstList.add(
          PhotoTagMst.builder()
              .accountNo(1L)
              .photoNo(2L)
              .tagNo(1L)
              .tagJapaneseName("海")
              .tagEnglishName("sea")
              .build());

      ArgumentCaptor<PhotoTagMstCondition> photoTagMstCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(photoTagMstList).when(photoTagMstMapper).select(photoTagMstCaptor.capture());

      PhotoDetailModel actual = photoDetailRepositoryImpl.getPhotoDetail(photoDetailSearchModel);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(1L, actual.getPhotoNo().value());
      assertFalse(actual.getIsFavorite().value());
      assertEquals(
          OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, Consts.JST), actual.getPhotoAt().value());
      assertEquals(1L, actual.getLocationNo().value());
      assertEquals("住所", actual.getGeoLocation().address().value());
      assertEquals(
          0, BigDecimal.valueOf(38.000).compareTo(actual.getGeoLocation().latitude().value()));
      assertEquals(
          0, BigDecimal.valueOf(115.000).compareTo(actual.getGeoLocation().longitude().value()));
      assertEquals("富士山", actual.getLocationName().value());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC111.jpg", actual.getImageFilePath().value());
      assertEquals("タイトル", actual.getPhotoJapaneseTitle().value());
      assertEquals("title", actual.getPhotoEnglishTitle().value());
      assertEquals("キャプション", actual.getCaption().value());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertEquals(2, actual.getPhotoTagModelList().size());
      assertEquals(1L, actual.getPhotoTagModelList().get(0).getTagNo().value());
      assertEquals("太陽", actual.getPhotoTagModelList().get(0).getTagJapaneseName().value());
      assertEquals("sun", actual.getPhotoTagModelList().get(0).getTagEnglishName().value());
      assertEquals("海", actual.getPhotoTagModelList().get(1).getTagJapaneseName().value());
      assertEquals("sea", actual.getPhotoTagModelList().get(1).getTagEnglishName().value());

      PhotoDetailGetDto photoDetailGetDtoCapture = photoDetailGetDtoCaptor.getValue();
      assertEquals(1L, photoDetailGetDtoCapture.getAccountNo());
      assertEquals(1L, photoDetailGetDtoCapture.getPhotoAccountNo());
      assertEquals(1L, photoDetailGetDtoCapture.getPhotoNo());

      PhotoTagMstCondition photoTagMstCapture = photoTagMstCaptor.getValue();
      assertEquals(1L, photoTagMstCapture.getAccountNo());
      assertEquals(1L, photoTagMstCapture.getPhotoNo());
      assertNull(photoTagMstCapture.getTagNo());
      assertNull(photoTagMstCapture.getTagJapaneseName());
      assertNull(photoTagMstCapture.getTagEnglishName());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：PhotoNotFoundExceptionをthrowする")
    void getPhotoDetail_PhotoNotFoundException() {
      PhotoDetailSearchModel photoDetailSearchModel =
          PhotoDetailSearchModel.builder()
              .accountNo(new AccountNo(1L))
              .photoAccountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(1L))
              .build();

      ArgumentCaptor<PhotoDetailGetDto> photoDetailGetDtoCaptor =
          ArgumentCaptor.forClass(PhotoDetailGetDto.class);
      doReturn(null).when(photoDetailMapper).getPhotoDetail(photoDetailGetDtoCaptor.capture());

      assertThrows(
          PhotoNotFoundException.class,
          () -> photoDetailRepositoryImpl.getPhotoDetail(photoDetailSearchModel));
      verify(photoTagMstMapper, times(0)).select(any(PhotoTagMstCondition.class));
    }
  }
}
