package com.web.gallary.repository.impl;

import java.util.ArrayList;
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
		List<KbnMst> KbnMstList = kbnMstMapper.select(kbnMst);
		
		List<KbnMstModel> kbnMstModelList = new ArrayList<KbnMstModel>();
		
		KbnMstList.stream().forEach(kbnMstData -> {
			kbnMstModelList.add(
					KbnMstModel.builder()
						.kbnClassCode(kbnMstData.getKbnClassCode())
						.kbnCode(kbnMstData.getKbnCode())
						.sortOrder(kbnMstData.getSortOrder())
						.kbnGroupCode(kbnMstData.getKbnGroupCode())
						.kbnClassJapaneseName(kbnMstData.getKbnClassJapaneseName())
						.kbnGroupJapaneseName(kbnMstData.getKbnGroupJapaneseName())
						.kbnJapaneseName(kbnMstData.getKbnJapaneseName())
						.kbnClassEnglishName(kbnMstData.getKbnClassEnglishName())
						.kbnGroupEnglishName(kbnMstData.getKbnGroupEnglishName())
						.kbnEnglishName(kbnMstData.getKbnEnglishName())
						.explanation(kbnMstData.getExplanation())
						.build()
				);
		});
		
		return kbnMstModelList;
	}
}