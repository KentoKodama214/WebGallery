package com.web.gallery.presentation.request.photo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 管理者用写真一覧表示時のリクエストパラメータを保持するクラス */
@Schema(description = "管理者用写真一覧リクエスト")
@Data
public class AdminPhotoListRequest {
  /** 写真所有者のアカウントIDによる絞り込み（任意） */
  @Schema(description = "写真所有者のアカウントID（未指定の場合は全アカウント）", example = "testuser01")
  @Pattern(regexp = "[a-zA-Z0-9]{8,20}", message = "{validation.common.pattern}")
  private String accountId;

  /** 管理者非公開フラグによる絞り込み（任意） */
  @Schema(description = "管理者非公開フラグ（未指定の場合は公開・非公開の両方）")
  private Boolean isHiddenByAdmin;

  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @JsonSetter(nulls = Nulls.SKIP)
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;
}
