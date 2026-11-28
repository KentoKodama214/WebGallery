package com.web.gallery.testsupport;

import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.AuthenticatorAssertionResponse;
import com.webauthn4j.data.AuthenticatorAttestationResponse;
import com.webauthn4j.data.AuthenticatorSelectionCriteria;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.PublicKeyCredential;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.data.PublicKeyCredentialRpEntity;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.PublicKeyCredentialUserEntity;
import com.webauthn4j.data.ResidentKeyRequirement;
import com.webauthn4j.data.UserVerificationRequirement;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.client.AuthenticationExtensionClientOutput;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import com.webauthn4j.test.authenticator.webauthn.NoneAttestationAuthenticator;
import com.webauthn4j.test.authenticator.webauthn.WebAuthnAuthenticatorAdaptor;
import com.webauthn4j.test.client.ClientPlatform;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * テスト用の擬似パスキー認証器
 *
 * <p>webauthn4j-testのエミュレータで登録・認証レスポンスを生成し、ブラウザの{@code PublicKeyCredential#toJSON()}と
 * 同じ形式のJSON文字列にして返す。1インスタンスが1台の認証器（内部に作成したクレデンシャルを保持する）に相当する
 */
public class PasskeyTestAuthenticator {

  /** テストプロファイルのRP ID（application-test.ymlの app.webauthn.rp-id） */
  public static final String RP_ID = "localhost";

  /** テストプロファイルで許可されているorigin（application-test.ymlの app.webauthn.allowed-origins） */
  public static final String ORIGIN = "http://localhost:3000";

  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private final NoneAttestationAuthenticator authenticator = new NoneAttestationAuthenticator();
  private final ClientPlatform clientPlatform;

  /** テストプロファイルで許可されているoriginで動作する認証器を生成する */
  public PasskeyTestAuthenticator() {
    this(ORIGIN);
  }

  /**
   * 指定したoriginで動作する認証器を生成する（origin不一致の検証用）
   *
   * @param origin ブラウザが付与するorigin
   */
  public PasskeyTestAuthenticator(String origin) {
    this.clientPlatform =
        new ClientPlatform(new Origin(origin), new WebAuthnAuthenticatorAdaptor(authenticator));
  }

  /**
   * 署名カウンタを認証のたびに増やすかどうかを設定する（同期パスキーのように常に0を返す認証器の再現用）
   *
   * @param enabled 増やす場合true
   */
  public void setCountUpEnabled(boolean enabled) {
    authenticator.setCountUpEnabled(enabled);
  }

  /**
   * パスキーを作成し、登録レスポンスのJSONを返す
   *
   * @param rpId RP ID
   * @param challenge サーバが発行したチャレンジ（base64url）
   * @param userHandle ユーザーハンドル（base64url）
   * @param accountId アカウントID
   * @return 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   */
  public String createRegistrationResponseJson(
      String rpId, String challenge, String userHandle, String accountId) {
    PublicKeyCredentialCreationOptions options =
        new PublicKeyCredentialCreationOptions(
            new PublicKeyCredentialRpEntity(rpId, "WebGallery"),
            new PublicKeyCredentialUserEntity(DECODER.decode(userHandle), accountId, accountId),
            new DefaultChallenge(DECODER.decode(challenge)),
            List.of(
                new PublicKeyCredentialParameters(
                    PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256)),
            300_000L,
            List.of(),
            new AuthenticatorSelectionCriteria(
                null, true, ResidentKeyRequirement.REQUIRED, UserVerificationRequirement.REQUIRED),
            null,
            AttestationConveyancePreference.NONE,
            null);
    PublicKeyCredential<AuthenticatorAttestationResponse, RegistrationExtensionClientOutput>
        credential = clientPlatform.create(options);
    AuthenticatorAttestationResponse response = credential.getResponse();
    String transports =
        response.getTransports().stream()
            .map(AuthenticatorTransport::getValue)
            .map(value -> "\"" + value + "\"")
            .collect(Collectors.joining(","));
    String id = ENCODER.encodeToString(credential.getRawId());
    return "{"
        + "\"id\":\""
        + id
        + "\","
        + "\"rawId\":\""
        + id
        + "\","
        + "\"type\":\"public-key\","
        + "\"response\":{"
        + "\"clientDataJSON\":\""
        + ENCODER.encodeToString(response.getClientDataJSON())
        + "\","
        + "\"attestationObject\":\""
        + ENCODER.encodeToString(response.getAttestationObject())
        + "\","
        + "\"transports\":["
        + transports
        + "]"
        + "},"
        + "\"clientExtensionResults\":{}"
        + "}";
  }

  /**
   * 保持しているパスキーで署名し、認証レスポンスのJSONを返す
   *
   * @param rpId RP ID
   * @param challenge サーバが発行したチャレンジ（base64url）
   * @return 認証レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   */
  public String createAuthenticationResponseJson(String rpId, String challenge) {
    PublicKeyCredentialRequestOptions options =
        new PublicKeyCredentialRequestOptions(
            new DefaultChallenge(DECODER.decode(challenge)),
            300_000L,
            rpId,
            List.of(),
            UserVerificationRequirement.REQUIRED,
            null);
    PublicKeyCredential<AuthenticatorAssertionResponse, AuthenticationExtensionClientOutput>
        credential = clientPlatform.get(options);
    AuthenticatorAssertionResponse response = credential.getResponse();
    String id = ENCODER.encodeToString(credential.getRawId());
    String userHandle =
        response.getUserHandle() != null
            ? "\"userHandle\":\"" + ENCODER.encodeToString(response.getUserHandle()) + "\","
            : "";
    return "{"
        + "\"id\":\""
        + id
        + "\","
        + "\"rawId\":\""
        + id
        + "\","
        + "\"type\":\"public-key\","
        + "\"response\":{"
        + "\"clientDataJSON\":\""
        + ENCODER.encodeToString(response.getClientDataJSON())
        + "\","
        + "\"authenticatorData\":\""
        + ENCODER.encodeToString(response.getAuthenticatorData())
        + "\","
        + userHandle
        + "\"signature\":\""
        + ENCODER.encodeToString(response.getSignature())
        + "\""
        + "},"
        + "\"clientExtensionResults\":{}"
        + "}";
  }
}
