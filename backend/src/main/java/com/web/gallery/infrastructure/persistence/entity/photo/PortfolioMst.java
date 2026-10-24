package com.web.gallery.infrastructure.persistence.entity.photo;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオマスタテーブルのEntityクラス */
@Data
@Builder
public class PortfolioMst {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新者 */
  private Long updatedBy;

  /** 更新日時 */
  private OffsetDateTime updatedAt;

  /** テーマ */
  private String theme;

  /** 説明文 */
  private String description;

  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}
   */
  private PortfolioTemplateEnum templateKbn;

  /** テンプレート設定（JSON） */
  private Map<String, Object> templateSettings;

  /** 設定バージョン */
  private Integer settingsVersion;

  /**
   * 公開状態区分
   *
   * <p>{@link PortfolioStatusEnum}
   */
  private PortfolioStatusEnum statusKbn;

  /**
   * 新規登録用のPortfolioDetailModelからPortfolioMstエンティティを生成する
   *
   * @param model 集約ルートで所有者・ポートフォリオ番号・公開状態を確定済みの{@link PortfolioDetailModel}
   * @return {@link PortfolioMst}
   */
  public static PortfolioMst fromForRegist(PortfolioDetailModel model) {
    return PortfolioMst.builder()
        .accountNo(model.getAccountNo().value())
        .portfolioNo(model.getPortfolioNo().value())
        .createdBy(model.getAccountNo().value())
        .updatedBy(model.getAccountNo().value())
        .theme(model.getTheme().value())
        .description(model.getDescription().value())
        .templateKbn(model.getTemplateKbn())
        .templateSettings(model.getTemplateSettings().value())
        .settingsVersion(Consts.PORTFOLIO_SETTINGS_VERSION)
        .statusKbn(model.getStatusKbn())
        .build();
  }
}
