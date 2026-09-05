package com.web.gallery.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** アカウント一覧の1ページ分の情報を受け渡すためのModelクラス */
@Value
@Builder
public class AccountPageModel {
  /** アカウント一覧 */
  @NonNull private AccountModelList accountModelList;

  /** 最後のページかどうか */
  @NonNull private Boolean isLast;

  /**
   * AccountModelListと最後のページかどうかからAccountPageModelを生成する
   *
   * @param accountModelList {@link AccountModelList}
   * @param isLast 最後のページかどうか
   * @return {@link AccountPageModel}
   */
  public static AccountPageModel of(AccountModelList accountModelList, Boolean isLast) {
    return AccountPageModel.builder().accountModelList(accountModelList).isLast(isLast).build();
  }
}
