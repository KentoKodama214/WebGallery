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
  /** パスキー追加登録（ログイン中のアカウントへの2件目以降の登録） */
  @JsonProperty("registration")
  REGISTRATION("registration"),
  /** パスキー認証（ログイン） */
  @JsonProperty("authentication")
  AUTHENTICATION("authentication"),
  /** アカウント新規登録（アカウントと最初のパスキーを同時に登録する） */
  @JsonProperty("signup")
  SIGNUP("signup"),
  /** 重要操作（パスキーの登録・削除、退会、リカバリーコードの再発行）の前の本人確認 */
  @JsonProperty("reauthentication")
  REAUTHENTICATION("reauthentication"),
  /** リカバリーコードによる復旧（新しいパスキーを登録してログインする） */
  @JsonProperty("recovery")
  RECOVERY("recovery");

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
