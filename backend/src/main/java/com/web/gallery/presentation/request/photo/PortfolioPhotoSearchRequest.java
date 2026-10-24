package com.web.gallery.presentation.request.photo;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** ポートフォリオに設定する写真の検索条件を保持するクラス */
@Schema(description = "ポートフォリオ用の写真検索リクエスト")
@Data
public class PortfolioPhotoSearchRequest {
  /** 写真のタイトル（日本語・英語のいずれかに部分一致。写真のタイトルの最大文字数と同じ100文字まで） */
  @Schema(description = "写真のタイトル（日本語・英語のいずれかに部分一致）", example = "金閣寺")
  @Size(max = 100, message = "{validation.common.max_length}")
  private String title;

  /** 撮影場所の名前（管理名・表示名のいずれかに部分一致。撮影場所の名前の最大文字数と同じ100文字まで） */
  @Schema(description = "撮影場所の名前（管理名・表示名のいずれかに部分一致）", example = "京都")
  @Size(max = 100, message = "{validation.common.max_length}")
  private String location;

  /** タグ名（日本語・英語のいずれかに部分一致） */
  @Schema(description = "タグ名（日本語・英語のいずれかに部分一致）", example = "風景")
  @Size(max = Consts.TAG_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
  private String tag;

  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  // 深いオフセットページングによる負荷増大を抑えるための上限（写真一覧と同じ）
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;
}
