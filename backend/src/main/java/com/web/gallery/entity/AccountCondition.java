package com.web.gallery.entity;

import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountGetModel;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** アカウントテーブルの抽出条件クラス */
@Data
@Builder
public class AccountCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** 削除フラグ */
  private Boolean isDeleted;

  /** アカウントID */
  private String accountId;

  /** アカウント名 */
  private String accountName;

  /** パスワード */
  private String password;

  /** 生年月日 */
  private LocalDate birthdate;

  /**
   * 性別区分
   *
   * <p>{@link SexEnum}
   */
  private SexEnum sexKbn;

  /** 出身都道府県区分コード */
  private String birthplacePrefectureKbnCode;

  /** 在住都道府県区分コード */
  private String residentPrefectureKbnCode;

  /** フリーメモ */
  private String freeMemo;

  /**
   * 権限区分
   *
   * <p>{@link AuthorityEnum}
   */
  private AuthorityEnum authorityKbn;

  /** 最終ログイン日時 */
  private OffsetDateTime lastLoginDatetime;

  /** ログイン失敗回数 */
  private Integer loginFailureCount;

  /** 管理者ロックフラグ */
  private Boolean isAdminLocked;

  /** 取得件数上限 */
  private Integer limit;

  /** 取得開始位置 */
  private Integer offset;

  /**
   * アカウント番号で検索するための抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link AccountCondition}
   */
  public static AccountCondition byAccountNo(Long accountNo) {
    return AccountCondition.builder().accountNo(accountNo).build();
  }

  /**
   * アカウントIDで検索するための抽出条件を生成する
   *
   * @param accountId アカウントID
   * @return {@link AccountCondition}
   */
  public static AccountCondition byAccountId(String accountId) {
    return AccountCondition.builder().accountId(accountId).build();
  }

  /**
   * アカウント存在チェック用の抽出条件を生成する
   *
   * @param accountNo 検索対象外のアカウント番号
   * @param accountId アカウントID
   * @return {@link AccountCondition}
   */
  public static AccountCondition forExistCheck(Long accountNo, String accountId) {
    return AccountCondition.builder().accountNo(accountNo).accountId(accountId).build();
  }

  /**
   * アカウント一覧取得用の抽出条件を生成する
   *
   * @param accountGetModel {@link AccountGetModel}
   * @return {@link AccountCondition}
   */
  public static AccountCondition forList(AccountGetModel accountGetModel) {
    return AccountCondition.builder()
        .isDeleted(false)
        .limit(accountGetModel.getLimit())
        .offset(accountGetModel.getOffset())
        .build();
  }

  /**
   * 管理者用アカウント一覧取得用の抽出条件を生成する（削除済みを含む全アカウントが対象）
   *
   * @param accountGetModel {@link AccountGetModel}
   * @return {@link AccountCondition}
   */
  public static AccountCondition forAdminList(AccountGetModel accountGetModel) {
    return AccountCondition.builder()
        .limit(accountGetModel.getLimit())
        .offset(accountGetModel.getOffset())
        .build();
  }
}
