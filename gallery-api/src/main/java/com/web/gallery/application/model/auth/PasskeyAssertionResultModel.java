package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.SignCount;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 署名検証に成功したパスキー認証レスポンス（assertion）の結果を受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PasskeyAssertionResultModel {
  /** 認証器が返した署名カウンタ */
  @NonNull private SignCount signCount;

  /** バックアップ済みフラグ（認証のたびに変わりうる） */
  @NonNull private IsBackedUp isBackedUp;
}
