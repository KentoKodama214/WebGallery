package com.web.gallery.application.model.photo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * FrameTemplateModelのコレクションを表すクラス
 *
 * @param frameTemplateModelList {@link FrameTemplateModel}のリスト
 */
public record FrameTemplateModelList(List<FrameTemplateModel> frameTemplateModelList)
    implements Iterable<FrameTemplateModel> {

  public FrameTemplateModelList {
    Objects.requireNonNull(frameTemplateModelList);
  }

  /**
   * FrameTemplateModelのリストからFrameTemplateModelListを生成する
   *
   * @param frameTemplateModelList {@link FrameTemplateModel}のリスト
   * @return {@link FrameTemplateModelList}
   */
  public static FrameTemplateModelList of(List<FrameTemplateModel> frameTemplateModelList) {
    return new FrameTemplateModelList(frameTemplateModelList);
  }

  /**
   * 空のFrameTemplateModelListを生成する
   *
   * @return {@link FrameTemplateModelList}
   */
  public static FrameTemplateModelList empty() {
    return FrameTemplateModelList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return frameTemplateModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return frameTemplateModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link FrameTemplateModel}
   */
  public FrameTemplateModel get(int index) {
    return frameTemplateModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link FrameTemplateModel}のStream
   */
  public Stream<FrameTemplateModel> stream() {
    return frameTemplateModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link FrameTemplateModel}のList
   */
  public List<FrameTemplateModel> toList() {
    return frameTemplateModelList;
  }

  @Override
  public Iterator<FrameTemplateModel> iterator() {
    return frameTemplateModelList.iterator();
  }
}
