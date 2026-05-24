package com.web.gallery.controller.response;

import java.util.List;

import com.web.gallery.model.KbnMstModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 都道府県グループのレスポンスパラメータを保持するクラス
 */
@Schema(description = "都道府県グループレスポンス")
@Data
@Builder
public class PrefectureGroupResponse {
	/** グループ名 */
	@Schema(description = "グループ名（地方名）", example = "北海道・東北")
	private String groupName;

	/** 都道府県リスト */
	@Schema(description = "都道府県リスト")
	private List<PrefectureResponse> prefectures;

	/**
	 * グループ名とKbnMstModelリストからPrefectureGroupResponseを生成する
	 *
	 * @param	groupName		グループ名
	 * @param	kbnMstModels	{@link KbnMstModel}のリスト
	 * @return					{@link PrefectureGroupResponse}
	 */
	public static PrefectureGroupResponse from(String groupName, List<KbnMstModel> kbnMstModels) {
		List<PrefectureResponse> prefectures = kbnMstModels.stream()
				.map(PrefectureResponse::from)
				.toList();

		return PrefectureGroupResponse.builder()
				.groupName(groupName)
				.prefectures(prefectures)
				.build();
	}
}
