package com.web.gallery.application.repository.auth;

import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;

/**
 * パスキー（WebAuthnクレデンシャル）データを永続化するRepositoryクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PasskeyCredentialRepository {
  /**
   * パスキーを登録する
   *
   * @param passkeyCredentialModel {@link PasskeyCredentialModel}
   */
  void save(PasskeyCredentialModel passkeyCredentialModel);

  /**
   * クレデンシャルIDに該当するパスキーを取得する
   *
   * @param credentialId クレデンシャルID
   * @return {@link PasskeyCredentialModel}、取得できない場合はnull
   */
  PasskeyCredentialModel findByCredentialId(CredentialId credentialId);

  /**
   * クレデンシャルIDに該当するパスキーを行ロック付きで取得する
   *
   * <p>同一パスキーによる同時認証を直列化し、署名カウンタの検証と更新の間の競合を防ぐために使用する
   *
   * @param credentialId クレデンシャルID
   * @return {@link PasskeyCredentialModel}、取得できない場合はnull
   */
  PasskeyCredentialModel findByCredentialIdForUpdate(CredentialId credentialId);

  /**
   * アカウント番号に該当するパスキーを登録日時の昇順で取得する
   *
   * @param accountNo アカウント番号
   * @return {@link PasskeyCredentialModelList}
   */
  PasskeyCredentialModelList findByAccountNo(AccountNo accountNo);

  /**
   * アカウント番号に該当するパスキーの件数を取得する
   *
   * @param accountNo アカウント番号
   * @return 件数
   */
  Integer countByAccountNo(AccountNo accountNo);

  /**
   * 認証に成功した結果（署名カウンタ・バックアップ状態・最終使用日時）を反映する
   *
   * @param passkeyCredentialModel 認証結果を反映した{@link PasskeyCredentialModel}
   */
  void updateAuthenticationResult(PasskeyCredentialModel passkeyCredentialModel);

  /**
   * パスキー名を変更する
   *
   * @param accountNo アカウント番号（他人のパスキーを変更できないよう条件に含める）
   * @param passkeyNo パスキー番号
   * @param passkeyName 変更後のパスキー名
   * @return 更新件数
   */
  Integer rename(AccountNo accountNo, PasskeyNo passkeyNo, PasskeyName passkeyName);

  /**
   * パスキーを削除する
   *
   * @param accountNo アカウント番号（他人のパスキーを削除できないよう条件に含める）
   * @param passkeyNo パスキー番号
   * @return 削除件数
   */
  Integer delete(AccountNo accountNo, PasskeyNo passkeyNo);
}
