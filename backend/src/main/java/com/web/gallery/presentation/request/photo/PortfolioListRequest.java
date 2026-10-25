package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
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
}
