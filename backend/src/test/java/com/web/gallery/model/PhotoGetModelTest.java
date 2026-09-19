package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoGetModelTest {

  private PhotoListGetModel.PhotoListGetModelBuilder baseBuilder() {
    return PhotoListGetModel.builder()
        .accountNo(new AccountNo(1L))
        .photoAccountId(null)
        .directionKbn(DirectionEnum.VERTICAL)
        .isFavoriteOnly(new IsFavoriteOnly(true))
        .tagList(List.of("風景"))
        .sortBy(SortPhotoEnum.PHOTO_AT)
        .pageNo(1)
        .searchExecuted(false)
        .logInitialView(false)
        .ipAddress(new IpAddress("203.0.113.1"))
        .referer(new Referer(""));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：1ページ目の場合、offsetが0になり、limitは表示件数+1になること")
    void of_firstPage() {
      PhotoListGetModel listGetModel = baseBuilder().pageNo(1).build();

      PhotoGetModel actual = PhotoGetModel.of(listGetModel, new AccountNo(9L), 20);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountNo(9L), actual.getPhotoAccountNo());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertEquals(new IsFavoriteOnly(true), actual.getIsFavoriteOnly());
      assertEquals(List.of("風景"), actual.getTagList());
      assertEquals(SortPhotoEnum.PHOTO_AT, actual.getSortBy());
      assertEquals(21, actual.getLimit());
      assertEquals(0, actual.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：2ページ目の場合、offsetが表示件数分進むこと")
    void of_secondPage() {
      PhotoListGetModel listGetModel = baseBuilder().pageNo(2).build();

      PhotoGetModel actual = PhotoGetModel.of(listGetModel, new AccountNo(9L), 20);

      assertEquals(20, actual.getOffset());
    }
  }
}
