package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** 本人のポートフォリオ詳細のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ詳細レスポンス")
@Data
@Builder
public class PortfolioDetailGetResponse {
  /** ポートフォリオ番号 */
  @Schema(description = "ポートフォリオ番号", example = "1")
  private Long portfolioNo;

  /** テーマ */
  @Schema(description = "テーマ", example = "日本の自然")
  private String theme;

  /** 説明文 */
  @Schema(description = "説明文")
  private String description;

  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}
   */
  @Schema(description = "テンプレート区分")
  private PortfolioTemplateEnum templateKbn;

  /** ポートフォリオ単位のテンプレート設定 */
  @Schema(description = "ポートフォリオ単位のテンプレート設定（項目はテンプレートごとに異なる）")
  private Map<String, Object> templateSettings;

  /**
   * 公開状態区分
   *
   * <p>{@link PortfolioStatusEnum}
   */
  @Schema(description = "公開状態区分")
  private PortfolioStatusEnum statusKbn;

  /** 作成日時 */
  @Schema(description = "作成日時")
  private OffsetDateTime createdAt;

  /** 更新日時 */
  @Schema(description = "更新日時")
  private OffsetDateTime updatedAt;

  /** 写真一覧（表示順） */
  @Schema(description = "写真一覧（表示順）")
  private List<PortfolioPhotoItemResponse> photoList;

  /** タグ一覧 */
  @Schema(description = "タグ一覧")
  private List<PortfolioTagItemResponse> tagList;

  /**
   * PortfolioDetailModelからPortfolioDetailGetResponseを生成する
   *
   * @param model {@link PortfolioDetailModel}
   * @return {@link PortfolioDetailGetResponse}
   */
  public static PortfolioDetailGetResponse from(PortfolioDetailModel model) {
    return PortfolioDetailGetResponse.builder()
        .portfolioNo(model.getPortfolioNo().value())
        .theme(model.getTheme().value())
        .description(model.getDescription().value())
        .templateKbn(model.getTemplateKbn())
        .templateSettings(model.getTemplateSettings().value())
        .statusKbn(model.getStatusKbn())
        .createdAt(model.getCreatedAt())
        .updatedAt(model.getUpdatedAt())
        .photoList(
            model.getPortfolioPhotoModelList().stream()
                .map(PortfolioPhotoItemResponse::from)
                .toList())
        .tagList(
            model.getPortfolioTagModelList().stream().map(PortfolioTagItemResponse::from).toList())
        .build();
  }
}
