package com.web.gallery.domain.model.photo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ポートフォリオのテンプレート固有の設定の値オブジェクト
 *
 * <p>ポートフォリオ単位の設定（{@code portfolio_mst.template_settings}）と写真単位の設定（{@code
 * portfolio_photo.photo_settings}）の両方を表す。テンプレートごとに項目が異なるため、JSONオブジェクトをそのまま キーと値の組で保持し、項目の妥当性は{@link
 * com.web.gallery.domain.service.PortfolioTemplateSettingsPolicy}がテンプレートごとに判定する
 *
 * <p>値はJSONの型（文字列・数値・真偽値・配列（{@link List}）・オブジェクト（{@link Map}））のみを想定し、
 * 生成時に入れ子のリスト・マップまで含めて変更不可なコピーを保持する。値がnullの項目は未設定とみなして取り除く
 *
 * @param value 設定項目名と値の組
 */
public record TemplateSettings(Map<String, Object> value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public TemplateSettings {
    if (value == null) {
      throw new IllegalArgumentException("テンプレート設定はnullにできません");
    }
    value = copyMap(value);
  }

  /**
   * 項目が1つもないテンプレート設定を生成する
   *
   * @return {@link TemplateSettings}
   */
  public static TemplateSettings empty() {
    return new TemplateSettings(Map.of());
  }

  /**
   * 未設定の場合にデフォルト値（項目なし）を返す
   *
   * @param nullable {@link TemplateSettings}（null許容）
   * @return nullableがnullでなければそのまま、nullであれば項目なしの{@link TemplateSettings}
   */
  public static TemplateSettings getOrDefault(TemplateSettings nullable) {
    return nullable != null ? nullable : empty();
  }

  /**
   * 設定されている項目名の一覧を取得する
   *
   * @return 項目名の一覧
   */
  public Set<String> keys() {
    return value.keySet();
  }

  /**
   * 指定した項目の値を取得する
   *
   * @param key 項目名
   * @return 値。未設定の場合はnull
   */
  public Object get(String key) {
    return value.get(key);
  }

  /**
   * 項目が1つもないかどうかを取得する
   *
   * @return 項目が1つもない場合はtrue
   */
  public Boolean isEmpty() {
    return value.isEmpty();
  }

  private static Map<String, Object> copyMap(Map<?, ?> source) {
    Map<String, Object> copied = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : source.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }
      copied.put(entry.getKey().toString(), copyValue(entry.getValue()));
    }
    return Collections.unmodifiableMap(copied);
  }

  private static Object copyValue(Object source) {
    if (source instanceof Map<?, ?> map) {
      return copyMap(map);
    }
    if (source instanceof List<?> list) {
      List<Object> copied = new ArrayList<>();
      for (Object element : list) {
        if (element != null) {
          copied.add(copyValue(element));
        }
      }
      return Collections.unmodifiableList(copied);
    }
    return source;
  }
}
