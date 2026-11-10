package com.web.gallery.domain.service;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 額縁型テンプレートの設定に関するビジネスルールを判定するドメインサービス
 *
 * <p>額縁型は、アカウントで作成した額縁テンプレートの写真枠に、写真を表示順に1枚ずつ当てはめて1枚の組写真にする。 ポートフォリオ単位の設定として{@code
 * frame}（選択時にコピーした額縁定義。構造は{@link FrameDefinitionPolicy}を参照）・ {@code
 * messages}（メッセージ枠ごとの文面。メッセージ枠の順）・{@code frameTemplateNo}（コピー元の額縁テンプレート番号。表示には使わない）を持ち、
 * 写真単位の設定は持たない。額縁定義をコピーして保持するため、コピー元の額縁テンプレートを変更・削除しても表示は変わらない
 */
@Component
@RequiredArgsConstructor
public class FrameTemplateSettingsPolicy {

  /** ポートフォリオ設定の項目名：コピー元の額縁テンプレート番号 */
  public static final String KEY_FRAME_TEMPLATE_NO = "frameTemplateNo";

  /** ポートフォリオ設定の項目名：額縁定義 */
  public static final String KEY_FRAME = "frame";

  /** ポートフォリオ設定の項目名：メッセージ枠ごとの文面 */
  public static final String KEY_MESSAGES = "messages";

  private static final Set<String> TEMPLATE_SETTING_KEYS =
      Set.of(KEY_FRAME_TEMPLATE_NO, KEY_FRAME, KEY_MESSAGES);

  private final FrameDefinitionPolicy frameDefinitionPolicy;

  /**
   * ポートフォリオ単位の設定が妥当かどうかを判定する
   *
   * <p>未知の項目を含まず、額縁定義が妥当で、文面がメッセージ枠の数以下・文字数上限以内の文字列の一覧である場合に妥当とする。
   * 下書き保存時にも適用するため、写真の枚数と写真枠の数の一致は問わない
   *
   * @param templateSettings ポートフォリオ単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidTemplateSettings(TemplateSettings templateSettings) {
    if (!TEMPLATE_SETTING_KEYS.containsAll(templateSettings.keys())) {
      return false;
    }
    Object frameTemplateNo = templateSettings.get(KEY_FRAME_TEMPLATE_NO);
    if (frameTemplateNo != null
        && !(frameTemplateNo instanceof Number number
            && number.doubleValue() == Math.floor(number.doubleValue())
            && number.longValue() > 0)) {
      return false;
    }
    if (!(templateSettings.get(KEY_FRAME) instanceof Map<?, ?> frameMap)) {
      return false;
    }
    FrameDefinition frame = toFrameDefinition(frameMap);
    if (!frameDefinitionPolicy.isValid(frame)) {
      return false;
    }
    return isValidMessages(
        templateSettings.get(KEY_MESSAGES), frameDefinitionPolicy.countTexts(frame));
  }

  /**
   * 写真単位の設定が妥当かどうかを判定する
   *
   * <p>額縁型は写真単位の設定を持たないため、空の場合だけを妥当とする
   *
   * @param photoSettings 写真単位の設定
   * @return 妥当な場合、true
   */
  public Boolean isValidPhotoSettings(TemplateSettings photoSettings) {
    return photoSettings.keys().isEmpty();
  }

  /**
   * ポートフォリオ単位の設定と写真枚数から、公開できる状態かどうかを判定する
   *
   * <p>額縁の写真枠の数と、ポートフォリオに設定した写真の枚数が一致する場合に公開できる （過不足があると、空いた写真枠や額縁に収まらない写真が生じるため）
   *
   * @param templateSettings ポートフォリオ単位の設定
   * @param photoCount ポートフォリオに設定した写真の枚数
   * @return 設定が妥当で、写真枠の数と写真の枚数が一致する場合、true
   */
  public Boolean isPublishable(TemplateSettings templateSettings, PhotoCount photoCount) {
    if (photoCount.value() == 0 || !isValidTemplateSettings(templateSettings)) {
      return false;
    }
    FrameDefinition frame = toFrameDefinition((Map<?, ?>) templateSettings.get(KEY_FRAME));
    return frameDefinitionPolicy.countSlots(frame).equals(photoCount.value());
  }

  /**
   * 写真単位の設定が、公開できる状態かどうかを判定する
   *
   * <p>額縁型は写真単位の設定を持たないため、常に公開できる
   *
   * @param photoSettings 写真単位の設定
   * @return 常にtrue
   */
  public Boolean isPublishablePhotoSettings(TemplateSettings photoSettings) {
    return true;
  }

  private boolean isValidMessages(Object messages, int textCount) {
    if (messages == null) {
      return true;
    }
    if (!(messages instanceof List<?> messageList) || messageList.size() > textCount) {
      return false;
    }
    return messageList.stream()
        .allMatch(message ->
            message instanceof String text && text.length() <= Consts.FRAME_MESSAGE_MAX_LENGTH);
  }

  @SuppressWarnings("unchecked")
  private FrameDefinition toFrameDefinition(Map<?, ?> frameMap) {
    // TemplateSettingsが保持するマップのキーは文字列に正規化済み
    return new FrameDefinition((Map<String, Object>) frameMap);
  }
}
