package com.web.gallery.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 写真詳細ページ表示時のリクエストパラメータを保持するクラス
 */
@Schema(description = "写真詳細リクエスト")
@Data
public class PhotoDetailRequest {
	/** アカウント番号 */
	@Schema(description = "アカウント番号", example = "1")
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Long accountNo;

	/** 写真番号 */
	@Schema(description = "写真番号", example = "1")
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Long photoNo;
}