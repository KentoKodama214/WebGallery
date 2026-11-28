package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** ポートフォリオの公開状態変更時のリクエストパラメータを保持するクラス */
@Schema(description = "ポートフォリオ公開状態変更リクエスト")
@Data
public class PortfolioStatusUpdateRequest {
  /**
   * 変更後の公開状態区分
   *
   * <p>{@link PortfolioStatusEnum}。未知の値はnullとして扱われ、必須チェックで不正となる
   */
  @Schema(description = "変更後の公開状態区分", example = "published")
  @NotNull(message = "{validation.common.notBlank}")
  private PortfolioStatusEnum statusKbn;
}
