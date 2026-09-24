package com.web.gallery.controller.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.model.photo.PhotoModel;
import com.web.gallery.model.photo.PhotoModelList;
import com.web.gallery.model.photo.PhotoPageModel;
import com.web.gallery.model.photo.PhotoTagModelList;
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
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoListGetResponseTest {

  private PhotoModelList createPhotoList() {
    List<PhotoModel> photoList = new ArrayList<PhotoModel>();
    photoList.add(
        PhotoModel.builder()
            .accountNo(new AccountNo(1L))
            .photoNo(new PhotoNo(1L))
            .favoriteCount(new FavoriteCount(1))
            .isFavorite(new IsFavorite(false))
            .photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
            .imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC111.jpg"))
            .caption(new Caption("キャプション1"))
            .directionKbn(DirectionEnum.VERTICAL)
            .photoTagModelList(PhotoTagModelList.empty())
            .build());
    photoList.add(
        PhotoModel.builder()
            .accountNo(new AccountNo(1L))
            .photoNo(new PhotoNo(2L))
            .favoriteCount(new FavoriteCount(1))
            .isFavorite(new IsFavorite(true))
            .photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
            .imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC222.jpg"))
            .caption(new Caption("キャプション2"))
            .directionKbn(DirectionEnum.HORIZONTAL)
            .photoTagModelList(PhotoTagModelList.empty())
            .build());
    photoList.add(
        PhotoModel.builder()
            .accountNo(new AccountNo(1L))
            .photoNo(new PhotoNo(3L))
            .favoriteCount(new FavoriteCount(1))
            .isFavorite(new IsFavorite(true))
            .photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
            .imageFilePath(new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC333.jpg"))
            .caption(new Caption("キャプション3"))
            .directionKbn(DirectionEnum.HORIZONTAL)
            .photoTagModelList(PhotoTagModelList.empty())
            .build());

    return PhotoModelList.of(photoList);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：DB側で絞り込み済みの写真一覧・isLastをそのままレスポンスに変換すること")
    void from_passThrough() {
      PhotoModelList photoList = createPhotoList();
      PhotoPageModel photoPageModel = PhotoPageModel.of(photoList, false);

      PhotoListGetResponse actual = PhotoListGetResponse.from(photoPageModel);
      assertFalse(actual.getIsLast());
      assertEquals(3, actual.getPhotoList().size());
      assertEquals(1L, actual.getPhotoList().get(0).getAccountNo());
      assertEquals(1L, actual.getPhotoList().get(0).getPhotoNo());
      assertFalse(actual.getPhotoList().get(0).getIsFavorite());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC111.jpg",
          actual.getPhotoList().get(0).getImageFilePath());
      assertEquals("キャプション1", actual.getPhotoList().get(0).getCaption());
      assertEquals(DirectionEnum.VERTICAL, actual.getPhotoList().get(0).getDirectionKbn());
      assertEquals(1L, actual.getPhotoList().get(1).getAccountNo());
      assertEquals(2L, actual.getPhotoList().get(1).getPhotoNo());
      assertTrue(actual.getPhotoList().get(1).getIsFavorite());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC222.jpg",
          actual.getPhotoList().get(1).getImageFilePath());
      assertEquals("キャプション2", actual.getPhotoList().get(1).getCaption());
      assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(1).getDirectionKbn());
      assertEquals(1L, actual.getPhotoList().get(2).getAccountNo());
      assertEquals(3L, actual.getPhotoList().get(2).getPhotoNo());
      assertTrue(actual.getPhotoList().get(2).getIsFavorite());
      assertEquals(
          "https://localhost:8080/image/aaaaaaaa/DSC333.jpg",
          actual.getPhotoList().get(2).getImageFilePath());
      assertEquals("キャプション3", actual.getPhotoList().get(2).getCaption());
      assertEquals(DirectionEnum.HORIZONTAL, actual.getPhotoList().get(2).getDirectionKbn());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：isLastがtrueの場合、そのままtrueとして変換されること")
    void from_isLastTrue() {
      PhotoModelList photoList = PhotoModelList.of(createPhotoList().toList().subList(0, 1));
      PhotoPageModel photoPageModel = PhotoPageModel.of(photoList, true);

      PhotoListGetResponse actual = PhotoListGetResponse.from(photoPageModel);
      assertTrue(actual.getIsLast());
      assertEquals(1, actual.getPhotoList().size());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：写真が0件の場合、空リストに変換されること")
    void from_empty() {
      PhotoPageModel photoPageModel = PhotoPageModel.of(PhotoModelList.empty(), true);

      PhotoListGetResponse actual = PhotoListGetResponse.from(photoPageModel);
      assertTrue(actual.getIsLast());
      assertTrue(actual.getPhotoList().isEmpty());
    }
  }
}
