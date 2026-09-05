package com.web.gallery.model;

import com.web.gallery.domain.photo.PhotoNo;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PhotoNoのコレクションを表すクラス
 *
 * @param photoNoList {@link PhotoNo}のリスト
 */
public record PhotoNoList(List<PhotoNo> photoNoList) implements Iterable<PhotoNo> {

  public PhotoNoList {
    Objects.requireNonNull(photoNoList);
  }

  /**
   * PhotoNoのリストからPhotoNoListを生成する
   *
   * @param photoNoList {@link PhotoNo}のリスト
   * @return {@link PhotoNoList}
   */
  public static PhotoNoList of(List<PhotoNo> photoNoList) {
    return new PhotoNoList(photoNoList);
  }

  /**
   * 写真番号のLongリストからPhotoNoListを生成する
   *
   * @param photoNoValueList 写真番号のLongリスト
   * @return {@link PhotoNoList}
   */
  public static PhotoNoList from(List<Long> photoNoValueList) {
    return PhotoNoList.of(photoNoValueList.stream().map(PhotoNo::new).toList());
  }

  /**
   * 空のPhotoNoListを生成する
   *
   * @return {@link PhotoNoList}
   */
  public static PhotoNoList empty() {
    return PhotoNoList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return photoNoList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return photoNoList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link PhotoNo}
   */
  public PhotoNo get(int index) {
    return photoNoList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link PhotoNo}のStream
   */
  public Stream<PhotoNo> stream() {
    return photoNoList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link PhotoNo}のList
   */
  public List<PhotoNo> toList() {
    return photoNoList;
  }

  @Override
  public Iterator<PhotoNo> iterator() {
    return photoNoList.iterator();
  }
}
