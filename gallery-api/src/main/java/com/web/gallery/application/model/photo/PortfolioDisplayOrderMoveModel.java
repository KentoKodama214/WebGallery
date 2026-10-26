package com.web.gallery.application.model.photo;

import com.web.gallery.domain.enumeration.MoveDirectionEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** ポートフォリオの表示順を1つ上・下へ移動するために必要な情報を受け渡すためのModelクラス */
@Value
@Builder
public class PortfolioDisplayOrderMoveModel {
  /** ログイン中のアカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 移動するポートフォリオ番号 */
  @NonNull private PortfolioNo portfolioNo;

  /** 移動方向 */
  @NonNull private MoveDirectionEnum direction;
}
