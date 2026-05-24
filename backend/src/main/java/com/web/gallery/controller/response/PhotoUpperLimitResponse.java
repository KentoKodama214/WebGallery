package com.web.gallery.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 写真登録上限チェックのレスポンスパラメータを保持するクラス
 */
@Schema(description = "写真登録上限チェックレスポンス")
@Data
@Builder
public class PhotoUpperLimitResponse {
	/** 写真の登録枚数が上限に達しているか */
	@Schema(description = "写真の登録枚数が上限に達しているか")
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
