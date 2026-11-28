package com.web.gallery.presentation.response.photo;

import com.web.gallery.domain.enumeration.GridRowPatternEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** グリッド型テンプレートの行パターン1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "グリッド型テンプレートの行パターン")
@Data
@Builder
public class GridRowPatternItemResponse {
  /** 行パターン（テンプレート設定の{@code rows[].pattern}に指定する値） */
  @Schema(description = "行パターン（テンプレート設定のrows[].patternに指定する値）", example = "TWO_EQUAL")
  private String pattern;

  /** 表示名 */
  @Schema(description = "表示名", example = "2枚均等")
  private String displayName;

  /** 1行に配置する写真の枚数 */
  @Schema(description = "1行に配置する写真の枚数", example = "2")
  private Integer photoCount;

  /**
   * GridRowPatternEnumからGridRowPatternItemResponseを生成する
   *
   * @param pattern {@link GridRowPatternEnum}
   * @return {@link GridRowPatternItemResponse}
   */
  public static GridRowPatternItemResponse of(GridRowPatternEnum pattern) {
    return GridRowPatternItemResponse.builder()
        .pattern(pattern.name())
        .displayName(pattern.getDisplayName())
        .photoCount(pattern.getPhotoCount())
        .build();
  }
}
