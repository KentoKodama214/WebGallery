package com.web.gallery.controller.response;

import com.web.gallery.model.PhotoTagModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 写真タグのレスポンスパラメータを保持するクラス
 */
@Schema(description = "写真タグレスポンス")
@Data
@Builder
public class PhotoTagResponse {
	/** アカウント番号 */
	@Schema(description = "アカウント番号", example = "1")
	private Long accountNo;

	/** 写真番号 */
	@Schema(description = "写真番号", example = "1")
	private Long photoNo;

	/** タグ番号 */
	@Schema(description = "タグ番号", example = "1")
	private Long tagNo;

	/** タグ日本語名 */
	@Schema(description = "タグ日本語名", example = "風景")
	private String tagJapaneseName;

	/** タグ英語名 */
	@Schema(description = "タグ英語名", example = "landscape")
	private String tagEnglishName;

	/**
	 * PhotoTagModelからPhotoTagResponseを生成する
	 *
	 * @param	model	{@link PhotoTagModel}
	 * @return			{@link PhotoTagResponse}
	 */
	public static PhotoTagResponse from(PhotoTagModel model) {
		return PhotoTagResponse.builder()
				.accountNo(model.getAccountNo().value())
				.photoNo(model.getPhotoNo())
				.tagNo(model.getTagNo())
				.tagJapaneseName(model.getTagJapaneseName())
				.tagEnglishName(model.getTagEnglishName())
				.build();
	}
}
