package com.web.gallery.dto;

import com.web.gallery.model.PhotoDetailSearchModel;
import java.util.Objects;
import lombok.Data;

/** 写真のメタデータを含めた詳細情報を取得するパラメータDtoクラス */
@Data
public class PhotoDetailGetDto {
  /** ログイン中のアカウントNo */
  private Long accountNo;

  /** 写真のアカウントNo */
  private Long photoAccountNo;

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
    return dto;
  }
}
