package com.web.gallery.controller.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.web.gallery.enumuration.DirectionEnum;
import com.web.gallery.model.PhotoDetailModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 写真詳細のレスポンスパラメータを保持するクラス
 */
@Schema(description = "写真詳細レスポンス")
@Data
@Builder
public class PhotoDetailGetResponse {
	/** アカウント番号 */
	@Schema(description = "アカウント番号", example = "1")
	private Long accountNo;

	/** 写真番号 */
	@Schema(description = "写真番号", example = "1")
	private Long photoNo;

	/** お気に入り */
	@Schema(description = "お気に入り")
	private Boolean isFavorite;

	/** 撮影日時 */
	@Schema(description = "撮影日時")
	private OffsetDateTime photoAt;

	/** ロケーション番号 */
	@Schema(description = "ロケーション番号")
	private Long locationNo;

	/** 住所 */
	@Schema(description = "住所")
	private String address;

	/** 緯度 */
	@Schema(description = "緯度")
	private BigDecimal latitude;

	/** 経度 */
	@Schema(description = "経度")
	private BigDecimal longitude;

	/** ロケーション名 */
	@Schema(description = "ロケーション名")
	private String locationName;

	/** 画像ファイルパス */
	@Schema(description = "画像ファイルパス")
	private String imageFilePath;

	/** 写真タイトル日本語名 */
	@Schema(description = "写真タイトル日本語名")
	private String photoJapaneseTitle;

	/** 写真タイトル英語名 */
	@Schema(description = "写真タイトル英語名")
	private String photoEnglishTitle;

	/** キャプション */
	@Schema(description = "キャプション")
	private String caption;

	/**
	 * 向き区分
	 * <p>
	 * {@link DirectionEnum}
	 */
	@Schema(description = "向き区分")
	private DirectionEnum directionKbn;

	/** 焦点距離 */
	@Schema(description = "焦点距離（mm）")
	private Integer focalLength;

	/** F値 */
	@Schema(description = "F値")
	private BigDecimal fValue;

	/** シャッタースピード */
	@Schema(description = "シャッタースピード（秒）")
	private BigDecimal shutterSpeed;

	/** ISO */
	@Schema(description = "ISO感度")
	private Integer iso;

	/** 写真タグリスト */
	@Schema(description = "写真タグリスト")
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
				.accountNo(model.getAccountNo().value())
				.photoNo(model.getPhotoNo().value())
				.isFavorite(model.getIsFavorite() != null ? model.getIsFavorite().value() : null)
				.photoAt(model.getPhotoAt() != null ? model.getPhotoAt().value() : null)
				.locationNo(model.getLocationNo() != null ? model.getLocationNo().value() : null)
				.address(model.getAddress() != null ? model.getAddress().value() : null)
				.latitude(model.getLatitude() != null ? model.getLatitude().value() : null)
				.longitude(model.getLongitude() != null ? model.getLongitude().value() : null)
				.locationName(model.getLocationName() != null ? model.getLocationName().value() : null)
				.imageFilePath(model.getImageFilePath().value())
				.photoJapaneseTitle(model.getPhotoJapaneseTitle() != null ? model.getPhotoJapaneseTitle().value() : null)
				.photoEnglishTitle(model.getPhotoEnglishTitle() != null ? model.getPhotoEnglishTitle().value() : null)
				.caption(model.getCaption() != null ? model.getCaption().value() : null)
				.directionKbn(model.getDirectionKbn())
				.focalLength(model.getFocalLength() != null ? model.getFocalLength().value() : null)
				.fValue(model.getFValue() != null ? model.getFValue().value() : null)
				.shutterSpeed(model.getShutterSpeed() != null ? model.getShutterSpeed().value() : null)
				.iso(model.getIso() != null ? model.getIso().value() : null)
				.photoTagList(photoTagResponseList)
				.build();
	}
}
