package com.web.gallery.controller.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.web.gallery.constant.Consts;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Arrays;
import lombok.Data;

/** 写真一覧表示時のリクエストパラメータを保持するクラス */
@Schema(description = "写真一覧リクエスト")
@Data
public class PhotoListRequest {
  /**
   * 向き区分コード
   *
   * <p>{@link DirectionEnum}
   */
  @Schema(description = "向き区分（未選択/縦/横/正方形）")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  private DirectionEnum directionKbn = DirectionEnum.NONE;

  /** お気に入り写真のみ */
  @Schema(description = "お気に入り写真のみ表示するか", example = "false")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  private Boolean isFavorite = Boolean.FALSE;

  /** タグリスト */
  @Schema(description = "タグリスト（カンマ区切り）", example = "風景,東京")
  private String tagList;

  /**
   * 並び順
   *
   * <p>{@link SortPhotoEnum}
   */
  @Schema(description = "並び順（photoAt: 撮影日順, favorite: お気に入り数順, season: 季節順）")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  private SortPhotoEnum sortBy = SortPhotoEnum.PHOTO_AT;

  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  // 深いオフセットページングによる負荷増大を抑えるための上限（20件/ページ換算で20万件相当）
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;

  /**
   * タグリストの指定数が上限以下かどうかを検証する
   *
   * <p>タグ1件につき絞り込み用の相関サブクエリが1つ追加されるため、大量指定によるクエリ負荷増大を防ぐ
   *
   * @return 上限以下の場合はtrue
   */
  @Schema(hidden = true)
  @AssertTrue(message = "{validation.photo.tagList.maxSize}")
  public boolean isTagListSizeValid() {
    if (tagList == null || tagList.isBlank()) {
      return true;
    }
    long tagCount =
        Arrays.stream(
                tagList.replace(Consts.FULL_SPACE, Consts.HALF_SPACE).split(Consts.HALF_SPACE))
            .filter(tag -> !tag.isEmpty())
            .count();
    return tagCount <= Consts.TAG_LIST_MAX_SIZE;
  }
}
