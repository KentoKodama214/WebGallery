package com.web.gallery.application.model.photo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PortfolioPhotoModelのコレクションを表すクラス
 *
 * @param portfolioPhotoModelList {@link PortfolioPhotoModel}のリスト
 */
public record PortfolioPhotoModelList(List<PortfolioPhotoModel> portfolioPhotoModelList)
    implements Iterable<PortfolioPhotoModel> {

  public PortfolioPhotoModelList {
    Objects.requireNonNull(portfolioPhotoModelList);
  }

  /**
   * PortfolioPhotoModelのリストからPortfolioPhotoModelListを生成する
   *
   * @param portfolioPhotoModelList {@link PortfolioPhotoModel}のリスト
   * @return {@link PortfolioPhotoModelList}
   */
  public static PortfolioPhotoModelList of(List<PortfolioPhotoModel> portfolioPhotoModelList) {
    return new PortfolioPhotoModelList(portfolioPhotoModelList);
  }

  /**
   * 空のPortfolioPhotoModelListを生成する
   *
   * @return {@link PortfolioPhotoModelList}
   */
  public static PortfolioPhotoModelList empty() {
    return PortfolioPhotoModelList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return portfolioPhotoModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return portfolioPhotoModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link PortfolioPhotoModel}
   */
  public PortfolioPhotoModel get(int index) {
    return portfolioPhotoModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link PortfolioPhotoModel}のStream
   */
  public Stream<PortfolioPhotoModel> stream() {
    return portfolioPhotoModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link PortfolioPhotoModel}のList
   */
  public List<PortfolioPhotoModel> toList() {
    return portfolioPhotoModelList;
  }

  @Override
  public Iterator<PortfolioPhotoModel> iterator() {
    return portfolioPhotoModelList.iterator();
  }
}
