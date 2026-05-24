package com.web.gallery.controller.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.web.gallery.enumuration.DirectionEnum;
import com.web.gallery.model.PhotoDetailModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真詳細のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PhotoDetailGetResponse {
	/** アカウント番号 */
	private Long accountNo;

	/** 写真番号 */
	private Long photoNo;

	/** お気に入り */
	private Boolean isFavorite;

	/** 撮影日時 */
	private OffsetDateTime photoAt;

	/** ロケーション番号 */
	private Long locationNo;

	/** 住所 */
	private String address;

	/** 緯度 */
	private BigDecimal latitude;

	/** 経度 */
	private BigDecimal longitude;

	/** ロケーション名 */
	private String locationName;

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

	/** 写真タグリスト */
	private List<PhotoTagResponse> photoTagList;

	/**
	 * PhotoDetailModelからPhotoDetailGetResponseを生成する
	 *
	 * @param	model	{@link PhotoDetailModel}
	 * @return			{@link PhotoDetailGetResponse}
	 */
	public static PhotoDetailGetResponse from(PhotoDetailModel model) {
		List<PhotoTagResponse> photoTagResponseList;
		if (Objects.isNull(model.getPhotoTagModelList())) {
			photoTagResponseList = Collections.emptyList();
		} else {
			photoTagResponseList = model.getPhotoTagModelList().stream()
					.map(PhotoTagResponse::from)
					.toList();
		}

		return PhotoDetailGetResponse.builder()
				.accountNo(model.getAccountNo())
				.photoNo(model.getPhotoNo())
				.isFavorite(model.getIsFavorite())
				.photoAt(model.getPhotoAt())
				.locationNo(model.getLocationNo())
				.address(model.getAddress())
				.latitude(model.getLatitude())
				.longitude(model.getLongitude())
				.locationName(model.getLocationName())
				.imageFilePath(model.getImageFilePath())
				.photoJapaneseTitle(model.getPhotoJapaneseTitle())
				.photoEnglishTitle(model.getPhotoEnglishTitle())
				.caption(model.getCaption())
				.directionKbn(model.getDirectionKbn())
				.focalLength(model.getFocalLength())
				.fValue(model.getFValue())
				.shutterSpeed(model.getShutterSpeed())
				.iso(model.getIso())
				.photoTagList(photoTagResponseList)
				.build();
	}
}
