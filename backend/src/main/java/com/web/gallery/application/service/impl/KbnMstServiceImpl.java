package com.web.gallery.application.service.impl;

import com.web.gallery.application.model.common.KbnMstModelList;
import com.web.gallery.application.repository.KbnMstRepository;
import com.web.gallery.application.service.KbnMstService;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.model.common.KbnClassCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 区分マスタに関するビジネスロジックを行うServiceの実装クラス */
@Service
@RequiredArgsConstructor
public class KbnMstServiceImpl implements KbnMstService {

  private final KbnMstRepository kbnMstRepository;

  /**
   * 都道府県の区分マスタを取得する
   *
   * @return {@link KbnMstModelList}
   */
  @Override
  @Transactional(readOnly = true)
  public KbnMstModelList getPrefectureList() {
    return kbnMstRepository.get(new KbnClassCode(Consts.PREFECTURE));
  }
}
