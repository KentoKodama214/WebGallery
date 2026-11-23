package com.web.gallery.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** WebAuthnチャレンジの用途を管理するEnumクラス */
@Schema(description = "WebAuthnチャレンジ用途区分")
@Getter
@AllArgsConstructor
public enum WebAuthnChallengePurposeEnum {
  /** パスキー登録 */
  @JsonProperty("registration")
  REGISTRATION("registration"),
  /** パスキー認証 */
  @JsonProperty("authentication")
  AUTHENTICATION("authentication");

  /** DBに保持する値 */
  private final String dbValue;

  /**
   * 名称からEnumを取得する
   *
   * <p>用途を取り違えたチャレンジを受け付けないよう、既定値へは倒さず該当しなければnullを返す
   *
   * @param value 値
   * @return {@link WebAuthnChallengePurposeEnum}、該当しない場合はnull
   */
  @JsonCreator
  public static WebAuthnChallengePurposeEnum getOrNull(String value) {
    return Arrays.stream(WebAuthnChallengePurposeEnum.values())
        .filter(e -> e.getDbValue().equals(value) || e.name().equals(value))
        .findFirst()
        .orElse(null);
  }
}
