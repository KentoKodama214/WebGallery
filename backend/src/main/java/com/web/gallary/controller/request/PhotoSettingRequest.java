package com.web.gallary.controller.request;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.web.gallary.enumuration.DirectionEnum;

import lombok.Data;

/**
 * 写真登録・編集ページ表示時のリクエストパラメータを保持するクラス
 */
@Data
public class PhotoSettingRequest {
	/** アカウント番号 */
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
	@JsonSetter(nulls = Nulls.SKIP)
	private DirectionEnum directionKbn = DirectionEnum.NONE;

	/** 焦点距離 */
	private Integer focalLength;

	/** F値 */
	private BigDecimal fValue;

	/** シャッタースピード */
	private BigDecimal shutterSpeed;

	/** ISO */
	private Integer iso;
	
	/** 写真タグリスト */
	private List<PhotoTagRequest> photoTagRequestList;
}