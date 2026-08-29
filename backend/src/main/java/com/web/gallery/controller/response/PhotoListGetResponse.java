package com.web.gallery.controller.response;

import java.util.List;

import com.web.gallery.model.PhotoPageModel;

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
	 * PhotoPageModelからPhotoListGetResponseを生成する<p>
	 * DB側で既にページング済みの結果をそのまま変換する
	 *
	 * @param	photoPageModel	{@link PhotoPageModel}
	 * @return					{@link PhotoListGetResponse}
	 */
	public static PhotoListGetResponse from(PhotoPageModel photoPageModel) {
		List<PhotoListResponse> photoListResponseList = photoPageModel.getPhotoModelList().stream()
				.map(PhotoListResponse::from)
				.toList();

		return PhotoListGetResponse.builder()
				.isLast(photoPageModel.getIsLast())
				.photoList(photoListResponseList)
				.build();
	}
}
