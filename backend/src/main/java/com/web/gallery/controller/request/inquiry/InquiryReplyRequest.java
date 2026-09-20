package com.web.gallery.controller.request.inquiry;

import com.web.gallery.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** お問い合わせ返信登録時のリクエストパラメータを保持するクラス */
@Schema(description = "お問い合わせ返信リクエスト")
@Data
public class InquiryReplyRequest {
  /** 返信本文 */
  @Schema(description = "返信本文", example = "ご報告ありがとうございます。調査いたします。")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.INQUIRY_BODY_MAX_LENGTH, message = "{validation.common.max_length}")
  private String body;
}
