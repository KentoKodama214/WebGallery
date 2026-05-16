package com.web.gallary.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.web.gallary.entity.KbnMst;
import com.web.gallary.mapper.KbnMstMapper;
import com.web.gallary.model.KbnMstModel;
import com.web.gallary.repository.KbnMstRepository;

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
	 * @return					{@link KbnMstModel}
	 */
	@Override
	public List<KbnMstModel> get(String kbnClassCode) {
		KbnMst kbnMst = KbnMst.builder()
				.kbnClassCode(kbnClassCode)
				.build();
		List<KbnMst> kbnMstList = kbnMstMapper.select(kbnMst);

		return kbnMstList.stream().map(KbnMstModel::from).toList();
	}
}