package com.web.gallery.model.photo;

import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.enumeration.SortPhotoEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.IsFavoriteOnly;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真の情報を取得する時の情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoGetModel {
  /** ログイン中のアカウントNo */
  private AccountNo accountNo;

  /** 写真のアカウントNo */
  @NonNull private AccountNo photoAccountNo;

  /**
   * 向き区分
   *
   * <p>{@link DirectionEnum}
   */
  @NonNull private DirectionEnum directionKbn;

  /** お気に入り写真のみ */
  private IsFavoriteOnly isFavoriteOnly;

  /** タグワードリスト */
  @NonNull private List<String> tagList;

  /**
   * 並び順
   *
   * <p>{@link SortPhotoEnum}
   */
  @NonNull private SortPhotoEnum sortBy;

  /** 取得件数上限（最後のページかどうかの判定用に、1ページあたりの表示件数より1件多く取得する） */
  @NonNull private Integer limit;

  /** 取得開始位置（0始まり） */
  @NonNull private Integer offset;

  /**
   * PhotoListGetModelと写真のアカウント番号、1ページあたりの表示件数からPhotoGetModelを生成する
   *
   * @param photoListGetModel {@link PhotoListGetModel}
   * @param photoAccountNo 写真のアカウントNo
   * @param photoCountPerPage 1ページあたりの表示件数
   * @return {@link PhotoGetModel}
   */
  public static PhotoGetModel of(
      PhotoListGetModel photoListGetModel, AccountNo photoAccountNo, Integer photoCountPerPage) {
    return PhotoGetModel.builder()
        .accountNo(photoListGetModel.getAccountNo())
        .photoAccountNo(photoAccountNo)
        .directionKbn(photoListGetModel.getDirectionKbn())
        .isFavoriteOnly(photoListGetModel.getIsFavoriteOnly())
        .tagList(photoListGetModel.getTagList())
        .sortBy(photoListGetModel.getSortBy())
        .limit(photoCountPerPage + 1)
        .offset((photoListGetModel.getPageNo() - 1) * photoCountPerPage)
        .build();
  }
}
