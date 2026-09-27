package com.web.gallery.infrastructure.persistence.repository.photo;

import com.web.gallery.application.model.photo.PhotoDetailModel;
import com.web.gallery.application.model.photo.PhotoDetailSearchModel;
import com.web.gallery.application.model.photo.PhotoGetModel;
import com.web.gallery.application.model.photo.PhotoModel;
import com.web.gallery.application.model.photo.PhotoModelList;
import com.web.gallery.application.model.photo.PhotoPageModel;
import com.web.gallery.application.model.photo.PhotoTagModel;
import com.web.gallery.application.model.photo.PhotoTagModelList;
import com.web.gallery.application.repository.photo.PhotoDetailRepository;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.Address;
import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.Latitude;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.Longitude;
import com.web.gallery.domain.model.photo.Caption;
import com.web.gallery.domain.model.photo.ExifData;
import com.web.gallery.domain.model.photo.FValue;
import com.web.gallery.domain.model.photo.FavoriteCount;
import com.web.gallery.domain.model.photo.FocalLength;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsFavorite;
import com.web.gallery.domain.model.photo.IsLocationPublic;
import com.web.gallery.domain.model.photo.Iso;
import com.web.gallery.domain.model.photo.LocationNo;
import com.web.gallery.domain.model.photo.PhotoAt;
import com.web.gallery.domain.model.photo.PhotoEnglishTitle;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.ShutterSpeed;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDetailDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDetailGetDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoDto;
import com.web.gallery.infrastructure.persistence.dto.photo.PhotoListGetDto;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoTagMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoDetailMapper;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoTagMstMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/** 写真のメタデータを含めた詳細情報を永続化するRepositoryの実装クラス */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoDetailRepositoryImpl implements PhotoDetailRepository {

  private final PhotoTagMstMapper photoTagMstMapper;
  private final PhotoDetailMapper photoDetailMapper;

  /**
   * 該当アカウントの写真の一覧を、ページング情報に従い取得する
   *
   * <p>最後のページかどうかを判定するため、DBからは1ページあたりの表示件数より1件多く取得し、 実際に返す件数が上限を超えていた場合は表示件数分のみに切り詰める
   *
   * @param photoGetModel {@link PhotoGetModel}
   * @return {@link PhotoPageModel}
   */
  @Override
  public PhotoPageModel getPhotoList(PhotoGetModel photoGetModel) {
    List<PhotoDto> photoDtoList =
        photoDetailMapper.getPhotoList(PhotoListGetDto.from(photoGetModel));

    Boolean isLast = photoDtoList.size() < photoGetModel.getLimit();
    List<PhotoDto> pageDtoList =
        isLast ? photoDtoList : photoDtoList.subList(0, photoGetModel.getLimit() - 1);

    if (pageDtoList.isEmpty()) {
      return PhotoPageModel.of(PhotoModelList.empty(), isLast);
    }

    List<Long> photoNoList = pageDtoList.stream().map(PhotoDto::getPhotoNo).toList();

    List<PhotoTagMst> photoTagMstList =
        photoTagMstMapper.select(PhotoTagMstCondition.from(photoGetModel, photoNoList));

    return PhotoPageModel.of(
        PhotoModelList.of(
            pageDtoList.stream().map(dto -> toPhotoModel(dto, photoTagMstList)).toList()),
        isLast);
  }

  /**
   * PhotoDtoとタグエンティティリストからPhotoModelを組み立てる
   *
   * @param dto {@link PhotoDto}
   * @param photoTagMstList 全タグエンティティリスト（内部で該当写真のタグをフィルタリングする）
   * @return {@link PhotoModel}
   */
  private PhotoModel toPhotoModel(PhotoDto dto, List<PhotoTagMst> photoTagMstList) {
    AccountNo accountNo = new AccountNo(dto.getAccountNo());
    PhotoNo photoNo = new PhotoNo(dto.getPhotoNo());
    PhotoTagModelList photoTagModelList =
        toPhotoTagModelList(photoTagMstList).filterByPhoto(accountNo, photoNo);
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

  /**
   * 写真のメタデータを含めた詳細情報を取得する
   *
   * @param photoDetailSearchModel {@link PhotoDetailSearchModel}
   * @return {@link PhotoDetailModel}
   * @throws GalleryException 写真が存在しなかった場合
   */
  @Override
  public PhotoDetailModel getPhotoDetail(PhotoDetailSearchModel photoDetailSearchModel)
      throws GalleryException {
    PhotoDetailGetDto photoGetDto = PhotoDetailGetDto.from(photoDetailSearchModel);
    PhotoDetailDto photoDetailDto = photoDetailMapper.getPhotoDetail(photoGetDto);

    if (Objects.isNull(photoDetailDto)) {
      log.warn(
          "Photo not found. (AccountNo: {}, PhotoAccountNo: {}, PhotoNo: {})",
          photoGetDto.getAccountNo(),
          photoGetDto.getPhotoAccountNo(),
          photoGetDto.getPhotoNo());
      throw ErrorEnum.PHOTO_NOT_FOUND.toException();
    }

    List<PhotoTagMst> photoTagMstList =
        photoTagMstMapper.select(PhotoTagMstCondition.from(photoDetailSearchModel));

    return toPhotoDetailModel(photoDetailDto, photoTagMstList);
  }

  /**
   * PhotoDetailDtoとタグエンティティリストからPhotoDetailModelを組み立てる
   *
   * @param dto {@link PhotoDetailDto}
   * @param photoTagMstList 該当写真のタグエンティティリスト
   * @return {@link PhotoDetailModel}
   */
  private PhotoDetailModel toPhotoDetailModel(
      PhotoDetailDto dto, List<PhotoTagMst> photoTagMstList) {
    PhotoTagModelList photoTagModelList = toPhotoTagModelList(photoTagMstList);
    return PhotoDetailModel.builder()
        .accountNo(new AccountNo(dto.getAccountNo()))
        .photoNo(new PhotoNo(dto.getPhotoNo()))
        .isFavorite(new IsFavorite(dto.getIsFavorite()))
        .photoAt(
            dto.getPhotoAt().isEqual(Consts.MIN_OFFSET_DATE_TIME)
                ? null
                : new PhotoAt(dto.getPhotoAt().withOffsetSameInstant(Consts.JST)))
        .locationNo(dto.getLocationNo() != null ? new LocationNo(dto.getLocationNo()) : null)
        .geoLocation(
            new GeoLocation(
                dto.getAddress() != null ? new Address(dto.getAddress()) : null,
                dto.getLatitude() != null ? new Latitude(dto.getLatitude()) : null,
                dto.getLongitude() != null ? new Longitude(dto.getLongitude()) : null))
        .displayName(
            dto.getDisplayName() != null ? new LocationDisplayName(dto.getDisplayName()) : null)
        .isLocationPublic(
            dto.getIsLocationPublic() != null
                ? new IsLocationPublic(dto.getIsLocationPublic())
                : null)
        .imageFilePath(new ImageFilePath(dto.getImageFilePath()))
        .photoJapaneseTitle(
            dto.getPhotoJapaneseTitle() != null
                ? new PhotoJapaneseTitle(dto.getPhotoJapaneseTitle())
                : null)
        .photoEnglishTitle(
            dto.getPhotoEnglishTitle() != null
                ? new PhotoEnglishTitle(dto.getPhotoEnglishTitle())
                : null)
        .caption(dto.getCaption() != null ? new Caption(dto.getCaption()) : null)
        .directionKbn(dto.getDirectionKbn())
        .exifData(
            new ExifData(
                dto.getFocalLength() != null && dto.getFocalLength() != 0
                    ? new FocalLength(dto.getFocalLength())
                    : null,
                dto.getFValue() != null && dto.getFValue().compareTo(BigDecimal.ZERO) == 1
                    ? new FValue(dto.getFValue())
                    : null,
                dto.getShutterSpeed() != null
                        && dto.getShutterSpeed().compareTo(BigDecimal.ZERO) == 1
                    ? new ShutterSpeed(dto.getShutterSpeed())
                    : null,
                dto.getIso() != null && dto.getIso() != 0 ? new Iso(dto.getIso()) : null))
        .photoTagModelList(photoTagModelList)
        .build();
  }

  /**
   * PhotoTagMstエンティティのリストからPhotoTagModelListを組み立てる
   *
   * <p>タグ番号の昇順でソートする
   *
   * @param photoTagMstList {@link PhotoTagMst}のリスト
   * @return {@link PhotoTagModelList}
   */
  private PhotoTagModelList toPhotoTagModelList(List<PhotoTagMst> photoTagMstList) {
    return PhotoTagModelList.of(photoTagMstList.stream().map(this::toPhotoTagModel).toList())
        .sortByTagNo();
  }

  /**
   * PhotoTagMstエンティティからPhotoTagModelを組み立てる
   *
   * @param entity {@link PhotoTagMst}
   * @return {@link PhotoTagModel}
   */
  private PhotoTagModel toPhotoTagModel(PhotoTagMst entity) {
    return PhotoTagModel.builder()
        .accountNo(new AccountNo(entity.getAccountNo()))
        .photoNo(new PhotoNo(entity.getPhotoNo()))
        .tagNo(new TagNo(entity.getTagNo()))
        .tagJapaneseName(new TagJapaneseName(entity.getTagJapaneseName()))
        .tagEnglishName(new TagEnglishName(entity.getTagEnglishName()))
        .build();
  }
}
