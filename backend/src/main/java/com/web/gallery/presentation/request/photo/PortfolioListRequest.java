package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** ポートフォリオ一覧取得時の絞り込み条件を保持するクラス */
@Schema(description = "ポートフォリオ一覧リクエスト")
@Data
public class PortfolioListRequest {
  /** 検索語（テーマ・タグ名（日本語・英語）のいずれかに部分一致） */
  @Schema(description = "検索語（テーマ・タグ名（日本語・英語）のいずれかに部分一致）", example = "自然")
  @Size(max = Consts.PORTFOLIO_THEME_MAX_LENGTH, message = "{validation.common.max_length}")
  private String keyword;

  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  // 深いオフセットページングによる負荷増大を抑えるための上限（写真一覧と同じ）
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;
}
