package com.web.gallery.application.model.photo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PortfolioModelのコレクションを表すクラス
 *
 * @param portfolioModelList {@link PortfolioModel}のリスト
 */
public record PortfolioModelList(List<PortfolioModel> portfolioModelList)
    implements Iterable<PortfolioModel> {

  public PortfolioModelList {
    Objects.requireNonNull(portfolioModelList);
  }

  /**
   * PortfolioModelのリストからPortfolioModelListを生成する
   *
   * @param portfolioModelList {@link PortfolioModel}のリスト
   * @return {@link PortfolioModelList}
   */
  public static PortfolioModelList of(List<PortfolioModel> portfolioModelList) {
    return new PortfolioModelList(portfolioModelList);
  }

  /**
   * 空のPortfolioModelListを生成する
   *
   * @return {@link PortfolioModelList}
   */
  public static PortfolioModelList empty() {
    return PortfolioModelList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return portfolioModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return portfolioModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link PortfolioModel}
   */
  public PortfolioModel get(int index) {
    return portfolioModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link PortfolioModel}のStream
   */
  public Stream<PortfolioModel> stream() {
    return portfolioModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link PortfolioModel}のList
   */
  public List<PortfolioModel> toList() {
    return portfolioModelList;
  }

  @Override
  public Iterator<PortfolioModel> iterator() {
    return portfolioModelList.iterator();
  }
}
