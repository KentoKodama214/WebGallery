package com.web.gallery.infrastructure.security;

import com.web.gallery.application.config.WebAuthnConfig;
import com.web.gallery.application.helper.WebAuthnVerifier;
import com.web.gallery.application.model.auth.PasskeyAssertionModel;
import com.web.gallery.application.model.auth.PasskeyAssertionResultModel;
import com.web.gallery.application.model.auth.PasskeyAttestationModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.SignCount;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * webauthn4jを用いてパスキー（WebAuthn）の登録・認証レスポンスを検証する{@link WebAuthnVerifier}の実装クラス
 *
 * <p>ユーザー検証（UV。生体認証・PIN）とユーザー存在確認（UP）を必須とし、originは{@code app.webauthn.allowed-origins}、RP IDは{@code
 * app.webauthn.rp-id}に一致するものだけを受け付ける。 attestationは要求しない（{@code attestation:
 * "none"}）ため、認証器の機種の真正性は検証しない。 署名カウンタの巻き戻り（同期パスキー以外で、保存値以下の値が返された場合）はwebauthn4jの既定の挙動で検証失敗となる。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class WebAuthnVerifierImpl implements WebAuthnVerifier {

  /** 受け付ける公開鍵の署名アルゴリズム（{@link Consts#PASSKEY_PUBLIC_KEY_ALGORITHMS}、優先順） */
  private static final List<PublicKeyCredentialParameters> PUB_KEY_CRED_PARAMS =
      Consts.PASSKEY_PUBLIC_KEY_ALGORITHMS.stream()
          .map(alg ->
              new PublicKeyCredentialParameters(
                  PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.create(alg)))
          .toList();

  private static final String TRANSPORT_DELIMITER = ",";

  private static final Base64.Encoder BASE64URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder BASE64URL_DECODER = Base64.getUrlDecoder();

  private final WebAuthnConfig webAuthnConfig;
  private final SecureRandom secureRandom = new SecureRandom();
  private final WebAuthnManager webAuthnManager;
  private final AttestedCredentialDataConverter attestedCredentialDataConverter;

  /**
   * コンストラクタ
   *
   * @param webAuthnConfig パスキーに関する設定値
   */
  public WebAuthnVerifierImpl(WebAuthnConfig webAuthnConfig) {
    this.webAuthnConfig = webAuthnConfig;
    ObjectConverter objectConverter = new ObjectConverter();
    // attestationを要求しない（"none"）ため、attestation statementの信頼性検証を行わないNonStrictを使う。
    // 署名・origin・RP ID・チャレンジ・UV/UPフラグ・署名カウンタの検証はNonStrictでも行われる
    this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager(objectConverter);
    this.attestedCredentialDataConverter = new AttestedCredentialDataConverter(objectConverter);
  }

  @Override
  public PasskeyChallenge generateChallenge() {
    return new PasskeyChallenge(randomBase64Url(Consts.WEBAUTHN_CHALLENGE_BYTES));
  }

  @Override
  public PasskeyUserHandle generateUserHandle() {
    return new PasskeyUserHandle(randomBase64Url(Consts.PASSKEY_USER_HANDLE_BYTES));
  }

  @Override
  public PasskeyChallenge readRegistrationChallenge(PasskeyResponseJson registrationResponseJson) {
    RegistrationData registrationData = parseRegistration(registrationResponseJson);
    return challengeOf(registrationData.getCollectedClientData());
  }

  @Override
  public PasskeyAttestationModel verifyRegistration(
      PasskeyResponseJson registrationResponseJson, PasskeyChallenge challenge) {
    RegistrationData registrationData = parseRegistration(registrationResponseJson);
    RegistrationParameters registrationParameters =
        new RegistrationParameters(serverProperty(challenge), PUB_KEY_CRED_PARAMS, true, true);
    try {
      webAuthnManager.verify(registrationData, registrationParameters);
    } catch (RuntimeException e) {
      // VerificationException（署名・origin・RP ID・チャレンジ・UV等の不一致）を含む
      throw new PasskeyVerificationException(
          "Passkey registration verification failed: " + e.getMessage(), e);
    }

    AuthenticatorData<?> authenticatorData =
        registrationData.getAttestationObject() != null
            ? registrationData.getAttestationObject().getAuthenticatorData()
            : null;
    if (authenticatorData == null || authenticatorData.getAttestedCredentialData() == null) {
      throw new PasskeyVerificationException(
          "Passkey registration response has no attested credential data");
    }
    com.webauthn4j.data.attestation.authenticator.AttestedCredentialData attestedCredentialData =
        authenticatorData.getAttestedCredentialData();

    return PasskeyAttestationModel.builder()
        .credentialId(new CredentialId(encode(attestedCredentialData.getCredentialId())))
        .attestedCredentialData(
            new AttestedCredentialData(
                attestedCredentialDataConverter.convert(attestedCredentialData)))
        .signCount(new SignCount(authenticatorData.getSignCount()))
        .transports(new PasskeyTransports(joinTransports(registrationData.getTransports())))
        .isBackupEligible(new IsBackupEligible(authenticatorData.isFlagBE()))
        .isBackedUp(new IsBackedUp(authenticatorData.isFlagBS()))
        .isUvInitialized(new IsUvInitialized(authenticatorData.isFlagUV()))
        .build();
  }

  @Override
  public PasskeyAssertionModel readAuthentication(PasskeyResponseJson authenticationResponseJson) {
    AuthenticationData authenticationData = parseAuthentication(authenticationResponseJson);
    if (authenticationData.getCredentialId() == null
        || authenticationData.getCredentialId().length == 0) {
      throw new PasskeyVerificationException(
          "Passkey authentication response has no credential id");
    }
    byte[] userHandle = authenticationData.getUserHandle();
    return PasskeyAssertionModel.builder()
        .credentialId(new CredentialId(encode(authenticationData.getCredentialId())))
        .challenge(challengeOf(authenticationData.getCollectedClientData()))
        .userHandle(
            userHandle != null && userHandle.length > 0
                ? new PasskeyUserHandle(encode(userHandle))
                : null)
        .build();
  }

  @Override
  public PasskeyAssertionResultModel verifyAuthentication(
      PasskeyResponseJson authenticationResponseJson,
      PasskeyChallenge challenge,
      PasskeyCredentialModel credential) {
    AuthenticationData authenticationData = parseAuthentication(authenticationResponseJson);
    // 検証の前提として、レスポンスのクレデンシャルIDが保存済みのものと一致することを確かめる
    // （呼び出し元はクレデンシャルIDで引き当てているが、取り違えを検証の段階でも防ぐ）
    if (!Arrays.equals(
        authenticationData.getCredentialId(), decode(credential.getCredentialId().value()))) {
      throw new PasskeyVerificationException("Passkey credential id mismatch");
    }

    AuthenticationParameters authenticationParameters =
        new AuthenticationParameters(
            serverProperty(challenge), toCredentialRecord(credential), null, true, true);
    try {
      webAuthnManager.verify(authenticationData, authenticationParameters);
    } catch (RuntimeException e) {
      // VerificationException（署名・origin・RP ID・チャレンジ・UV・署名カウンタの巻き戻り等）を含む
      throw new PasskeyVerificationException(
          "Passkey authentication verification failed: " + e.getMessage(), e);
    }

    AuthenticatorData<?> authenticatorData = authenticationData.getAuthenticatorData();
    if (authenticatorData == null) {
      throw new PasskeyVerificationException(
          "Passkey authentication response has no authenticator data");
    }
    return PasskeyAssertionResultModel.builder()
        .signCount(new SignCount(authenticatorData.getSignCount()))
        .isBackedUp(new IsBackedUp(authenticatorData.isFlagBS()))
        .build();
  }

  /**
   * 登録レスポンスのJSONを解釈する
   *
   * @param json 登録レスポンス
   * @return {@link RegistrationData}
   * @throws PasskeyVerificationException 解釈できない場合
   */
  private RegistrationData parseRegistration(PasskeyResponseJson json) {
    try {
      return webAuthnManager.parseRegistrationResponseJSON(json.value());
    } catch (RuntimeException e) {
      throw new PasskeyVerificationException(
          "Passkey registration response could not be parsed: " + e.getMessage(), e);
    }
  }

  /**
   * 認証レスポンスのJSONを解釈する
   *
   * @param json 認証レスポンス
   * @return {@link AuthenticationData}
   * @throws PasskeyVerificationException 解釈できない場合
   */
  private AuthenticationData parseAuthentication(PasskeyResponseJson json) {
    try {
      return webAuthnManager.parseAuthenticationResponseJSON(json.value());
    } catch (RuntimeException e) {
      throw new PasskeyVerificationException(
          "Passkey authentication response could not be parsed: " + e.getMessage(), e);
    }
  }

  /**
   * clientDataJSONからチャレンジを取り出す
   *
   * @param clientData {@link CollectedClientData}
   * @return {@link PasskeyChallenge}
   * @throws PasskeyVerificationException チャレンジが含まれていない場合
   */
  private PasskeyChallenge challengeOf(CollectedClientData clientData) {
    if (clientData == null
        || clientData.getChallenge() == null
        || clientData.getChallenge().getValue().length == 0) {
      throw new PasskeyVerificationException("Passkey response has no challenge");
    }
    return new PasskeyChallenge(encode(clientData.getChallenge().getValue()));
  }

  /**
   * 検証に使うサーバ側の期待値（origin・RP ID・チャレンジ）を組み立てる
   *
   * @param challenge サーバが発行したチャレンジ
   * @return {@link ServerProperty}
   */
  private ServerProperty serverProperty(PasskeyChallenge challenge) {
    Set<Origin> origins =
        webAuthnConfig.getAllowedOrigins().stream().map(Origin::create).collect(Collectors.toSet());
    // topOriginは指定しない（iframe等のクロスオリジンからの呼び出しは受け付けない）
    return ServerProperty.builder()
        .origins(origins)
        .rpId(webAuthnConfig.getRpId())
        .challenge(new DefaultChallenge(decode(challenge.value())))
        .build();
  }

  /**
   * 保存済みのパスキーからwebauthn4jの{@link CredentialRecord}を復元する
   *
   * @param credential 保存済みのパスキー
   * @return {@link CredentialRecord}
   */
  private CredentialRecord toCredentialRecord(PasskeyCredentialModel credential) {
    com.webauthn4j.data.attestation.authenticator.AttestedCredentialData attestedCredentialData;
    try {
      attestedCredentialData =
          attestedCredentialDataConverter.convert(credential.getAttestedCredentialData().value());
    } catch (RuntimeException e) {
      throw new PasskeyVerificationException(
          "Stored passkey credential data could not be restored. (passkeyNo: "
              + (credential.getPasskeyNo() != null ? credential.getPasskeyNo().value() : null)
              + ")",
          e);
    }
    return new CredentialRecordImpl(
        null,
        credential.getIsUvInitialized().value(),
        credential.getIsBackupEligible().value(),
        credential.getIsBackedUp().value(),
        credential.getSignCount().value(),
        attestedCredentialData,
        null,
        null,
        null,
        splitTransports(credential.getTransports().value()));
  }

  /**
   * 認証器の通信手段をカンマ区切りの文字列にする
   *
   * @param transports 通信手段（null可）
   * @return カンマ区切りの文字列（未取得の場合は空文字）
   */
  private String joinTransports(Set<AuthenticatorTransport> transports) {
    if (transports == null || transports.isEmpty()) {
      return Consts.STRING_EMPTY;
    }
    return transports.stream()
        .map(AuthenticatorTransport::getValue)
        .sorted()
        .collect(Collectors.joining(TRANSPORT_DELIMITER));
  }

  /**
   * カンマ区切りの文字列を認証器の通信手段に戻す
   *
   * @param transports カンマ区切りの文字列
   * @return 通信手段
   */
  private Set<AuthenticatorTransport> splitTransports(String transports) {
    if (transports == null || transports.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(transports.split(TRANSPORT_DELIMITER))
        .filter(value -> !value.isBlank())
        .map(AuthenticatorTransport::create)
        .collect(Collectors.toSet());
  }

  /**
   * 指定バイト数の暗号論的乱数をbase64urlで返す
   *
   * @param bytes バイト数
   * @return base64url文字列（パディングなし）
   */
  private String randomBase64Url(int bytes) {
    byte[] value = new byte[bytes];
    secureRandom.nextBytes(value);
    return encode(value);
  }

  private static String encode(byte[] value) {
    return BASE64URL_ENCODER.encodeToString(value);
  }

  private static byte[] decode(String value) {
    try {
      return BASE64URL_DECODER.decode(value);
    } catch (IllegalArgumentException e) {
      throw new PasskeyVerificationException("Invalid base64url value", e);
    }
  }
}
