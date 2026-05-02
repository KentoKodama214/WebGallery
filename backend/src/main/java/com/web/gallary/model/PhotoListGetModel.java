package com.web.gallary.model;

import java.util.List;

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
}