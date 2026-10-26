package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.enumeration.MoveDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** ポートフォリオの表示順の移動時のリクエストパラメータを保持するクラス */
@Schema(description = "ポートフォリオ表示順移動リクエスト")
@Data
public class PortfolioDisplayOrderMoveRequest {
  /**
   * 移動方向
   *
   * <p>{@link MoveDirectionEnum}。未知の値はnullとして扱われ、必須チェックで不正となる
   */
  @Schema(description = "移動方向（up: 1つ上へ、down: 1つ下へ）", example = "up")
  @NotNull(message = "{validation.common.notBlank}")
  private MoveDirectionEnum direction;
}
