package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * ポートフォリオタグの情報を受け渡すためのModelクラス
 *
 * <p>{@code accountNo}・{@code portfolioNo}・{@code tagNo}は登録・更新リクエストからの生成時は未設定（null）となり、
 * 集約ルート（{@link com.web.gallery.application.aggregate.Portfolio}）が所有者・並び順に応じて設定する
 */
@Value
@Builder(toBuilder = true)
public class PortfolioTagModel {
  /** アカウント番号 */
  private AccountNo accountNo;

  /** ポートフォリオ番号 */
  private PortfolioNo portfolioNo;

  /** タグ番号 */
  private TagNo tagNo;

  /** タグ日本語名 */
  @NonNull private TagJapaneseName tagJapaneseName;

  /** タグ英語名 */
  @NonNull private TagEnglishName tagEnglishName;
}
