package com.web.gallery.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.common.UpdatedAt;
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
	private Long id;

	/** アカウント番号 */
	private AccountNo accountNo;

	/** 写真番号 */
	private Long photoNo;

	/** 作成者 */
	private CreatedBy createdBy;

	/** 作成日時 */
	private CreatedAt createdAt;

	/** 更新者 */
	private UpdatedBy updatedBy;

	/** 更新日時 */
	private UpdatedAt updatedAt;

	/** 削除フラグ */
	private IsDeleted isDeleted;

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
	 * 写真登録用のPhotoDetailModelからPhotoMstエンティティを生成する
	 *
	 * @param	model		{@link PhotoDetailModel}
	 * @param	filePath	写真の保存ファイルパス
	 * @param	newPhotoNo	新規採番した写真番号
	 * @return				{@link PhotoMst}
	 */
	public static PhotoMst fromForRegist(PhotoDetailModel model, String filePath, Long newPhotoNo) {
		return PhotoMst.builder()
				.accountNo(model.getAccountNo())
				.photoNo(newPhotoNo)
				.createdBy(new CreatedBy(model.getAccountNo().value()))
				.updatedBy(new UpdatedBy(model.getAccountNo().value()))
				.photoAt(
					Optional.ofNullable(model.getPhotoAt()).orElse(Consts.MIN_OFFSET_DATE_TIME))
				.locationNo(
					Optional.ofNullable(model.getLocationNo()).orElse(0L))
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
				.updatedBy(new UpdatedBy(model.getAccountNo().value()))
				.isDeleted(new IsDeleted(false))
				.photoAt(
					Optional.ofNullable(model.getPhotoAt()).orElse(Consts.MIN_OFFSET_DATE_TIME))
				.locationNo(
					Optional.ofNullable(model.getLocationNo()).orElse(0L))
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
	public static PhotoMst condition(Long accountNo, Long photoNo) {
		return PhotoMst.builder()
				.accountNo(new AccountNo(accountNo))
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
				.updatedBy(new UpdatedBy(model.getAccountNo().value()))
				.isDeleted(new IsDeleted(true))
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
	public static PhotoMst conditionForCount(Long accountNo) {
		return PhotoMst.builder()
				.accountNo(new AccountNo(accountNo))
				.isDeleted(new IsDeleted(false))
				.build();
	}
}
