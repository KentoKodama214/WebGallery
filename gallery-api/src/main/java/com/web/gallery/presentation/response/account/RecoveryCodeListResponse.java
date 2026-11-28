package com.web.gallery.presentation.response.account;

import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.domain.model.auth.RecoveryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 発行したリカバリーコードのレスポンスパラメータを保持するクラス
 *
 * <p>平文のコードはこの応答でのみ返し、再表示はできない（DBにはハッシュのみ保存する）
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(description = "リカバリーコード発行レスポンス")
@Data
@Builder
public class RecoveryCodeListResponse {
  /** リカバリーコード（4文字ごとにハイフンで区切った表示用の形） */
  @Schema(description = "リカバリーコード（4文字ごとにハイフンで区切った表示用の形）", example = "[\"ABCD-EFGH-JKMN-PQRS\"]")
  private List<String> recoveryCodes;

  /**
   * RecoveryCodeListからRecoveryCodeListResponseを生成する
   *
   * @param recoveryCodeList {@link RecoveryCodeList}
   * @return {@link RecoveryCodeListResponse}
   */
  public static RecoveryCodeListResponse from(RecoveryCodeList recoveryCodeList) {
    return RecoveryCodeListResponse.builder()
        .recoveryCodes(recoveryCodeList.stream().map(RecoveryCode::toDisplayString).toList())
        .build();
  }
}
