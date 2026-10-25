package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 本人のポートフォリオ一覧を絞り込んで取得するために必要な情報を受け渡すためのModelクラス */
@Value
@Builder
public class PortfolioListGetModel {
  /** ログイン中のアカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 検索語（テーマ・タグ名（日本語・英語）のいずれかに部分一致。未指定はnull） */
  private String keyword;
}
