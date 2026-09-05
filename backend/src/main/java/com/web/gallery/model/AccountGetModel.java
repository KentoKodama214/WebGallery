package com.web.gallery.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** アカウントの一覧をDBから取得する際のページング情報を受け渡すためのModelクラス */
@Value
@Builder
public class AccountGetModel {
  /** 取得件数上限（最後のページかどうかの判定用に、1ページあたりの表示件数より1件多く取得する） */
  @NonNull private Integer limit;

  /** 取得開始位置（0始まり） */
  @NonNull private Integer offset;

  /**
   * AccountListGetModelと1ページあたりの表示件数からAccountGetModelを生成する
   *
   * @param accountListGetModel {@link AccountListGetModel}
   * @param accountCountPerPage 1ページあたりの表示件数
   * @return {@link AccountGetModel}
   */
  public static AccountGetModel of(
      AccountListGetModel accountListGetModel, Integer accountCountPerPage) {
    return AccountGetModel.builder()
        .limit(accountCountPerPage + 1)
        .offset((accountListGetModel.getPageNo() - 1) * accountCountPerPage)
        .build();
  }
}
