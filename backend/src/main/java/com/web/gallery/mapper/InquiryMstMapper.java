package com.web.gallery.mapper;

import com.web.gallery.dto.InquiryDetailDto;
import com.web.gallery.dto.InquiryDto;
import com.web.gallery.entity.inquiry.InquiryMst;
import com.web.gallery.entity.inquiry.InquiryMstCondition;
import com.web.gallery.entity.inquiry.InquiryMstUpdateTarget;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** お問い合わせマスタテーブルのMapperクラス */
@Mapper
public interface InquiryMstMapper {
  /**
   * お問い合わせマスタを登録する
   *
   * @param inquiryMst {@link InquiryMst}
   * @return 登録件数
   */
  public Integer insert(InquiryMst inquiryMst);

  /**
   * お問い合わせマスタを更新する
   *
   * @param condition 更新対象の抽出条件
   * @param target 更新内容
   * @return 更新件数
   */
  public Integer update(
      @Param("condition") InquiryMstCondition condition,
      @Param("target") InquiryMstUpdateTarget target);

  /**
   * アカウントが登録済みの最大のお問い合わせ番号を取得する
   *
   * @param accountNo アカウント番号
   * @return 最大のお問い合わせ番号
   */
  public Long getMaxInquiryNo(Long accountNo);

  /**
   * 条件に該当する自分のお問い合わせ一覧を取得する
   *
   * @param condition {@link InquiryMstCondition}
   * @return {@link InquiryDto}のリスト
   */
  public List<InquiryDto> selectList(InquiryMstCondition condition);

  /**
   * 条件に該当するお問い合わせ一覧を、アカウント（{@code common.account}）と結合して取得する（管理者用）
   *
   * @param condition {@link InquiryMstCondition}
   * @return {@link InquiryDto}のリスト
   */
  public List<InquiryDto> selectListForAdmin(InquiryMstCondition condition);

  /**
   * 条件に該当する自分のお問い合わせ詳細を取得する
   *
   * @param condition {@link InquiryMstCondition}
   * @return {@link InquiryDetailDto}
   */
  public InquiryDetailDto selectDetail(InquiryMstCondition condition);

  /**
   * 条件に該当するお問い合わせ詳細を、アカウント（{@code common.account}）と結合して取得する（管理者用）
   *
   * @param condition {@link InquiryMstCondition}
   * @return {@link InquiryDetailDto}
   */
  public InquiryDetailDto selectDetailForAdmin(InquiryMstCondition condition);
}
