package com.web.gallary.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.model.KbnMstModel;
import com.web.gallary.repository.KbnMstRepository;
import com.web.gallary.service.KbnMstService;

import lombok.RequiredArgsConstructor;

/**
 * 区分マスタに関するビジネスロジックを行うServiceの実装クラス
 */
@Service
@RequiredArgsConstructor
public class KbnMstServiceImpl implements KbnMstService {

	private final KbnMstRepository kbnMstRepository;
	
	/**
	 * 都道府県の区分マスタを取得する
	 * 
	 * @return	{@link KbnMstModel}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<KbnMstModel> getPrefectureList() {
		return kbnMstRepository.get("prefecture");
	}
}