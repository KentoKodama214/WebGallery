package com.web.gallery.application.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.Caption;
import com.web.gallery.domain.model.photo.FavoriteCount;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsFavorite;
import com.web.gallery.domain.model.photo.PhotoAt;
import com.web.gallery.domain.model.photo.PhotoNo;
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
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class PhotoModelListTest {

  private PhotoModel createPhotoModel(
      Long photoNo, DirectionEnum directionKbn, Boolean isFavorite) {
    return PhotoModel.builder()
        .accountNo(new AccountNo(1L))
        .photoNo(new PhotoNo(photoNo))
        .favoriteCount(new FavoriteCount(1))
        .isFavorite(new IsFavorite(isFavorite))
        .photoAt(new PhotoAt(OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0))))
        .imageFilePath(
            new ImageFilePath("https://localhost:8080/image/aaaaaaaa/DSC" + photoNo + ".jpg"))
        .caption(new Caption("キャプション" + photoNo))
        .directionKbn(directionKbn)
        .photoTagModelList(PhotoTagModelList.empty())
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class sorted {
    @Test
    @Order(1)
    @DisplayName("正常系：指定のComparatorでソートされること")
    void sorted_success() {
      PhotoModelList photoModelList =
          PhotoModelList.of(
              List.of(
                  createPhotoModel(1L, DirectionEnum.VERTICAL, false),
                  createPhotoModel(2L, DirectionEnum.HORIZONTAL, false)));

      PhotoModelList actual =
          photoModelList.sorted(
              Comparator.comparing(
                  (PhotoModel photoModel) -> photoModel.getPhotoNo().value(),
                  Comparator.reverseOrder()));

      assertEquals(2, actual.size());
      assertEquals(new PhotoNo(2L), actual.get(0).getPhotoNo());
      assertEquals(new PhotoNo(1L), actual.get(1).getPhotoNo());
    }
  }
}
