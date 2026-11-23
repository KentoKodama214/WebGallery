package com.web.gallery.presentation.response.account;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * パスキーのレスポンスパラメータを保持するクラス
 *
 * <p>公開鍵・クレデンシャルID・署名カウンタなどの技術的な値は返さない
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(description = "パスキーレスポンス")
@Data
@Builder
public class PasskeyResponse {
  /** パスキー番号 */
  @Schema(description = "パスキー番号", example = "1")
  private Long passkeyNo;

  /** パスキー名 */
  @Schema(description = "パスキー名", example = "MacBookのTouch ID")
  private String passkeyName;

  /** 同期パスキーかどうか（複数の端末で使える） */
  @Schema(description = "同期パスキーかどうか（iCloudキーチェーン・Googleパスワードマネージャー等で複数の端末に同期される）")
  private Boolean isSynced;

  /** 登録日時 */
  @Schema(description = "登録日時")
  private OffsetDateTime createdAt;

  /** 最終使用日時 */
  @Schema(description = "最終使用日時")
  private OffsetDateTime lastUsedAt;

  /**
   * PasskeyCredentialModelからPasskeyResponseを生成する
   *
   * @param model {@link PasskeyCredentialModel}
   * @return {@link PasskeyResponse}
   */
  public static PasskeyResponse from(PasskeyCredentialModel model) {
    return PasskeyResponse.builder()
        .passkeyNo(model.getPasskeyNo() != null ? model.getPasskeyNo().value() : null)
        .passkeyName(model.getPasskeyName().value())
        .isSynced(model.getIsBackupEligible().value())
        .createdAt(model.getCreatedAt() != null ? model.getCreatedAt().value() : null)
        .lastUsedAt(model.getLastUsedAt() != null ? model.getLastUsedAt().value() : null)
        .build();
  }
}
