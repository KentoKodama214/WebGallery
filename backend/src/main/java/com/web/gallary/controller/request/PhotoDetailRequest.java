package com.web.gallary.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 写真詳細ページ表示時のリクエストパラメータを保持するクラス
 */
@Data
public class PhotoDetailRequest {
	/** アカウント番号 */
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Integer accountNo;
	
	/** 写真番号 */
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Integer photoNo;
}