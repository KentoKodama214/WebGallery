package com.web.gallery.model;

import com.web.gallery.controller.request.PhotoDeleteRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真を削除するときの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoDeleteModel {
	/** アカウント番号 */
	@NonNull
	private AccountNo accountNo;

	/** 写真番号 */
	@NonNull
	private PhotoNo photoNo;

	/** 画像ファイルパス */
	@NonNull
	private ImageFilePath imageFilePath;

	/**
	 * 写真削除リクエストとログイン中のアカウント番号からPhotoDeleteModelを生成する<p>
	 * アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の写真を操作するIDORを防ぐため）
	 *
	 * @param	request		{@link PhotoDeleteRequest}
	 * @param	accountNo	ログイン中のアカウント番号
	 * @return				{@link PhotoDeleteModel}
	 */
	public static PhotoDeleteModel from(PhotoDeleteRequest request, AccountNo accountNo) {
		return PhotoDeleteModel.builder()
				.accountNo(accountNo)
				.photoNo(new PhotoNo(request.getPhotoNo()))
				.imageFilePath(new ImageFilePath(request.getImageFilePath()))
				.build();
	}
}
