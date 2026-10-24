package com.web.gallery.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ポートフォリオのテンプレートを管理するEnumクラス
 *
 * <p>テンプレートを追加する場合は、このEnumに値を追加し、DBの{@code photo.portfolio_template_enum}型に同じ値を追加したうえで、 {@link
 * com.web.gallery.domain.service.PortfolioTemplateSettingsPolicy}にテンプレート固有の設定の検証を追加する
 */
@Schema(description = "ポートフォリオのテンプレート区分")
@Getter
@AllArgsConstructor
public enum PortfolioTemplateEnum {
  /** フィーチャー型（写真ごとにテーマ・サブテーマ・説明を持つセクションを縦に並べる） */
  @JsonProperty("feature")
  FEATURE("feature", "フィーチャー"),
  /** グリッド型（1行に2〜3枚の写真を組み合わせて並べる） */
  @JsonProperty("grid")
  GRID("grid", "グリッド");

  /** DBに保持する値 */
  private final String dbValue;

  /** 表示名 */
  private final String displayName;

  /**
   * 値からEnumを取得する
   *
   * <p>テンプレートの指定誤りを別のテンプレートとして黙って扱わないよう、該当するEnumがなければnullを返す（リクエストの必須チェックで不正値として扱われる）
   *
   * @param value 値
   * @return {@link PortfolioTemplateEnum}。該当するEnumがなければnull
   */
  @JsonCreator
  public static PortfolioTemplateEnum fromValue(String value) {
    return Arrays.stream(PortfolioTemplateEnum.values())
        .filter(e -> e.getDbValue().equals(value) || e.name().equals(value))
        .findFirst()
        .orElse(null);
  }
}
