package com.web.gallery.repository.impl;

import com.web.gallery.application.model.common.KbnMstModelList;
import com.web.gallery.application.repository.KbnMstRepository;
import com.web.gallery.domain.model.common.KbnClassCode;
import com.web.gallery.entity.common.KbnMst;
import com.web.gallery.entity.common.KbnMstCondition;
import com.web.gallery.mapper.KbnMstMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** 区分マスタデータを永続化するRepositoryの実装クラス */
@Repository
@RequiredArgsConstructor
public class KbnMstRepositoryImpl implements KbnMstRepository {

  private final KbnMstMapper kbnMstMapper;

  /**
   * 区分クラスコードに該当する区分マスタの一覧を取得する
   *
   * @param kbnClassCode 区分クラスコード
   * @return {@link KbnMstModelList}
   */
  @Override
  public KbnMstModelList get(KbnClassCode kbnClassCode) {
    List<KbnMst> kbnMstList =
        kbnMstMapper.select(KbnMstCondition.byKbnClassCode(kbnClassCode.value()));

    return KbnMstModelList.from(kbnMstList);
  }
}
