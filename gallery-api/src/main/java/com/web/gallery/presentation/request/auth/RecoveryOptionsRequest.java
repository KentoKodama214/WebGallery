package com.web.gallery.presentation.request.auth;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 復旧オプション発行時のリクエストパラメータを保持するクラス */
@Schema(description = "復旧オプション発行リクエスト")
@Data
public class RecoveryOptionsRequest {
  /** アカウントID */
  @Schema(description = "アカウントID", example = "testuser01")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = 20, message = "{validation.common.max_length}")
  private String accountId;

  /**
   * リカバリーコード
   *
   * <p>ハイフン・空白の有無、英字の大文字・小文字は問わない
   */
  @Schema(description = "リカバリーコード（ハイフン・空白の有無、大文字・小文字は問わない）", example = "ABCD-EFGH-JKMN-PQRS")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.RECOVERY_CODE_INPUT_MAX_LENGTH, message = "{validation.common.max_length}")
  private String recoveryCode;
}
