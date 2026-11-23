package com.web.gallery.application.repository.auth;

import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.domain.model.auth.PasskeyChallenge;

/**
 * WebAuthnチャレンジを永続化するRepositoryクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface WebAuthnChallengeRepository {
  /**
   * チャレンジを登録する
   *
   * @param webAuthnChallengeModel {@link WebAuthnChallengeModel}
   */
  void save(WebAuthnChallengeModel webAuthnChallengeModel);

  /**
   * 有効期限内のパスキー登録用チャレンジを取得し、同時に削除する（1度だけ使用できる）
   *
   * <p>削除と取得を1文で行うため、同じチャレンジを並行して2回使用することはできない
   *
   * @param challenge チャレンジ
   * @return {@link WebAuthnChallengeModel}、該当しない（未発行・使用済み・期限切れ・用途違い）場合はnull
   */
  WebAuthnChallengeModel consumeForRegistration(PasskeyChallenge challenge);

  /**
   * 有効期限内のパスキー認証用チャレンジを取得し、同時に削除する（1度だけ使用できる）
   *
   * <p>削除と取得を1文で行うため、同じチャレンジを並行して2回使用することはできない
   *
   * @param challenge チャレンジ
   * @return {@link WebAuthnChallengeModel}、該当しない（未発行・使用済み・期限切れ・用途違い）場合はnull
   */
  WebAuthnChallengeModel consumeForAuthentication(PasskeyChallenge challenge);

  /** 有効期限切れのチャレンジを削除する */
  void deleteExpired();
}
