package com.web.gallery.service.impl;

import java.util.List;

import com.web.gallery.constant.Consts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.model.KbnMstModel;
import com.web.gallery.repository.KbnMstRepository;
import com.web.gallery.service.KbnMstService;

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
		return kbnMstRepository.get(Consts.PREFECTURE);
	}
}