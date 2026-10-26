package com.web.gallery.presentation.request.photo;

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
  /** 検索語（写真のタイトル・撮影場所の名前・撮影場所の住所・タグ名のいずれかに部分一致。写真のタイトル・撮影場所の名前の最大文字数と同じ100文字まで） */
  @Schema(description = "検索語（写真のタイトル・撮影場所の名前・撮影場所の住所・タグ名のいずれかに部分一致）", example = "京都")
  @Size(max = 100, message = "{validation.common.max_length}")
  private String keyword;

  /** ページ番号 */
  @Schema(description = "ページ番号", example = "1")
  @NotNull(message = "{validation.common.notBlank}")
  @Positive(message = "{validation.common.positive}")
  // 深いオフセットページングによる負荷増大を抑えるための上限（写真一覧と同じ）
  @Max(value = 10000, message = "{validation.common.max}")
  private Integer pageNo = 1;
}
