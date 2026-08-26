package com.web.gallery.model;

import java.util.Optional;

import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.PhotoTagSaveRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
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
	private AccountNo accountNo;

	/** 写真番号 */
	private PhotoNo photoNo;

	/** タグ番号 */
	private TagNo tagNo;

	/** タグ日本語名 */
	@NonNull
	private TagJapaneseName tagJapaneseName;

	/** タグ英語名 */
	@NonNull
	private TagEnglishName tagEnglishName;

	/**
	 * PhotoTagMstエンティティからPhotoTagModelを生成する
	 *
	 * @param	entity	{@link PhotoTagMst}
	 * @return			{@link PhotoTagModel}
	 */
	public static PhotoTagModel from(PhotoTagMst entity) {
		return PhotoTagModel.builder()
				.accountNo(new AccountNo(entity.getAccountNo()))
				.photoNo(new PhotoNo(entity.getPhotoNo()))
				.tagNo(new TagNo(entity.getTagNo()))
				.tagJapaneseName(new TagJapaneseName(entity.getTagJapaneseName()))
				.tagEnglishName(new TagEnglishName(entity.getTagEnglishName()))
				.build();
	}

	/**
	 * タグ登録用にphotoNoとtagNoを差し替えたPhotoTagModelを生成する
	 *
	 * @param	source	元のPhotoTagModel
	 * @param	photoNo	写真番号
	 * @param	tagNo	タグ番号
	 * @return			{@link PhotoTagModel}
	 */
	public static PhotoTagModel forRegist(PhotoTagModel source, PhotoNo photoNo, TagNo tagNo) {
		return PhotoTagModel.builder()
				.accountNo(source.getAccountNo())
				.photoNo(photoNo)
				.tagNo(tagNo)
				.tagJapaneseName(source.getTagJapaneseName())
				.tagEnglishName(source.getTagEnglishName())
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
				.accountNo(new AccountNo(request.getAccountNo()))
				.photoNo(request.getPhotoNo() != null ? new PhotoNo(request.getPhotoNo()) : null)
				.tagNo(request.getTagNo() != null ? new TagNo(request.getTagNo()) : null)
				.tagJapaneseName(new TagJapaneseName(Optional.ofNullable(request.getTagJapaneseName()).orElse(Consts.STRING_EMPTY)))
				.tagEnglishName(new TagEnglishName(Optional.ofNullable(request.getTagEnglishName()).orElse(Consts.STRING_EMPTY)))
				.build();
	}
}
