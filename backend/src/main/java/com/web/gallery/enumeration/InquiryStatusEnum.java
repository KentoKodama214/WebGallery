package com.web.gallery.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** お問い合わせのステータスを管理するEnumクラス */
@Schema(description = "お問い合わせステータス区分")
@Getter
@AllArgsConstructor
public enum InquiryStatusEnum {
  /** 未対応 */
  @JsonProperty("unreplied")
  UNREPLIED("unreplied"),
  /** 回答済み */
  @JsonProperty("replied")
  REPLIED("replied");

  /** DBに保持する値 */
  private final String dbValue;

  /**
   * 名称からEnumを取得する
   *
   * <p>該当するEnumがなければ、デフォルトでUNREPLIEDを返す
   *
   * @param value 値
   * @return {@link InquiryStatusEnum}
   */
  @JsonCreator
  public static InquiryStatusEnum getOrDefault(String value) {
    return Arrays.stream(InquiryStatusEnum.values())
        .filter(e -> e.getDbValue().equals(value) || e.name().equals(value))
        .findFirst()
        .orElse(InquiryStatusEnum.UNREPLIED);
  }
}
