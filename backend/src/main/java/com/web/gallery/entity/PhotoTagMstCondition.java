package com.web.gallery.entity;

import java.util.List;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.model.PhotoDetailSearchModel;
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

	/** 写真番号リスト */
	private List<PhotoNo> photoNoList;

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
	 * PhotoDetailSearchModelから抽出条件を生成する
	 *
	 * @param	model	{@link PhotoDetailSearchModel}
	 * @return			{@link PhotoTagMstCondition}
	 */
	public static PhotoTagMstCondition from(PhotoDetailSearchModel model) {
		return PhotoTagMstCondition.builder()
				.accountNo(model.getPhotoAccountNo())
				.photoNo(model.getPhotoNo())
				.build();
	}

	/**
	 * PhotoGetModelから抽出条件を生成する
	 * <p>
	 * 取得対象の写真番号リストで絞り込むことで、アカウント全体のタグではなく該当写真のタグのみを取得する
	 *
	 * @param	model			{@link PhotoGetModel}
	 * @param	photoNoList		抽出対象の写真番号リスト
	 * @return					{@link PhotoTagMstCondition}
	 */
	public static PhotoTagMstCondition from(PhotoGetModel model, List<PhotoNo> photoNoList) {
		return PhotoTagMstCondition.builder()
				.accountNo(model.getPhotoAccountNo())
				.photoNoList(photoNoList)
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
