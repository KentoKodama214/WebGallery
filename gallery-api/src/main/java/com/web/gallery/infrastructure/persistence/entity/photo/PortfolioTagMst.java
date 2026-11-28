package com.web.gallery.infrastructure.persistence.entity.photo;

import com.web.gallery.application.model.photo.PortfolioTagModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオタグマスタテーブルのEntityクラス */
@Data
@Builder
public class PortfolioTagMst {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /** タグ番号 */
  private Long tagNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** タグ日本語名 */
  private String tagJapaneseName;

  /** タグ英語名 */
  private String tagEnglishName;

  /**
   * PortfolioTagModelからPortfolioTagMstエンティティを生成する
   *
   * @param model 集約ルートで所有者・ポートフォリオ番号・タグ番号を確定済みの{@link PortfolioTagModel}
   * @return {@link PortfolioTagMst}
   */
  public static PortfolioTagMst from(PortfolioTagModel model) {
    return PortfolioTagMst.builder()
        .accountNo(model.getAccountNo().value())
        .portfolioNo(model.getPortfolioNo().value())
        .tagNo(model.getTagNo().value())
        .createdBy(model.getAccountNo().value())
        .tagJapaneseName(model.getTagJapaneseName().value())
        .tagEnglishName(model.getTagEnglishName().value())
        .build();
  }
}
