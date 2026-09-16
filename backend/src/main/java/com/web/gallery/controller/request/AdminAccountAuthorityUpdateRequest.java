package com.web.gallery.controller.request;

import com.web.gallery.enumeration.AuthorityEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 管理者用アカウント権限変更時のリクエストパラメータを保持するクラス */
@Schema(description = "管理者用アカウント権限変更リクエスト")
@Data
public class AdminAccountAuthorityUpdateRequest {
  /**
   * 権限区分
   *
   * <p>{@link AuthorityEnum}
   */
  @Schema(description = "権限区分", example = "normal-user")
  @NotNull(message = "{validation.common.notBlank}")
  private AuthorityEnum authorityKbn;
}
