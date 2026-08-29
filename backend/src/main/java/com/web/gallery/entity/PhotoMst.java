package com.web.gallery.entity;

import java.io.File;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.web.gallery.constant.Consts;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.model.PhotoDetailModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真マスタテーブルのEntityクラス
 */
@Data
@Builder
public class PhotoMst {
	/** ID */
	private Long id;

	/** アカウント番号 */
	private Long accountNo;

	/** 写真番号 */
	private Long photoNo;

	/** 作成者 */
	private Long createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** 更新者 */
	private Long updatedBy;

	/** 更新日時 */
	private OffsetDateTime updatedAt;

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
	 * 写真登録用のPhotoDetailModelからPhotoMstエンティティを生成する
	 *
	 * @param	model		{@link PhotoDetailModel}
	 * @param	filePath	写真の保存ファイルパス
	 * @param	newPhotoNo	新規採番した写真番号
	 * @return				{@link PhotoMst}
	 */
	public static PhotoMst fromForRegist(PhotoDetailModel model, String filePath, Long newPhotoNo) {
		var exifData = model.getExifData();
		return PhotoMst.builder()
				.accountNo(model.getAccountNo().value())
				.photoNo(newPhotoNo)
				.createdBy(model.getAccountNo().value())
				.updatedBy(model.getAccountNo().value())
				.photoAt(model.getPhotoAt() != null ? model.getPhotoAt().value() : Consts.MIN_OFFSET_DATE_TIME)
				.locationNo(model.getLocationNo() != null ? model.getLocationNo().value() : 0L)
				.imageFilePath(filePath)
				.imageFileName(new File(filePath).getName())
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
}
