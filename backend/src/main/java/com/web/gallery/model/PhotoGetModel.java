package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;

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
	 * アカウント番号と写真アカウント番号からPhotoGetModelを生成する
	 *
	 * @param	accountNo		ログイン中のアカウントNo
	 * @param	photoAccountNo	写真のアカウントNo
	 * @return					{@link PhotoGetModel}
	 */
	public static PhotoGetModel of(AccountNo accountNo, AccountNo photoAccountNo) {
		return PhotoGetModel.builder()
				.accountNo(accountNo)
				.photoAccountNo(photoAccountNo)
				.build();
	}
}
