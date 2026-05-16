package com.web.gallary.model;

import java.util.List;
import java.util.Optional;

import com.web.gallary.controller.request.PhotoListRequest;
import com.web.gallary.enumuration.DirectionEnum;
import com.web.gallary.enumuration.SortPhotoEnum;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真の一覧を取得するために必要な情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoListGetModel {
	/** ログイン中のアカウントNo */
	private Integer accountNo;
	
	/** 写真のアカウントID */
	@NonNull
	private String photoAccountId;
	
	/** 
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	@NonNull
	private DirectionEnum directionKbn;
	
	/** お気に入り写真のみ */
	@NonNull
	private Boolean isFavoriteOnly;
	
	/** タグワードリスト */
	@NonNull
	private List<String> tagList;
	
	/**
	 * 並び順
	 * <p>
	 * {@link SortPhotoEnum}
	 */
	@NonNull
	private SortPhotoEnum sortBy;

	/**
	 * 写真一覧リクエストからPhotoListGetModelを生成する
	 *
	 * @param	request			{@link PhotoListRequest}
	 * @param	accountNo		ログイン中のアカウントNo
	 * @param	photoAccountId	写真のアカウントID
	 * @param	tagList			タグワードリスト
	 * @return					{@link PhotoListGetModel}
	 */
	public static PhotoListGetModel from(PhotoListRequest request, Integer accountNo, String photoAccountId, List<String> tagList) {
		return PhotoListGetModel.builder()
				.accountNo(accountNo)
				.photoAccountId(photoAccountId)
				.directionKbn(request.getDirectionKbn())
				.isFavoriteOnly(Optional.ofNullable(request.getIsFavorite()).orElse(Boolean.FALSE))
				.tagList(tagList)
				.sortBy(request.getSortBy())
				.build();
	}
}