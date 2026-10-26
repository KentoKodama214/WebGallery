package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.domain.enumeration.DirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオ用の写真検索結果の写真1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ用の写真検索結果の写真")
@Data
@Builder
public class PortfolioPhotoSearchItemResponse {
  /** 写真番号 */
  @Schema(description = "写真番号", example = "1")
  private Long photoNo;

  /** 写真のタイトル（日本語） */
  @Schema(description = "写真のタイトル（日本語）", example = "金閣寺")
  private String photoJapaneseTitle;

  /** 画像の署名付きURL */
  @Schema(description = "画像の署名付きURL")
  private String imageFilePath;

  /**
   * 向き区分
   *
   * <p>{@link DirectionEnum}
   */
  @Schema(description = "向き区分（縦/横/正方形）")
  private DirectionEnum directionKbn;

  /**
   * PortfolioPhotoModelからPortfolioPhotoSearchItemResponseを生成する
   *
   * @param model {@link PortfolioPhotoModel}
   * @return {@link PortfolioPhotoSearchItemResponse}
   */
  public static PortfolioPhotoSearchItemResponse from(PortfolioPhotoModel model) {
    return PortfolioPhotoSearchItemResponse.builder()
        .photoNo(model.getPhotoNo().value())
        .photoJapaneseTitle(model.getPhotoJapaneseTitle().value())
        .imageFilePath(model.getImageFilePath().value())
        .directionKbn(model.getDirectionKbn())
        .build();
  }
}
