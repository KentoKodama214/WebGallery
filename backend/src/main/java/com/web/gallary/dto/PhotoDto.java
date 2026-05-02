package com.web.gallary.dto;

import java.time.OffsetDateTime;

import com.web.gallary.enumuration.DirectionEnum;

import lombok.Data;

/**
 * 写真の基本データを保持するDtoクラス
 */
@Data
public class PhotoDto {
	/** アカウントNo */
	private Integer accountNo;
	
	/** 写真番号 */
	private Integer photoNo;

	/** お気に入り数 */
	private Integer favoriteCount;
	
	/** お気に入り */
	private Boolean isFavorite;
	
	/** 撮影日時 */
	private OffsetDateTime photoAt;
	
	/** 画像ファイルパス */
	private String imageFilePath;
	
	/** キャプション */
	private String caption;

	/** 
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	private DirectionEnum directionKbn;
}