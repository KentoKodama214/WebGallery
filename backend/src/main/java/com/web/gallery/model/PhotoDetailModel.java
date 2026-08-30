package com.web.gallery.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.PhotoSaveRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.ExifData;
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
@Builder(toBuilder = true)
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

	/** 位置情報（住所・緯度・経度） */
	private GeoLocation geoLocation;

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

	/** EXIF情報（焦点距離・F値・シャッタースピード・ISO） */
	private ExifData exifData;

	/** 写真タグリスト */
	private PhotoTagModelList photoTagModelList;

	/**
	 * 位置情報を取得する。未設定の場合は全項目未設定のGeoLocationを返す
	 *
	 * @return	{@link GeoLocation}
	 */
	public GeoLocation getGeoLocation() {
		return geoLocation != null ? geoLocation : GeoLocation.empty();
	}

	/**
	 * EXIF情報を取得する。未設定の場合は全項目未設定のExifDataを返す
	 *
	 * @return	{@link ExifData}
	 */
	public ExifData getExifData() {
		return exifData != null ? exifData : ExifData.empty();
	}

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
				.accountNo(new AccountNo(dto.getAccountNo()))
				.photoNo(new PhotoNo(dto.getPhotoNo()))
				.isFavorite(new IsFavorite(dto.getIsFavorite()))
				.photoAt(
					dto.getPhotoAt().isEqual(Consts.MIN_OFFSET_DATE_TIME) ? null : new PhotoAt(dto.getPhotoAt().withOffsetSameInstant(Consts.JST)))
				.locationNo(dto.getLocationNo() != null ? new LocationNo(dto.getLocationNo()) : null)
				.geoLocation(new GeoLocation(
					dto.getAddress() != null ? new Address(dto.getAddress()) : null,
					dto.getLatitude() != null ? new Latitude(dto.getLatitude()) : null,
					dto.getLongitude() != null ? new Longitude(dto.getLongitude()) : null))
				.locationName(dto.getLocationName() != null ? new LocationName(dto.getLocationName()) : null)
				.imageFilePath(new ImageFilePath(dto.getImageFilePath()))
				.photoJapaneseTitle(dto.getPhotoJapaneseTitle() != null ? new PhotoJapaneseTitle(dto.getPhotoJapaneseTitle()) : null)
				.photoEnglishTitle(dto.getPhotoEnglishTitle() != null ? new PhotoEnglishTitle(dto.getPhotoEnglishTitle()) : null)
				.caption(dto.getCaption() != null ? new Caption(dto.getCaption()) : null)
				.directionKbn(dto.getDirectionKbn())
				.exifData(new ExifData(
					dto.getFocalLength() != null && dto.getFocalLength() != 0 ? new FocalLength(dto.getFocalLength()) : null,
					dto.getFValue() != null && dto.getFValue().compareTo(BigDecimal.ZERO) == 1 ? new FValue(dto.getFValue()) : null,
					dto.getShutterSpeed() != null && dto.getShutterSpeed().compareTo(BigDecimal.ZERO) == 1 ? new ShutterSpeed(dto.getShutterSpeed()) : null,
					dto.getIso() != null && dto.getIso() != 0 ? new Iso(dto.getIso()) : null))
				.photoTagModelList(photoTagModelList)
				.build();
	}

	/**
	 * 写真保存リクエストとログイン中のアカウント番号からPhotoDetailModelを生成する<p>
	 * アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の写真を操作するIDORを防ぐため）
	 *
	 * @param	request		{@link PhotoSaveRequest}
	 * @param	accountNo	ログイン中のアカウント番号
	 * @return				{@link PhotoDetailModel}
	 */
	public static PhotoDetailModel from(PhotoSaveRequest request, AccountNo accountNo) {
		PhotoTagModelList photoTagModelList = Objects.isNull(request.getPhotoTagRegistRequestList())
				? PhotoTagModelList.empty()
				: PhotoTagModelList.of(request.getPhotoTagRegistRequestList().stream()
						.map(tagRequest -> PhotoTagModel.from(tagRequest, accountNo))
						.toList());
		return PhotoDetailModel.builder()
				.accountNo(accountNo)
				.photoNo(request.getPhotoNo() != null ? new PhotoNo(request.getPhotoNo()) : null)
				.isFavorite(request.getIsFavorite() != null ? new IsFavorite(request.getIsFavorite()) : null)
				.photoAt(Optional.ofNullable(request.getPhotoAt())
						.map(photoAt -> new PhotoAt(photoAt.atOffset(Consts.JST))).orElse(null))
				.locationNo(request.getLocationNo() != null ? new LocationNo(request.getLocationNo()) : null)
				.geoLocation(new GeoLocation(
					request.getAddress() != null ? new Address(request.getAddress()) : null,
					request.getLatitude() != null ? new Latitude(request.getLatitude()) : null,
					request.getLongitude() != null ? new Longitude(request.getLongitude()) : null))
				.locationName(request.getLocationName() != null ? new LocationName(request.getLocationName()) : null)
				.imageFile(request.getImageFile() != null ? new ImageFile(request.getImageFile()) : null)
				.imageFilePath(new ImageFilePath(Optional.ofNullable(request.getImageFilePath()).orElse(Consts.STRING_EMPTY)))
				.photoJapaneseTitle(request.getPhotoJapaneseTitle() != null ? new PhotoJapaneseTitle(request.getPhotoJapaneseTitle()) : null)
				.photoEnglishTitle(request.getPhotoEnglishTitle() != null ? new PhotoEnglishTitle(request.getPhotoEnglishTitle()) : null)
				.caption(request.getCaption() != null ? new Caption(request.getCaption()) : null)
				.directionKbn(request.getDirectionKbn())
				.exifData(new ExifData(
					request.getFocalLength() != null ? new FocalLength(request.getFocalLength()) : null,
					request.getFValue() != null ? new FValue(request.getFValue()) : null,
					request.getShutterSpeed() != null ? new ShutterSpeed(request.getShutterSpeed()) : null,
					request.getIso() != null ? new Iso(request.getIso()) : null))
				.photoTagModelList(photoTagModelList)
				.build();
	}
}
