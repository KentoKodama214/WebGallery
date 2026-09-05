package com.web.gallery.aggregate;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.PhotoNoList;

/** アカウントと、それに紐づく写真・お気に入り・タグ・リフレッシュトークンのライフサイクルを管理する集約ルートクラス */
public class Account {

  /** アカウント番号 */
  private final AccountNo accountNo;

  /** 削除フラグ */
  private boolean deleted;

  /** 削除処理の結果、削除された写真番号一覧（削除時点で未削除だった写真のみ） */
  private PhotoNoList deletedPhotoNoList;

  private Account(AccountNo accountNo) {
    this.accountNo = accountNo;
  }

  /**
   * 削除用のAccountを生成する
   *
   * @param accountNo アカウント番号
   * @return {@link Account}
   */
  public static Account forDelete(AccountNo accountNo) {
    Account account = new Account(accountNo);
    account.markAsDeleted();
    return account;
  }

  /** 削除済みとしてマークする */
  public void markAsDeleted() {
    this.deleted = true;
  }

  /**
   * 削除処理の結果、削除された写真番号一覧を記録する
   *
   * @param deletedPhotoNoList {@link PhotoNoList}
   */
  public void recordDeletedPhotoNos(PhotoNoList deletedPhotoNoList) {
    this.deletedPhotoNoList = deletedPhotoNoList;
  }

  /**
   * アカウント番号を取得する
   *
   * @return アカウント番号
   */
  public AccountNo getAccountNo() {
    return accountNo;
  }

  /**
   * 削除済みかどうかを取得する
   *
   * @return 削除済みの場合、true
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * 削除処理の結果、削除された写真番号一覧を取得する
   *
   * <p>{@link #forDelete}によるリポジトリでの削除実行後にのみ値が設定される
   *
   * @return {@link PhotoNoList}
   */
  public PhotoNoList getDeletedPhotoNoList() {
    return deletedPhotoNoList;
  }
}
