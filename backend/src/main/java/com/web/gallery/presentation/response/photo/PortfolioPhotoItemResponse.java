package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.domain.enumeration.DirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオ詳細の写真1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ詳細の写真")
@Data
@Builder
public class PortfolioPhotoItemResponse {
  /** 写真番号 */
  @Schema(description = "写真番号", example = "1")
  private Long photoNo;

  /** 表示順 */
  @Schema(description = "表示順（1始まり）", example = "1")
  private Integer displayOrder;

  /** 写真単位のテンプレート設定 */
  @Schema(description = "写真単位のテンプレート設定（項目はテンプレートごとに異なる）")
  private Map<String, Object> photoSettings;

  /** 写真タイトル日本語名 */
  @Schema(description = "写真タイトル日本語名", example = "金閣寺")
  private String photoJapaneseTitle;

  /** 画像ファイルパス（署名付きURL） */
  @Schema(description = "画像ファイルパス（署名付きURL）")
  private String imageFilePath;

  /**
   * 向き区分
   *
   * <p>{@link DirectionEnum}
   */
  @Schema(description = "向き区分")
  private DirectionEnum directionKbn;

  /**
   * PortfolioPhotoModelからPortfolioPhotoItemResponseを生成する
   *
   * @param model {@link PortfolioPhotoModel}
   * @return {@link PortfolioPhotoItemResponse}
   */
  public static PortfolioPhotoItemResponse from(PortfolioPhotoModel model) {
    return PortfolioPhotoItemResponse.builder()
        .photoNo(model.getPhotoNo().value())
        .displayOrder(model.getDisplayOrder().value())
        .photoSettings(model.getPhotoSettings().value())
        .photoJapaneseTitle(model.getPhotoJapaneseTitle().value())
        .imageFilePath(model.getImageFilePath().value())
        .directionKbn(model.getDirectionKbn())
        .build();
  }
}
