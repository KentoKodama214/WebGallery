package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioTagModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオ詳細のタグ1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ詳細のタグ")
@Data
@Builder
public class PortfolioTagItemResponse {
  /** タグ番号 */
  @Schema(description = "タグ番号", example = "1")
  private Long tagNo;

  /** タグ日本語名 */
  @Schema(description = "タグ日本語名", example = "風景")
  private String tagJapaneseName;

  /** タグ英語名 */
  @Schema(description = "タグ英語名", example = "landscape")
  private String tagEnglishName;

  /**
   * PortfolioTagModelからPortfolioTagItemResponseを生成する
   *
   * @param model {@link PortfolioTagModel}
   * @return {@link PortfolioTagItemResponse}
   */
  public static PortfolioTagItemResponse from(PortfolioTagModel model) {
    return PortfolioTagItemResponse.builder()
        .tagNo(model.getTagNo().value())
        .tagJapaneseName(model.getTagJapaneseName().value())
        .tagEnglishName(model.getTagEnglishName().value())
        .build();
  }
}
