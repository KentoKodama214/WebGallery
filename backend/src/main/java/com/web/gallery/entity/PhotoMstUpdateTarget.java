package com.web.gallery.entity;

import java.math.BigDecimal;
import java.util.Optional;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDetailModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真マスタテーブルの更新対象クラス
 */
@Data
@Builder
public class PhotoMstUpdateTarget {
	/** 更新者 */
	private UpdatedBy updatedBy;

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
	 * 写真更新用のPhotoDetailModelから更新対象を生成する
	 *
	 * @param	model	{@link PhotoDetailModel}
	 * @return			{@link PhotoMstUpdateTarget}
	 */
	public static PhotoMstUpdateTarget fromForUpdate(PhotoDetailModel model) {
		return PhotoMstUpdateTarget.builder()
				.updatedBy(new UpdatedBy(model.getAccountNo().value()))
				.isDeleted(new IsDeleted(false))
				.photoAt(new PhotoAt(
					Optional.ofNullable(model.getPhotoAt()).map(PhotoAt::value).orElse(Consts.MIN_OFFSET_DATE_TIME)))
				.locationNo(new LocationNo(
					Optional.ofNullable(model.getLocationNo()).map(LocationNo::value).orElse(0L)))
				.imageFilePath(model.getImageFilePath())
				.photoJapaneseTitle(new PhotoJapaneseTitle(
					Optional.ofNullable(model.getPhotoJapaneseTitle()).map(PhotoJapaneseTitle::value).orElse(Consts.STRING_EMPTY)))
				.photoEnglishTitle(new PhotoEnglishTitle(
					Optional.ofNullable(model.getPhotoEnglishTitle()).map(PhotoEnglishTitle::value).orElse(Consts.STRING_EMPTY)))
				.caption(new Caption(
					Optional.ofNullable(model.getCaption()).map(Caption::value).orElse(Consts.STRING_EMPTY)))
				.directionKbn(
					Optional.ofNullable(model.getDirectionKbn()).orElse(DirectionEnum.NONE))
				.focalLength(new FocalLength(
					Optional.ofNullable(model.getFocalLength()).map(FocalLength::value).orElse(0)))
				.fValue(new FValue(
					Optional.ofNullable(model.getFValue()).map(FValue::value).orElse(BigDecimal.ZERO)))
				.shutterSpeed(new ShutterSpeed(
					Optional.ofNullable(model.getShutterSpeed()).map(ShutterSpeed::value).orElse(BigDecimal.ZERO)))
				.iso(new Iso(
					Optional.ofNullable(model.getIso()).map(Iso::value).orElse(0)))
				.build();
	}

	/**
	 * 写真削除用のPhotoDeleteModelから更新対象を生成する
	 *
	 * @param	model	{@link PhotoDeleteModel}
	 * @return			{@link PhotoMstUpdateTarget}
	 */
	public static PhotoMstUpdateTarget forDelete(PhotoDeleteModel model) {
		return PhotoMstUpdateTarget.builder()
				.updatedBy(new UpdatedBy(model.getAccountNo().value()))
				.isDeleted(new IsDeleted(true))
				.build();
	}
}
