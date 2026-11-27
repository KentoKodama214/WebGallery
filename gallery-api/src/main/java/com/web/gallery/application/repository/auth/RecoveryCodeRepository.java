package com.web.gallery.application.repository.auth;

import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.RecoveryCode;

/**
 * リカバリーコードを永続化するRepositoryクラス
 *
 * <p>コードの平文は保存せず、{@link RecoveryCode#toHash()}で求めたハッシュのみを保存・照合する
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface RecoveryCodeRepository {
  /**
   * リカバリーコードをまとめて登録する
   *
   * @param accountNo アカウント番号
   * @param recoveryCodeList 登録するリカバリーコード（平文。ハッシュ化して保存する）
   */
  void saveAll(AccountNo accountNo, RecoveryCodeList recoveryCodeList);

  /**
   * アカウントのリカバリーコードをすべて削除する
   *
   * @param accountNo アカウント番号
   */
  void deleteByAccountNo(AccountNo accountNo);

  /**
   * アカウントのリカバリーコードと照合し、一致すればそのコードを削除する（1度だけ使用できる）
   *
   * <p>照合と削除を1文で行うため、同じコードを並行して2回使用することはできない
   *
   * @param accountNo アカウント番号
   * @param recoveryCode 入力されたリカバリーコード
   * @return 一致して削除した場合true
   */
  Boolean consume(AccountNo accountNo, RecoveryCode recoveryCode);

  /**
   * アカウントの未使用のリカバリーコードの件数を取得する
   *
   * @param accountNo アカウント番号
   * @return 件数
   */
  Integer countByAccountNo(AccountNo accountNo);
}
