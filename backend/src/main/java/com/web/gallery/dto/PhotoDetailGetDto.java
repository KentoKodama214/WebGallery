package com.web.gallery.dto;

import lombok.Data;

/**
 * 写真のメタデータを含めた詳細情報を取得するパラメータDtoクラス
 */
@Data
public class PhotoDetailGetDto {
	/** ログイン中のアカウントNo */
	private Long accountNo;

	/** 写真のアカウントNo */
	private Long photoAccountNo;

	/** 写真番号 */
	private Long photoNo;
}