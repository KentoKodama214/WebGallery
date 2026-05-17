package com.web.gallery.controller.response;

import lombok.Builder;
import lombok.Data;

/**
 * 写真登録上限チェックのレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PhotoUpperLimitResponse {
	/** 写真の登録枚数が上限に達しているか */
	private Boolean isReachedUpperLimit;

	/**
	 * レスポンスを生成する
	 *
	 * @param	isReachedUpperLimit	写真の登録枚数が上限に達しているか
	 * @return						{@link PhotoUpperLimitResponse}
	 */
	public static PhotoUpperLimitResponse of(Boolean isReachedUpperLimit) {
		return PhotoUpperLimitResponse.builder()
				.isReachedUpperLimit(isReachedUpperLimit)
				.build();
	}
}
