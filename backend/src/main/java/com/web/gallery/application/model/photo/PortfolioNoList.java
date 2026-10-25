package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.photo.PortfolioNo;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * PortfolioNoのコレクションを表すクラス
 *
 * @param portfolioNoList {@link PortfolioNo}のリスト
 */
public record PortfolioNoList(List<PortfolioNo> portfolioNoList) implements Iterable<PortfolioNo> {

  public PortfolioNoList {
    Objects.requireNonNull(portfolioNoList);
  }

  /**
   * PortfolioNoのリストからPortfolioNoListを生成する
   *
   * @param portfolioNoList {@link PortfolioNo}のリスト
   * @return {@link PortfolioNoList}
   */
  public static PortfolioNoList of(List<PortfolioNo> portfolioNoList) {
    return new PortfolioNoList(portfolioNoList);
  }

  /**
   * ポートフォリオ番号のLongリストからPortfolioNoListを生成する
   *
   * @param portfolioNoValueList ポートフォリオ番号のLongリスト
   * @return {@link PortfolioNoList}
   */
  public static PortfolioNoList from(List<Long> portfolioNoValueList) {
    return PortfolioNoList.of(portfolioNoValueList.stream().map(PortfolioNo::new).toList());
  }

  /**
   * 空のPortfolioNoListを生成する
   *
   * @return {@link PortfolioNoList}
   */
  public static PortfolioNoList empty() {
    return PortfolioNoList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return portfolioNoList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return portfolioNoList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link PortfolioNo}
   */
  public PortfolioNo get(int index) {
    return portfolioNoList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link PortfolioNo}のStream
   */
  public Stream<PortfolioNo> stream() {
    return portfolioNoList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link PortfolioNo}のList
   */
  public List<PortfolioNo> toList() {
    return portfolioNoList;
  }

  /**
   * 並び順を問わず、同じポートフォリオ番号の集合（重複なし）であるかを判定する
   *
   * @param other 比較対象の{@link PortfolioNoList}
   * @return 双方に重複がなく、同じポートフォリオ番号の集合である場合はtrue
   */
  public Boolean isSameSetAs(PortfolioNoList other) {
    Set<PortfolioNo> thisSet = Set.copyOf(portfolioNoList);
    Set<PortfolioNo> otherSet = Set.copyOf(other.toList());
    return thisSet.size() == portfolioNoList.size()
        && otherSet.size() == other.size()
        && thisSet.equals(otherSet);
  }

  @Override
  public Iterator<PortfolioNo> iterator() {
    return portfolioNoList.iterator();
  }
}
