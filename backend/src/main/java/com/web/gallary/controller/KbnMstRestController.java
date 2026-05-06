package com.web.gallary.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.controller.response.PrefectureGroupResponse;
import com.web.gallary.controller.response.PrefectureResponse;
import com.web.gallary.helper.KbnHelper;
import com.web.gallary.model.KbnMstModel;
import com.web.gallary.service.KbnMstService;

import lombok.RequiredArgsConstructor;

/**
 * 区分マスタに関するAPI通信を扱うRestControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@RestController
@RequiredArgsConstructor
public class KbnMstRestController {
	private final KbnMstService kbnMstService;
	private final KbnHelper kbnHelper;

	/**
	 * 都道府県一覧取得
	 *
	 * @return	都道府県グループリスト
	 */
	@GetMapping(ApiRoutes.API_PREFECTURES)
	public ResponseEntity<List<PrefectureGroupResponse>> getPrefectures() {
		List<KbnMstModel> prefectureList = kbnMstService.getPrefectureList();
		Map<String, List<KbnMstModel>> groupedMap = kbnHelper.convertToLinkedHashMap(prefectureList);

		List<PrefectureGroupResponse> response = new ArrayList<>();
		for (Map.Entry<String, List<KbnMstModel>> entry : groupedMap.entrySet()) {
			List<PrefectureResponse> prefectures = entry.getValue().stream()
					.map(kbn -> PrefectureResponse.builder()
							.kbnCode(kbn.getKbnCode())
							.kbnJapaneseName(kbn.getKbnJapaneseName())
							.build())
					.toList();

			response.add(PrefectureGroupResponse.builder()
					.groupName(entry.getKey())
					.prefectures(prefectures)
					.build());
		}

		return ResponseEntity.ok(response);
	}
}
