package com.web.gallery.domain.service;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.model.photo.FrameDefinition;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 額縁定義に関するビジネスルールを判定するドメインサービス
 *
 * <p>額縁定義は、額縁テンプレート（{@code frame_template_mst.frame_definition}）と、額縁型のポートフォリオが選択時にコピーして
 * 保持する定義（{@code portfolio_mst.template_settings.frame}）の両方で同じ構造を持つ。構造は以下のとおり
 *
 * <ul>
 *   <li>{@code size}（必須）：額縁のサイズ。{@code width}・{@code height}（mm）
 *   <li>{@code background}（任意）：額縁の背景（台紙）。{@code color}（{@code #RRGGBB}）
 *   <li>{@code border}（任意）：額縁の外枠。{@code color}（{@code #RRGGBB}）・{@code width}（額縁の横幅に対する%）
 *   <li>{@code slots}（必須。1件以上）：写真枠の一覧。{@code x}・{@code y}・{@code width}・{@code
 *       height}（左上の位置と大きさ。額縁に対する%）・ {@code rotation}（傾き。度）・{@code borderColor}・{@code
 *       borderWidth}（枠線。額縁の横幅に対する%）・{@code shadow}（影）
 *   <li>{@code texts}（任意）：メッセージ枠の一覧。{@code x}・{@code y}・{@code width}（額縁に対する%）・{@code rotation}・
 *       {@code fontFamily}（フォント）・{@code fontSize}（額縁の横幅に対する%）・{@code color}・{@code
 *       align}（揃え）・{@code bold}（太字）
 * </ul>
 *
 * <p>位置・大きさを額縁に対する%で持つことで、表示する画面の幅によらず同じ比率で描画できるようにする。 写真は表示順に写真枠へ割り当て、メッセージの文面はポートフォリオごとに入力する
 */
@Component
public class FrameDefinitionPolicy {

  /** 額縁定義の項目名：サイズ */
  public static final String KEY_SIZE = "size";

  /** 額縁定義の項目名：背景 */
  public static final String KEY_BACKGROUND = "background";

  /** 額縁定義の項目名：外枠 */
  public static final String KEY_BORDER = "border";

  /** 額縁定義の項目名：写真枠の一覧 */
  public static final String KEY_SLOTS = "slots";

  /** 額縁定義の項目名：メッセージ枠の一覧 */
  public static final String KEY_TEXTS = "texts";

  /** 選択できるフォント（画面側のフォント定義のキーと一致させる） */
  public static final Set<String> FONT_FAMILIES =
      Set.of("gothic", "mincho", "maru", "handwriting", "serif", "script");

  /** 選択できるメッセージの揃え */
  public static final Set<String> TEXT_ALIGNS = Set.of("left", "center", "right");

  private static final Set<String> DEFINITION_KEYS =
      Set.of(KEY_SIZE, KEY_BACKGROUND, KEY_BORDER, KEY_SLOTS, KEY_TEXTS);

  private static final Set<String> SIZE_KEYS = Set.of("width", "height");

  private static final Set<String> BACKGROUND_KEYS = Set.of("color");

  private static final Set<String> BORDER_KEYS = Set.of("color", "width");

  private static final Set<String> SLOT_KEYS =
      Set.of("x", "y", "width", "height", "rotation", "borderColor", "borderWidth", "shadow");

  private static final Set<String> TEXT_KEYS =
      Set.of("x", "y", "width", "rotation", "fontFamily", "fontSize", "color", "align", "bold");

  private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");

  /** 額縁のサイズの下限（mm） */
  private static final double SIZE_MIN = 50;

  /** 額縁のサイズの上限（mm） */
  private static final double SIZE_MAX = 3000;

  /** 外枠の太さの上限（額縁の横幅に対する%） */
  private static final double BORDER_WIDTH_MAX = 20;

  /** 写真枠の枠線の太さの上限（額縁の横幅に対する%） */
  private static final double SLOT_BORDER_WIDTH_MAX = 10;

  /** 傾きの絶対値の上限（度） */
  private static final double ROTATION_MAX = 180;

  /** 文字サイズの下限（額縁の横幅に対する%） */
  private static final double FONT_SIZE_MIN = 0.5;

  /** 文字サイズの上限（額縁の横幅に対する%） */
  private static final double FONT_SIZE_MAX = 30;

  /**
   * 額縁定義が妥当かどうかを判定する
   *
   * <p>未知の項目を含まず、必須の項目が揃い、各項目が型と値の範囲を満たす場合に妥当とする
   *
   * @param definition 額縁定義
   * @return 妥当な場合、true
   */
  public Boolean isValid(FrameDefinition definition) {
    Map<String, Object> value = definition.value();
    if (!DEFINITION_KEYS.containsAll(value.keySet())) {
      return false;
    }
    return isValidSize(value.get(KEY_SIZE))
        && isValidBackground(value.get(KEY_BACKGROUND))
        && isValidBorder(value.get(KEY_BORDER))
        && isValidSlots(value.get(KEY_SLOTS))
        && isValidTexts(value.get(KEY_TEXTS));
  }

  /**
   * 額縁定義の写真枠の数を取得する
   *
   * @param definition 額縁定義
   * @return 写真枠の数。写真枠の一覧が未設定・配列でない場合は0
   */
  public Integer countSlots(FrameDefinition definition) {
    return definition.value().get(KEY_SLOTS) instanceof List<?> slots ? slots.size() : 0;
  }

  /**
   * 額縁定義のメッセージ枠の数を取得する
   *
   * @param definition 額縁定義
   * @return メッセージ枠の数。メッセージ枠の一覧が未設定・配列でない場合は0
   */
  public Integer countTexts(FrameDefinition definition) {
    return definition.value().get(KEY_TEXTS) instanceof List<?> texts ? texts.size() : 0;
  }

  private boolean isValidSize(Object size) {
    if (!(size instanceof Map<?, ?> sizeMap) || !SIZE_KEYS.equals(sizeMap.keySet())) {
      return false;
    }
    return isNumberWithin(sizeMap.get("width"), SIZE_MIN, SIZE_MAX)
        && isNumberWithin(sizeMap.get("height"), SIZE_MIN, SIZE_MAX);
  }

  private boolean isValidBackground(Object background) {
    if (background == null) {
      return true;
    }
    if (!(background instanceof Map<?, ?> backgroundMap)
        || !BACKGROUND_KEYS.containsAll(backgroundMap.keySet())) {
      return false;
    }
    return isOptionalColor(backgroundMap.get("color"));
  }

  private boolean isValidBorder(Object border) {
    if (border == null) {
      return true;
    }
    if (!(border instanceof Map<?, ?> borderMap) || !BORDER_KEYS.containsAll(borderMap.keySet())) {
      return false;
    }
    return isOptionalColor(borderMap.get("color"))
        && isOptionalNumberWithin(borderMap.get("width"), 0, BORDER_WIDTH_MAX);
  }

  private boolean isValidSlots(Object slots) {
    if (!(slots instanceof List<?> slotList)
        || slotList.isEmpty()
        || slotList.size() > Consts.FRAME_SLOT_MAX_SIZE) {
      return false;
    }
    return slotList.stream().allMatch(this::isValidSlot);
  }

  private boolean isValidSlot(Object slot) {
    if (!(slot instanceof Map<?, ?> slotMap) || !SLOT_KEYS.containsAll(slotMap.keySet())) {
      return false;
    }
    return isValidRect(slotMap.get("x"), slotMap.get("y"), slotMap.get("width"))
        && isPositiveNumberWithin(slotMap.get("height"), 100)
        && isOptionalNumberWithin(slotMap.get("rotation"), -ROTATION_MAX, ROTATION_MAX)
        && isOptionalColor(slotMap.get("borderColor"))
        && isOptionalNumberWithin(slotMap.get("borderWidth"), 0, SLOT_BORDER_WIDTH_MAX)
        && isOptionalBoolean(slotMap.get("shadow"));
  }

  private boolean isValidTexts(Object texts) {
    if (texts == null) {
      return true;
    }
    if (!(texts instanceof List<?> textList) || textList.size() > Consts.FRAME_TEXT_MAX_SIZE) {
      return false;
    }
    return textList.stream().allMatch(this::isValidText);
  }

  private boolean isValidText(Object text) {
    if (!(text instanceof Map<?, ?> textMap) || !TEXT_KEYS.containsAll(textMap.keySet())) {
      return false;
    }
    return isValidRect(textMap.get("x"), textMap.get("y"), textMap.get("width"))
        && isOptionalNumberWithin(textMap.get("rotation"), -ROTATION_MAX, ROTATION_MAX)
        && textMap.get("fontFamily") instanceof String fontFamily
        && FONT_FAMILIES.contains(fontFamily)
        && isNumberWithin(textMap.get("fontSize"), FONT_SIZE_MIN, FONT_SIZE_MAX)
        && isOptionalColor(textMap.get("color"))
        && (textMap.get("align") == null
            || textMap.get("align") instanceof String align && TEXT_ALIGNS.contains(align))
        && isOptionalBoolean(textMap.get("bold"));
  }

  /**
   * 左上の位置（0〜100%）と横幅（0%超〜100%）が額縁の範囲内かどうかを判定する
   *
   * @param x 左端の位置
   * @param y 上端の位置
   * @param width 横幅
   * @return 範囲内の場合、true
   */
  private boolean isValidRect(Object x, Object y, Object width) {
    return isNumberWithin(x, 0, 100)
        && isNumberWithin(y, 0, 100)
        && isPositiveNumberWithin(width, 100);
  }

  private boolean isNumberWithin(Object value, double min, double max) {
    if (!(value instanceof Number number)) {
      return false;
    }
    double doubleValue = number.doubleValue();
    return Double.isFinite(doubleValue) && doubleValue >= min && doubleValue <= max;
  }

  private boolean isPositiveNumberWithin(Object value, double max) {
    return isNumberWithin(value, 0, max) && ((Number) value).doubleValue() > 0;
  }

  private boolean isOptionalNumberWithin(Object value, double min, double max) {
    return value == null || isNumberWithin(value, min, max);
  }

  private boolean isOptionalColor(Object value) {
    return value == null || value instanceof String color && COLOR_PATTERN.matcher(color).matches();
  }

  private boolean isOptionalBoolean(Object value) {
    return value == null || value instanceof Boolean;
  }
}
