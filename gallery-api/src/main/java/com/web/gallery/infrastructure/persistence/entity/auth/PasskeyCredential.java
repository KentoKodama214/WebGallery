package com.web.gallery.infrastructure.persistence.entity.auth;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * パスキーテーブルのEntityクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class PasskeyCredential {
  /** パスキー番号 */
  private Long passkeyNo;

  /** アカウント番号 */
  private Long accountNo;

  /** クレデンシャルID（base64url） */
  private String credentialId;

  /** 認証器データ */
  private byte[] attestedCredentialData;

  /** 署名カウンタ */
  private Long signCount;

  /** 認証器の通信手段（カンマ区切り） */
  private String transports;

  /** バックアップ可能フラグ */
  private Boolean isBackupEligible;

  /** バックアップ済みフラグ */
  private Boolean isBackedUp;

  /** ユーザー検証済みフラグ */
  private Boolean isUvInitialized;

  /** パスキー名 */
  private String passkeyName;

  /** 最終使用日時 */
  private OffsetDateTime lastUsedAt;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新者 */
  private Long updatedBy;

  /** 更新日時 */
  private OffsetDateTime updatedAt;

  /**
   * PasskeyCredentialModelからPasskeyCredentialエンティティを生成する
   *
   * @param model {@link PasskeyCredentialModel}
   * @return {@link PasskeyCredential}
   */
  public static PasskeyCredential from(PasskeyCredentialModel model) {
    return PasskeyCredential.builder()
        .accountNo(model.getAccountNo().value())
        .credentialId(model.getCredentialId().value())
        .attestedCredentialData(model.getAttestedCredentialData().value())
        .signCount(model.getSignCount().value())
        .transports(model.getTransports().value())
        .isBackupEligible(model.getIsBackupEligible().value())
        .isBackedUp(model.getIsBackedUp().value())
        .isUvInitialized(model.getIsUvInitialized().value())
        .passkeyName(model.getPasskeyName().value())
        .createdBy(model.getAccountNo().value())
        .updatedBy(model.getAccountNo().value())
        .build();
  }
}
