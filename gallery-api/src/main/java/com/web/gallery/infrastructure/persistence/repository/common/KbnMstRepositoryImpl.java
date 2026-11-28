package com.web.gallery.infrastructure.persistence.repository.common;

import com.web.gallery.application.model.common.KbnMstModel;
import com.web.gallery.application.model.common.KbnMstModelList;
import com.web.gallery.application.repository.common.KbnMstRepository;
import com.web.gallery.domain.model.common.Explanation;
import com.web.gallery.domain.model.common.KbnClassCode;
import com.web.gallery.domain.model.common.KbnClassEnglishName;
import com.web.gallery.domain.model.common.KbnClassJapaneseName;
import com.web.gallery.domain.model.common.KbnCode;
import com.web.gallery.domain.model.common.KbnEnglishName;
import com.web.gallery.domain.model.common.KbnGroupCode;
import com.web.gallery.domain.model.common.KbnGroupEnglishName;
import com.web.gallery.domain.model.common.KbnGroupJapaneseName;
import com.web.gallery.domain.model.common.KbnJapaneseName;
import com.web.gallery.domain.model.common.SortOrder;
import com.web.gallery.infrastructure.persistence.entity.common.KbnMst;
import com.web.gallery.infrastructure.persistence.entity.common.KbnMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.common.KbnMstMapper;
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

    return KbnMstModelList.of(kbnMstList.stream().map(this::toKbnMstModel).toList());
  }

  /**
   * KbnMstエンティティからKbnMstModelを組み立てる
   *
   * @param entity {@link KbnMst}
   * @return {@link KbnMstModel}
   */
  private KbnMstModel toKbnMstModel(KbnMst entity) {
    return KbnMstModel.builder()
        .kbnClassCode(new KbnClassCode(entity.getKbnClassCode()))
        .kbnCode(new KbnCode(entity.getKbnCode()))
        .sortOrder(new SortOrder(entity.getSortOrder()))
        .kbnGroupCode(new KbnGroupCode(entity.getKbnGroupCode()))
        .kbnClassJapaneseName(new KbnClassJapaneseName(entity.getKbnClassJapaneseName()))
        .kbnGroupJapaneseName(new KbnGroupJapaneseName(entity.getKbnGroupJapaneseName()))
        .kbnJapaneseName(new KbnJapaneseName(entity.getKbnJapaneseName()))
        .kbnClassEnglishName(new KbnClassEnglishName(entity.getKbnClassEnglishName()))
        .kbnGroupEnglishName(new KbnGroupEnglishName(entity.getKbnGroupEnglishName()))
        .kbnEnglishName(new KbnEnglishName(entity.getKbnEnglishName()))
        .explanation(new Explanation(entity.getExplanation()))
        .build();
  }
}
