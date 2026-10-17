package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioModel;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオ一覧の1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ一覧の項目")
@Data
@Builder
public class PortfolioListItemResponse {
  /** ポートフォリオ番号 */
  @Schema(description = "ポートフォリオ番号", example = "1")
  private Long portfolioNo;

  /** テーマ */
  @Schema(description = "テーマ", example = "日本の自然")
  private String theme;

  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}
   */
  @Schema(description = "テンプレート区分")
  private PortfolioTemplateEnum templateKbn;

  /**
   * 公開状態区分
   *
   * <p>{@link PortfolioStatusEnum}
   */
  @Schema(description = "公開状態区分")
  private PortfolioStatusEnum statusKbn;

  /** 写真の枚数 */
  @Schema(description = "写真の枚数", example = "4")
  private Integer photoCount;

  /** 作成日時 */
  @Schema(description = "作成日時")
  private OffsetDateTime createdAt;

  /** 更新日時 */
  @Schema(description = "更新日時")
  private OffsetDateTime updatedAt;

  /**
   * PortfolioModelからPortfolioListItemResponseを生成する
   *
   * @param model {@link PortfolioModel}
   * @return {@link PortfolioListItemResponse}
   */
  public static PortfolioListItemResponse from(PortfolioModel model) {
    return PortfolioListItemResponse.builder()
        .portfolioNo(model.getPortfolioNo().value())
        .theme(model.getTheme().value())
        .templateKbn(model.getTemplateKbn())
        .statusKbn(model.getStatusKbn())
        .photoCount(model.getPhotoCount().value())
        .createdAt(model.getCreatedAt())
        .updatedAt(model.getUpdatedAt())
        .build();
  }
}
