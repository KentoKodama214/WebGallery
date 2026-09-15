package com.web.gallery.dto;

import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Data;

/** アカウントと権限区分を結合した情報を保持するDtoクラス */
@Data
public class AccountDto {
  /** アカウント番号 */
  private Long accountNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新者 */
  private Long updatedBy;

  /** 更新日時 */
  private OffsetDateTime updatedAt;

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
}
