package com.web.gallery.dto;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.PhotoGetModel;

import lombok.Data;

/**
 * 写真の一覧を取得するパラメータDtoクラス
 */
@Data
public class PhotoListGetDto {
	/** ログイン中のアカウントNo */
	private AccountNo accountNo;

	/** 写真のアカウントNo */
	private AccountNo photoAccountNo;

	/**
	 * PhotoGetModelからPhotoListGetDtoを生成する
	 *
	 * @param	model	{@link PhotoGetModel}
	 * @return			{@link PhotoListGetDto}
	 */
	public static PhotoListGetDto from(PhotoGetModel model) {
		PhotoListGetDto dto = new PhotoListGetDto();
		dto.setAccountNo(model.getAccountNo());
		dto.setPhotoAccountNo(model.getPhotoAccountNo());
		return dto;
	}
}
