package com.web.gallery.infrastructure.persistence.dto.photo;

import com.web.gallery.application.model.photo.AdminPhotoGetModel;
import java.util.Objects;
import lombok.Data;

/** 管理者用の写真一覧を取得するパラメータDtoクラス */
@Data
public class AdminPhotoListGetDto {
  /** 写真所有者のアカウントID（絞り込み不要の場合はnull） */
  private String accountId;

  /** 管理者非公開フラグ（絞り込み不要の場合はnull） */
  private Boolean isHiddenByAdmin;

  /** 取得件数上限 */
  private Integer limit;

  /** 取得開始位置 */
  private Integer offset;

  /**
   * AdminPhotoGetModelからAdminPhotoListGetDtoを生成する
   *
   * @param model {@link AdminPhotoGetModel}
   * @return {@link AdminPhotoListGetDto}
   */
  public static AdminPhotoListGetDto from(AdminPhotoGetModel model) {
    AdminPhotoListGetDto dto = new AdminPhotoListGetDto();
    dto.setAccountId(Objects.nonNull(model.getAccountId()) ? model.getAccountId().value() : null);
    dto.setIsHiddenByAdmin(
        Objects.nonNull(model.getIsHiddenByAdmin()) ? model.getIsHiddenByAdmin().value() : null);
    dto.setLimit(model.getLimit());
    dto.setOffset(model.getOffset());
    return dto;
  }
}
