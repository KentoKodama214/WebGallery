package com.web.gallery.presentation.response.auth;

import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * パスキー認証オプションのレスポンスパラメータを保持するクラス
 *
 * <p>ログイン用と、重要操作の前の本人確認（再認証）用の両方で使う。 W3C WebAuthnの{@code
 * PublicKeyCredentialRequestOptionsJSON}の形式で返し、クライアントは {@code
 * PublicKeyCredential.parseRequestOptionsFromJSON()}等でそのまま{@code navigator.credentials.get()}に渡す
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(description = "パスキー認証オプションレスポンス")
@Data
@Builder
public class PasskeyAuthenticationOptionsResponse {
  private static final String PUBLIC_KEY = "public-key";

  /** ユーザー検証の要求（生体認証・PINを必須とする） */
  private static final String USER_VERIFICATION_REQUIRED = "required";

  /** チャレンジ（base64url） */
  @Schema(description = "チャレンジ（base64url）")
  private String challenge;

  /** タイムアウト（ミリ秒） */
  @Schema(description = "タイムアウト（ミリ秒）", example = "300000")
  private Long timeout;

  /** RP ID */
  @Schema(description = "RP ID", example = "localhost")
  private String rpId;

  /** 許可するクレデンシャル（ログインではアカウントを特定しないため空、本人確認では本人のパスキー） */
  @Schema(description = "許可するクレデンシャル（ログインでは空、本人確認では本人のパスキー）")
  private List<CredentialDescriptor> allowCredentials;

  /** ユーザー検証の要求 */
  @Schema(description = "ユーザー検証の要求", example = "required")
  private String userVerification;

  /**
   * PasskeyAuthenticationOptionsModelからPasskeyAuthenticationOptionsResponseを生成する
   *
   * @param model {@link PasskeyAuthenticationOptionsModel}
   * @return {@link PasskeyAuthenticationOptionsResponse}
   */
  public static PasskeyAuthenticationOptionsResponse from(PasskeyAuthenticationOptionsModel model) {
    return PasskeyAuthenticationOptionsResponse.builder()
        .challenge(model.getChallenge().value())
        .timeout(model.getTimeoutMillis().value())
        .rpId(model.getRpId().value())
        .allowCredentials(
            model.getAllowCredentialIds().stream()
                .map(id -> new CredentialDescriptor(id.value(), PUBLIC_KEY))
                .toList())
        .userVerification(USER_VERIFICATION_REQUIRED)
        .build();
  }

  /**
   * クレデンシャルの識別子
   *
   * @param id クレデンシャルID（base64url）
   * @param type クレデンシャルの種類（public-key）
   */
  @Schema(description = "クレデンシャルの識別子")
  public record CredentialDescriptor(String id, String type) {}
}
