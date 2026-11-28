package com.web.gallery.application.helper;

import com.web.gallery.application.model.auth.PasskeyAssertionModel;
import com.web.gallery.application.model.auth.PasskeyAssertionResultModel;
import com.web.gallery.application.model.auth.PasskeyAttestationModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;

/**
 * パスキー（WebAuthn）の登録・認証レスポンスを検証する技術詳細のポート
 *
 * <p>RP ID・許可するorigin・ユーザー検証（UV）必須の判定は実装側が{@code app.webauthn.*}の設定に基づいて行う。
 * 検証に失敗した場合はいずれのメソッドも{@link PasskeyVerificationException}を投げる
 */
public interface WebAuthnVerifier {

  /**
   * WebAuthnチャレンジ（暗号論的乱数）を生成する
   *
   * @return {@link PasskeyChallenge}
   */
  PasskeyChallenge generateChallenge();

  /**
   * パスキーのユーザーハンドル（WebAuthnのuser.id。暗号論的乱数）を生成する
   *
   * @return {@link PasskeyUserHandle}
   */
  PasskeyUserHandle generateUserHandle();

  /**
   * 登録レスポンスのclientDataJSONからチャレンジを読み取る（署名・内容の検証は行わない）
   *
   * @param registrationResponseJson 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @return {@link PasskeyChallenge}
   * @throws PasskeyVerificationException JSONとして解釈できない場合
   */
  PasskeyChallenge readRegistrationChallenge(PasskeyResponseJson registrationResponseJson);

  /**
   * 登録レスポンスを検証し、保存すべきクレデンシャル情報を取り出す
   *
   * @param registrationResponseJson 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param challenge サーバが発行したチャレンジ
   * @return {@link PasskeyAttestationModel}
   * @throws PasskeyVerificationException 検証に失敗した場合
   */
  PasskeyAttestationModel verifyRegistration(
      PasskeyResponseJson registrationResponseJson, PasskeyChallenge challenge);

  /**
   * 認証レスポンスからクレデンシャルID・チャレンジ・ユーザーハンドルを読み取る（署名の検証は行わない）
   *
   * @param authenticationResponseJson 認証レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @return {@link PasskeyAssertionModel}
   * @throws PasskeyVerificationException JSONとして解釈できない場合
   */
  PasskeyAssertionModel readAuthentication(PasskeyResponseJson authenticationResponseJson);

  /**
   * 認証レスポンスを、保存済みのクレデンシャル（公開鍵・署名カウンタ）とサーバが発行したチャレンジで検証する
   *
   * <p>署名カウンタの巻き戻り（クローンされた認証器の疑い）も検証失敗として扱う
   *
   * @param authenticationResponseJson 認証レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param challenge サーバが発行したチャレンジ
   * @param credential 保存済みのクレデンシャル
   * @return {@link PasskeyAssertionResultModel}
   * @throws PasskeyVerificationException 検証に失敗した場合
   */
  PasskeyAssertionResultModel verifyAuthentication(
      PasskeyResponseJson authenticationResponseJson,
      PasskeyChallenge challenge,
      PasskeyCredentialModel credential);
}
