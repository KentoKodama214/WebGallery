package com.web.gallery.application.model.photo;

import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.LocationManagementName;
import com.web.gallery.domain.model.photo.Caption;
import com.web.gallery.domain.model.photo.ExifData;
import com.web.gallery.domain.model.photo.ImageFile;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsFavorite;
import com.web.gallery.domain.model.photo.IsLocationPublic;
import com.web.gallery.domain.model.photo.LocationNo;
import com.web.gallery.domain.model.photo.PhotoAt;
import com.web.gallery.domain.model.photo.PhotoEnglishTitle;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真のメタデータを含めた詳細情報を受け渡すためのModelクラス */
@Value
@Builder(toBuilder = true)
public class PhotoDetailModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 写真番号 */
  private PhotoNo photoNo;

  /** お気に入り */
  private IsFavorite isFavorite;

  /** 撮影日時 */
  private PhotoAt photoAt;

  /** ロケーション番号 */
  private LocationNo locationNo;

  /** 位置情報（住所・緯度・経度） */
  private GeoLocation geoLocation;

  /** ロケーション管理名（新規入力時のみ設定。既存選択・写真詳細取得では未設定） */
  private LocationManagementName managementName;

  /** ロケーション表示名 */
  private LocationDisplayName displayName;

  /** 位置情報公開フラグ */
  private IsLocationPublic isLocationPublic;

  /** 画像ファイル */
  private ImageFile imageFile;

  /** 画像ファイルパス */
  @NonNull private ImageFilePath imageFilePath;

  /** 写真タイトル日本語名 */
  private PhotoJapaneseTitle photoJapaneseTitle;

  /** 写真タイトル英語名 */
  private PhotoEnglishTitle photoEnglishTitle;

  /** キャプション */
  private Caption caption;

  /**
   * 向き区分
   *
   * <p>{@link DirectionEnum}
   */
  private DirectionEnum directionKbn;

  /** EXIF情報（焦点距離・F値・シャッタースピード・ISO） */
  private ExifData exifData;

  /** 写真タグリスト */
  private PhotoTagModelList photoTagModelList;

  /**
   * 位置情報を取得する。未設定の場合は全項目未設定のGeoLocationを返す
   *
   * @return {@link GeoLocation}
   */
  public GeoLocation getGeoLocation() {
    return geoLocation != null ? geoLocation : GeoLocation.empty();
  }

  /**
   * EXIF情報を取得する。未設定の場合は全項目未設定のExifDataを返す
   *
   * @return {@link ExifData}
   */
  public ExifData getExifData() {
    return exifData != null ? exifData : ExifData.empty();
  }
}
