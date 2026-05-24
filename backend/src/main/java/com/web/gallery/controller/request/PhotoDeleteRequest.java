package com.web.gallery.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 写真削除時のリクエストパラメータを保持するクラス
 */
@Data
public class PhotoDeleteRequest {
	/** アカウント番号 */
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Long accountNo;

	/** 写真番号 */
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Long photoNo;
	
	/** 画像ファイルパス */
	@NotBlank(message = "{validation.common.notBlank}")
	private String imageFilePath;
}