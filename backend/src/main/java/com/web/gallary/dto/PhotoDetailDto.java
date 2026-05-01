package com.web.gallary.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.web.gallary.enumuration.DirectionEnum;

import lombok.Data;

/**
 * 写真のメタデータを含めた詳細情報を保持するDtoクラス
 */
@Data
public class PhotoDetailDto {
	/** アカウントNo */
	private Integer accountNo;
	
	/** 写真番号 */
	private Integer photoNo;

	/** お気に入り */
	private Boolean isFavorite;

	/** 撮影日時 */
	private OffsetDateTime photoAt;
	
	/** ロケーション番号 */
	private Integer locationNo;
	
	/** 住所 */
	private String address;

	/** 緯度 */
	private BigDecimal latitude;

	/** 経度 */
	private BigDecimal longitude;
	
	/** ロケーション名 */
	private String locationName;
	
	/** 画像ファイルパス */
	private String imageFilePath;

	/** 写真タイトル日本語名 */
	private String photoJapaneseTitle;

	/** 写真タイトル英語名 */
	private String photoEnglishTitle;
	
	/** キャプション */
	private String caption;

	/** 
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	private DirectionEnum directionKbn;

	/** 焦点距離 */
	private Integer focalLength;

	/** F値 */
	private BigDecimal fValue;

	/** シャッタースピード */
	private BigDecimal shutterSpeed;

	/** ISO */
	private Integer iso;
}