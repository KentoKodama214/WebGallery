package com.web.gallery.presentation.response.account;

import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * パスキー登録オプションのレスポンスパラメータを保持するクラス
 *
 * <p>W3C WebAuthnの{@code PublicKeyCredentialCreationOptionsJSON}の形式で返し、クライアントは {@code
 * PublicKeyCredential.parseCreationOptionsFromJSON()}等でそのまま{@code
 * navigator.credentials.create()}に渡す
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(description = "パスキー登録オプションレスポンス")
@Data
@Builder
public class PasskeyRegistrationOptionsResponse {
  private static final String PUBLIC_KEY = "public-key";
  private static final String REQUIRED = "required";
  private static final String ATTESTATION_NONE = "none";

  /** RP（サービス）情報 */
  @Schema(description = "RP（サービス）情報")
  private RelyingParty rp;

  /** ユーザー情報 */
  @Schema(description = "ユーザー情報")
  private User user;

  /** チャレンジ（base64url） */
  @Schema(description = "チャレンジ（base64url）")
  private String challenge;

  /** 受け付ける公開鍵の署名アルゴリズム */
  @Schema(description = "受け付ける公開鍵の署名アルゴリズム")
  private List<PublicKeyCredentialParameter> pubKeyCredParams;

  /** タイムアウト（ミリ秒） */
  @Schema(description = "タイムアウト（ミリ秒）", example = "300000")
  private Long timeout;

  /** 登録済みのクレデンシャル（同じ認証器への二重登録を防ぐ） */
  @Schema(description = "登録済みのクレデンシャル（同じ認証器への二重登録を防ぐ）")
  private List<CredentialDescriptor> excludeCredentials;

  /** 認証器の選択条件 */
  @Schema(description = "認証器の選択条件")
  private AuthenticatorSelection authenticatorSelection;

  /** attestationの要求（認証器の機種の証明は求めない） */
  @Schema(description = "attestationの要求", example = "none")
  private String attestation;

  /**
   * PasskeyRegistrationOptionsModelからPasskeyRegistrationOptionsResponseを生成する
   *
   * @param model {@link PasskeyRegistrationOptionsModel}
   * @return {@link PasskeyRegistrationOptionsResponse}
   */
  public static PasskeyRegistrationOptionsResponse from(PasskeyRegistrationOptionsModel model) {
    return PasskeyRegistrationOptionsResponse.builder()
        .rp(new RelyingParty(model.getRpId().value(), model.getRpName().value()))
        .user(
            new User(
                model.getUserHandle().value(),
                model.getAccountId().value(),
                model.getAccountName().value()))
        .challenge(model.getChallenge().value())
        .pubKeyCredParams(
            Consts.PASSKEY_PUBLIC_KEY_ALGORITHMS.stream()
                .map(alg -> new PublicKeyCredentialParameter(PUBLIC_KEY, alg))
                .toList())
        .timeout(model.getTimeoutMillis().value())
        .excludeCredentials(
            model.getExcludeCredentialIds().stream()
                .map(id -> new CredentialDescriptor(id.value(), PUBLIC_KEY))
                .toList())
        // パスキー（discoverable credential）として作成させ、ログイン時にアカウントIDの入力を不要にする
        .authenticatorSelection(new AuthenticatorSelection(REQUIRED, true, REQUIRED))
        .attestation(ATTESTATION_NONE)
        .build();
  }

  /**
   * RP（サービス）情報
   *
   * @param id RP ID
   * @param name RP名
   */
  @Schema(description = "RP（サービス）情報")
  public record RelyingParty(String id, String name) {}

  /**
   * ユーザー情報
   *
   * @param id ユーザーハンドル（base64url）
   * @param name アカウントID
   * @param displayName アカウント名
   */
  @Schema(description = "ユーザー情報")
  public record User(String id, String name, String displayName) {}

  /**
   * 公開鍵の署名アルゴリズム
   *
   * @param type クレデンシャルの種類（public-key）
   * @param alg COSEアルゴリズム識別子
   */
  @Schema(description = "公開鍵の署名アルゴリズム")
  public record PublicKeyCredentialParameter(String type, Long alg) {}

  /**
   * クレデンシャルの識別子
   *
   * @param id クレデンシャルID（base64url）
   * @param type クレデンシャルの種類（public-key）
   */
  @Schema(description = "クレデンシャルの識別子")
  public record CredentialDescriptor(String id, String type) {}

  /**
   * 認証器の選択条件
   *
   * @param residentKey discoverable credentialの要求
   * @param requireResidentKey discoverable credentialを必須とするか（WebAuthn Level 1互換）
   * @param userVerification ユーザー検証の要求
   */
  @Schema(description = "認証器の選択条件")
  public record AuthenticatorSelection(
      String residentKey, Boolean requireResidentKey, String userVerification) {}
}
