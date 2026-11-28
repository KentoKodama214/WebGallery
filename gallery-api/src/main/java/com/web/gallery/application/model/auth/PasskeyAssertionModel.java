package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 署名検証前のパスキー認証レスポンス（assertion）から読み取った値を受け渡すためのModelクラス
 *
 * <p>検証に使う保存済みクレデンシャル・チャレンジを引き当てるためだけに使う。 ここに含まれる値は未検証のため、これだけで認証の成否を判断してはならない
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PasskeyAssertionModel {
  /** クレデンシャルID */
  @NonNull private CredentialId credentialId;

  /** clientDataJSONに含まれるチャレンジ */
  @NonNull private PasskeyChallenge challenge;

  /** ユーザーハンドル（認証器が返さない場合はnull） */
  private PasskeyUserHandle userHandle;
}
