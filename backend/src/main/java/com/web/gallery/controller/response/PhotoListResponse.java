package com.web.gallery.controller.response;

import com.web.gallery.enumuration.DirectionEnum;
import com.web.gallery.model.PhotoModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 写真一覧の写真のメタデータを含めた詳細情報のレスポンスパラメータを保持するクラス
 */
@Schema(description = "写真一覧アイテムレスポンス")
@Data
@Builder
public class PhotoListResponse {
	/** アカウント番号 */
	@Schema(description = "アカウント番号", example = "1")
	private Long accountNo;

	/** 写真番号 */
	@Schema(description = "写真番号", example = "1")
	private Long photoNo;

	/** お気に入り */
	@Schema(description = "お気に入り")
	private Boolean isFavorite;

	/** 画像ファイルパス */
	@Schema(description = "画像ファイルパス")
	private String imageFilePath;

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

	/**
	 * PhotoModelからPhotoListResponseを生成する
	 *
	 * @param	model	{@link PhotoModel}
	 * @return			{@link PhotoListResponse}
	 */
	public static PhotoListResponse from(PhotoModel model) {
		return PhotoListResponse.builder()
				.accountNo(model.getAccountNo().value())
				.photoNo(model.getPhotoNo())
				.isFavorite(model.getIsFavorite())
				.imageFilePath(model.getImageFilePath())
				.caption(model.getCaption())
				.directionKbn(model.getDirectionKbn())
				.build();
	}
}