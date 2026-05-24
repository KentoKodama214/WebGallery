package com.web.gallery.controller;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.gallery.constant.ApiRoutes;
import com.web.gallery.controller.response.PrefectureGroupResponse;
import com.web.gallery.helper.KbnHelper;
import com.web.gallery.model.KbnMstModel;
import com.web.gallery.service.KbnMstService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 区分マスタに関するAPI通信を扱うRestControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "都道府県", description = "都道府県マスタに関するAPI")
public class KbnMstRestController {
	private final KbnMstService kbnMstService;
	private final KbnHelper kbnHelper;

	/**
	 * 都道府県一覧取得
	 *
	 * @return	都道府県グループリスト
	 */
	@Operation(summary = "都道府県一覧取得", description = "都道府県を地方ごとにグループ化して取得する")
	@ApiResponse(responseCode = "200", description = "取得成功")
	@GetMapping(ApiRoutes.API_PREFECTURES)
	public ResponseEntity<List<PrefectureGroupResponse>> getPrefectures() {
		List<KbnMstModel> prefectureList = kbnMstService.getPrefectureList();
		Map<String, List<KbnMstModel>> groupedMap = kbnHelper.convertToLinkedHashMap(prefectureList);

		List<PrefectureGroupResponse> response = groupedMap.entrySet().stream()
				.map(entry -> PrefectureGroupResponse.from(entry.getKey(), entry.getValue()))
				.toList();

		return ResponseEntity.ok(response);
	}
}
