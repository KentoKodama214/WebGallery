package com.web.gallary.model;

import java.time.OffsetDateTime;
import java.util.List;

import com.web.gallary.enumuration.DirectionEnum;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真の情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoModel {
	/** アカウント番号 */
	@NonNull
	private Integer accountNo;
	
	/** 写真番号 */
	@NonNull
	private Integer photoNo;

	/** お気に入り数 */
	@NonNull
	private Integer favoriteCount;
	
	/** お気に入り */
	@NonNull
	private Boolean isFavorite;
	
	/** 撮影日時 */
	@NonNull
	private OffsetDateTime photoAt;
	
	/** 画像ファイルパス */
	@NonNull
	private String imageFilePath;
	
	/** キャプション */
	@NonNull
	private String caption;

	/** 
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	@NonNull
	private DirectionEnum directionKbn;
	
	/** 写真タグリスト */
	@NonNull
	private List<PhotoTagModel> photoTagModelList;
}