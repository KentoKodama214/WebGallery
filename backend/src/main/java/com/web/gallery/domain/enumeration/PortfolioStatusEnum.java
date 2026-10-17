package com.web.gallery.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** ポートフォリオの公開状態を管理するEnumクラス */
@Schema(description = "ポートフォリオの公開状態区分")
@Getter
@AllArgsConstructor
public enum PortfolioStatusEnum {
  /** 下書き */
  @JsonProperty("draft")
  DRAFT("draft"),
  /** 公開 */
  @JsonProperty("published")
  PUBLISHED("published");

  /** DBに保持する値 */
  private final String dbValue;

  /**
   * 値からEnumを取得する
   *
   * <p>公開状態の指定誤りを黙って別の状態として扱わないよう、該当するEnumがなければnullを返す（リクエストの必須チェックで不正値として扱われる）
   *
   * @param value 値
   * @return {@link PortfolioStatusEnum}。該当するEnumがなければnull
   */
  @JsonCreator
  public static PortfolioStatusEnum fromValue(String value) {
    return Arrays.stream(PortfolioStatusEnum.values())
        .filter(e -> e.getDbValue().equals(value) || e.name().equals(value))
        .findFirst()
        .orElse(null);
  }
}
