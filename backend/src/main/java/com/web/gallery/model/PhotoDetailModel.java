package com.web.gallery.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.PhotoSaveRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoEnglishTitle;
import com.web.gallery.domain.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.ShutterSpeed;
import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumeration.DirectionEnum;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真のメタデータを含めた詳細情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoDetailModel {
	/** アカウント番号 */
	@NonNull
	private AccountNo accountNo;

	/** 写真番号 */
	private PhotoNo photoNo;

	/** お気に入り */
	private IsFavorite isFavorite;

	/** 撮影日時 */
	private PhotoAt photoAt;

	/** ロケーション番号 */
	private LocationNo locationNo;

	/** 住所 */
	private Address address;

	/** 緯度 */
	private Latitude latitude;

	/** 経度 */
	private Longitude longitude;

	/** ロケーション名 */
	private LocationName locationName;

	/** 画像ファイル */
	private ImageFile imageFile;

	/** 画像ファイルパス */
	@NonNull
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

	/** 写真タグリスト */
	private PhotoTagModelList photoTagModelList;

	/**
	 * PhotoDetailDtoとタグエンティティリストからPhotoDetailModelを生成する
	 *
	 * @param	dto				{@link PhotoDetailDto}
	 * @param	photoTagMstList	該当写真のタグエンティティリスト
	 * @return					{@link PhotoDetailModel}
	 */
	public static PhotoDetailModel from(PhotoDetailDto dto, List<PhotoTagMst> photoTagMstList) {
		PhotoTagModelList photoTagModelList = PhotoTagModelList.from(photoTagMstList);
		return PhotoDetailModel.builder()
				.accountNo(dto.getAccountNo())
				.photoNo(dto.getPhotoNo())
				.isFavorite(dto.getIsFavorite())
				.photoAt(
					dto.getPhotoAt().value().isEqual(Consts.MIN_OFFSET_DATE_TIME) ? null : new PhotoAt(dto.getPhotoAt().value().plusHours(9)))
				.locationNo(dto.getLocationNo())
				.address(dto.getAddress())
				.latitude(dto.getLatitude())
				.longitude(dto.getLongitude())
				.locationName(dto.getLocationName())
				.imageFilePath(dto.getImageFilePath())
				.photoJapaneseTitle(dto.getPhotoJapaneseTitle())
				.photoEnglishTitle(dto.getPhotoEnglishTitle())
				.caption(dto.getCaption())
				.directionKbn(dto.getDirectionKbn())
				.focalLength(dto.getFocalLength() != null && dto.getFocalLength().value() != 0 ? dto.getFocalLength() : null)
				.fValue(dto.getFValue() != null && dto.getFValue().value().compareTo(BigDecimal.ZERO) == 1 ? dto.getFValue() : null)
				.shutterSpeed(dto.getShutterSpeed() != null && dto.getShutterSpeed().value().compareTo(BigDecimal.ZERO) == 1 ? dto.getShutterSpeed() : null)
				.iso(dto.getIso() != null && dto.getIso().value() != 0 ? dto.getIso() : null)
				.photoTagModelList(photoTagModelList)
				.build();
	}

	/**
	 * 写真保存リクエストからPhotoDetailModelを生成する
	 *
	 * @param	request	{@link PhotoSaveRequest}
	 * @return			{@link PhotoDetailModel}
	 */
	public static PhotoDetailModel from(PhotoSaveRequest request) {
		PhotoTagModelList photoTagModelList = Objects.isNull(request.getPhotoTagRegistRequestList())
				? PhotoTagModelList.empty()
				: PhotoTagModelList.of(request.getPhotoTagRegistRequestList().stream()
						.map(PhotoTagModel::from)
						.toList());
		return PhotoDetailModel.builder()
				.accountNo(new AccountNo(request.getAccountNo()))
				.photoNo(request.getPhotoNo() != null ? new PhotoNo(request.getPhotoNo()) : null)
				.isFavorite(request.getIsFavorite() != null ? new IsFavorite(request.getIsFavorite()) : null)
				.photoAt(Optional.ofNullable(request.getPhotoAt())
						.map(photoAt -> new PhotoAt(photoAt.atOffset(Consts.JST))).orElse(null))
				.locationNo(request.getLocationNo() != null ? new LocationNo(request.getLocationNo()) : null)
				.address(request.getAddress() != null ? new Address(request.getAddress()) : null)
				.latitude(request.getLatitude() != null ? new Latitude(request.getLatitude()) : null)
				.longitude(request.getLongitude() != null ? new Longitude(request.getLongitude()) : null)
				.locationName(request.getLocationName() != null ? new LocationName(request.getLocationName()) : null)
				.imageFile(request.getImageFile() != null ? new ImageFile(request.getImageFile()) : null)
				.imageFilePath(new ImageFilePath(Optional.ofNullable(request.getImageFilePath()).orElse(Consts.STRING_EMPTY)))
				.photoJapaneseTitle(request.getPhotoJapaneseTitle() != null ? new PhotoJapaneseTitle(request.getPhotoJapaneseTitle()) : null)
				.photoEnglishTitle(request.getPhotoEnglishTitle() != null ? new PhotoEnglishTitle(request.getPhotoEnglishTitle()) : null)
				.caption(request.getCaption() != null ? new Caption(request.getCaption()) : null)
				.directionKbn(request.getDirectionKbn())
				.focalLength(request.getFocalLength() != null ? new FocalLength(request.getFocalLength()) : null)
				.fValue(request.getFValue() != null ? new FValue(request.getFValue()) : null)
				.shutterSpeed(request.getShutterSpeed() != null ? new ShutterSpeed(request.getShutterSpeed()) : null)
				.iso(request.getIso() != null ? new Iso(request.getIso()) : null)
				.photoTagModelList(photoTagModelList)
				.build();
	}
}
