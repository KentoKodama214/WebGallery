package com.web.gallery.infrastructure.persistence.entity.photo;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオマスタテーブルの更新対象クラス */
@Data
@Builder
public class PortfolioMstUpdateTarget {
  /** 更新者 */
  private Long updatedBy;

  /** 削除フラグ */
  private Boolean isDeleted;

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
   * 内容の更新（写真の除外に伴う公開状態の差し戻しを含む）に伴う更新対象を生成する
   *
   * <p>公開状態は{@link com.web.gallery.application.aggregate.Portfolio}が決定済みの値をそのまま読み取る
   *
   * @param detail 更新後の{@link PortfolioDetailModel}
   * @return {@link PortfolioMstUpdateTarget}
   */
  public static PortfolioMstUpdateTarget forUpdate(PortfolioDetailModel detail) {
    return PortfolioMstUpdateTarget.builder()
        .updatedBy(detail.getAccountNo().value())
        .theme(detail.getTheme().value())
        .description(detail.getDescription().value())
        .templateKbn(detail.getTemplateKbn())
        .templateSettings(detail.getTemplateSettings().value())
        .settingsVersion(Consts.PORTFOLIO_SETTINGS_VERSION)
        .statusKbn(detail.getStatusKbn())
        .build();
  }

  /**
   * 公開状態の変更に伴う更新対象を生成する
   *
   * @param detail 公開状態変更後の{@link PortfolioDetailModel}
   * @return {@link PortfolioMstUpdateTarget}
   */
  public static PortfolioMstUpdateTarget forStatus(PortfolioDetailModel detail) {
    return PortfolioMstUpdateTarget.builder()
        .updatedBy(detail.getAccountNo().value())
        .statusKbn(detail.getStatusKbn())
        .build();
  }

  /**
   * 論理削除に伴う更新対象を生成する
   *
   * @param accountNo 削除したユーザーのアカウント番号
   * @return {@link PortfolioMstUpdateTarget}
   */
  public static PortfolioMstUpdateTarget forDelete(Long accountNo) {
    return PortfolioMstUpdateTarget.builder().updatedBy(accountNo).isDeleted(true).build();
  }
}
