package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** ポートフォリオに設定する写真を、本人の写真から検索するために必要な情報を受け渡すためのModelクラス */
@Value
@Builder(toBuilder = true)
public class PortfolioPhotoSearchModel {
  /** ログイン中のアカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 検索語（写真のタイトル・撮影場所の名前・撮影場所の住所・タグ名のいずれかに部分一致。未指定はnull） */
  private String keyword;

  /** ページ番号 */
  @NonNull private Integer pageNo;

  /** 1ページあたりの件数（設定値のためServiceで設定する。リクエストからの変換時はnull） */
  private Integer pageSize;
}
