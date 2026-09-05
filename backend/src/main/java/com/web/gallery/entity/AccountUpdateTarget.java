package com.web.gallery.entity;

import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountModel;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

/** アカウントテーブルの更新対象クラス */
@Data
@Builder
public class AccountUpdateTarget {
  /** 更新者 */
  private Long updatedBy;

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

  /**
   * アカウント更新用のAccountModelから更新対象を生成する
   *
   * @param model {@link AccountModel}
   * @param passwordEncoder {@link PasswordEncoder}
   * @return {@link AccountUpdateTarget}
   */
  public static AccountUpdateTarget fromForUpdate(
      AccountModel model, PasswordEncoder passwordEncoder) {
    // Modelに値がある項目だけを更新対象に含める（AccountMapper.xmlのUPDATEは target.xxx != null の項目のみSET句に出す）。
    // 未指定項目をセンチネル値で埋めると、プロフィール更新のたびに last_login_datetime や
    // login_failure_count まで書き換わり、監査情報の破壊やロックカウンタの意図しないリセットを招くため、
    // null は「変更なし」としてそのまま渡す。
    AccountUpdateTarget target =
        AccountUpdateTarget.builder()
            .accountId(model.getAccountId() != null ? model.getAccountId().value() : null)
            .accountName(model.getAccountName() != null ? model.getAccountName().value() : null)
            .birthdate(model.getBirthdate() != null ? model.getBirthdate().value() : null)
            .sexKbn(model.getSexKbn())
            .birthplacePrefectureKbnCode(
                model.getBirthplacePrefectureKbnCode() != null
                    ? model.getBirthplacePrefectureKbnCode().value()
                    : null)
            .residentPrefectureKbnCode(
                model.getResidentPrefectureKbnCode() != null
                    ? model.getResidentPrefectureKbnCode().value()
                    : null)
            .freeMemo(model.getFreeMemo() != null ? model.getFreeMemo().value() : null)
            .lastLoginDatetime(
                model.getLastLoginDatetime() != null ? model.getLastLoginDatetime().value() : null)
            .loginFailureCount(
                model.getLoginFailureCount() != null ? model.getLoginFailureCount().value() : null)
            .build();

    if (model.getPassword() != null) {
      target.setPassword(passwordEncoder.encode(model.getPassword().value()));
    }

    return target;
  }

  /**
   * ログイン失敗回数更新用のAccountModelから更新対象を生成する
   *
   * @param model {@link AccountModel}
   * @return {@link AccountUpdateTarget}
   */
  public static AccountUpdateTarget fromForUpdateLoginFailure(AccountModel model) {
    return AccountUpdateTarget.builder()
        .lastLoginDatetime(
            model.getLastLoginDatetime() != null ? model.getLastLoginDatetime().value() : null)
        .loginFailureCount(
            model.getLoginFailureCount() != null ? model.getLoginFailureCount().value() : 0)
        .isAdminLocked(model.getIsAdminLocked() != null ? model.getIsAdminLocked().value() : null)
        .build();
  }
}
