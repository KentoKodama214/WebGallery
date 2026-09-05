package com.web.gallery.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 写真お気に入り解除時のリクエストパラメータを保持するクラス */
@Schema(description = "お気に入り解除リクエスト")
@Data
public class PhotoFavoriteDeleteRequest {
  /** お気に入り写真アカウント番号 */
  @Schema(description = "お気に入り写真のアカウント番号", example = "1")
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  private Long favoritePhotoAccountNo;

  /** お気に入り写真番号 */
  @Schema(description = "お気に入り写真の写真番号", example = "1")
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  private Long favoritePhotoNo;
}
