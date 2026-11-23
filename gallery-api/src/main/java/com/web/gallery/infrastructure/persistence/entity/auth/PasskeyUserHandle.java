package com.web.gallery.infrastructure.persistence.entity.auth;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * パスキーユーザーハンドルテーブルのEntityクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class PasskeyUserHandle {
  /** パスキーユーザーハンドルNo */
  private Long passkeyUserHandleNo;

  /** アカウント番号 */
  private Long accountNo;

  /** ユーザーハンドル */
  private String userHandle;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /**
   * アカウント番号・ユーザーハンドルからPasskeyUserHandleエンティティを生成する
   *
   * @param accountNo アカウント番号
   * @param userHandle ユーザーハンドル
   * @return {@link PasskeyUserHandle}
   */
  public static PasskeyUserHandle of(Long accountNo, String userHandle) {
    return PasskeyUserHandle.builder()
        .accountNo(accountNo)
        .userHandle(userHandle)
        .createdBy(accountNo)
        .build();
  }
}
