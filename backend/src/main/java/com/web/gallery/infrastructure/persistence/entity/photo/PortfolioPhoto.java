package com.web.gallery.infrastructure.persistence.entity.photo;

import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオ写真テーブルのEntityクラス */
@Data
@Builder
public class PortfolioPhoto {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /** 写真番号 */
  private Long photoNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 表示順 */
  private Integer displayOrder;

  /** 写真設定（JSON） */
  private Map<String, Object> photoSettings;

  /**
   * PortfolioPhotoModelからPortfolioPhotoエンティティを生成する
   *
   * @param model 集約ルートで所有者・ポートフォリオ番号・表示順を確定済みの{@link PortfolioPhotoModel}
   * @return {@link PortfolioPhoto}
   */
  public static PortfolioPhoto from(PortfolioPhotoModel model) {
    return PortfolioPhoto.builder()
        .accountNo(model.getAccountNo().value())
        .portfolioNo(model.getPortfolioNo().value())
        .photoNo(model.getPhotoNo().value())
        .createdBy(model.getAccountNo().value())
        .displayOrder(model.getDisplayOrder().value())
        .photoSettings(model.getPhotoSettings().value())
        .build();
  }
}
