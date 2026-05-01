package com.web.gallary.dto;

import lombok.Data;

/**
 * 写真の一覧を取得するパラメータDtoクラス
 */
@Data
public class PhotoListGetDto {
	/** ログイン中のアカウントNo */
	private Integer accountNo;
	
	/** 写真のアカウントNo */
	private Integer photoAccountNo;
}