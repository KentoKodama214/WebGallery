package com.web.gallery.application.model.inquiry;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * InquiryReplyModelのコレクションを表すクラス
 *
 * @param inquiryReplyModelList {@link InquiryReplyModel}のリスト
 */
public record InquiryReplyModelList(List<InquiryReplyModel> inquiryReplyModelList)
    implements Iterable<InquiryReplyModel> {

  public InquiryReplyModelList {
    Objects.requireNonNull(inquiryReplyModelList);
  }

  /**
   * InquiryReplyModelのリストからInquiryReplyModelListを生成する
   *
   * @param inquiryReplyModelList {@link InquiryReplyModel}のリスト
   * @return {@link InquiryReplyModelList}
   */
  public static InquiryReplyModelList of(List<InquiryReplyModel> inquiryReplyModelList) {
    return new InquiryReplyModelList(inquiryReplyModelList);
  }

  /**
   * 空のInquiryReplyModelListを生成する
   *
   * @return {@link InquiryReplyModelList}
   */
  public static InquiryReplyModelList empty() {
    return InquiryReplyModelList.of(List.of());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return inquiryReplyModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return inquiryReplyModelList.isEmpty();
  }

  /**
   * Streamに変換する
   *
   * @return {@link InquiryReplyModel}のStream
   */
  public Stream<InquiryReplyModel> stream() {
    return inquiryReplyModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link InquiryReplyModel}のList
   */
  public List<InquiryReplyModel> toList() {
    return inquiryReplyModelList;
  }

  @Override
  public Iterator<InquiryReplyModel> iterator() {
    return inquiryReplyModelList.iterator();
  }
}
