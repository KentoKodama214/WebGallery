package com.web.gallery.infrastructure.persistence.entity.photo;

import lombok.Builder;
import lombok.Data;

/** ポートフォリオマスタテーブルの抽出条件クラス */
@Data
@Builder
public class PortfolioMstCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /**
   * アカウント番号・ポートフォリオ番号による抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioMstCondition}
   */
  public static PortfolioMstCondition byAccountAndPortfolio(Long accountNo, Long portfolioNo) {
    return PortfolioMstCondition.builder().accountNo(accountNo).portfolioNo(portfolioNo).build();
  }

  /**
   * アカウント番号による抽出条件を生成する
   *
   * <p>本人のポートフォリオ一覧の取得と、アカウント削除時のポートフォリオの一括削除に使用する
   *
   * @param accountNo アカウント番号
   * @return {@link PortfolioMstCondition}
   */
  public static PortfolioMstCondition byAccountNo(Long accountNo) {
    return PortfolioMstCondition.builder().accountNo(accountNo).build();
  }
}
