package com.web.gallery.controller.response;

import com.web.gallery.model.KbnMstModel;

import lombok.Builder;
import lombok.Data;

/**
 * 都道府県のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PrefectureResponse {
	/** 区分コード */
	private String kbnCode;

	/** 区分日本語名 */
	private String kbnJapaneseName;

	/**
	 * KbnMstModelからPrefectureResponseを生成する
	 *
	 * @param	model	{@link KbnMstModel}
	 * @return			{@link PrefectureResponse}
	 */
	public static PrefectureResponse from(KbnMstModel model) {
		return PrefectureResponse.builder()
				.kbnCode(model.getKbnCode())
				.kbnJapaneseName(model.getKbnJapaneseName())
				.build();
	}
}
