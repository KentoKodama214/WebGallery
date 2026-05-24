package com.web.gallery.controller.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.web.gallery.enumuration.DirectionEnum;
import com.web.gallery.enumuration.SortPhotoEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 写真一覧表示時のリクエストパラメータを保持するクラス
 */
@Schema(description = "写真一覧リクエスト")
@Data
public class PhotoListRequest {
	/**
	 * 向き区分コード
	 * <p>
	 * {@link DirectionEnum}
	 */
	@Schema(description = "向き区分（未選択/縦/横/正方形）")
	@JsonSetter(nulls = Nulls.SKIP)
	private DirectionEnum directionKbn = DirectionEnum.NONE;

	/** お気に入り写真のみ */
	@Schema(description = "お気に入り写真のみ表示するか", example = "false")
	@JsonSetter(nulls = Nulls.SKIP)
	private Boolean isFavorite = Boolean.FALSE;

	/** タグリスト */
	@Schema(description = "タグリスト（カンマ区切り）", example = "風景,東京")
	private String tagList;

	/**
	 * 並び順
	 * <p>
	 * {@link SortPhotoEnum}
	 */
	@Schema(description = "並び順（photoAt: 撮影日順, favorite: お気に入り数順, season: 季節順）")
	@JsonSetter(nulls = Nulls.SKIP)
	private SortPhotoEnum sortBy = SortPhotoEnum.PHOTO_AT;

	/** ページ番号 */
	@Schema(description = "ページ番号", example = "1")
	@JsonSetter(nulls = Nulls.SKIP)
	private Integer pageNo = 1;
}