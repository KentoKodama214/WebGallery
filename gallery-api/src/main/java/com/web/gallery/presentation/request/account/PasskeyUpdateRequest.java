package com.web.gallery.presentation.request.account;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** パスキー名変更時のリクエストパラメータを保持するクラス */
@Schema(description = "パスキー名変更リクエスト")
@Data
public class PasskeyUpdateRequest {
  /** パスキー名 */
  @Schema(description = "パスキー名", example = "iPhoneのFace ID")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PASSKEY_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
  private String passkeyName;
}
