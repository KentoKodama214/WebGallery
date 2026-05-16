package com.web.gallary.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.web.gallary.constant.Consts;
import com.web.gallary.controller.request.PhotoSaveRequest;
import com.web.gallary.enumuration.DirectionEnum;

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
				.imageFilePath(Optional.ofNullable(request.getImageFilePath()).orElse(""))
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