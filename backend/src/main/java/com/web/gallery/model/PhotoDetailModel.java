package com.web.gallery.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.web.gallery.constant.Consts;
import com.web.gallery.controller.request.PhotoSaveRequest;
import com.web.gallery.dto.PhotoDetailDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumuration.DirectionEnum;

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
	private Integer accountNo;
	
	/** 写真番号 */
	private Integer photoNo;

	/** お気に入り */
	private Boolean isFavorite;
	
	/** 撮影日時 */
	private OffsetDateTime photoAt;
	
	/** ロケーション番号 */
	private Integer locationNo;
	
	/** 住所 */
	private String address;

	/** 緯度 */
	private BigDecimal latitude;

	/** 経度 */
	private BigDecimal longitude;
	
	/** ロケーション名 */
	private String locationName;
	
	/** 画像ファイル */
	private MultipartFile imageFile;
	
	/** 画像ファイルパス */
	@NonNull
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
	
	/** 写真タグリスト */
	private List<PhotoTagModel> photoTagModelList;

	/**
	 * PhotoDetailDtoとタグエンティティリストからPhotoDetailModelを生成する
	 *
	 * @param	dto				{@link PhotoDetailDto}
	 * @param	photoTagMstList	該当写真のタグエンティティリスト
	 * @return					{@link PhotoDetailModel}
	 */
	public static PhotoDetailModel from(PhotoDetailDto dto, List<PhotoTagMst> photoTagMstList) {
		List<PhotoTagModel> photoTagModelList = photoTagMstList.stream().map(PhotoTagModel::from).toList();
		return PhotoDetailModel.builder()
				.accountNo(dto.getAccountNo())
				.photoNo(dto.getPhotoNo())
				.isFavorite(dto.getIsFavorite())
				.photoAt(
					dto.getPhotoAt().isEqual(Consts.MIN_OFFSET_DATE_TIME) ? null : dto.getPhotoAt().plusHours(9))
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
				.focalLength(dto.getFocalLength() != 0 ? dto.getFocalLength() : null)
				.fValue(dto.getFValue().compareTo(BigDecimal.ZERO) == 1 ? dto.getFValue() : null)
				.shutterSpeed(dto.getShutterSpeed().compareTo(BigDecimal.ZERO) == 1 ? dto.getShutterSpeed() : null)
				.iso(dto.getIso() != 0 ? dto.getIso() : null)
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
		List<PhotoTagModel> photoTagModelList = Objects.isNull(request.getPhotoTagRegistRequestList())
				? new ArrayList<PhotoTagModel>()
				: request.getPhotoTagRegistRequestList().stream()
						.map(PhotoTagModel::from)
						.collect(Collectors.toList());
		return PhotoDetailModel.builder()
				.accountNo(request.getAccountNo())
				.photoNo(request.getPhotoNo())
				.isFavorite(request.getIsFavorite())
				.photoAt(Optional.ofNullable(request.getPhotoAt())
						.map(photoAt -> photoAt.atOffset(Consts.JST)).orElse(null))
				.locationNo(request.getLocationNo())
				.address(request.getAddress())
				.latitude(request.getLatitude())
				.longitude(request.getLongitude())
				.locationName(request.getLocationName())
				.imageFile(request.getImageFile())
				.imageFilePath(Optional.ofNullable(request.getImageFilePath()).orElse(Consts.STRING_EMPTY))
				.photoJapaneseTitle(request.getPhotoJapaneseTitle())
				.photoEnglishTitle(request.getPhotoEnglishTitle())
				.caption(request.getCaption())
				.directionKbn(request.getDirectionKbn())
				.focalLength(request.getFocalLength())
				.fValue(request.getFValue())
				.shutterSpeed(request.getShutterSpeed())
				.iso(request.getIso())
				.photoTagModelList(photoTagModelList)
				.build();
	}
}