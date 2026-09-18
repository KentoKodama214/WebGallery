package com.web.gallery.entity;

import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryGetModel;
import lombok.Builder;
import lombok.Data;

/** お問い合わせマスタテーブルの抽出条件クラス */
@Data
@Builder
public class InquiryMstCondition {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** お問い合わせ番号 */
  private Long inquiryNo;

  /**
   * ステータス区分
   *
   * <p>{@link InquiryStatusEnum}
   */
  private InquiryStatusEnum statusKbn;

  /** 取得件数上限 */
  private Integer limit;

  /** 取得開始位置 */
  private Integer offset;

  /**
   * ID（サロゲートキー）による抽出条件を生成する
   *
   * @param id ID
   * @return {@link InquiryMstCondition}
   */
  public static InquiryMstCondition byId(Long id) {
    return InquiryMstCondition.builder().id(id).build();
  }

  /**
   * アカウント番号・お問い合わせ番号による抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryMstCondition}
   */
  public static InquiryMstCondition byAccountAndInquiryNo(Long accountNo, Long inquiryNo) {
    return InquiryMstCondition.builder().accountNo(accountNo).inquiryNo(inquiryNo).build();
  }

  /**
   * 自分のお問い合わせ一覧取得用の抽出条件を生成する
   *
   * @param inquiryGetModel {@link InquiryGetModel}
   * @return {@link InquiryMstCondition}
   */
  public static InquiryMstCondition forList(InquiryGetModel inquiryGetModel) {
    return InquiryMstCondition.builder()
        .accountNo(inquiryGetModel.getAccountNo().value())
        .limit(inquiryGetModel.getLimit())
        .offset(inquiryGetModel.getOffset())
        .build();
  }

  /**
   * 管理者用お問い合わせ一覧取得用の抽出条件を生成する
   *
   * @param inquiryGetModel {@link InquiryGetModel}
   * @return {@link InquiryMstCondition}
   */
  public static InquiryMstCondition forAdminList(InquiryGetModel inquiryGetModel) {
    return InquiryMstCondition.builder()
        .statusKbn(inquiryGetModel.getStatusKbn())
        .limit(inquiryGetModel.getLimit())
        .offset(inquiryGetModel.getOffset())
        .build();
  }
}
