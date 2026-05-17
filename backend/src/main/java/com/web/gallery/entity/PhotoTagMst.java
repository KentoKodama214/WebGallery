package com.web.gallery.entity;

import java.time.OffsetDateTime;

import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真タグマスタテーブルのEntityクラス
 */
@Data
@Builder
public class PhotoTagMst {
	/** ID */
	private Integer id;

	/** アカウント番号 */
	private Integer accountNo;

	/** 写真番号 */
	private Integer photoNo;

	/** タグ番号 */
	private Integer tagNo;

	/** 作成者 */
	private Integer createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** タグ日本語名 */
	private String tagJapaneseName;

	/** タグ英語名 */
	private String tagEnglishName;

	/**
	 * PhotoTagModelからPhotoTagMstエンティティを生成する
	 *
	 * @param	model	{@link PhotoTagModel}
	 * @return			{@link PhotoTagMst}
	 */
	public static PhotoTagMst from(PhotoTagModel model) {
		return PhotoTagMst.builder()
				.accountNo(model.getAccountNo())
				.photoNo(model.getPhotoNo())
				.tagNo(model.getTagNo())
				.createdBy(model.getAccountNo())
				.tagJapaneseName(model.getTagJapaneseName())
				.tagEnglishName(model.getTagEnglishName())
				.build();
	}

	/**
	 * 写真タグ削除用のPhotoTagDeleteModelからPhotoTagMstエンティティを生成する
	 *
	 * @param	model	{@link PhotoTagDeleteModel}
	 * @return			{@link PhotoTagMst}
	 */
	public static PhotoTagMst from(PhotoTagDeleteModel model) {
		return PhotoTagMst.builder()
				.accountNo(model.getAccountNo())
				.photoNo(model.getPhotoNo())
				.build();
	}

	/**
	 * PhotoDetailGetModelから条件用のPhotoTagMstエンティティを生成する
	 *
	 * @param	model	{@link PhotoDetailGetModel}
	 * @return			{@link PhotoTagMst}
	 */
	public static PhotoTagMst condition(PhotoDetailGetModel model) {
		return PhotoTagMst.builder()
				.accountNo(model.getPhotoAccountNo())
				.photoNo(model.getPhotoNo())
				.build();
	}

	/**
	 * PhotoGetModelから条件用のPhotoTagMstエンティティを生成する
	 *
	 * @param	model	{@link PhotoGetModel}
	 * @return			{@link PhotoTagMst}
	 */
	public static PhotoTagMst condition(PhotoGetModel model) {
		return PhotoTagMst.builder()
				.accountNo(model.getPhotoAccountNo())
				.build();
	}
}