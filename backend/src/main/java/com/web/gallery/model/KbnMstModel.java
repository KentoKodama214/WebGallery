package com.web.gallery.model;

import com.web.gallery.domain.common.Explanation;
import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.common.KbnClassEnglishName;
import com.web.gallery.domain.common.KbnClassJapaneseName;
import com.web.gallery.domain.common.KbnCode;
import com.web.gallery.domain.common.KbnEnglishName;
import com.web.gallery.domain.common.KbnGroupCode;
import com.web.gallery.domain.common.KbnGroupEnglishName;
import com.web.gallery.domain.common.KbnGroupJapaneseName;
import com.web.gallery.domain.common.KbnJapaneseName;
import com.web.gallery.domain.common.SortOrder;
import com.web.gallery.entity.KbnMst;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 区分マスタの情報を受け渡すためのModelクラス */
@Value
@Builder
public class KbnMstModel {
  /** 区分分類コード */
  @NonNull private KbnClassCode kbnClassCode;

  /** 区分コード */
  @NonNull private KbnCode kbnCode;

  /** 並び順 */
  @NonNull private SortOrder sortOrder;

  /** 区分グループコード */
  @NonNull private KbnGroupCode kbnGroupCode;

  /** 区分分類日本語名 */
  @NonNull private KbnClassJapaneseName kbnClassJapaneseName;

  /** 区分グループ日本語名 */
  @NonNull private KbnGroupJapaneseName kbnGroupJapaneseName;

  /** 区分日本語名 */
  @NonNull private KbnJapaneseName kbnJapaneseName;

  /** 区分分類英語名 */
  @NonNull private KbnClassEnglishName kbnClassEnglishName;

  /** 区分グループ英語名 */
  @NonNull private KbnGroupEnglishName kbnGroupEnglishName;

  /** 区分英語名 */
  @NonNull private KbnEnglishName kbnEnglishName;

  /** 説明 */
  @NonNull private Explanation explanation;

  /**
   * KbnMstエンティティからKbnMstModelを生成する
   *
   * @param entity {@link KbnMst}
   * @return {@link KbnMstModel}
   */
  public static KbnMstModel from(KbnMst entity) {
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
