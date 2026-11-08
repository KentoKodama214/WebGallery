package com.web.gallery.infrastructure.persistence.dto.photo;

import com.web.gallery.application.model.photo.PhotoGetModel;
import com.web.gallery.domain.enumeration.DirectionEnum;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/** 写真の一覧を取得するパラメータDtoクラス */
@Data
public class PhotoListGetDto {
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

  /**
   * 向き区分（絞り込み不要の場合はnull）
   *
   * <p>{@link DirectionEnum}
   */
  private DirectionEnum directionKbn;

  /** お気に入りの写真のみに絞り込むならtrue */
  private Boolean isFavoriteOnly;

  /** タグワードリスト（絞り込み不要の場合は空リストまたは空文字を含むリスト） */
  private List<String> tagList;

  /** 並び順（"PHOTO_AT" / "FAVORITE" / "SEASON"） */
  private String sortBy;

  /** 取得件数上限 */
  private Integer limit;

  /** 取得開始位置 */
  private Integer offset;

  /**
   * PhotoGetModelからPhotoListGetDtoを生成する
   *
   * @param model {@link PhotoGetModel}
   * @return {@link PhotoListGetDto}
   */
  public static PhotoListGetDto from(PhotoGetModel model) {
    PhotoListGetDto dto = new PhotoListGetDto();
    dto.setAccountNo(Objects.nonNull(model.getAccountNo()) ? model.getAccountNo().value() : null);
    dto.setPhotoAccountNo(model.getPhotoAccountNo().value());
    dto.setIncludeHiddenByAdmin(
        Objects.nonNull(dto.getAccountNo()) && dto.getAccountNo().equals(dto.getPhotoAccountNo()));
    dto.setDirectionKbn(
        DirectionEnum.NONE.equals(model.getDirectionKbn()) ? null : model.getDirectionKbn());
    dto.setIsFavoriteOnly(
        Objects.nonNull(model.getIsFavoriteOnly()) && model.getIsFavoriteOnly().value());
    dto.setTagList(model.getTagList());
    dto.setSortBy(Objects.nonNull(model.getSortBy()) ? model.getSortBy().name() : null);
    dto.setLimit(model.getLimit());
    dto.setOffset(model.getOffset());
    return dto;
  }
}
