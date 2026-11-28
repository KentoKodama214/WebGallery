package com.web.gallery.infrastructure.persistence.dto.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PhotoGetModel;
import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.enumeration.SortPhotoEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.IsFavoriteOnly;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoListGetDtoTest {

  private PhotoGetModel.PhotoGetModelBuilder baseBuilder() {
    return PhotoGetModel.builder()
        .photoAccountNo(new AccountNo(1L))
        .directionKbn(DirectionEnum.HORIZONTAL)
        .tagList(List.of())
        .sortBy(SortPhotoEnum.PHOTO_AT)
        .limit(21)
        .offset(0);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：お気に入りのみフィルタがtrueの場合、trueが設定されること")
    void from_isFavoriteOnlyTrue() {
      PhotoGetModel model = baseBuilder().isFavoriteOnly(new IsFavoriteOnly(true)).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertTrue(actual.getIsFavoriteOnly());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：お気に入りのみフィルタがfalseの場合、falseが設定されること")
    void from_isFavoriteOnlyFalse() {
      PhotoGetModel model = baseBuilder().isFavoriteOnly(new IsFavoriteOnly(false)).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertFalse(actual.getIsFavoriteOnly());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：お気に入りのみフィルタが未設定の場合、falseが設定されること")
    void from_isFavoriteOnlyNull() {
      PhotoGetModel model = baseBuilder().isFavoriteOnly(null).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertFalse(actual.getIsFavoriteOnly());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：ログイン中のアカウント番号が未設定の場合、nullが設定されること")
    void from_accountNoNull() {
      PhotoGetModel model = baseBuilder().accountNo(null).isFavoriteOnly(null).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertNull(actual.getAccountNo());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：向き区分がNONEの場合、nullが設定されること")
    void from_directionKbnNone() {
      PhotoGetModel model =
          baseBuilder().directionKbn(DirectionEnum.NONE).isFavoriteOnly(null).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertNull(actual.getDirectionKbn());
    }

    @Test
    @Order(6)
    @DisplayName("正常系：ログイン中のアカウント番号が写真のアカウント番号と一致する場合、管理者により非公開にされた写真も取得対象に含めること")
    void from_owner() {
      PhotoGetModel model = baseBuilder().accountNo(new AccountNo(1L)).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertTrue(actual.getIncludeHiddenByAdmin());
    }

    @Test
    @Order(7)
    @DisplayName("正常系：ログイン中のアカウント番号が写真のアカウント番号と異なる場合、管理者により非公開にされた写真は取得対象に含めないこと")
    void from_notOwner() {
      PhotoGetModel model = baseBuilder().accountNo(new AccountNo(2L)).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertFalse(actual.getIncludeHiddenByAdmin());
    }

    @Test
    @Order(8)
    @DisplayName("正常系：未ログインの場合、管理者により非公開にされた写真は取得対象に含めないこと")
    void from_anonymous() {
      PhotoGetModel model = baseBuilder().accountNo(null).build();

      PhotoListGetDto actual = PhotoListGetDto.from(model);

      assertFalse(actual.getIncludeHiddenByAdmin());
    }
  }
}
