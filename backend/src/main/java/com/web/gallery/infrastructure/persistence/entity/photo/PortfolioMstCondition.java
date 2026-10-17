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

  /** 削除フラグ */
  private Boolean isDeleted;

  /**
   * アカウント番号・ポートフォリオ番号による抽出条件を生成する（削除済みを除外）
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioMstCondition}
   */
  public static PortfolioMstCondition byAccountAndPortfolioNotDeleted(
      Long accountNo, Long portfolioNo) {
    return PortfolioMstCondition.builder()
        .accountNo(accountNo)
        .portfolioNo(portfolioNo)
        .isDeleted(false)
        .build();
  }

  /**
   * アカウント番号による抽出条件を生成する（削除済みを除外）
   *
   * <p>本人のポートフォリオ一覧の取得に使用する
   *
   * @param accountNo アカウント番号
   * @return {@link PortfolioMstCondition}
   */
  public static PortfolioMstCondition byAccountNoNotDeleted(Long accountNo) {
    return PortfolioMstCondition.builder().accountNo(accountNo).isDeleted(false).build();
  }

  /**
   * アカウント番号による抽出条件を生成する（削除済みを含む）
   *
   * <p>アカウント削除時に、そのアカウントのポートフォリオをまとめて物理削除するために使用する
   *
   * @param accountNo アカウント番号
   * @return {@link PortfolioMstCondition}
   */
  public static PortfolioMstCondition byAccountNo(Long accountNo) {
    return PortfolioMstCondition.builder().accountNo(accountNo).build();
  }
}
