package com.web.gallery.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 写真削除時のリクエストパラメータを保持するクラス
 */
@Schema(description = "写真削除リクエスト")
@Data
public class PhotoDeleteRequest {
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

	/** 画像ファイルパス */
	@Schema(description = "画像ファイルパス", example = "/photos/testuser01/photo1.jpg")
	@NotBlank(message = "{validation.common.notBlank}")
	private String imageFilePath;
}