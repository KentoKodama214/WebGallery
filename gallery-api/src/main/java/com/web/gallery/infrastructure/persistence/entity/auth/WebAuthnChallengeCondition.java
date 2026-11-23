package com.web.gallery.infrastructure.persistence.entity.auth;

import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import lombok.Builder;
import lombok.Data;

/** WebAuthnチャレンジテーブルの抽出条件クラス */
@Data
@Builder
public class WebAuthnChallengeCondition {
  /** チャレンジ */
  private String challenge;

  /** 用途区分 */
  private WebAuthnChallengePurposeEnum purposeKbn;

  /** アカウント番号 */
  private Long accountNo;

  /**
   * チャレンジ・用途区分で消費（取得と同時に削除）用の抽出条件を生成する
   *
   * @param challenge チャレンジ
   * @param purposeKbn 用途区分
   * @return {@link WebAuthnChallengeCondition}
   */
  public static WebAuthnChallengeCondition byChallengeAndPurpose(
      String challenge, WebAuthnChallengePurposeEnum purposeKbn) {
    return WebAuthnChallengeCondition.builder().challenge(challenge).purposeKbn(purposeKbn).build();
  }

  /**
   * アカウント番号で削除用の抽出条件を生成する（退会時に登録用チャレンジの残骸を消す）
   *
   * @param accountNo アカウント番号
   * @return {@link WebAuthnChallengeCondition}
   */
  public static WebAuthnChallengeCondition byAccountNo(Long accountNo) {
    return WebAuthnChallengeCondition.builder().accountNo(accountNo).build();
  }
}
