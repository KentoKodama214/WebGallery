package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.AdminPhotoModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** 管理者用写真一覧の1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "管理者用写真一覧アイテムレスポンス")
@Data
@Builder
public class AdminPhotoListItemResponse {
  /** 写真所有者のアカウント番号 */
  @Schema(description = "写真所有者のアカウント番号", example = "1")
  private Long accountNo;

  /** 写真所有者のアカウントID */
  @Schema(description = "写真所有者のアカウントID")
  private String accountId;

  /** 写真所有者のアカウント名 */
  @Schema(description = "写真所有者のアカウント名")
  private String accountName;

  /** 写真番号 */
  @Schema(description = "写真番号", example = "1")
  private Long photoNo;

  /** 写真タイトル日本語名 */
  @Schema(description = "写真タイトル日本語名")
  private String photoJapaneseTitle;

  /** 画像ファイルパス（署名付きURL） */
  @Schema(description = "画像ファイルパス（署名付きURL）")
  private String imageFilePath;

  /** 管理者非公開フラグ */
  @Schema(description = "管理者により非公開にされているか")
  private Boolean isHiddenByAdmin;

  /** 登録日時 */
  @Schema(description = "登録日時")
  private OffsetDateTime createdAt;

  /**
   * AdminPhotoModelからAdminPhotoListItemResponseを生成する
   *
   * @param model {@link AdminPhotoModel}
   * @return {@link AdminPhotoListItemResponse}
   */
  public static AdminPhotoListItemResponse from(AdminPhotoModel model) {
    return AdminPhotoListItemResponse.builder()
        .accountNo(model.getAccountNo().value())
        .accountId(model.getAccountId().value())
        .accountName(model.getAccountName().value())
        .photoNo(model.getPhotoNo().value())
        .photoJapaneseTitle(model.getPhotoJapaneseTitle().value())
        .imageFilePath(model.getImageFilePath().value())
        .isHiddenByAdmin(model.getIsHiddenByAdmin().value())
        .createdAt(model.getCreatedAt())
        .build();
  }
}
