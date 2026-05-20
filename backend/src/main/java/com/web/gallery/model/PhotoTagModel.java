package com.web.gallery.model;

import java.util.Optional;

import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.PhotoTagSaveRequest;
import com.web.gallery.entity.PhotoTagMst;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真タグの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoTagModel {
	/** アカウント番号 */
	@NonNull
	private Integer accountNo;

	/** 写真番号 */
	private Integer photoNo;

	/** タグ番号 */
	private Integer tagNo;

	/** タグ日本語名 */
	@NonNull
	private String tagJapaneseName;

	/** タグ英語名 */
	@NonNull
	private String tagEnglishName;

	/**
	 * PhotoTagMstエンティティからPhotoTagModelを生成する
	 *
	 * @param	entity	{@link PhotoTagMst}
	 * @return			{@link PhotoTagModel}
	 */
	public static PhotoTagModel from(PhotoTagMst entity) {
		return PhotoTagModel.builder()
				.accountNo(entity.getAccountNo())
				.photoNo(entity.getPhotoNo())
				.tagNo(entity.getTagNo())
				.tagJapaneseName(entity.getTagJapaneseName())
				.tagEnglishName(entity.getTagEnglishName())
				.build();
	}

	/**
	 * 写真タグ保存リクエストからPhotoTagModelを生成する
	 *
	 * @param	request	{@link PhotoTagSaveRequest}
	 * @return			{@link PhotoTagModel}
	 */
	public static PhotoTagModel from(PhotoTagSaveRequest request) {
		return PhotoTagModel.builder()
				.accountNo(request.getAccountNo())
				.photoNo(request.getPhotoNo())
				.tagNo(request.getTagNo())
				.tagJapaneseName(Optional.ofNullable(request.getTagJapaneseName()).orElse(Consts.STRING_EMPTY))
				.tagEnglishName(Optional.ofNullable(request.getTagEnglishName()).orElse(Consts.STRING_EMPTY))
				.build();
	}
}