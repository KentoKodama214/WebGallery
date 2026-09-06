package com.web.gallery.model;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.Caption;
import com.web.gallery.domain.photo.FavoriteCount;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.IsFavorite;
import com.web.gallery.domain.photo.PhotoAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumeration.DirectionEnum;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真の情報を受け渡すためのModelクラス */
@Value
@Builder(toBuilder = true)
public class PhotoModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** お気に入り数 */
  private FavoriteCount favoriteCount;

  /** お気に入り */
  private IsFavorite isFavorite;

  /** 撮影日時 */
  @NonNull private PhotoAt photoAt;

  /** 画像ファイルパス */
  @NonNull private ImageFilePath imageFilePath;

  /** キャプション */
  @NonNull private Caption caption;

  /**
   * 向き区分
   *
   * <p>{@link DirectionEnum}
   */
  @NonNull private DirectionEnum directionKbn;

  /** 写真タグリスト */
  @NonNull private PhotoTagModelList photoTagModelList;

  /**
   * PhotoDtoとタグエンティティリストからPhotoModelを生成する
   *
   * @param dto {@link PhotoDto}
   * @param photoTagMstList 全タグエンティティリスト（内部で該当写真のタグをフィルタリングする）
   * @return {@link PhotoModel}
   */
  public static PhotoModel from(PhotoDto dto, List<PhotoTagMst> photoTagMstList) {
    AccountNo accountNo = new AccountNo(dto.getAccountNo());
    PhotoNo photoNo = new PhotoNo(dto.getPhotoNo());
    PhotoTagModelList photoTagModelList =
        PhotoTagModelList.from(photoTagMstList).filterByPhoto(accountNo, photoNo);
    return PhotoModel.builder()
        .accountNo(accountNo)
        .photoNo(photoNo)
        .favoriteCount(new FavoriteCount(dto.getFavoriteCount()))
        .isFavorite(new IsFavorite(dto.getIsFavorite()))
        .photoAt(new PhotoAt(dto.getPhotoAt().withOffsetSameInstant(Consts.JST)))
        .imageFilePath(new ImageFilePath(dto.getImageFilePath()))
        .caption(new Caption(dto.getCaption()))
        .directionKbn(dto.getDirectionKbn())
        .photoTagModelList(photoTagModelList)
        .build();
  }
}
