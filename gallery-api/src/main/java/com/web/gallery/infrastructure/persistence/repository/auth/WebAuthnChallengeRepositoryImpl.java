package com.web.gallery.infrastructure.persistence.repository.auth;

import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.application.repository.auth.WebAuthnChallengeRepository;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallenge;
import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallengeCondition;
import com.web.gallery.infrastructure.persistence.mapper.auth.WebAuthnChallengeMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * WebAuthnチャレンジを永続化するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class WebAuthnChallengeRepositoryImpl implements WebAuthnChallengeRepository {
  private final WebAuthnChallengeMapper webAuthnChallengeMapper;

  @Override
  public void save(WebAuthnChallengeModel webAuthnChallengeModel) {
    webAuthnChallengeMapper.insert(WebAuthnChallenge.from(webAuthnChallengeModel));
  }

  @Override
  public WebAuthnChallengeModel consumeForRegistration(PasskeyChallenge challenge) {
    return consume(challenge, WebAuthnChallengePurposeEnum.REGISTRATION);
  }

  @Override
  public WebAuthnChallengeModel consumeForAuthentication(PasskeyChallenge challenge) {
    return consume(challenge, WebAuthnChallengePurposeEnum.AUTHENTICATION);
  }

  /**
   * 有効期限内かつ用途が一致するチャレンジを取得し、同時に削除する
   *
   * @param challenge チャレンジ
   * @param purposeKbn 用途区分
   * @return {@link WebAuthnChallengeModel}、該当しない場合はnull
   */
  private WebAuthnChallengeModel consume(
      PasskeyChallenge challenge, WebAuthnChallengePurposeEnum purposeKbn) {
    List<WebAuthnChallenge> consumed =
        webAuthnChallengeMapper.consume(
            WebAuthnChallengeCondition.byChallengeAndPurpose(challenge.value(), purposeKbn));
    return consumed.isEmpty() ? null : toWebAuthnChallengeModel(consumed.getFirst());
  }

  @Override
  public void deleteExpired() {
    webAuthnChallengeMapper.deleteExpired();
  }

  /**
   * WebAuthnChallengeエンティティからWebAuthnChallengeModelを組み立てる
   *
   * @param entity {@link WebAuthnChallenge}
   * @return {@link WebAuthnChallengeModel}
   */
  private WebAuthnChallengeModel toWebAuthnChallengeModel(WebAuthnChallenge entity) {
    return WebAuthnChallengeModel.of(
        new PasskeyChallenge(entity.getChallenge()),
        entity.getPurposeKbn(),
        entity.getAccountNo() == Consts.ACCOUNT_NO_UNKNOWN
            ? null
            : new AccountNo(entity.getAccountNo()),
        new ExpiresAt(entity.getExpiresAt()));
  }
}
