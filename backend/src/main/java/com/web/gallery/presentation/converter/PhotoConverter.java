package com.web.gallery.presentation.converter;

import com.web.gallery.application.model.photo.PhotoDeleteModel;
import com.web.gallery.application.model.photo.PhotoDetailModel;
import com.web.gallery.application.model.photo.PhotoFavoriteModel;
import com.web.gallery.application.model.photo.PhotoListGetModel;
import com.web.gallery.application.model.photo.PhotoTagModel;
import com.web.gallery.application.model.photo.PhotoTagModelList;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.Address;
import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.Latitude;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.LocationManagementName;
import com.web.gallery.domain.model.common.Longitude;
import com.web.gallery.domain.model.common.Referer;
import com.web.gallery.domain.model.photo.Caption;
import com.web.gallery.domain.model.photo.ExifData;
import com.web.gallery.domain.model.photo.ImageFile;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsFavorite;
import com.web.gallery.domain.model.photo.IsFavoriteOnly;
import com.web.gallery.domain.model.photo.IsLocationPublic;
import com.web.gallery.domain.model.photo.LocationNo;
import com.web.gallery.domain.model.photo.PhotoAt;
import com.web.gallery.domain.model.photo.PhotoEnglishTitle;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.presentation.request.photo.PhotoBulkSaveRequest;
import com.web.gallery.presentation.request.photo.PhotoDeleteRequest;
import com.web.gallery.presentation.request.photo.PhotoFavoriteDeleteRequest;
import com.web.gallery.presentation.request.photo.PhotoFavoriteRegistRequest;
import com.web.gallery.presentation.request.photo.PhotoListRequest;
import com.web.gallery.presentation.request.photo.PhotoSaveRequest;
import com.web.gallery.presentation.request.photo.PhotoTagSaveRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 写真関連のRequestをModelへ変換するConverterクラス */
@Component
public class PhotoConverter {

  /**
   * 写真一覧リクエストからPhotoListGetModelを生成する
   *
   * @param request {@link PhotoListRequest}
   * @param accountNo ログイン中のアカウントNo
   * @param photoAccountId 写真のアカウントID
   * @param ipAddress 送信元IPアドレス
   * @param referer リファラ
   * @return {@link PhotoListGetModel}
   */
  public PhotoListGetModel toPhotoListGetModel(
      PhotoListRequest request,
      Long accountNo,
      String photoAccountId,
      IpAddress ipAddress,
      Referer referer) {
    Optional<String> tagsOpt = Optional.ofNullable(request.getTagList());
    // 空文字トークンを除外し、件数上限を強制する。
    // （バリデーション側 PhotoListRequest#isTagListSizeValid は空文字を除外して数えるため、
    //   ここで除外しないと「全角スペースの大量指定」で相関サブクエリを無制限に増やせてしまう）
    List<String> tagList =
        tagsOpt
            .map(
                tag ->
                    Arrays.stream(
                            tag.replace(Consts.FULL_SPACE, Consts.HALF_SPACE)
                                .split(Consts.HALF_SPACE))
                        .filter(t -> !t.isEmpty())
                        .limit(Consts.TAG_LIST_MAX_SIZE)
                        .collect(Collectors.toCollection(ArrayList::new)))
            .orElseGet(ArrayList::new);

    return PhotoListGetModel.builder()
        .accountNo(accountNo != null ? new AccountNo(accountNo) : null)
        .photoAccountId(new AccountId(photoAccountId))
        .directionKbn(request.getDirectionKbn())
        .isFavoriteOnly(
            new IsFavoriteOnly(Optional.ofNullable(request.getIsFavorite()).orElse(Boolean.FALSE)))
        .tagList(tagList)
        .sortBy(request.getSortBy())
        .pageNo(request.getPageNo())
        .searchExecuted(Optional.ofNullable(request.getSearchExecuted()).orElse(Boolean.FALSE))
        .logInitialView(Optional.ofNullable(request.getLogInitialView()).orElse(Boolean.FALSE))
        .ipAddress(ipAddress)
        .referer(referer)
        .build();
  }

  /**
   * 写真削除リクエストとログイン中のアカウント番号からPhotoDeleteModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の写真を操作するIDORを防ぐため）
   *
   * @param request {@link PhotoDeleteRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link PhotoDeleteModel}
   */
  public PhotoDeleteModel toPhotoDeleteModel(PhotoDeleteRequest request, AccountNo accountNo) {
    return PhotoDeleteModel.builder()
        .accountNo(accountNo)
        .photoNo(new PhotoNo(request.getPhotoNo()))
        .imageFilePath(new ImageFilePath(request.getImageFilePath()))
        .build();
  }

