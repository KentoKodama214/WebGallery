package com.web.gallery.controller.response;

import java.util.List;

import com.web.gallery.model.PhotoModelList;

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
	 * @param	photoList			{@link PhotoModelList}
	 * @param	pageNo				ページ番号
	 * @param	photoCountPerPage	1ページあたりの写真数
	 * @return						{@link PhotoListGetResponse}
	 */
	public static PhotoListGetResponse from(PhotoModelList photoList, Integer pageNo, Integer photoCountPerPage) {
		List<PhotoListResponse> photoListResponseList = photoList.stream()
				.skip((long) (pageNo - 1) * photoCountPerPage)
				.limit(photoCountPerPage)
				.map(PhotoListResponse::from)
				.toList();

		return PhotoListGetResponse.builder()
				.isLast(pageNo * photoCountPerPage >= photoList.size())
				.photoList(photoListResponseList)
				.build();
	}
}