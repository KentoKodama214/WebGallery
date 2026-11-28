package com.web.gallery.infrastructure.persistence.mapper.auth;

import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallenge;
import com.web.gallery.infrastructure.persistence.entity.auth.WebAuthnChallengeCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * WebAuthnチャレンジテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface WebAuthnChallengeMapper {
  /**
   * チャレンジを登録する
   *
   * @param webAuthnChallenge {@link WebAuthnChallenge}
   * @return 登録件数
   */
  public Integer insert(WebAuthnChallenge webAuthnChallenge);

  /**
   * 有効期限内かつ抽出条件（チャレンジ・用途区分）に該当するチャレンジを削除し、削除した行を返す
   *
   * <p>{@code DELETE ... RETURNING}の1文で取得と削除を行うため、同じチャレンジを並行して2回消費することはできない
   *
   * @param condition {@link WebAuthnChallengeCondition}
   * @return 削除した{@link WebAuthnChallenge}のリスト（該当なしの場合は空）
   */
  public List<WebAuthnChallenge> consume(WebAuthnChallengeCondition condition);

  /**
   * 抽出条件に該当するチャレンジを削除する
   *
   * @param condition {@link WebAuthnChallengeCondition}
   * @return 削除件数
   */
  public Integer delete(WebAuthnChallengeCondition condition);

  /**
   * 有効期限切れのチャレンジを削除する
   *
   * @return 削除件数
   */
  public Integer deleteExpired();
}
