package com.web.gallery.dto;

import java.util.Objects;

import com.web.gallery.model.PhotoGetModel;

import lombok.Data;

/**
 * 写真の一覧を取得するパラメータDtoクラス
 */
@Data
public class PhotoListGetDto {
	/** ログイン中のアカウントNo */
	private Long accountNo;

	/** 写真のアカウントNo */
	private Long photoAccountNo;

	/**
	 * PhotoGetModelからPhotoListGetDtoを生成する
	 *
	 * @param	model	{@link PhotoGetModel}
	 * @return			{@link PhotoListGetDto}
	 */
	public static PhotoListGetDto from(PhotoGetModel model) {
		PhotoListGetDto dto = new PhotoListGetDto();
		dto.setAccountNo(Objects.nonNull(model.getAccountNo()) ? model.getAccountNo().value() : null);
		dto.setPhotoAccountNo(model.getPhotoAccountNo().value());
		return dto;
	}
}
