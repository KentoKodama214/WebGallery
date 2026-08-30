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
	 * タグ登録用にaccountNo・photoNo・tagNoを差し替えたPhotoTagModelを生成する<p>
	 * アカウント番号は元のPhotoTagModelの値ではなく、集約ルート（＝写真の所有者）の値を必ず採用する
	 * （元の値はクライアント入力由来のため、他人の写真へのタグ注入を防ぐ）
	 *
	 * @param	source		元のPhotoTagModel
	 * @param	accountNo	写真所有者のアカウント番号
	 * @param	photoNo		写真番号
	 * @param	tagNo		タグ番号
	 * @return				{@link PhotoTagModel}
	 */
	public static PhotoTagModel forRegist(PhotoTagModel source, AccountNo accountNo, PhotoNo photoNo, TagNo tagNo) {
		return PhotoTagModel.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.tagNo(tagNo)
				.tagJapaneseName(source.getTagJapaneseName())
				.tagEnglishName(source.getTagEnglishName())
				.build();
	}

	/**
	 * 写真タグ保存リクエストとログイン中のアカウント番号からPhotoTagModelを生成する<p>
	 * アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の写真へタグを注入するIDORを防ぐため）。
	 * 写真番号・タグ番号は登録時にサーバ側で採番するためここでは設定しない
	 *
	 * @param	request		{@link PhotoTagSaveRequest}
	 * @param	accountNo	ログイン中のアカウント番号
	 * @return				{@link PhotoTagModel}
	 */
	public static PhotoTagModel from(PhotoTagSaveRequest request, AccountNo accountNo) {
		return PhotoTagModel.builder()
				.accountNo(accountNo)
				.tagJapaneseName(new TagJapaneseName(Optional.ofNullable(request.getTagJapaneseName()).orElse(Consts.STRING_EMPTY)))
				.tagEnglishName(new TagEnglishName(Optional.ofNullable(request.getTagEnglishName()).orElse(Consts.STRING_EMPTY)))
				.build();
	}
}
