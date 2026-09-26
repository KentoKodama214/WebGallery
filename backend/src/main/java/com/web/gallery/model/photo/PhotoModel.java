package com.web.gallery.model.photo;

import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.Caption;
import com.web.gallery.domain.model.photo.FavoriteCount;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsFavorite;
import com.web.gallery.domain.model.photo.PhotoAt;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.entity.photo.PhotoTagMst;
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
