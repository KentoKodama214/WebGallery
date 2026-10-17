package com.web.gallery.application.model.photo;

import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * ポートフォリオ一覧の1件分の情報を受け渡すためのModelクラス
 *
 * <p>写真削除に伴い下書きに戻したポートフォリオの通知にも用いる
 */
@Value
@Builder
public class PortfolioModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** ポートフォリオ番号 */
  @NonNull private PortfolioNo portfolioNo;

  /** テーマ */
  @NonNull private PortfolioTheme theme;

  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}
   */
  @NonNull private PortfolioTemplateEnum templateKbn;

  /**
   * 公開状態区分
   *
   * <p>{@link PortfolioStatusEnum}
   */
  @NonNull private PortfolioStatusEnum statusKbn;

  /** 写真の枚数 */
  @NonNull private PhotoCount photoCount;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新日時 */
  private OffsetDateTime updatedAt;
}
