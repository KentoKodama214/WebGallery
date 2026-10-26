package com.web.gallery.infrastructure.persistence.entity.photo;

import lombok.Builder;
import lombok.Data;

/** ポートフォリオ写真テーブルの抽出条件クラス */
@Data
@Builder
public class PortfolioPhotoCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /** 写真番号 */
  private Long photoNo;

  /**
   * アカウント番号・ポートフォリオ番号による抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioPhotoCondition}
   */
  public static PortfolioPhotoCondition byAccountAndPortfolio(Long accountNo, Long portfolioNo) {
    return PortfolioPhotoCondition.builder().accountNo(accountNo).portfolioNo(portfolioNo).build();
  }

  /**
   * アカウント番号による抽出条件を生成する
   *
   * <p>アカウント削除時に、そのアカウントのポートフォリオ写真をまとめて削除するために使用する
   *
   * @param accountNo アカウント番号
   * @return {@link PortfolioPhotoCondition}
   */
  public static PortfolioPhotoCondition byAccountNo(Long accountNo) {
    return PortfolioPhotoCondition.builder().accountNo(accountNo).build();
  }
}
