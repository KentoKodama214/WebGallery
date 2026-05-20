package com.web.gallery.service;

import java.util.List;

import com.web.gallery.model.KbnMstModel;

/**
 * 区分マスタに関するビジネスロジックを行うServiceクラス
 */
public interface KbnMstService {
	/**
	 * 都道府県の区分マスタを取得する
	 * 
	 * @return	{@link KbnMstModel}
	 */
	List<KbnMstModel> getPrefectureList();
}