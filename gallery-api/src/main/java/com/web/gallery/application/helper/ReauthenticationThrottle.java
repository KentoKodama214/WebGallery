package com.web.gallery.application.helper;

/**
 * 現在のパスワードによる本人確認（再認証）の失敗回数を、アカウント単位で数えるインターフェース
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ReauthenticationThrottle {

  /**
   * 当該アカウントが再認証のロックアウト中かどうかを判定する
   *
   * @param accountNo アカウント番号
   * @return ロックアウト中の場合true
   */
  boolean isLockedOut(Long accountNo);

  /**
   * 再認証の失敗を1回記録する
   *
   * @param accountNo アカウント番号
   */
  void recordFailure(Long accountNo);

  /**
   * 当該アカウントの失敗記録を破棄する（再認証成功時に呼ぶ）
   *
   * @param accountNo アカウント番号
   */
  void reset(Long accountNo);
}
