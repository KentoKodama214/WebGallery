package com.web.gallery.infrastructure.persistence.mapper.inquiry;

import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMst;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMstCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** お問い合わせ返信マスタテーブルのMapperクラス */
@Mapper
public interface InquiryReplyMstMapper {
  /**
   * お問い合わせ返信マスタを登録する
   *
   * @param inquiryReplyMst {@link InquiryReplyMst}
   * @return 登録件数
   */
  public Integer insert(InquiryReplyMst inquiryReplyMst);

  /**
   * お問い合わせが登録済みの最大の返信番号を取得する
   *
   * @param inquiryId お問い合わせID
   * @return 最大の返信番号
   */
  public Long getMaxReplyNo(Long inquiryId);

  /**
   * 条件に該当するお問い合わせ返信マスタを物理削除する
   *
   * @param condition 削除対象の抽出条件
   * @return 削除件数
   */
  public Integer delete(InquiryReplyMstCondition condition);

  /**
   * 条件に該当するお問い合わせ返信の件数を取得する
   *
   * @param condition {@link InquiryReplyMstCondition}
   * @return 件数
   */
  public int count(InquiryReplyMstCondition condition);

  /**
   * 条件に該当するお問い合わせ返信一覧を、返信番号の昇順で取得する
   *
   * @param condition {@link InquiryReplyMstCondition}
   * @return {@link InquiryReplyMst}のリスト
   */
  public List<InquiryReplyMst> selectList(InquiryReplyMstCondition condition);
}
