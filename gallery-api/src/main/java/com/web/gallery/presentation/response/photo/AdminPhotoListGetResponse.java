package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.AdminPhotoPageModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 管理者用写真一覧のレスポンスパラメータを保持するクラス */
@Schema(description = "管理者用写真一覧レスポンス")
@Data
@Builder
public class AdminPhotoListGetResponse {
  /** 最後まで取得できたか */
  @Schema(description = "最後のページかどうか")
  private Boolean isLast;

  /** 写真一覧 */
  @Schema(description = "写真一覧")
  private List<AdminPhotoListItemResponse> photoList;

  /**
   * AdminPhotoPageModelからAdminPhotoListGetResponseを生成する
   *
   * @param adminPhotoPageModel {@link AdminPhotoPageModel}
   * @return {@link AdminPhotoListGetResponse}
   */
  public static AdminPhotoListGetResponse from(AdminPhotoPageModel adminPhotoPageModel) {
    return AdminPhotoListGetResponse.builder()
        .isLast(adminPhotoPageModel.getIsLast())
        .photoList(
            adminPhotoPageModel.getAdminPhotoModelList().stream()
                .map(AdminPhotoListItemResponse::from)
                .toList())
        .build();
  }
}
