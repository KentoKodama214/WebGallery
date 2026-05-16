package com.web.gallary.controller.response;

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
}
