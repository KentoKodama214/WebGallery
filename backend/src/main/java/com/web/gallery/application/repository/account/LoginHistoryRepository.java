package com.web.gallery.application.repository.account;

import com.web.gallery.application.model.account.LoginHistoryModel;
import com.web.gallery.domain.model.account.AccountNo;

/**
 * ログイン履歴データを永続化するRepositoryクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface LoginHistoryRepository {
  /**
   * ログイン履歴を保存する
   *
   * @param loginHistoryModel {@link LoginHistoryModel}
   */
  void save(LoginHistoryModel loginHistoryModel);

  /**
   * アカウント番号に該当するログイン履歴を削除する
   *
   * @param accountNo アカウント番号
   */
  void deleteByAccountNo(AccountNo accountNo);
}
