package com.web.gallery.controller.request;

import com.web.gallery.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** お問い合わせ新規登録時のリクエストパラメータを保持するクラス */
@Schema(description = "お問い合わせ登録リクエスト")
@Data
public class InquiryRegistRequest {
  /** 件名 */
  @Schema(description = "件名", example = "写真が表示されない")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.INQUIRY_SUBJECT_MAX_LENGTH, message = "{validation.common.max_length}")
  private String subject;

  /** 本文 */
  @Schema(description = "本文", example = "アップロードした写真がギャラリーに表示されません。")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.INQUIRY_BODY_MAX_LENGTH, message = "{validation.common.max_length}")
  private String body;
}
