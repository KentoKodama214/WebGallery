package com.web.gallery.infrastructure.persistence.dto.photo;

import com.web.gallery.domain.enumeration.DirectionEnum;
import java.util.Map;
import lombok.Data;

/** ポートフォリオ写真と、写真マスタの表示用の情報を結合して保持するDtoクラス */
@Data
public class PortfolioPhotoDto {
  /** アカウント番号 */
  private Long accountNo;

  /** ポートフォリオ番号 */
  private Long portfolioNo;

  /** 写真番号 */
  private Long photoNo;

  /** 表示順 */
  private Integer displayOrder;

  /** 写真設定（JSON） */
  private Map<String, Object> photoSettings;

  /** 写真タイトル日本語名 */
  private String photoJapaneseTitle;

  /** 画像ファイルパス */
  private String imageFilePath;

  /**
   * 向き区分
   *
   * <p>{@link DirectionEnum}
   */
  private DirectionEnum directionKbn;
}
