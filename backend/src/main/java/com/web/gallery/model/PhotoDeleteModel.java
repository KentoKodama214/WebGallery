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
	 * 写真削除リクエストからPhotoDeleteModelを生成する
	 *
	 * @param	request	{@link PhotoDeleteRequest}
	 * @return			{@link PhotoDeleteModel}
	 */
	public static PhotoDeleteModel from(PhotoDeleteRequest request) {
		return PhotoDeleteModel.builder()
				.accountNo(new AccountNo(request.getAccountNo()))
				.photoNo(new PhotoNo(request.getPhotoNo()))
				.imageFilePath(new ImageFilePath(request.getImageFilePath()))
				.build();
	}
}
