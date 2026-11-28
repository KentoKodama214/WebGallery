package com.web.gallery.application.model.account;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** アカウントの一覧を取得するために必要な情報を受け渡すためのModelクラス */
@Value
@Builder
public class AccountListGetModel {
  /** ページ番号 */
  @NonNull private Integer pageNo;
}
