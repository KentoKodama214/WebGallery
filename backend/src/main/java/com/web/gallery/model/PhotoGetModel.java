package com.web.gallery.model;

import java.util.List;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真の情報を取得する時の情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoGetModel {
	/** ログイン中のアカウントNo */
	private AccountNo accountNo;

	/** 写真のアカウントNo */
	@NonNull
	private AccountNo photoAccountNo;

	/**
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	@NonNull
	private DirectionEnum directionKbn;

	/** お気に入り写真のみ */
	private IsFavoriteOnly isFavoriteOnly;

	/** タグワードリスト */
	@NonNull
	private List<String> tagList;

	/**
	 * 並び順
	 * <p>
	 * {@link SortPhotoEnum}
	 */
	@NonNull
	private SortPhotoEnum sortBy;

	/**
	 * PhotoListGetModelと写真のアカウント番号からPhotoGetModelを生成する
	 *
	 * @param	photoListGetModel	{@link PhotoListGetModel}
	 * @param	photoAccountNo		写真のアカウントNo
	 * @return						{@link PhotoGetModel}
	 */
	public static PhotoGetModel of(PhotoListGetModel photoListGetModel, AccountNo photoAccountNo) {
		return PhotoGetModel.builder()
				.accountNo(photoListGetModel.getAccountNo())
				.photoAccountNo(photoAccountNo)
				.directionKbn(photoListGetModel.getDirectionKbn())
				.isFavoriteOnly(photoListGetModel.getIsFavoriteOnly())
				.tagList(photoListGetModel.getTagList())
				.sortBy(photoListGetModel.getSortBy())
				.build();
	}
}
