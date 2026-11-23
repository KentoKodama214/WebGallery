package com.web.gallery.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** ログイン方式を管理するEnumクラス */
@Schema(description = "ログイン方式区分")
@Getter
@AllArgsConstructor
public enum LoginMethodEnum {
  /** パスワード */
  @JsonProperty("password")
  PASSWORD("password"),
  /** パスキー */
  @JsonProperty("passkey")
  PASSKEY("passkey");

  /** DBに保持する値 */
  private final String dbValue;

  /**
   * 名称からEnumを取得する
   *
   * <p>該当するEnumがなければ、デフォルトでPASSWORDを返す
   *
   * @param value 値
   * @return {@link LoginMethodEnum}
   */
  @JsonCreator
  public static LoginMethodEnum getOrDefault(String value) {
    return Arrays.stream(LoginMethodEnum.values())
        .filter(e -> e.getDbValue().equals(value) || e.name().equals(value))
        .findFirst()
        .orElse(LoginMethodEnum.PASSWORD);
  }
}
