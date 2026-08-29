package com.web.gallery.entity;

import java.io.File;
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

	/** 画像ファイル名 */
	private String imageFileName;

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
	 * アカウント番号・写真番号による抽出条件を生成する（削除済みを除外）<p>
	 * 更新・削除対象のWHERE句に使用し、削除済みの写真への二重更新・二重削除を防ぐ
	 *
	 * @param	accountNo	アカウント番号
	 * @param	photoNo		写真番号
	 * @return				{@link PhotoMstCondition}
	 */
	public static PhotoMstCondition byAccountAndPhotoNotDeleted(Long accountNo, Long photoNo) {
		return PhotoMstCondition.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.isDeleted(false)
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
				.imageFileName(new File(model.getImageFile().value().getOriginalFilename()).getName())
				.build();
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
