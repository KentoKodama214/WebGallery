package com.web.gallery.infrastructure.persistence.entity.auth;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * リカバリーコードテーブルのEntityクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class RecoveryCode {
  /** リカバリーコードNo */
  private Long recoveryCodeNo;

  /** アカウント番号 */
  private Long accountNo;

  /** コードハッシュ */
  private String codeHash;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /**
   * アカウント番号・コードハッシュからRecoveryCodeエンティティを生成する
   *
   * @param accountNo アカウント番号
   * @param codeHash コードハッシュ
   * @return {@link RecoveryCode}
   */
  public static RecoveryCode of(Long accountNo, String codeHash) {
    return RecoveryCode.builder()
        .accountNo(accountNo)
        .codeHash(codeHash)
        .createdBy(accountNo)
        .build();
  }
}
