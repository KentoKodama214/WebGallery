package com.web.gallery.infrastructure.persistence.dto.photo;

import com.web.gallery.application.model.photo.PhotoDetailSearchModel;
import java.util.Objects;
import lombok.Data;

/** 写真のメタデータを含めた詳細情報を取得するパラメータDtoクラス */
@Data
public class PhotoDetailGetDto {
  /** ログイン中のアカウントNo */
  private Long accountNo;

  /** 写真のアカウントNo */
  private Long photoAccountNo;

  /**
   * 管理者により非公開にされた写真も取得対象に含めるならtrue
   *
   * <p>閲覧者が写真の所有者本人である場合のみtrue（本人以外・未ログインには非公開の写真を返さない）
   */
  private Boolean includeHiddenByAdmin;

  /** 写真番号 */
  private Long photoNo;

  /**
   * PhotoDetailSearchModelからPhotoDetailGetDtoを生成する
   *
   * @param model {@link PhotoDetailSearchModel}
   * @return {@link PhotoDetailGetDto}
   */
  public static PhotoDetailGetDto from(PhotoDetailSearchModel model) {
    PhotoDetailGetDto dto = new PhotoDetailGetDto();
    dto.setAccountNo(Objects.nonNull(model.getAccountNo()) ? model.getAccountNo().value() : null);
    dto.setPhotoAccountNo(model.getPhotoAccountNo().value());
    dto.setPhotoNo(model.getPhotoNo().value());
    dto.setIncludeHiddenByAdmin(
        Objects.nonNull(dto.getAccountNo()) && dto.getAccountNo().equals(dto.getPhotoAccountNo()));
    return dto;
  }
}
