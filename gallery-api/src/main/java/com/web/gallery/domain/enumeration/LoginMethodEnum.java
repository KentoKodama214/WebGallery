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
  /** パスキー */
  @JsonProperty("passkey")
  PASSKEY("passkey"),
  /** リカバリーコードによる復旧（リカバリーコードで本人確認し、新しいパスキーを登録してログインした） */
  @JsonProperty("recovery_code")
  RECOVERY_CODE("recovery_code");

  /** DBに保持する値 */
  private final String dbValue;

  /**
   * 名称からEnumを取得する
   *
   * <p>該当するEnumがなければ、デフォルトでPASSKEYを返す
   *
   * @param value 値
   * @return {@link LoginMethodEnum}
   */
  @JsonCreator
  public static LoginMethodEnum getOrDefault(String value) {
    return Arrays.stream(LoginMethodEnum.values())
        .filter(e -> e.getDbValue().equals(value) || e.name().equals(value))
        .findFirst()
        .orElse(LoginMethodEnum.PASSKEY);
  }
}
