package com.web.gallery.dto;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;

import lombok.Data;

/**
 * 写真のメタデータを含めた詳細情報を取得するパラメータDtoクラス
 */
@Data
public class PhotoDetailGetDto {
	/** ログイン中のアカウントNo */
	private AccountNo accountNo;

	/** 写真のアカウントNo */
	private AccountNo photoAccountNo;

	/** 写真番号 */
	private PhotoNo photoNo;
}
