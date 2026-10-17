package com.web.gallery.application.model.photo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PortfolioDetailModelのコレクションを表すクラス
 *
 * @param portfolioDetailModelList {@link PortfolioDetailModel}のリスト
 */
public record PortfolioDetailModelList(List<PortfolioDetailModel> portfolioDetailModelList)
    implements Iterable<PortfolioDetailModel> {

  public PortfolioDetailModelList {
    Objects.requireNonNull(portfolioDetailModelList);
  }

  /**
   * PortfolioDetailModelのリストからPortfolioDetailModelListを生成する
   *
   * @param portfolioDetailModelList {@link PortfolioDetailModel}のリスト
   * @return {@link PortfolioDetailModelList}
   */
  public static PortfolioDetailModelList of(List<PortfolioDetailModel> portfolioDetailModelList) {
    return new PortfolioDetailModelList(portfolioDetailModelList);
  }

  /**
   * 空のPortfolioDetailModelListを生成する
   *
   * @return {@link PortfolioDetailModelList}
   */
  public static PortfolioDetailModelList empty() {
    return PortfolioDetailModelList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return portfolioDetailModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return portfolioDetailModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link PortfolioDetailModel}
   */
  public PortfolioDetailModel get(int index) {
    return portfolioDetailModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link PortfolioDetailModel}のStream
   */
  public Stream<PortfolioDetailModel> stream() {
    return portfolioDetailModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link PortfolioDetailModel}のList
   */
  public List<PortfolioDetailModel> toList() {
    return portfolioDetailModelList;
  }

  @Override
  public Iterator<PortfolioDetailModel> iterator() {
    return portfolioDetailModelList.iterator();
  }
}
