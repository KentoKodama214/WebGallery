package com.web.gallary.dto;

import lombok.Data;

/**
 * 写真のメタデータを含めた詳細情報を取得するパラメータDtoクラス
 */
@Data
public class PhotoDetailGetDto {
	/** ログイン中のアカウントNo */
	private Integer accountNo;
	
	/** 写真のアカウントNo */
	private Integer photoAccountNo;
	
	/** 写真番号 */
	private Integer photoNo;
}