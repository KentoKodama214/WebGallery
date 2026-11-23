package com.web.gallery.infrastructure.persistence.entity.auth;

import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * WebAuthnチャレンジテーブルのEntityクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class WebAuthnChallenge {
  /** チャレンジNo */
  private Long challengeNo;

  /** チャレンジ */
  private String challenge;

  /** 用途区分 */
  private WebAuthnChallengePurposeEnum purposeKbn;

  /** アカウント番号 */
  private Long accountNo;

  /** 有効期限 */
  private OffsetDateTime expiresAt;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /**
   * WebAuthnChallengeModelからWebAuthnChallengeエンティティを生成する
   *
   * @param model {@link WebAuthnChallengeModel}
   * @return {@link WebAuthnChallenge}
   */
  public static WebAuthnChallenge from(WebAuthnChallengeModel model) {
    return WebAuthnChallenge.builder()
        .challenge(model.getChallenge().value())
        .purposeKbn(model.getPurposeKbn())
        .accountNo(accountNoOf(model))
        .expiresAt(model.getExpiresAt().value())
        .createdBy(accountNoOf(model))
        .build();
  }

  /**
   * DBに保存するアカウント番号を返す（認証用でアカウントが未確定の場合は{@link Consts#ACCOUNT_NO_UNKNOWN}）
   *
   * @param model {@link WebAuthnChallengeModel}
   * @return アカウント番号
   */
  private static Long accountNoOf(WebAuthnChallengeModel model) {
    return model.getAccountNo() != null ? model.getAccountNo().value() : Consts.ACCOUNT_NO_UNKNOWN;
  }
}
