package com.web.gallery.entity;

import com.web.gallery.enumeration.AuthorityEnum;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** アカウント権限テーブルのEntityクラス */
@Data
@Builder
public class AccountAuthority {
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

  /**
   * 権限区分
   *
   * <p>{@link AuthorityEnum}
   */
  private AuthorityEnum authorityKbn;

  /**
   * アカウント登録用のAccountAuthorityエンティティを生成する
   *
   * <p>新規登録時の権限区分は常に{@link AuthorityEnum#MINI}で固定する（外部から指定不可）
   *
   * @param accountNo アカウント番号
   * @return {@link AccountAuthority}
   */
  public static AccountAuthority forRegist(Long accountNo) {
    return AccountAuthority.builder()
        .accountNo(accountNo)
        .createdBy(0L)
        .updatedBy(0L)
        .authorityKbn(AuthorityEnum.MINI)
        .build();
  }
}
