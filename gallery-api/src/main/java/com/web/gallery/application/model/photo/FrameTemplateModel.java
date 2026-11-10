package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 額縁テンプレートを受け渡すためのModelクラス
 *
 * <p>{@code frameTemplateNo}は新規登録リクエストからの生成時は未設定（null）となり、Service層で採番して設定する。 {@code
 * createdAt}・{@code updatedAt}は登録・更新リクエストからの生成時は未設定（null）となる
 */
@Value
@Builder(toBuilder = true)
public class FrameTemplateModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 額縁テンプレート番号 */
  private FrameTemplateNo frameTemplateNo;

  /** テンプレート名 */
  @NonNull private FrameTemplateName templateName;

  /** 額縁定義 */
  @NonNull private FrameDefinition frameDefinition;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新日時 */
  private OffsetDateTime updatedAt;
}
