package com.web.gallary.enumuration;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 写真の表示順を管理するEnumクラス
 */
@Getter
@AllArgsConstructor
public enum SortPhotoEnum {
	/** 撮影日順 */
	@JsonProperty("photoAt")
	PHOTO_AT("photoAt"),
	/** お気に入り数順 */
	@JsonProperty("favorite")
	FAVORITE("favorite"),
	/** 季節・時期順 */
	@JsonProperty("season")
	SEASON("season");

	/** クエリパラメータで使用する値 */
	private final String queryValue;

	/**
	 * クエリパラメータの値からEnumを取得する<p>
	 * 該当するEnumがなければ、デフォルトでPHOTO_ATを返す
	 *
	 * @param value 値
	 * @return {@link SortPhotoEnum}
	 */
	@JsonCreator
	public static SortPhotoEnum getOrDefault(String value) {
		return Arrays.stream(SortPhotoEnum.values())
				.filter(e -> e.getQueryValue().equals(value) || e.name().equals(value))
				.findFirst()
				.orElse(SortPhotoEnum.PHOTO_AT);
	}
}