package com.web.gallery.controller.response;

import java.util.List;

import com.web.gallery.model.PhotoModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 写真一覧のレスポンスパラメータを保持するクラス
 */
@Schema(description = "写真一覧レスポンス")
@Data
@Builder
public class PhotoListGetResponse {
	/** 最後まで写真を取得できたか */
	@Schema(description = "最後のページかどうか")
	private Boolean isLast;

	/** 写真リスト */
	@Schema(description = "写真リスト")
	private List<PhotoListResponse> photoList;

	/**
	 * 写真リストとページネーション情報からPhotoListGetResponseを生成する
	 *
	 * @param	photoList			{@link PhotoModel}のリスト
	 * @param	pageNo				ページ番号
	 * @param	photoCountPerPage	1ページあたりの写真数
	 * @return						{@link PhotoListGetResponse}
	 */
	public static PhotoListGetResponse from(List<PhotoModel> photoList, Integer pageNo, Integer photoCountPerPage) {
		List<PhotoListResponse> photoListResponseList = photoList.subList(
				(pageNo - 1) * photoCountPerPage,
				Math.min(pageNo * photoCountPerPage, photoList.size())).stream()
				.map(PhotoListResponse::from)
				.toList();

		return PhotoListGetResponse.builder()
				.isLast(pageNo * photoCountPerPage >= photoList.size())
				.photoList(photoListResponseList)
				.build();
	}
}