package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真タグを削除するときの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoTagDeleteModel {
	/** アカウント番号 */
	@NonNull
	private AccountNo accountNo;

	/** 写真番号 */
	@NonNull
	private PhotoNo photoNo;

	/**
	 * アカウント番号と写真番号からPhotoTagDeleteModelを生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	photoNo		写真番号
	 * @return				{@link PhotoTagDeleteModel}
	 */
	public static PhotoTagDeleteModel of(AccountNo accountNo, PhotoNo photoNo) {
		return PhotoTagDeleteModel.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.build();
	}
}
