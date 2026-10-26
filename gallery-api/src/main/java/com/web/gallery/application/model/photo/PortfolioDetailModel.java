package com.web.gallery.application.model.photo;

import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * ポートフォリオの詳細情報（写真・タグを含む）を受け渡すためのModelクラス
 *
 * <p>{@code portfolioNo}・{@code statusKbn}・{@code createdAt}・{@code updatedAt}は登録・更新リクエストからの生成時は
 * 未設定（null）となる
 */
@Value
@Builder(toBuilder = true)
public class PortfolioDetailModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** ポートフォリオ番号 */
  private PortfolioNo portfolioNo;

  /** テーマ */
  @NonNull private PortfolioTheme theme;

  /** 説明文 */
  @NonNull private PortfolioDescription description;

  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}
   */
  @NonNull private PortfolioTemplateEnum templateKbn;

  /** ポートフォリオ単位のテンプレート設定 */
  @NonNull private TemplateSettings templateSettings;

  /**
   * 公開状態区分
   *
   * <p>{@link PortfolioStatusEnum}
   */
  private PortfolioStatusEnum statusKbn;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新日時 */
  private OffsetDateTime updatedAt;

  /** 写真一覧（表示順） */
  @NonNull private PortfolioPhotoModelList portfolioPhotoModelList;

  /** タグ一覧 */
  @NonNull private PortfolioTagModelList portfolioTagModelList;
}
