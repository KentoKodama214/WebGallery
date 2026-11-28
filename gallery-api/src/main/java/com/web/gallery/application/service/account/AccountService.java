package com.web.gallery.application.service.account;

import com.web.gallery.application.model.account.AccountListGetModel;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.account.AccountPageModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;

/** アカウントに関するビジネスロジックを行うServiceクラス */
public interface AccountService {
  /**
   * アカウント新規登録の開始時に、最初のパスキーを作成するためのオプション（チャレンジ等）を発行する
   *
   * @param accountId 登録するアカウントID（WebAuthnのuser.name）
   * @param accountName 登録するアカウント名（WebAuthnのuser.displayName）
   * @return {@link PasskeyRegistrationOptionsModel}
   * @throws GalleryException アカウントIDが既に使われている場合
   */
  PasskeyRegistrationOptionsModel createSignupOptions(AccountId accountId, AccountName accountName)
      throws GalleryException;

  /**
   * パスキーの登録レスポンスを検証し、アカウントと最初のパスキーを新規登録する
   *
   * <p>あわせてリカバリーコードを発行する。平文のコードは戻り値でのみ返し、DBにはハッシュのみ保存する
   *
   * @param accountModel {@link AccountModel}
   * @param registrationResponseJson 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param passkeyName パスキー名
   * @return 発行したリカバリーコード
   * @throws GalleryException 登録レスポンスの検証に失敗した場合、またはアカウントIDが既に使われている場合
   */
  RecoveryCodeList registAccount(
      AccountModel accountModel,
      PasskeyResponseJson registrationResponseJson,
      PasskeyName passkeyName)
      throws GalleryException;

  /**
   * アカウントを更新する
   *
   * @param accountModel {@link AccountModel}
   * @return アカウントIDが重複しており更新をスキップした場合、true
   * @throws GalleryException 更新に失敗した場合
   */
  Boolean updateAccount(AccountModel accountModel) throws GalleryException;

  /**
   * アカウントIDからアカウント情報を取得する
   *
   * @param accountId アカウントID
   * @return {@link AccountModel}
   */
  AccountModel getAccountById(AccountId accountId);

  /**
   * アカウントの一覧を、ページング情報に従い取得する
   *
   * @param accountListGetModel {@link AccountListGetModel}
   * @return {@link AccountPageModel}
   */
  AccountPageModel getAccountList(AccountListGetModel accountListGetModel);

  /**
   * 管理者用：削除済みを含む全アカウントの一覧を、ページング情報に従い取得する
   *
   * @param accountListGetModel {@link AccountListGetModel}
   * @return {@link AccountPageModel}
   */
  AccountPageModel getAccountListForAdmin(AccountListGetModel accountListGetModel);

  /**
   * 管理者用：アカウントのロックを解除する（ログイン失敗回数を0にリセット）
   *
   * @param accountNo アカウント番号
   * @throws GalleryException 更新に失敗した場合
   */
  void unlockAccount(AccountNo accountNo) throws GalleryException;

  /**
   * 管理者用：アカウントを強制ロックする（ログイン失敗回数を上限超過に設定）
   *
   * @param accountNo アカウント番号
   * @throws GalleryException 更新に失敗した場合
   */
  void lockAccount(AccountNo accountNo) throws GalleryException;

  /**
   * 管理者用：アカウントの権限を変更する
   *
   * @param accountModel {@link AccountModel}（{@link AccountModel#forAuthorityChange}で生成したもの）
   * @throws GalleryException 更新に失敗した場合
   */
  void updateAccountAuthority(AccountModel accountModel) throws GalleryException;

  /**
   * アカウントを削除する
   *
   * <p>パスキーによる本人確認（再認証）を行う
   *
   * @param accountNo アカウント番号
   * @param accountId アカウントID
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス（{@link
   *     #createReauthenticationOptions}で発行したチャレンジへの署名）
   * @throws GalleryException 本人確認に失敗した場合
   */
  void deleteAccount(
      AccountNo accountNo, AccountId accountId, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException;

  /**
   * 重要操作（パスキーの登録・削除、退会、リカバリーコードの再発行）の前の本人確認に使う、認証オプション（チャレンジ等）を発行する
   *
   * @param accountNo アカウント番号
   * @return {@link PasskeyAuthenticationOptionsModel}（allowCredentialsは本人のパスキー）
   */
  PasskeyAuthenticationOptionsModel createReauthenticationOptions(AccountNo accountNo);

  /**
   * パスキー登録の開始時に、クライアントへ渡すオプション（チャレンジ等）を発行する
   *
   * @param accountNo アカウント番号
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @return {@link PasskeyRegistrationOptionsModel}
   * @throws GalleryException 本人確認に失敗した場合、またはパスキーの登録数が上限に達している場合
   */
  PasskeyRegistrationOptionsModel createPasskeyRegistrationOptions(
      AccountNo accountNo, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException;

  /**
   * パスキーの登録レスポンスを検証し、パスキーを登録する
   *
   * @param accountNo アカウント番号
   * @param registrationResponseJson 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param passkeyName パスキー名
   * @return 登録した{@link PasskeyCredentialModel}
   * @throws GalleryException 登録レスポンスの検証に失敗した場合、登録済みのパスキーの場合、または登録数が上限に達している場合
   */
  PasskeyCredentialModel registerPasskey(
      AccountNo accountNo, PasskeyResponseJson registrationResponseJson, PasskeyName passkeyName)
      throws GalleryException;

  /**
   * アカウントに登録されているパスキーを取得する
   *
   * @param accountNo アカウント番号
   * @return {@link PasskeyCredentialModelList}
   */
  PasskeyCredentialModelList getPasskeyList(AccountNo accountNo);

  /**
   * パスキー名を変更する
   *
   * @param accountNo アカウント番号
   * @param passkeyNo パスキー番号
   * @param passkeyName 変更後のパスキー名
   * @throws GalleryException 自身のパスキーとして存在しない場合
   */
  void renamePasskey(AccountNo accountNo, PasskeyNo passkeyNo, PasskeyName passkeyName)
      throws GalleryException;

  /**
   * パスキーを削除する
   *
   * @param accountNo アカウント番号
   * @param passkeyNo パスキー番号
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @throws GalleryException 本人確認に失敗した場合、自身のパスキーとして存在しない場合、または最後の1件の場合
   */
  void deletePasskey(
      AccountNo accountNo, PasskeyNo passkeyNo, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException;

  /**
   * 未使用のリカバリーコードの件数を取得する
   *
   * @param accountNo アカウント番号
   * @return 件数
   */
  Integer getRecoveryCodeCount(AccountNo accountNo);

  /**
   * リカバリーコードを再発行する（未使用のコードはすべて無効になる）
   *
   * @param accountNo アカウント番号
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @return 発行したリカバリーコード
   * @throws GalleryException 本人確認に失敗した場合
   */
  RecoveryCodeList regenerateRecoveryCodes(
      AccountNo accountNo, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException;
}
