package com.web.gallery.model;

import com.web.gallery.controller.request.PhotoDeleteRequest;

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
	private Long accountNo;

	/** 写真番号 */
	@NonNull
	private Long photoNo;
	
	/** 画像ファイルパス */
	@NonNull
	private String imageFilePath;

	/**
	 * 写真削除リクエストからPhotoDeleteModelを生成する
	 *
	 * @param	request	{@link PhotoDeleteRequest}
	 * @return			{@link PhotoDeleteModel}
	 */
	public static PhotoDeleteModel from(PhotoDeleteRequest request) {
		return PhotoDeleteModel.builder()
				.accountNo(request.getAccountNo())
				.photoNo(request.getPhotoNo())
				.imageFilePath(request.getImageFilePath())
				.build();
	}
}