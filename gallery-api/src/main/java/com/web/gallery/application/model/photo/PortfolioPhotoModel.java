package com.web.gallery.application.model.photo;

import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.TemplateSettings;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * ポートフォリオに設定した写真の情報を受け渡すためのModelクラス
 *
 * <p>{@code accountNo}・{@code portfolioNo}・{@code displayOrder}は登録・更新リクエストからの生成時は未設定（null）となり、
 * 集約ルート（{@link com.web.gallery.application.aggregate.Portfolio}）が所有者・並び順に応じて設定する。 {@code
 * photoJapaneseTitle}・{@code imageFilePath}・{@code directionKbn}は詳細取得時のみ設定される表示用の写真情報
 */
@Value
@Builder(toBuilder = true)
public class PortfolioPhotoModel {
  /** アカウント番号 */
  private AccountNo accountNo;

  /** ポートフォリオ番号 */
  private PortfolioNo portfolioNo;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** 表示順 */
  private DisplayOrder displayOrder;

  /** 写真単位のテンプレート設定 */
  @NonNull private TemplateSettings photoSettings;

  /** 写真タイトル日本語名（詳細取得時のみ設定） */
  private PhotoJapaneseTitle photoJapaneseTitle;

  /** 画像ファイルパス（詳細取得時のみ設定。Service層で署名付きURLに差し替える） */
  private ImageFilePath imageFilePath;

  /**
   * 向き区分（詳細取得時のみ設定）
   *
   * <p>{@link DirectionEnum}
   */
  private DirectionEnum directionKbn;
}
