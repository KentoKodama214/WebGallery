package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理者による写真の公開状態変更時のリクエストパラメータを保持するクラス */
@Schema(description = "管理者用写真公開状態変更リクエスト")
@Data
public class AdminPhotoVisibilityUpdateRequest {
  /** 非公開にする場合はtrue、非公開を解除する場合はfalse */
  @Schema(description = "非公開にする場合はtrue、非公開を解除する場合はfalse", example = "true")
  @NotNull(message = "{validation.common.notBlank}")
  private Boolean isHiddenByAdmin;

  /** 理由（管理者のみが参照し、写真所有者には表示しない） */
  @Schema(description = "理由（写真所有者には表示しない）", example = "お問い合わせで不適切な画像との報告があったため")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.MODERATION_REASON_MAX_LENGTH, message = "{validation.common.max_length}")
  private String reason;
}
