package com.web.gallery.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** アカウント更新のレスポンスパラメータを保持するクラス */
@Schema(description = "アカウント更新レスポンス")
@Data
@Builder
public class AccountUpdateResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** アカウントIDが重複しているか */
  @Schema(description = "アカウントIDが重複しているか")
  private Boolean isDuplicateAccountId;

  /** アカウントIDが更新されたか */
  @Schema(description = "アカウントIDが更新されたか")
  private Boolean isAccountIdChanged;

  /** パスワードが更新されたか */
  @Schema(description = "パスワードが更新されたか")
  private Boolean isPasswordChanged;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /**
   * 成功レスポンスを生成する
   *
   * @param isDuplicateAccountId アカウントIDが重複しているか
   * @param isAccountIdChanged アカウントIDが更新されたか
   * @param isPasswordChanged パスワードが更新されたか
   * @param message メッセージ
   * @return {@link AccountUpdateResponse}
   */
  public static AccountUpdateResponse of(
      Boolean isDuplicateAccountId,
      Boolean isAccountIdChanged,
      Boolean isPasswordChanged,
      String message) {
    return AccountUpdateResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isDuplicateAccountId(isDuplicateAccountId)
        .isAccountIdChanged(isAccountIdChanged)
        .isPasswordChanged(isPasswordChanged)
        .message(message)
        .build();
  }
}
