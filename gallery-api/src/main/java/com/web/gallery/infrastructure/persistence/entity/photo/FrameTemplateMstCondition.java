package com.web.gallery.infrastructure.persistence.entity.photo;

import lombok.Builder;
import lombok.Data;

/** 額縁テンプレートマスタテーブルの抽出条件クラス */
@Data
@Builder
public class FrameTemplateMstCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** 額縁テンプレート番号 */
  private Long frameTemplateNo;

  /**
   * アカウント番号・額縁テンプレート番号による抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @param frameTemplateNo 額縁テンプレート番号
   * @return {@link FrameTemplateMstCondition}
   */
  public static FrameTemplateMstCondition byAccountAndFrameTemplate(
      Long accountNo, Long frameTemplateNo) {
    return FrameTemplateMstCondition.builder()
        .accountNo(accountNo)
        .frameTemplateNo(frameTemplateNo)
        .build();
  }

  /**
   * アカウント番号による抽出条件を生成する
   *
   * <p>本人の額縁テンプレート一覧の取得と、アカウント削除時の額縁テンプレートの一括削除に使用する
   *
   * @param accountNo アカウント番号
   * @return {@link FrameTemplateMstCondition}
   */
  public static FrameTemplateMstCondition byAccountNo(Long accountNo) {
    return FrameTemplateMstCondition.builder().accountNo(accountNo).build();
  }
}
