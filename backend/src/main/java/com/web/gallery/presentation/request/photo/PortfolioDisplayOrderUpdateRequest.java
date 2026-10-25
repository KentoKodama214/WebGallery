package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/** ポートフォリオの表示順更新時のリクエストパラメータを保持するクラス */
@Schema(description = "ポートフォリオ表示順更新リクエスト")
@Data
public class PortfolioDisplayOrderUpdateRequest {
  /**
   * 表示順に並べたポートフォリオ番号（この並び順が表示順になる）
   *
   * <p>本人の全ポートフォリオ（下書きを含む）を過不足・重複なく指定する
   */
  @Schema(description = "表示順に並べたポートフォリオ番号（本人の全ポートフォリオを過不足・重複なく指定する）", example = "[3, 1, 2]")
  @NotEmpty(message = "{validation.common.notBlank}")
  @Size(max = Consts.PORTFOLIO_DISPLAY_ORDER_MAX_SIZE)
  private List<
          @NotNull(message = "{validation.common.notBlank}")
          @Positive(message = "{validation.common.positive}") Long>
      portfolioNoList;
}
