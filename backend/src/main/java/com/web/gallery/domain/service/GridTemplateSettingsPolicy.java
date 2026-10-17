package com.web.gallery.domain.service;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.GridRowPatternEnum;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * グリッド型テンプレートの設定に関するビジネスルールを判定するドメインサービス
 *
 * <p>グリッド型は、1行に2〜3枚の写真を組み合わせて並べる。ポートフォリオ単位の設定として{@code rows}（行の一覧。各行は {@code pattern}に{@link
 * GridRowPatternEnum}の名前を持つ）を、写真単位の設定として{@code caption}（キャプション）を持つ。 各行は写真を表示順に、行パターンの枚数ずつ消費する
 */
@Component
public class GridTemplateSettingsPolicy {

  /** ポートフォリオ設定の項目名：行の一覧 */
  public static final String KEY_ROWS = "rows";

  /** 行の項目名：行パターン */
  public static final String KEY_PATTERN = "pattern";

  /** 写真設定の項目名：キャプション */
  public static final String KEY_CAPTION = "caption";

  private static final Set<String> TEMPLATE_SETTING_KEYS = Set.of(KEY_ROWS);

  private static final Set<String> ROW_KEYS = Set.of(KEY_PATTERN);

  private static final Set<String> PHOTO_SETTING_KEYS = Set.of(KEY_CAPTION);

  /**
   * ポートフォリオ単位の設定が妥当かどうかを判定する
   *
   * <p>未知の項目を含まず、行の一覧の各行が既知の行パターンを持つ場合に妥当とする。下書き保存時にも適用するため、 行の枚数の合計と写真の枚数の一致は問わない
   *
   * @param templateSettings ポートフォリオ単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidTemplateSettings(TemplateSettings templateSettings) {
    if (!TEMPLATE_SETTING_KEYS.containsAll(templateSettings.keys())) {
      return false;
    }
    Object rows = templateSettings.get(KEY_ROWS);
    if (rows == null) {
      return true;
    }
    if (!(rows instanceof List<?> rowList) || rowList.size() > Consts.PORTFOLIO_PHOTO_MAX_SIZE) {
      return false;
    }
    return rowList.stream().allMatch(row -> toPattern(row) != null);
  }

  /**
   * 写真単位の設定が妥当かどうかを判定する
   *
   * <p>未知の項目を含まず、キャプションが文字数上限以内の文字列である場合に妥当とする（未入力は許容する）
   *
   * @param photoSettings 写真単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidPhotoSettings(TemplateSettings photoSettings) {
    if (!PHOTO_SETTING_KEYS.containsAll(photoSettings.keys())) {
      return false;
    }
    Object caption = photoSettings.get(KEY_CAPTION);
    return caption == null
        || (caption instanceof String text && text.length() <= Consts.PORTFOLIO_CAPTION_MAX_LENGTH);
  }

  /**
   * ポートフォリオ単位の設定と写真枚数から、公開できる状態かどうかを判定する
   *
   * <p>行パターンごとの写真枚数の合計が、ポートフォリオに設定した写真の枚数と一致する場合に公開できる （過不足があると、行に配置されない写真や空き枠が生じるため）
   *
   * @param templateSettings ポートフォリオ単位の設定
   * @param photoCount ポートフォリオに設定した写真の枚数
   * @return 写真が1枚以上設定され、行の枚数の合計と写真の枚数が一致する場合、true
   */
  public Boolean isPublishable(TemplateSettings templateSettings, PhotoCount photoCount) {
    if (photoCount.value() == 0 || !isValidTemplateSettings(templateSettings)) {
      return false;
    }
    if (!(templateSettings.get(KEY_ROWS) instanceof List<?> rowList)) {
      return false;
    }
    int totalPhotoCount =
        rowList.stream().map(this::toPattern).mapToInt(GridRowPatternEnum::getPhotoCount).sum();
    return totalPhotoCount == photoCount.value();
  }

  /**
   * 写真単位の設定が、公開できる状態かどうかを判定する
   *
   * <p>キャプションは任意項目のため、常に公開できる
   *
   * @param photoSettings 写真単位の設定
   * @return 常にtrue
   */
  public Boolean isPublishablePhotoSettings(TemplateSettings photoSettings) {
    return true;
  }

  private GridRowPatternEnum toPattern(Object row) {
    if (!(row instanceof Map<?, ?> rowMap)
        || !ROW_KEYS.containsAll(rowMap.keySet())
        || !(rowMap.get(KEY_PATTERN) instanceof String patternName)) {
      return null;
    }
    return GridRowPatternEnum.fromName(patternName);
  }
}
