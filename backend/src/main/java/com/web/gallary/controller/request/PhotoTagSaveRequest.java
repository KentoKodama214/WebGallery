package com.web.gallary.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 写真保存時のタグ保存のリクエストパラメータを保持するクラス
 */
@Data
public class PhotoTagSaveRequest {
	/** アカウント番号 */
	@Positive(message = "{validation.common.positive}")
	private Integer accountNo;

	/** 写真番号 */
	@Positive(message = "{validation.common.positive}")
	private Integer photoNo;

	/** タグ番号 */
	@Positive(message = "{validation.common.positive}")
	private Integer tagNo;

	/** タグ日本語名 */
	@NotBlank(message = "{validation.common.notBlank}")
	@Pattern(regexp = "(?!.*( |　)).*", message = "{validation.common.disable_space}")
	private String tagJapaneseName;

	/** タグ英語名 */
	private String tagEnglishName;
}