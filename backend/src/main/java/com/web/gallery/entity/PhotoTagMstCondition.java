package com.web.gallery.entity;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoTagDeleteModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真タグマスタテーブルの抽出条件クラス
 */
@Data
@Builder
public class PhotoTagMstCondition {
	/** アカウント番号 */
	private AccountNo accountNo;

	/** 写真番号 */
	private PhotoNo photoNo;

	/** タグ番号 */
	private TagNo tagNo;

	/** タグ日本語名 */
	private TagJapaneseName tagJapaneseName;

	/** タグ英語名 */
	private TagEnglishName tagEnglishName;

	/**
	 * 写真タグ削除用のPhotoTagDeleteModelから抽出条件を生成する
	 *
	 * @param	model	{@link PhotoTagDeleteModel}
	 * @return			{@link PhotoTagMstCondition}
	 */
	public static PhotoTagMstCondition from(PhotoTagDeleteModel model) {
		return PhotoTagMstCondition.builder()
				.accountNo(model.getAccountNo())
				.photoNo(model.getPhotoNo())
				.build();
	}

	/**
	 * PhotoDetailGetModelから抽出条件を生成する
	 *
	 * @param	model	{@link PhotoDetailGetModel}
	 * @return			{@link PhotoTagMstCondition}
	 */
	public static PhotoTagMstCondition from(PhotoDetailGetModel model) {
		return PhotoTagMstCondition.builder()
				.accountNo(model.getPhotoAccountNo())
				.photoNo(model.getPhotoNo())
				.build();
	}

	/**
	 * PhotoGetModelから抽出条件を生成する
	 *
	 * @param	model	{@link PhotoGetModel}
	 * @return			{@link PhotoTagMstCondition}
	 */
	public static PhotoTagMstCondition from(PhotoGetModel model) {
		return PhotoTagMstCondition.builder()
				.accountNo(model.getPhotoAccountNo())
				.build();
	}

	/**
	 * アカウント番号で写真タグ削除用の抽出条件を生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link PhotoTagMstCondition}
	 */
	public static PhotoTagMstCondition byAccountNo(AccountNo accountNo) {
		return PhotoTagMstCondition.builder()
				.accountNo(accountNo)
				.build();
	}
}
