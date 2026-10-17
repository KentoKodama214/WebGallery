package com.web.gallery.application.model.photo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PortfolioTagModelのコレクションを表すクラス
 *
 * @param portfolioTagModelList {@link PortfolioTagModel}のリスト
 */
public record PortfolioTagModelList(List<PortfolioTagModel> portfolioTagModelList)
    implements Iterable<PortfolioTagModel> {

  public PortfolioTagModelList {
    Objects.requireNonNull(portfolioTagModelList);
  }

  /**
   * PortfolioTagModelのリストからPortfolioTagModelListを生成する
   *
   * @param portfolioTagModelList {@link PortfolioTagModel}のリスト
   * @return {@link PortfolioTagModelList}
   */
  public static PortfolioTagModelList of(List<PortfolioTagModel> portfolioTagModelList) {
    return new PortfolioTagModelList(portfolioTagModelList);
  }

  /**
   * 空のPortfolioTagModelListを生成する
   *
   * @return {@link PortfolioTagModelList}
   */
  public static PortfolioTagModelList empty() {
    return PortfolioTagModelList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return portfolioTagModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return portfolioTagModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link PortfolioTagModel}
   */
  public PortfolioTagModel get(int index) {
    return portfolioTagModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link PortfolioTagModel}のStream
   */
  public Stream<PortfolioTagModel> stream() {
    return portfolioTagModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link PortfolioTagModel}のList
   */
  public List<PortfolioTagModel> toList() {
    return portfolioTagModelList;
  }

  @Override
  public Iterator<PortfolioTagModel> iterator() {
    return portfolioTagModelList.iterator();
  }
}
