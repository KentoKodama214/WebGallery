package com.web.gallery.infrastructure.persistence.entity.photo;

import lombok.Builder;
import lombok.Data;

/** ポートフォリオタグマスタテーブルの抽出条件クラス */
@Data
@Builder
public class PortfolioTagMstCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /**
   * アカウント番号・ポートフォリオ番号による抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioTagMstCondition}
   */
  public static PortfolioTagMstCondition byAccountAndPortfolio(Long accountNo, Long portfolioNo) {
    return PortfolioTagMstCondition.builder().accountNo(accountNo).portfolioNo(portfolioNo).build();
  }

  /**
   * アカウント番号による抽出条件を生成する
   *
   * <p>アカウント削除時に、そのアカウントのポートフォリオタグをまとめて削除するために使用する
   *
   * @param accountNo アカウント番号
   * @return {@link PortfolioTagMstCondition}
   */
  public static PortfolioTagMstCondition byAccountNo(Long accountNo) {
    return PortfolioTagMstCondition.builder().accountNo(accountNo).build();
  }
}
