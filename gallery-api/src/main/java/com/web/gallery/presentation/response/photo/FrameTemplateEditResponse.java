package com.web.gallery.presentation.response.photo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** 額縁テンプレートの登録・更新・削除のレスポンスパラメータを保持するクラス */
@Schema(description = "額縁テンプレート編集レスポンス")
@Data
@Builder
public class FrameTemplateEditResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 成功 */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /** 額縁テンプレート番号 */
  @Schema(description = "額縁テンプレート番号", example = "1")
  private Long frameTemplateNo;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @param frameTemplateNo 額縁テンプレート番号
   * @return {@link FrameTemplateEditResponse}
   */
  public static FrameTemplateEditResponse of(String message, Long frameTemplateNo) {
    return FrameTemplateEditResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .frameTemplateNo(frameTemplateNo)
        .build();
  }
}
