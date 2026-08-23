package com.web.gallery.dto;

import java.util.List;
import java.util.Objects;

import com.web.gallery.enumeration.DirectionEnum;
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
	 * 向き区分（絞り込み不要の場合はnull）
	 * <p>
	 * {@link DirectionEnum}
	 */
	private DirectionEnum directionKbn;

	/** お気に入りの写真のみに絞り込むならtrue */
	private Boolean isFavoriteOnly;

	/** タグワードリスト（絞り込み不要の場合は空リストまたは空文字を含むリスト） */
	private List<String> tagList;

	/** 並び順（"PHOTO_AT" / "FAVORITE" / "SEASON"） */
	private String sortBy;

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
		dto.setDirectionKbn(DirectionEnum.NONE.equals(model.getDirectionKbn()) ? null : model.getDirectionKbn());
		dto.setIsFavoriteOnly(Objects.nonNull(model.getIsFavoriteOnly()) && model.getIsFavoriteOnly().value());
		dto.setTagList(model.getTagList());
		dto.setSortBy(Objects.nonNull(model.getSortBy()) ? model.getSortBy().name() : null);
		return dto;
	}
}
