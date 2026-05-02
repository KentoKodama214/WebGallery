package com.web.gallary.enumuration;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 写真の向きを管理するEnumクラス
 */
@Getter
@AllArgsConstructor
public enum DirectionEnum {
	/** 未選択 */
	@JsonProperty("")
	NONE("none"),
	/** 縦 */
	@JsonProperty("vertical")
	VERTICAL("vertical"),
	/** 横 */
	@JsonProperty("horizontal")
	HORIZONTAL("horizontal"),
	/** 正方形 */
	@JsonProperty("square")
	SQUARE("square");
	
	/** DBに保持する値 */
	private final String dbValue;
	
	/**
	 * 名称からEnumを取得する<p>
	 * 該当するEnumがなければ、デフォルトでNONEを返す
	 * 
	 * @param value 値
	 * @return {@link DirectionEnum}
	 */
	@JsonCreator
	public static DirectionEnum getOrDefault(String value) {
		return Arrays.stream(DirectionEnum.values())
				.filter(e -> e.getDbValue().equals(value) || e.name().equals(value))
				.findFirst()
				.orElse(DirectionEnum.NONE);
    }
}