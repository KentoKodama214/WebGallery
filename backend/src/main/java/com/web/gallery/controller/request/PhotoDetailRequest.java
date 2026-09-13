package com.web.gallery.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 写真詳細表示時のリクエストパラメータを保持するクラス */
@Schema(description = "写真詳細リクエスト")
@Data
public class PhotoDetailRequest {

  /**
   * クライアント（ブラウザ）が取得した遷移元URL（{@code document.referrer}）
   *
   * <p>サーバーが受け取るHTTPリクエストのRefererヘッダーは、SPAの同一ページからのAPI呼び出しである以上常に自ページの
   * URLになってしまい、外部サイトからの本来の流入元を表さない。そのためフロントエンドが{@code document.referrer}を このパラメータとして明示的に送信する
   */
  @Schema(description = "クライアントが取得した遷移元URL（document.referrer）", example = "https://example.com/")
  @Size(max = 2048, message = "{validation.common.max_length}")
  private String referer;
}
