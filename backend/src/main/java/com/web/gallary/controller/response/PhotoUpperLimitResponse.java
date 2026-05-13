package com.web.gallary.controller.response;

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
}
