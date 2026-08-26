package com.web.gallery.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.model.PhotoDetailModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真マスタテーブルの抽出条件クラス
 */
@Data
@Builder
public class PhotoMstCondition {
	/** アカウント番号 */
	private Long accountNo;

	/** 写真番号 */
	private Long photoNo;

	/** 削除フラグ */
	private Boolean isDeleted;

	/** 撮影日時 */
	private OffsetDateTime photoAt;

	/** ロケーション番号 */
	private Long locationNo;

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

	/**
	 * アカウント番号・写真番号による抽出条件を生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	photoNo		写真番号
	 * @return				{@link PhotoMstCondition}
	 */
	public static PhotoMstCondition byAccountAndPhoto(Long accountNo, Long photoNo) {
		return PhotoMstCondition.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.build();
	}

	/**
	 * 写真存在チェック用の抽出条件を生成する
	 *
	 * @param	model	{@link PhotoDetailModel}
	 * @return			{@link PhotoMstCondition}
	 */
	public static PhotoMstCondition forExistCheck(PhotoDetailModel model) {
		return PhotoMstCondition.builder()
				.accountNo(model.getAccountNo().value())
				.imageFilePath(escapeLike(model.getImageFile().value().getOriginalFilename()))
				.build();
	}

	/**
	 * LIKE検索のワイルドカード文字（{@code \}・{@code %}・{@code _}）をエスケープする
	 *
	 * @param	value	エスケープ対象の文字列
	 * @return			エスケープ後の文字列
	 */
	private static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	/**
	 * 写真件数取得用の抽出条件を生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link PhotoMstCondition}
	 */
	public static PhotoMstCondition forCount(Long accountNo) {
		return PhotoMstCondition.builder()
				.accountNo(accountNo)
				.isDeleted(false)
				.build();
	}
}
