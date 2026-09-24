package com.web.gallery.controller.response.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** 管理者用アカウント権限変更操作のレスポンスパラメータを保持するクラス */
@Schema(description = "管理者用アカウント権限変更レスポンス")
@Data
@Builder
public class AdminAccountAuthorityUpdateResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 成功フラグ */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @return {@link AdminAccountAuthorityUpdateResponse}
   */
  public static AdminAccountAuthorityUpdateResponse of(String message) {
    return AdminAccountAuthorityUpdateResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .build();
  }
}
