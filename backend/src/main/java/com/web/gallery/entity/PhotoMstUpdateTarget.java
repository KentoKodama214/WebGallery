package com.web.gallery.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.web.gallery.constant.Consts;
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
	private Long updatedBy;

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
	 * 写真更新用のPhotoDetailModelから更新対象を生成する
	 *
	 * @param	model	{@link PhotoDetailModel}
	 * @return			{@link PhotoMstUpdateTarget}
	 */
	public static PhotoMstUpdateTarget fromForUpdate(PhotoDetailModel model) {
		var exifData = model.getExifData();
		return PhotoMstUpdateTarget.builder()
				.updatedBy(model.getAccountNo().value())
				.isDeleted(false)
				.photoAt(model.getPhotoAt() != null ? model.getPhotoAt().value() : Consts.MIN_OFFSET_DATE_TIME)
				.locationNo(model.getLocationNo() != null ? model.getLocationNo().value() : 0L)
				.imageFilePath(model.getImageFilePath().value())
				.photoJapaneseTitle(model.getPhotoJapaneseTitle() != null ? model.getPhotoJapaneseTitle().value() : Consts.STRING_EMPTY)
				.photoEnglishTitle(model.getPhotoEnglishTitle() != null ? model.getPhotoEnglishTitle().value() : Consts.STRING_EMPTY)
				.caption(model.getCaption() != null ? model.getCaption().value() : Consts.STRING_EMPTY)
				.directionKbn(DirectionEnum.getOrDefault(model.getDirectionKbn()))
				.focalLength(exifData.focalLength() != null ? exifData.focalLength().value() : 0)
				.fValue(exifData.fValue() != null ? exifData.fValue().value() : BigDecimal.ZERO)
				.shutterSpeed(exifData.shutterSpeed() != null ? exifData.shutterSpeed().value() : BigDecimal.ZERO)
				.iso(exifData.iso() != null ? exifData.iso().value() : 0)
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
				.updatedBy(model.getAccountNo().value())
				.isDeleted(true)
				.build();
	}
}
