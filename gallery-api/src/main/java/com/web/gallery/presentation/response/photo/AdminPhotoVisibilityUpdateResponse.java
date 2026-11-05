package com.web.gallery.presentation.response.photo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** 管理者による写真の公開状態変更のレスポンスパラメータを保持するクラス */
@Schema(description = "管理者用写真公開状態変更レスポンス")
@Data
@Builder
public class AdminPhotoVisibilityUpdateResponse {
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
   * @return {@link AdminPhotoVisibilityUpdateResponse}
   */
  public static AdminPhotoVisibilityUpdateResponse of(String message) {
    return AdminPhotoVisibilityUpdateResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .build();
  }
}
