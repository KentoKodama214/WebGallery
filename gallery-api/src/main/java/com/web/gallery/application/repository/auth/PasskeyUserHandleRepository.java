package com.web.gallery.application.repository.auth;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;

/**
 * パスキーのユーザーハンドル（WebAuthnのuser.id）を永続化するRepositoryクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PasskeyUserHandleRepository {
  /**
   * アカウント番号に該当するユーザーハンドルを取得する
   *
   * @param accountNo アカウント番号
   * @return {@link PasskeyUserHandle}、未発行の場合はnull
   */
  PasskeyUserHandle findByAccountNo(AccountNo accountNo);

  /**
   * ユーザーハンドルを登録する
   *
   * <p>同一アカウントへ同時に発行された場合は先に登録されたものを残し、後発の登録は何もしない
   *
   * @param accountNo アカウント番号
   * @param userHandle ユーザーハンドル
   */
  void saveIfAbsent(AccountNo accountNo, PasskeyUserHandle userHandle);
}
