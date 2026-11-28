package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ポートフォリオ登録・更新時のタグのリクエストパラメータを保持するクラス
 *
 * <p>アカウント番号・ポートフォリオ番号・タグ番号はサーバ側で採番するため受け付けない （クライアント値を採用すると他人のポートフォリオへタグを注入できるIDORになる）
 */
@Schema(description = "ポートフォリオタグ保存リクエスト")
@Data
public class PortfolioTagSaveRequest {
  /** タグ日本語名 */
  @Schema(description = "タグ日本語名", example = "風景")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.TAG_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
  @Pattern(regexp = "(?!.*( |　)).*", message = "{validation.common.disable_space}")
  private String tagJapaneseName;

  /** タグ英語名 */
  @Schema(description = "タグ英語名", example = "landscape")
  @Size(max = Consts.TAG_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
  @Pattern(regexp = "(?!.*( |　)).*", message = "{validation.common.disable_space}")
  private String tagEnglishName;
}
