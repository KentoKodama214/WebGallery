package com.web.gallery.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.entity.KbnMst;
import com.web.gallery.entity.KbnMstCondition;
import com.web.gallery.mapper.KbnMstMapper;
import com.web.gallery.model.KbnMstModelList;
import com.web.gallery.repository.KbnMstRepository;

import lombok.RequiredArgsConstructor;

/**
 * 区分マスタデータを永続化するRepositoryの実装クラス
 */
@Repository
@RequiredArgsConstructor
public class KbnMstRepositoryImpl implements KbnMstRepository {

	private final KbnMstMapper kbnMstMapper;

	/**
	 * 区分クラスコードに該当する区分マスタの一覧を取得する
	 *
	 * @param	kbnClassCode	区分クラスコード
	 * @return					{@link KbnMstModelList}
	 */
	@Override
	public KbnMstModelList get(KbnClassCode kbnClassCode) {
		List<KbnMst> kbnMstList = kbnMstMapper.select(KbnMstCondition.byKbnClassCode(kbnClassCode.value()));

		return KbnMstModelList.from(kbnMstList);
	}
}