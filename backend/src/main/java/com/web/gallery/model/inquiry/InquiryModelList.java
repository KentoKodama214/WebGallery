package com.web.gallery.model.inquiry;

import com.web.gallery.dto.InquiryDto;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * InquiryModelのコレクションを表すクラス
 *
 * @param inquiryModelList {@link InquiryModel}のリスト
 */
public record InquiryModelList(List<InquiryModel> inquiryModelList)
    implements Iterable<InquiryModel> {

  public InquiryModelList {
    Objects.requireNonNull(inquiryModelList);
  }

  /**
   * InquiryModelのリストからInquiryModelListを生成する
   *
   * @param inquiryModelList {@link InquiryModel}のリスト
   * @return {@link InquiryModelList}
   */
  public static InquiryModelList of(List<InquiryModel> inquiryModelList) {
    return new InquiryModelList(inquiryModelList);
  }

  /**
   * 空のInquiryModelListを生成する
   *
   * @return {@link InquiryModelList}
   */
  public static InquiryModelList empty() {
    return InquiryModelList.of(List.of());
  }

  /**
   * InquiryDtoのリストからInquiryModelListを生成する
   *
   * @param inquiryDtoList {@link InquiryDto}のリスト
   * @return {@link InquiryModelList}
   */
  public static InquiryModelList from(List<InquiryDto> inquiryDtoList) {
    return InquiryModelList.of(inquiryDtoList.stream().map(InquiryModel::from).toList());
  }

  /**
   * 要素数を取得する
   *
   * @return 要素数
   */
  public Integer size() {
    return inquiryModelList.size();
  }

  /**
   * 要素が空かどうかを取得する
   *
   * @return 要素が空の場合はtrue
   */
  public Boolean isEmpty() {
    return inquiryModelList.isEmpty();
  }

  /**
   * 指定インデックスの要素を取得する
   *
   * @param index インデックス
   * @return {@link InquiryModel}
   */
  public InquiryModel get(int index) {
    return inquiryModelList.get(index);
  }

  /**
   * Streamに変換する
   *
   * @return {@link InquiryModel}のStream
   */
  public Stream<InquiryModel> stream() {
    return inquiryModelList.stream();
  }

  /**
   * Listに変換する
   *
   * @return {@link InquiryModel}のList
   */
  public List<InquiryModel> toList() {
    return inquiryModelList;
  }

  @Override
  public Iterator<InquiryModel> iterator() {
    return inquiryModelList.iterator();
  }
}
