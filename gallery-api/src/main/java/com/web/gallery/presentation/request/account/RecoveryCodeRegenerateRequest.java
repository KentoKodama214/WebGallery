package com.web.gallery.presentation.request.account;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** リカバリーコード再発行時のリクエストパラメータを保持するクラス */
@Schema(description = "リカバリーコード再発行リクエスト")
@Data
public class RecoveryCodeRegenerateRequest {
  /**
   * 本人確認用の認証レスポンス
   *
   * <p>本人確認オプション発行API（{@code /reauthentication-options}）で発行したチャレンジに、本人のパスキーで署名した {@code
   * navigator.credentials.get()}の結果を{@code toJSON()}し、さらにJSON文字列にしたもの。
   * アクセストークンを盗んだ攻撃者に復旧手段を握らせないため必須。
   */
  @Schema(description = "本人確認用の認証レスポンス（PublicKeyCredential#toJSON()のJSON文字列）")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PASSKEY_RESPONSE_JSON_MAX_LENGTH, message = "{validation.common.max_length}")
  private String credential;
}
