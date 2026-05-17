package com.web.gallery.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import com.web.gallery.constant.Consts;
import com.web.gallery.enumuration.DirectionEnum;
import com.web.gallery.model.PhotoDeleteModel;
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
	private Integer id;

	/** アカウント番号 */
	private Integer accountNo;

	/** 写真番号 */
	private Integer photoNo;

	/** 作成者 */
	private Integer createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** 更新者 */
	private Integer updatedBy;

	/** 更新日時 */
	private OffsetDateTime updatedAt;

	/** 削除フラグ */
	private Boolean isDeleted;

	/** 撮影日時 */
	private OffsetDateTime photoAt;

	/** ロケーション番号 */
	private Integer locationNo;

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
	 * 写真登録用のPhotoDetailModelからPhotoMstエンティティを生成する
	 *
	 * @param	model		{@link PhotoDetailModel}
	 * @param	filePath	写真の保存ファイルパス
	 * @param	newPhotoNo	新規採番した写真番号
	 * @return				{@link PhotoMst}
	 */
	public static PhotoMst fromForRegist(PhotoDetailModel model, String filePath, Integer newPhotoNo) {
		return PhotoMst.builder()
				.accountNo(model.getAccountNo())
				.photoNo(newPhotoNo)
				.createdBy(model.getAccountNo())
				.updatedBy(model.getAccountNo())
				.photoAt(
					Optional.ofNullable(model.getPhotoAt()).orElse(Consts.MIN_OFFSET_DATE_TIME))
				.locationNo(
					Optional.ofNullable(model.getLocationNo()).orElse(0))
				.imageFilePath(filePath)
				.photoJapaneseTitle(
					Optional.ofNullable(model.getPhotoJapaneseTitle()).orElse(Consts.STRING_EMPTY))
				.photoEnglishTitle(
					Optional.ofNullable(model.getPhotoEnglishTitle()).orElse(Consts.STRING_EMPTY))
				.caption(
					Optional.ofNullable(model.getCaption()).orElse(Consts.STRING_EMPTY))
				.directionKbn(
					Optional.ofNullable(model.getDirectionKbn()).orElse(DirectionEnum.NONE))
				.focalLength(
					Optional.ofNullable(model.getFocalLength()).orElse(0))
				.fValue(
					Optional.ofNullable(model.getFValue()).orElse(BigDecimal.ZERO))
				.shutterSpeed(
					Optional.ofNullable(model.getShutterSpeed()).orElse(BigDecimal.ZERO))
				.iso(
					Optional.ofNullable(model.getIso()).orElse(0))
				.build();
	}

	/**
	 * 写真更新用のPhotoDetailModelからPhotoMstエンティティを生成する
	 *
	 * @param	model	{@link PhotoDetailModel}
	 * @return			{@link PhotoMst}
	 */
	public static PhotoMst targetForUpdate(PhotoDetailModel model) {
		return PhotoMst.builder()
				.updatedBy(model.getAccountNo())
				.isDeleted(false)
				.photoAt(
					Optional.ofNullable(model.getPhotoAt()).orElse(Consts.MIN_OFFSET_DATE_TIME))
				.locationNo(
					Optional.ofNullable(model.getLocationNo()).orElse(0))
				.imageFilePath(model.getImageFilePath())
				.photoJapaneseTitle(
					Optional.ofNullable(model.getPhotoJapaneseTitle()).orElse(Consts.STRING_EMPTY))
				.photoEnglishTitle(
					Optional.ofNullable(model.getPhotoEnglishTitle()).orElse(Consts.STRING_EMPTY))
				.caption(
					Optional.ofNullable(model.getCaption()).orElse(Consts.STRING_EMPTY))
				.directionKbn(
					Optional.ofNullable(model.getDirectionKbn()).orElse(DirectionEnum.NONE))
				.focalLength(
					Optional.ofNullable(model.getFocalLength()).orElse(0))
				.fValue(
					Optional.ofNullable(model.getFValue()).orElse(BigDecimal.ZERO))
				.shutterSpeed(
					Optional.ofNullable(model.getShutterSpeed()).orElse(BigDecimal.ZERO))
				.iso(
					Optional.ofNullable(model.getIso()).orElse(0))
				.build();
	}

	/**
	 * 条件用のPhotoMstエンティティを生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	photoNo		写真番号
	 * @return				{@link PhotoMst}
	 */
	public static PhotoMst condition(Integer accountNo, Integer photoNo) {
		return PhotoMst.builder()
				.accountNo(accountNo)
				.photoNo(photoNo)
				.build();
	}

	/**
	 * 写真削除用のPhotoDeleteModelからターゲットPhotoMstエンティティを生成する
	 *
	 * @param	model	{@link PhotoDeleteModel}
	 * @return			{@link PhotoMst}
	 */
	public static PhotoMst targetForDelete(PhotoDeleteModel model) {
		return PhotoMst.builder()
				.updatedBy(model.getAccountNo())
				.isDeleted(true)
				.build();
	}

	/**
	 * 写真存在チェック用のPhotoDetailModelからPhotoMstエンティティを生成する
	 *
	 * @param	model	{@link PhotoDetailModel}
	 * @return			{@link PhotoMst}
	 */
	public static PhotoMst conditionForExistCheck(PhotoDetailModel model) {
		return PhotoMst.builder()
				.accountNo(model.getAccountNo())
				.imageFilePath(model.getImageFile().getOriginalFilename())
				.build();
	}

	/**
	 * 写真件数取得用の条件PhotoMstエンティティを生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link PhotoMst}
	 */
	public static PhotoMst conditionForCount(Integer accountNo) {
		return PhotoMst.builder()
				.accountNo(accountNo)
				.isDeleted(false)
				.build();
	}
}