package com.web.gallery.entity.account;

import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.model.account.AccountModel;
import lombok.Builder;
import lombok.Data;

/** アカウント権限テーブルの更新対象クラス */
@Data
@Builder
public class AccountAuthorityUpdateTarget {
  /** 更新者 */
  private Long updatedBy;

  /**
   * 権限区分
   *
   * <p>{@link AuthorityEnum}
   */
  private AuthorityEnum authorityKbn;

  /**
   * 権限変更用のAccountModelから更新対象を生成する
   *
   * @param model {@link AccountModel}
   * @return {@link AccountAuthorityUpdateTarget}
   */
  public static AccountAuthorityUpdateTarget fromForUpdate(AccountModel model) {
    return AccountAuthorityUpdateTarget.builder()
        .updatedBy(0L)
        .authorityKbn(model.getAuthorityKbn())
        .build();
  }
}
