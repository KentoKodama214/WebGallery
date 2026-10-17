package com.web.gallery.infrastructure.persistence.dto.photo;

import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import java.time.OffsetDateTime;
import lombok.Data;

/** ポートフォリオ一覧の1件分（写真の枚数を含む）を保持するDtoクラス */
@Data
public class PortfolioDto {
  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /** テーマ */
  private String theme;

  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}
   */
  private PortfolioTemplateEnum templateKbn;

  /**
   * 公開状態区分
   *
   * <p>{@link PortfolioStatusEnum}
   */
  private PortfolioStatusEnum statusKbn;

  /** 写真の枚数 */
  private Integer photoCount;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新日時 */
  private OffsetDateTime updatedAt;
}
