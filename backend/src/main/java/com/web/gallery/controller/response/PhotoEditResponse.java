package com.web.gallery.controller.response;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;

/**
 * 写真登録・編集のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PhotoEditResponse {
	/** HTTPステータス */
	private Integer httpStatus;

	/** 登録成功 */
	private Boolean isSuccess;

	/** メッセージ */
	private String message;

	/** 写真番号 */
	private Long photoNo;

	/** 画像ファイルパス */
	private String imageFilePath;

	/**
	 * 成功レスポンスを生成する
	 *
	 * @param	message			メッセージ
	 * @param	photoNo			写真番号
	 * @param	imageFilePath	画像ファイルパス
	 * @return					{@link PhotoEditResponse}
	 */
	public static PhotoEditResponse of(String message, Long photoNo, String imageFilePath) {
		return PhotoEditResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isSuccess(true)
				.message(message)
				.photoNo(photoNo)
				.imageFilePath(imageFilePath)
				.build();
	}
}