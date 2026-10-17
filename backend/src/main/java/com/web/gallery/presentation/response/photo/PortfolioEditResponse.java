package com.web.gallery.presentation.response.photo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** ポートフォリオの登録・更新・公開状態変更・削除のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ編集レスポンス")
@Data
@Builder
public class PortfolioEditResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 成功 */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /** ポートフォリオ番号 */
  @Schema(description = "ポートフォリオ番号", example = "1")
  private Long portfolioNo;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioEditResponse}
   */
  public static PortfolioEditResponse of(String message, Long portfolioNo) {
    return PortfolioEditResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .portfolioNo(portfolioNo)
        .build();
  }
}
