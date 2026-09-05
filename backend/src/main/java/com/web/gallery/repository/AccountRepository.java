package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.AccountGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountPageModel;

/** アカウントデータを永続化するRepositoryクラス */
public interface AccountRepository {
  /**
   * Accountテーブルで該当するレコードを取得する
   *
   * @param accountNo アカウント番号
   * @return AccountModel {@link AccountModel}
   *     <p>取得できない場合はnullを返す
   */
  AccountModel getByAccountNo(AccountNo accountNo);

  /**
   * Accountテーブルで該当するレコードを取得する
   *
   * @param accountId アカウントId
   * @return AccountModel {@link AccountModel}
   *     <p>取得できない場合はnullを返す
   */
  AccountModel getByAccountId(AccountId accountId);

  /**
   * Accountテーブルへ登録する
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException 登録に失敗した場合
   */
  void regist(AccountModel accountModel) throws GalleryException;

  /**
   * Accountテーブルで該当するレコードを更新する
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException 更新に失敗した場合
   */
  void update(AccountModel accountModel) throws GalleryException;

  /**
   * Accountテーブルのログイン失敗回数を更新する
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException 更新に失敗した場合
   */
  void updateLoginFailureCount(AccountModel accountModel) throws GalleryException;

  /**
   * Accountテーブルのログイン失敗回数をSQL側で原子的にインクリメントする
   *
   * @param accountNo アカウント番号
   * @throws GalleryException 更新に失敗した場合
   */
  void incrementLoginFailureCount(AccountNo accountNo) throws GalleryException;

  /**
   * アカウントIDに該当するアカウントの存在有無をチェックする（新規登録用、除外なし）
   *
   * @param accountId アカウントID
   * @return true：存在する
   */
  Boolean isExistAccount(AccountId accountId);

  /**
   * アカウントIDに該当するアカウントの存在有無をチェックする（更新用、自分自身を除外）
   *
   * @param accountNo 検索対象外のアカウント番号
   * @param accountId アカウントID
   * @return true：存在する
   */
  Boolean isExistAccount(AccountNo accountNo, AccountId accountId);

  /**
   * アカウントの一覧を、ページング情報に従い取得する
   *
   * @param accountGetModel {@link AccountGetModel}
   * @return {@link AccountPageModel}
   */
  AccountPageModel getAccountList(AccountGetModel accountGetModel);

  /**
   * 管理者用：削除済みを含む全アカウントの一覧を、ページング情報に従い取得する
   *
   * @param accountGetModel {@link AccountGetModel}
   * @return {@link AccountPageModel}
   */
  AccountPageModel getAccountListForAdmin(AccountGetModel accountGetModel);

  /**
   * Accountテーブルから該当するレコードを物理削除する
   *
   * @param accountNo アカウント番号
   */
  void delete(AccountNo accountNo);

  /**
   * アカウントの行ロックを取得する（排他制御用）
   *
   * <p>写真番号の採番・登録枚数上限チェックなど、アカウント単位で処理を直列化したい場合に使用する
   *
   * @param accountNo アカウント番号
   */
  void lockForUpdate(AccountNo accountNo);

  /**
   * ログイン試行を同一アカウントIDで直列化するためのトランザクションレベルのアドバイザリロックを取得する
   *
   * <p>トランザクション終了時に自動解放される。ロックアウト判定と失敗回数加算の間の競合による ログイン失敗回数上限のバイパスを防ぐために使用する。行ロックではないため、
   * 別トランザクションでの失敗回数加算（UPDATE）をブロックしない
   *
   * @param accountId アカウントID
   */
  void lockForLoginAttempt(AccountId accountId);
}
