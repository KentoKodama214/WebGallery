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
   * 検索語のLIKEパターン（部分一致。ワイルドカード文字はエスケープ済み）
   *
   * <p>テーマ・タグ名（日本語・英語）のいずれかに一致するポートフォリオに絞り込む（OR条件）
   */
  private String keywordPattern;

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

  /**
   * アカウント番号と、検索語のテーマ・タグ名への部分一致（OR条件）による抽出条件を生成する
   *
   * <p>本人のポートフォリオ一覧の絞り込みに使用する
   *
   * @param accountNo アカウント番号
   * @param keywordPattern 検索語のLIKEパターン（未指定はnull）
   * @return {@link PortfolioMstCondition}
   */
  public static PortfolioMstCondition byAccountNoAndKeyword(Long accountNo, String keywordPattern) {
    return PortfolioMstCondition.builder()
        .accountNo(accountNo)
        .keywordPattern(keywordPattern)
        .build();
  }
}
