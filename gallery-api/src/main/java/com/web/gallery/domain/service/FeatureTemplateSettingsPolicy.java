package com.web.gallery.domain.service;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * フィーチャー型テンプレートの設定に関するビジネスルールを判定するドメインサービス
 *
 * <p>フィーチャー型は、写真ごとにテーマ・サブテーマ・説明を持つセクションを縦に並べる。ポートフォリオ単位の設定は持たず、 写真単位の設定として{@code theme}（テーマ）・{@code
 * subTheme}（サブテーマ）・{@code description}（説明）を持つ
 */
@Component
public class FeatureTemplateSettingsPolicy {

  /** 写真設定の項目名：テーマ */
  public static final String KEY_THEME = "theme";

  /** 写真設定の項目名：サブテーマ */
  public static final String KEY_SUB_THEME = "subTheme";

  /** 写真設定の項目名：説明 */
  public static final String KEY_DESCRIPTION = "description";

  private static final Set<String> PHOTO_SETTING_KEYS =
      Set.of(KEY_THEME, KEY_SUB_THEME, KEY_DESCRIPTION);

  /**
   * ポートフォリオ単位の設定が妥当かどうかを判定する
   *
   * <p>フィーチャー型はポートフォリオ単位の設定を持たないため、項目が1つもない場合のみ妥当とする
   *
   * @param templateSettings ポートフォリオ単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidTemplateSettings(TemplateSettings templateSettings) {
    return templateSettings.isEmpty();
  }

  /**
   * 写真単位の設定が妥当かどうかを判定する
   *
   * <p>未知の項目を含まず、各項目が文字数上限以内の文字列である場合に妥当とする。下書き保存時にも適用するため、 未入力（項目なし・空文字）は許容する
   *
   * @param photoSettings 写真単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidPhotoSettings(TemplateSettings photoSettings) {
    if (!PHOTO_SETTING_KEYS.containsAll(photoSettings.keys())) {
      return false;
    }
    return isStringWithin(photoSettings.get(KEY_THEME), Consts.PORTFOLIO_THEME_MAX_LENGTH)
        && isStringWithin(photoSettings.get(KEY_SUB_THEME), Consts.PORTFOLIO_THEME_MAX_LENGTH)
        && isStringWithin(
            photoSettings.get(KEY_DESCRIPTION), Consts.PORTFOLIO_DESCRIPTION_MAX_LENGTH);
  }

  /**
   * ポートフォリオ単位の設定と写真枚数から、公開できる状態かどうかを判定する
   *
   * @param templateSettings ポートフォリオ単位の設定
   * @param photoCount ポートフォリオに設定した写真の枚数
   * @return 写真が1枚以上設定されている場合、true
   */
  public Boolean isPublishable(TemplateSettings templateSettings, PhotoCount photoCount) {
    return photoCount.value() > 0;
  }

  /**
   * 写真単位の設定が、公開できる状態かどうかを判定する
   *
   * @param photoSettings 写真単位の設定
   * @return テーマが入力されている場合、true
   */
  public Boolean isPublishablePhotoSettings(TemplateSettings photoSettings) {
    return photoSettings.get(KEY_THEME) instanceof String theme && !theme.isBlank();
  }

  private boolean isStringWithin(Object value, int maxLength) {
    if (value == null) {
      return true;
    }
    return value instanceof String text && text.length() <= maxLength;
  }
}
