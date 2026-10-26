package com.web.gallery.application.model.photo;

import com.web.gallery.domain.enumeration.MoveDirectionEnum;
import com.web.gallery.domain.model.photo.PortfolioNo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
   * 指定したポートフォリオ番号を含むかどうかを判定する
   *
   * @param portfolioNo {@link PortfolioNo}
   * @return 含む場合はtrue
   */
  public Boolean contains(PortfolioNo portfolioNo) {
    return portfolioNoList.contains(portfolioNo);
  }

  /**
   * 指定したポートフォリオ番号を、隣の要素と入れ替えて1つ上（前）・下（後ろ）へ移動した新しいPortfolioNoListを生成する
   *
   * <p>先頭を上へ・末尾を下へ移動しようとした場合と、含まれないポートフォリオ番号を指定した場合は、並びを変えずに返す
   *
   * @param portfolioNo 移動するポートフォリオ番号
   * @param direction 移動方向
   * @return 移動後の{@link PortfolioNoList}
   */
  public PortfolioNoList move(PortfolioNo portfolioNo, MoveDirectionEnum direction) {
    int index = portfolioNoList.indexOf(portfolioNo);
    int targetIndex = direction == MoveDirectionEnum.UP ? index - 1 : index + 1;
    if (index < 0 || targetIndex < 0 || targetIndex >= portfolioNoList.size()) {
      return this;
    }
    List<PortfolioNo> moved = new ArrayList<>(portfolioNoList);
    Collections.swap(moved, index, targetIndex);
    return PortfolioNoList.of(moved);
  }

  @Override
  public Iterator<PortfolioNo> iterator() {
    return portfolioNoList.iterator();
  }
}
