package com.web.gallary.controller.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 都道府県グループのレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PrefectureGroupResponse {
	/** グループ名 */
	private String groupName;

	/** 都道府県リスト */
	private List<PrefectureResponse> prefectures;
}