  /**
   * お気に入り登録リクエストからPhotoFavoriteModelを生成する
   *
   * @param request {@link PhotoFavoriteRegistRequest}
   * @param accountNo アカウント番号
   * @return {@link PhotoFavoriteModel}
   */
  public PhotoFavoriteModel toPhotoFavoriteModel(
      PhotoFavoriteRegistRequest request, Long accountNo) {
    return PhotoFavoriteModel.builder()
        .accountNo(new AccountNo(accountNo))
        .favoritePhotoAccountNo(new AccountNo(request.getFavoritePhotoAccountNo()))
        .favoritePhotoNo(new PhotoNo(request.getFavoritePhotoNo()))
        .build();
  }

  /**
   * お気に入り解除リクエストからPhotoFavoriteModelを生成する
   *
   * @param request {@link PhotoFavoriteDeleteRequest}
   * @param accountNo アカウント番号
   * @return {@link PhotoFavoriteModel}
   */
  public PhotoFavoriteModel toPhotoFavoriteModel(
      PhotoFavoriteDeleteRequest request, Long accountNo) {
    return PhotoFavoriteModel.builder()
        .accountNo(new AccountNo(accountNo))
        .favoritePhotoAccountNo(new AccountNo(request.getFavoritePhotoAccountNo()))
        .favoritePhotoNo(new PhotoNo(request.getFavoritePhotoNo()))
        .build();
  }

  /**
   * 写真保存リクエストとログイン中のアカウント番号からPhotoDetailModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の写真を操作するIDORを防ぐため）
   *
   * @param request {@link PhotoSaveRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link PhotoDetailModel}
   */
  public PhotoDetailModel toPhotoDetailModelForRegist(
      PhotoSaveRequest request, AccountNo accountNo) {
    PhotoTagModelList photoTagModelList =
        Objects.isNull(request.getPhotoTagRegistRequestList())
            ? PhotoTagModelList.empty()
            : PhotoTagModelList.of(
                request.getPhotoTagRegistRequestList().stream()
                    .map(tagRequest -> toPhotoTagModel(tagRequest, accountNo))
                    .toList());
    return PhotoDetailModel.builder()
        .accountNo(accountNo)
        .photoNo(request.getPhotoNo() != null ? new PhotoNo(request.getPhotoNo()) : null)
        .isFavorite(
            request.getIsFavorite() != null ? new IsFavorite(request.getIsFavorite()) : null)
        .photoAt(
            Optional.ofNullable(request.getPhotoAt())
                .map(photoAt -> new PhotoAt(photoAt.atOffset(Consts.JST)))
                .orElse(null))
        .locationNo(
            request.getLocationNo() != null ? new LocationNo(request.getLocationNo()) : null)
        .geoLocation(
            new GeoLocation(
                request.getAddress() != null ? new Address(request.getAddress()) : null,
                request.getLatitude() != null ? new Latitude(request.getLatitude()) : null,
                request.getLongitude() != null ? new Longitude(request.getLongitude()) : null))
        .managementName(
            request.getManagementName() != null
                ? new LocationManagementName(request.getManagementName())
                : null)
        .displayName(
            request.getDisplayName() != null
                ? new LocationDisplayName(request.getDisplayName())
                : null)
        .isLocationPublic(
            request.getIsLocationPublic() != null
                ? new IsLocationPublic(request.getIsLocationPublic())
                : null)
        .imageFile(request.getImageFile() != null ? new ImageFile(request.getImageFile()) : null)
        .imageFilePath(
            new ImageFilePath(
                Optional.ofNullable(request.getImageFilePath()).orElse(Consts.STRING_EMPTY)))
        .photoJapaneseTitle(
            request.getPhotoJapaneseTitle() != null
                ? new PhotoJapaneseTitle(request.getPhotoJapaneseTitle())
                : null)
        .photoEnglishTitle(
            request.getPhotoEnglishTitle() != null
                ? new PhotoEnglishTitle(request.getPhotoEnglishTitle())
                : null)
        .caption(request.getCaption() != null ? new Caption(request.getCaption()) : null)
        .directionKbn(request.getDirectionKbn())
        .exifData(
            ExifData.fromRawValues(
                request.getFocalLength(),
                request.getFValue(),
                request.getShutterSpeed(),
                request.getIso()))
        .photoTagModelList(photoTagModelList)
        .build();
  }

