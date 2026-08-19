package com.web.gallery.repository;

import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.model.KbnMstModelList;

/**
 * 区分マスタデータを永続化するRepositoryクラス
 */
public interface KbnMstRepository {
	/**
	 * 区分クラスコードに該当する区分マスタの一覧を取得する
	 *
	 * @param	kbnClassCode	区分クラスコード
	 * @return					{@link KbnMstModelList}
	 */
	KbnMstModelList get(KbnClassCode kbnClassCode);
}