package com.web.gallery.controller.response.photo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** 写真新規一括登録のレスポンスパラメータを保持するクラス */
@Schema(description = "写真新規一括登録レスポンス")
@Data
@Builder
public class PhotoBulkEditResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 登録成功 */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /** 登録した写真の枚数 */
  @Schema(description = "登録した写真の枚数")
  private Integer registeredCount;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @param registeredCount 登録した写真の枚数
   * @return {@link PhotoBulkEditResponse}
   */
  public static PhotoBulkEditResponse of(String message, Integer registeredCount) {
    return PhotoBulkEditResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .registeredCount(registeredCount)
        .build();
  }
}
