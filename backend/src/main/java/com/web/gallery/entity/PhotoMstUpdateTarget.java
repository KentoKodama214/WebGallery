package com.web.gallery.entity;

import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
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
		ExifData exifData = model.getExifData();
		return PhotoMstUpdateTarget.builder()
				.updatedBy(new UpdatedBy(model.getAccountNo().value()))
				.isDeleted(new IsDeleted(false))
				.photoAt(PhotoAt.getOrDefault(model.getPhotoAt()))
				.locationNo(LocationNo.getOrDefault(model.getLocationNo()))
				.imageFilePath(model.getImageFilePath())
				.photoJapaneseTitle(PhotoJapaneseTitle.getOrDefault(model.getPhotoJapaneseTitle()))
				.photoEnglishTitle(PhotoEnglishTitle.getOrDefault(model.getPhotoEnglishTitle()))
				.caption(Caption.getOrDefault(model.getCaption()))
				.directionKbn(DirectionEnum.getOrDefault(model.getDirectionKbn()))
				.focalLength(FocalLength.getOrDefault(exifData.focalLength()))
				.fValue(FValue.getOrDefault(exifData.fValue()))
				.shutterSpeed(ShutterSpeed.getOrDefault(exifData.shutterSpeed()))
				.iso(Iso.getOrDefault(exifData.iso()))
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
