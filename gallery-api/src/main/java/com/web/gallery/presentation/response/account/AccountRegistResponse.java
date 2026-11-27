package com.web.gallery.presentation.response.account;

import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.domain.model.auth.RecoveryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** アカウント登録のレスポンスパラメータを保持するクラス */
@Schema(description = "アカウント登録レスポンス")
@Data
@Builder
public class AccountRegistResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 登録成功 */
  @Schema(description = "登録成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /** リカバリーコード（4文字ごとにハイフンで区切った表示用の形。この応答でのみ返し、再表示はできない） */
  @Schema(description = "リカバリーコード（この応答でのみ返し、再表示はできない）", example = "[\"ABCD-EFGH-JKMN-PQRS\"]")
  private List<String> recoveryCodes;

  /**
   * 成功レスポンスを生成する
   *
   * @param isSuccess 登録成功
   * @param message メッセージ
   * @param recoveryCodes 発行したリカバリーコード
   * @return {@link AccountRegistResponse}
   */
  public static AccountRegistResponse of(
      Boolean isSuccess, String message, RecoveryCodeList recoveryCodes) {
    return AccountRegistResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(isSuccess)
        .message(message)
        .recoveryCodes(recoveryCodes.stream().map(RecoveryCode::toDisplayString).toList())
        .build();
  }
}
