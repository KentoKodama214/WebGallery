package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.LoginHistoryModel;

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
