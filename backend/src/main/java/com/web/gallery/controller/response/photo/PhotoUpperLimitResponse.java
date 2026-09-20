package com.web.gallery.controller.response.photo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** 写真登録上限チェックのレスポンスパラメータを保持するクラス */
@Schema(description = "写真登録上限チェックレスポンス")
@Data
@Builder
public class PhotoUpperLimitResponse {
  /** 写真の登録枚数が上限に達しているか */
  @Schema(description = "写真の登録枚数が上限に達しているか")
  private Boolean isReachedUpperLimit;

  /** 残り登録可能枚数（上限が存在しない権限区分の場合はnull） */
  @Schema(description = "残り登録可能枚数（上限が存在しない権限区分の場合はnull）")
  private Integer remainingCount;

  /**
   * レスポンスを生成する
   *
   * @param isReachedUpperLimit 写真の登録枚数が上限に達しているか
   * @param remainingCount 残り登録可能枚数（上限が存在しない権限区分の場合はnull）
   * @return {@link PhotoUpperLimitResponse}
   */
  public static PhotoUpperLimitResponse of(Boolean isReachedUpperLimit, Integer remainingCount) {
    return PhotoUpperLimitResponse.builder()
        .isReachedUpperLimit(isReachedUpperLimit)
        .remainingCount(remainingCount)
        .build();
  }
}
