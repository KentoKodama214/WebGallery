package com.web.gallery.controller.response;

import com.web.gallery.model.KbnMstModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 都道府県のレスポンスパラメータを保持するクラス
 */
@Schema(description = "都道府県レスポンス")
@Data
@Builder
public class PrefectureResponse {
	/** 区分コード */
	@Schema(description = "区分コード", example = "Hokkaido")
	private String kbnCode;

	/** 区分日本語名 */
	@Schema(description = "都道府県名", example = "北海道")
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
