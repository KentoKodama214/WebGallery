package com.web.gallary.controller.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 写真一覧のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PhotoListGetResponse {
	/** 最後まで写真を取得できたか */
	private Boolean isLast;
	
	/** 写真リスト */
	private List<PhotoListResponse> photoList;
}