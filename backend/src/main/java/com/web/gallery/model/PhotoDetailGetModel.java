package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真のメタデータを含めた詳細情報を取得するときの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoDetailGetModel {
	/** ログイン中のアカウントNo */
	private AccountNo accountNo;

	/** 写真のアカウントNo */
	@NonNull
	private AccountNo photoAccountNo;

	/** 写真番号 */
	@NonNull
	private PhotoNo photoNo;

	/**
	 * パラメータからPhotoDetailGetModelを生成する
	 *
	 * @param	accountNo		ログイン中のアカウントNo
	 * @param	photoAccountNo	写真のアカウントNo
	 * @param	photoNo			写真番号
	 * @return					{@link PhotoDetailGetModel}
	 */
	public static PhotoDetailGetModel of(Long accountNo, Long photoAccountNo, Long photoNo) {
		return PhotoDetailGetModel.builder()
				.accountNo(accountNo != null ? new AccountNo(accountNo) : null)
				.photoAccountNo(new AccountNo(photoAccountNo))
				.photoNo(new PhotoNo(photoNo))
				.build();
	}
}
