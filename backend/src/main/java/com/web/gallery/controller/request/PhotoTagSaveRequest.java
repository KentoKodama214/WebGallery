package com.web.gallery.controller.request;

import com.web.gallery.constant.Consts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 写真保存時のタグ保存のリクエストパラメータを保持するクラス
 */
@Schema(description = "写真タグ保存リクエスト")
@Data
public class PhotoTagSaveRequest {
	/** アカウント番号 */
	@Schema(description = "アカウント番号", example = "1")
	@Positive(message = "{validation.common.positive}")
	private Long accountNo;

	/** 写真番号 */
	@Schema(description = "写真番号", example = "1")
	@Positive(message = "{validation.common.positive}")
	private Long photoNo;

	/** タグ番号 */
	@Schema(description = "タグ番号", example = "1")
	@Positive(message = "{validation.common.positive}")
	private Long tagNo;

	/** タグ日本語名 */
	@Schema(description = "タグ日本語名", example = "風景")
	@NotBlank(message = "{validation.common.notBlank}")
	@Size(max = Consts.TAG_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
	@Pattern(regexp = "(?!.*( |　)).*", message = "{validation.common.disable_space}")
	private String tagJapaneseName;

	/** タグ英語名 */
	@Schema(description = "タグ英語名", example = "landscape")
	@Size(max = Consts.TAG_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
	@Pattern(regexp = "(?!.*( |　)).*", message = "{validation.common.disable_space}")
	private String tagEnglishName;
}