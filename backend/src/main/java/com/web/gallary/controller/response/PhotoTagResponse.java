package com.web.gallary.controller.response;

import com.web.gallary.model.PhotoTagModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真タグのレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PhotoTagResponse {
	/** アカウント番号 */
	private Integer accountNo;

	/** 写真番号 */
	private Integer photoNo;

	/** タグ番号 */
	private Integer tagNo;

	/** タグ日本語名 */
	private String tagJapaneseName;

	/** タグ英語名 */
	private String tagEnglishName;

	/**
	 * PhotoTagModelからPhotoTagResponseを生成する
	 *
	 * @param	model	{@link PhotoTagModel}
	 * @return			{@link PhotoTagResponse}
	 */
	public static PhotoTagResponse from(PhotoTagModel model) {
		return PhotoTagResponse.builder()
				.accountNo(model.getAccountNo())
				.photoNo(model.getPhotoNo())
				.tagNo(model.getTagNo())
				.tagJapaneseName(model.getTagJapaneseName())
				.tagEnglishName(model.getTagEnglishName())
				.build();
	}
}
