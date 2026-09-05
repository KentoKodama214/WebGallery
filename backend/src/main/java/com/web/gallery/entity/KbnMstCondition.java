package com.web.gallery.entity;

import lombok.Builder;
import lombok.Data;

/** 区分マスタテーブルの抽出条件クラス */
@Data
@Builder
public class KbnMstCondition {
  /** 区分分類コード */
  private String kbnClassCode;

  /** 区分コード */
  private String kbnCode;

  /** 並び順 */
  private Integer sortOrder;

  /** 区分グループコード */
  private String kbnGroupCode;

  /** 区分分類日本語名 */
  private String kbnClassJapaneseName;

  /** 区分グループ日本語名 */
  private String kbnGroupJapaneseName;

  /** 区分日本語名 */
  private String kbnJapaneseName;

  /** 区分分類英語名 */
  private String kbnClassEnglishName;

  /** 区分グループ英語名 */
  private String kbnGroupEnglishName;

  /** 区分英語名 */
  private String kbnEnglishName;

  /** 説明 */
  private String explanation;

  /**
   * 区分分類コードによる抽出条件を生成する
   *
   * @param kbnClassCode 区分分類コード
   * @return {@link KbnMstCondition}
   */
  public static KbnMstCondition byKbnClassCode(String kbnClassCode) {
    return KbnMstCondition.builder().kbnClassCode(kbnClassCode).build();
  }
}
