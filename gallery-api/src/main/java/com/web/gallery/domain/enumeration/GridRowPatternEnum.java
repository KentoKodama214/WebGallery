package com.web.gallery.domain.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * グリッド型テンプレートの1行あたりの写真の組み合わせパターンを管理するEnumクラス
 *
 * <p>テンプレート設定（JSON）にはEnum名（例: {@code TWO_EQUAL}）で保持する
 */
@Schema(description = "グリッド型テンプレートの行パターン")
@Getter
@AllArgsConstructor
public enum GridRowPatternEnum {
  /** 2枚を均等幅で並べる */
  TWO_EQUAL(2, "2枚均等"),
  /** 3枚を均等幅で並べる */
  THREE_EQUAL(3, "3枚均等"),
  /** 横長1枚（左）と縦長1枚（右）を並べる */
  WIDE_NARROW(2, "横長1枚+縦長1枚（縦長が右）"),
  /** 縦長1枚（左）と横長1枚（右）を並べる */
  NARROW_WIDE(2, "縦長1枚+横長1枚（縦長が左）");

  /** 1行に配置する写真の枚数 */
  private final Integer photoCount;

  /** 表示名 */
  private final String displayName;

  /**
   * Enum名からEnumを取得する
   *
   * @param name Enum名
   * @return {@link GridRowPatternEnum}。該当するEnumがなければnull
   */
  public static GridRowPatternEnum fromName(String name) {
    return Arrays.stream(GridRowPatternEnum.values())
        .filter(e -> e.name().equals(name))
        .findFirst()
        .orElse(null);
  }
}
