package com.web.gallery.infrastructure.persistence.entity.auth;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** パスキーテーブルの更新対象クラス */
@Data
@Builder
public class PasskeyCredentialUpdateTarget {
  /** 署名カウンタ */
  private Long signCount;

  /** バックアップ済みフラグ */
  private Boolean isBackedUp;

  /** 最終使用日時 */
  private OffsetDateTime lastUsedAt;

  /** パスキー名 */
  private String passkeyName;

  /** 更新者 */
  private Long updatedBy;

  /**
   * 認証結果を反映したPasskeyCredentialModelから更新対象を生成する
   *
   * @param model {@link PasskeyCredentialModel}
   * @return {@link PasskeyCredentialUpdateTarget}
   */
  public static PasskeyCredentialUpdateTarget forAuthenticationResult(
      PasskeyCredentialModel model) {
    return PasskeyCredentialUpdateTarget.builder()
        .signCount(model.getSignCount().value())
        .isBackedUp(model.getIsBackedUp().value())
        .lastUsedAt(model.getLastUsedAt().value())
        .updatedBy(model.getAccountNo().value())
        .build();
  }

  /**
   * パスキー名の変更用に更新対象を生成する
   *
   * @param passkeyName 変更後のパスキー名
   * @param updatedBy 更新者
   * @return {@link PasskeyCredentialUpdateTarget}
   */
  public static PasskeyCredentialUpdateTarget forRename(String passkeyName, Long updatedBy) {
    return PasskeyCredentialUpdateTarget.builder()
        .passkeyName(passkeyName)
        .updatedBy(updatedBy)
        .build();
  }
}
