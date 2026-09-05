package com.web.gallery.controller.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** アカウント一覧表示時のリクエストパラメータを保持するクラス */
@Schema(description = "アカウント一覧リクエスト")
@Data
public class AccountListRequest {
  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  // 深いオフセットページングによる負荷増大を抑えるための上限（未認証で叩ける一覧APIのため写真一覧と同値に統一）
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;
}
