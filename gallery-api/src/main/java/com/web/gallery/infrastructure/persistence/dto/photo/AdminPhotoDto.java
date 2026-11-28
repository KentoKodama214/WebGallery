package com.web.gallery.infrastructure.persistence.dto.photo;

import java.time.OffsetDateTime;
import lombok.Data;

/** 管理者用写真一覧の1件分のデータを保持するDtoクラス */
@Data
public class AdminPhotoDto {
  /** 写真所有者のアカウント番号 */
  private Long accountNo;

  /** 写真所有者のアカウントID */
  private String accountId;

  /** 写真所有者のアカウント名 */
  private String accountName;

  /** 写真番号 */
  private Long photoNo;

  /** 写真タイトル日本語名 */
  private String photoJapaneseTitle;

  /** 画像ファイルパス */
  private String imageFilePath;

  /** 管理者非公開フラグ */
  private Boolean isHiddenByAdmin;

  /** 登録日時 */
  private OffsetDateTime createdAt;
}
