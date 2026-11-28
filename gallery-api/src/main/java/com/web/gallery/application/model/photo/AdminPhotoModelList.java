package com.web.gallery.application.model.photo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * AdminPhotoModelのコレクションを表すクラス
 *
 * @param adminPhotoModelList {@link AdminPhotoModel}のリスト
 */
public record AdminPhotoModelList(List<AdminPhotoModel> adminPhotoModelList)
    implements Iterable<AdminPhotoModel> {

  public AdminPhotoModelList {
    Objects.requireNonNull(adminPhotoModelList);
  }

  /**
   * AdminPhotoModelのリストからAdminPhotoModelListを生成する
   *
   * @param adminPhotoModelList {@link AdminPhotoModel}のリスト
   * @return {@link AdminPhotoModelList}
   */
  public static AdminPhotoModelList of(List<AdminPhotoModel> adminPhotoModelList) {
    return new AdminPhotoModelList(adminPhotoModelList);
  }

  /**
   * 空のAdminPhotoModelListを生成する
   *
   * @return {@link AdminPhotoModelList}
   */
  public static AdminPhotoModelList empty() {
    return AdminPhotoModelList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return adminPhotoModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return adminPhotoModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link AdminPhotoModel}
   */
  public AdminPhotoModel get(int index) {
    return adminPhotoModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link AdminPhotoModel}のStream
   */
  public Stream<AdminPhotoModel> stream() {
    return adminPhotoModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link AdminPhotoModel}のList
   */
  public List<AdminPhotoModel> toList() {
    return adminPhotoModelList;
  }

  @Override
  public Iterator<AdminPhotoModel> iterator() {
    return adminPhotoModelList.iterator();
  }
}
