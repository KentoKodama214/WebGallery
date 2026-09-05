package com.web.gallery.enumeration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.web.gallery.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** 性別を管理するEnumクラス */
@Schema(description = "性別区分")
@Slf4j
@Getter
@AllArgsConstructor
public enum SexEnum {
  /** 未選択 */
  @JsonProperty(Consts.STRING_NONE)
  NONE(Consts.STRING_NONE),
  /** 男性 */
  @JsonProperty("man")
  MAN("man"),
  /** 女性 */
  @JsonProperty("woman")
  WOMAN("woman");

  /** DBに保持する値 */
  private final String dbValue;

  /**
   * 名称からEnumを取得する
   *
   * <p>該当するEnumがなければ、デフォルトでNONEを返す
   *
   * @param value 値
   * @return {@link SexEnum}
   */
  public static SexEnum getOrDefault(String value) {
    if (Objects.isNull(value)) {
      return SexEnum.NONE;
    }

    try {
      String upperValue = value.toUpperCase();
      return SexEnum.valueOf(upperValue);
    } catch (IllegalArgumentException | NullPointerException e) {
      log.info("SexEnum: Invalid value. (value: {})", value);
      return SexEnum.NONE;
    }
  }

  /**
   * 未設定の場合にデフォルトでNONEを返す
   *
   * @param nullable {@link SexEnum}（null許容）
   * @return nullableがnullでなければそのまま、nullであればNONE
   */
  public static SexEnum getOrDefault(SexEnum nullable) {
    return nullable != null ? nullable : SexEnum.NONE;
  }
}
