package com.web.gallery.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 並び順の移動方向を管理するEnumクラス */
@Schema(description = "並び順の移動方向")
@Getter
@AllArgsConstructor
public enum MoveDirectionEnum {
  /** 1つ上（前）へ */
  @JsonProperty("up")
  UP("up"),
  /** 1つ下（後ろ）へ */
  @JsonProperty("down")
  DOWN("down");

  /** リクエストで使用する値 */
  private final String requestValue;

  /**
   * 値からEnumを取得する
   *
   * <p>移動方向の指定誤りを黙って別の方向として扱わないよう、該当するEnumがなければnullを返す（リクエストの必須チェックで不正値として扱われる）
   *
   * @param value 値
   * @return {@link MoveDirectionEnum}。該当するEnumがなければnull
   */
  @JsonCreator
  public static MoveDirectionEnum fromValue(String value) {
    return Arrays.stream(MoveDirectionEnum.values())
        .filter(e -> e.getRequestValue().equals(value) || e.name().equals(value))
        .findFirst()
        .orElse(null);
  }
}
