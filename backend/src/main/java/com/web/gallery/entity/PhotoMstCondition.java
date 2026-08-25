package com.web.gallery.entity;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.ShutterSpeed;
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
	private AccountNo accountNo;

	/** 写真番号 */
	private PhotoNo photoNo;

	/** 削除フラグ */
	private IsDeleted isDeleted;

	/** 撮影日時 */
	private PhotoAt photoAt;

	/** ロケーション番号 */
	private LocationNo locationNo;

	/** 画像ファイルパス */
	private ImageFilePath imageFilePath;

	/** 写真タイトル日本語名 */
	private PhotoJapaneseTitle photoJapaneseTitle;

	/** 写真タイトル英語名 */
	private PhotoEnglishTitle photoEnglishTitle;

	/** キャプション */
	private Caption caption;

	/**
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	private DirectionEnum directionKbn;

	/** 焦点距離 */
	private FocalLength focalLength;

	/** F値 */
	private FValue fValue;

	/** シャッタースピード */
	private ShutterSpeed shutterSpeed;

	/** ISO */
	private Iso iso;

	/**
	 * アカウント番号・写真番号による抽出条件を生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	photoNo		写真番号
	 * @return				{@link PhotoMstCondition}
	 */
	public static PhotoMstCondition byAccountAndPhoto(Long accountNo, Long photoNo) {
		return PhotoMstCondition.builder()
				.accountNo(new AccountNo(accountNo))
				.photoNo(new PhotoNo(photoNo))
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
				.accountNo(model.getAccountNo())
				.imageFilePath(new ImageFilePath(model.getImageFile().value().getOriginalFilename()))
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
				.accountNo(new AccountNo(accountNo))
				.isDeleted(new IsDeleted(false))
				.build();
	}
}
