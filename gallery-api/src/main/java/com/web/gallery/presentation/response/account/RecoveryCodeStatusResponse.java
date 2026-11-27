package com.web.gallery.presentation.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * リカバリーコードの状態のレスポンスパラメータを保持するクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(description = "リカバリーコード状態レスポンス")
@Data
@Builder
public class RecoveryCodeStatusResponse {
  /** 未使用のリカバリーコードの件数 */
  @Schema(description = "未使用のリカバリーコードの件数", example = "10")
  private Integer remainingCount;

  /** 1回に発行するリカバリーコードの個数 */
  @Schema(description = "1回に発行するリカバリーコードの個数", example = "10")
  private Integer issueCount;

  /**
   * 件数からRecoveryCodeStatusResponseを生成する
   *
   * @param remainingCount 未使用のリカバリーコードの件数
   * @param issueCount 1回に発行するリカバリーコードの個数
   * @return {@link RecoveryCodeStatusResponse}
   */
  public static RecoveryCodeStatusResponse of(Integer remainingCount, Integer issueCount) {
    return RecoveryCodeStatusResponse.builder()
        .remainingCount(remainingCount)
        .issueCount(issueCount)
        .build();
  }
}
