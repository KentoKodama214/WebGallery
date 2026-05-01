package com.web.gallary.repository;

import java.util.List;

import com.web.gallary.model.KbnMstModel;

/**
 * 区分マスタデータを永続化するRepositoryクラス
 */
public interface KbnMstRepository {
	/**
	 * 区分クラスコードに該当する区分マスタの一覧を取得する
	 * 
	 * @param	kbnClassCode	区分クラスコード
	 * @return					{@link KbnMstModel}
	 */
	List<KbnMstModel> get(String kbnClassCode);
}