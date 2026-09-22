package com.web.gallery.model.common;

import com.web.gallery.entity.common.LocationMst;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * LocationModelのコレクションを表すクラス
 *
 * @param locationModelList {@link LocationModel}のリスト
 */
public record LocationModelList(List<LocationModel> locationModelList)
    implements Iterable<LocationModel> {

  public LocationModelList {
    Objects.requireNonNull(locationModelList);
  }

  /**
   * LocationModelのリストからLocationModelListを生成する
   *
   * @param locationModelList {@link LocationModel}のリスト
   * @return {@link LocationModelList}
   */
  public static LocationModelList of(List<LocationModel> locationModelList) {
    return new LocationModelList(locationModelList);
  }

  /**
   * 空のLocationModelListを生成する
   *
   * @return {@link LocationModelList}
   */
  public static LocationModelList empty() {
    return LocationModelList.of(List.of());
  }

  /**
   * LocationMstエンティティのリストからLocationModelListを生成する
   *
   * @param locationMstList {@link LocationMst}のリスト
   * @return {@link LocationModelList}
   */
  public static LocationModelList from(List<LocationMst> locationMstList) {
    return LocationModelList.of(locationMstList.stream().map(LocationModel::from).toList());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return locationModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return locationModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link LocationModel}
   */
  public LocationModel get(int index) {
    return locationModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link LocationModel}のStream
   */
  public Stream<LocationModel> stream() {
    return locationModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link LocationModel}のList
   */
  public List<LocationModel> toList() {
    return locationModelList;
  }

  @Override
  public Iterator<LocationModel> iterator() {
    return locationModelList.iterator();
  }
}