  /**
   * 写真新規一括登録リクエストの共通メタデータと、そのうち1枚分の画像ファイルからPhotoDetailModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の写真を操作するIDORを防ぐため）。
   * タイトル〜タグの共通メタデータは一括登録対象の全画像で共有し、画像ファイル・向き区分・EXIF情報のみ引数の1件分を設定する
   * （複数枚では縦向き・横向きが混在しうるため、向き区分は呼び出し元が画像ファイルの実際のピクセルサイズから
   * 判定した値を渡す。EXIF情報も同様に、画像ファイルから抽出した値とクライアント申告値をマージ済みのものを
   * 呼び出し元から受け取る）。新規登録専用のため、写真番号・画像ファイルパスは常に未設定とする
   *
   * @param request {@link PhotoBulkSaveRequest}
   * @param imageFile 一括登録対象のうち1枚分の画像ファイル
   * @param directionKbn 画像ファイルの実際のピクセルサイズから判定した向き区分
   * @param exifData 画像ファイルから抽出した値とクライアント申告値をマージ済みのEXIF情報
   * @param accountNo ログイン中のアカウント番号
   * @return {@link PhotoDetailModel}
   */
  public PhotoDetailModel toPhotoDetailModelForBulkRegist(
      PhotoBulkSaveRequest request,
      MultipartFile imageFile,
      DirectionEnum directionKbn,
      ExifData exifData,
      AccountNo accountNo) {
    PhotoTagModelList photoTagModelList =
        Objects.isNull(request.getPhotoTagRegistRequestList())
            ? PhotoTagModelList.empty()
            : PhotoTagModelList.of(
                request.getPhotoTagRegistRequestList().stream()
                    .map(tagRequest -> toPhotoTagModel(tagRequest, accountNo))
                    .toList());
    return PhotoDetailModel.builder()
        .accountNo(accountNo)
        .photoAt(
            Optional.ofNullable(request.getPhotoAt())
                .map(photoAt -> new PhotoAt(photoAt.atOffset(Consts.JST)))
                .orElse(null))
        .locationNo(
            request.getLocationNo() != null ? new LocationNo(request.getLocationNo()) : null)
        .geoLocation(
            new GeoLocation(
                request.getAddress() != null ? new Address(request.getAddress()) : null,
                request.getLatitude() != null ? new Latitude(request.getLatitude()) : null,
                request.getLongitude() != null ? new Longitude(request.getLongitude()) : null))
        .managementName(
            request.getManagementName() != null
                ? new LocationManagementName(request.getManagementName())
                : null)
        .displayName(
            request.getDisplayName() != null
                ? new LocationDisplayName(request.getDisplayName())
                : null)
        .isLocationPublic(
            request.getIsLocationPublic() != null
                ? new IsLocationPublic(request.getIsLocationPublic())
                : null)
        .imageFile(imageFile != null ? new ImageFile(imageFile) : null)
        .imageFilePath(new ImageFilePath(Consts.STRING_EMPTY))
        .photoJapaneseTitle(
            request.getPhotoJapaneseTitle() != null
                ? new PhotoJapaneseTitle(request.getPhotoJapaneseTitle())
                : null)
        .photoEnglishTitle(
            request.getPhotoEnglishTitle() != null
                ? new PhotoEnglishTitle(request.getPhotoEnglishTitle())
                : null)
        .caption(request.getCaption() != null ? new Caption(request.getCaption()) : null)
        .directionKbn(directionKbn)
        .exifData(exifData)
        .photoTagModelList(photoTagModelList)
        .build();
  }

  /**
   * 写真タグ保存リクエストとログイン中のアカウント番号からPhotoTagModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人の写真へタグを注入するIDORを防ぐため）。
   * 写真番号・タグ番号は登録時にサーバ側で採番するためここでは設定しない
   *
   * @param request {@link PhotoTagSaveRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link PhotoTagModel}
   */
  private PhotoTagModel toPhotoTagModel(PhotoTagSaveRequest request, AccountNo accountNo) {
    return PhotoTagModel.builder()
        .accountNo(accountNo)
        .tagJapaneseName(
            new TagJapaneseName(
                Optional.ofNullable(request.getTagJapaneseName()).orElse(Consts.STRING_EMPTY)))
        .tagEnglishName(
            new TagEnglishName(
                Optional.ofNullable(request.getTagEnglishName()).orElse(Consts.STRING_EMPTY)))
        .build();
  }
}
