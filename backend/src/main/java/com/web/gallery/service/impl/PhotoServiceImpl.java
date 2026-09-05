package com.web.gallery.service.impl;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoCount;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.event.PhotoDeletedEvent;
import com.web.gallery.event.PhotoRegisteredEvent;
import com.web.gallery.event.PhotoUpdatedEvent;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModel;
import com.web.gallery.model.PhotoPageModel;
import com.web.gallery.model.PhotoSaveResultModel;
import com.web.gallery.policy.ImageFileValidationPolicy;
import com.web.gallery.policy.PhotoFileExtensionPolicy;
import com.web.gallery.policy.PhotoQuotaPolicy;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.repository.PhotoAggregateRepository;
import com.web.gallery.repository.PhotoDetailRepository;
import com.web.gallery.repository.PhotoMstRepository;
import com.web.gallery.service.PhotoService;
import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 写真に関するビジネスロジックを行うServiceの実装クラス */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

  private final PhotoDetailRepository photoDetailRepository;
  private final PhotoMstRepository photoMstRepository;
  private final PhotoAggregateRepository photoAggregateRepository;
  private final AccountRepository accountRepository;
  private final FileRepository fileRepository;
  private final PhotoConfig photoConfig;
  private final PhotoQuotaPolicy photoQuotaPolicy;
  private final ImageFileValidationPolicy imageFileValidationPolicy;
  private final PhotoFileExtensionPolicy photoFileExtensionPolicy;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * 写真一覧を、ページング情報に従い取得する
   *
   * <p>季節・時期順の場合、DB側でも近似的に月日順で並び替えた上でページング対象を絞り込んでいるが、 取得したページ内での厳密な並び順を保証するためアプリケーション層でも並び替えを行う
   *
   * @param photoListGetModel {@link PhotoListGetModel}
   * @return {@link PhotoPageModel}
   * @throws GalleryException 指定のアカウントが存在しなかった場合
   */
  @Override
  @Transactional(readOnly = true)
  public PhotoPageModel getPhotoList(PhotoListGetModel photoListGetModel) throws GalleryException {
    AccountModel accountModel =
        accountRepository.getByAccountId(photoListGetModel.getPhotoAccountId());
    if (Objects.isNull(accountModel)) {
      throw ErrorEnum.PHOTO_NOT_FOUND.toException();
    }

    PhotoPageModel photoPageModel =
        photoDetailRepository.getPhotoList(
            PhotoGetModel.of(
                photoListGetModel,
                accountModel.getAccountNo(),
                photoConfig.getPhotoCountPerPage()));

    if (SortPhotoEnum.SEASON.equals(photoListGetModel.getSortBy())) {
      return PhotoPageModel.of(
          photoPageModel.getPhotoModelList().sorted(getSeasonComparator()),
          photoPageModel.getIsLast());
    }
    return photoPageModel;
  }

  /**
   * 写真のメタデータを含めた詳細情報を取得する
   *
   * @param photoDetailGetModel {@link PhotoDetailGetModel}
   * @return {@link PhotoDetailModel}
   * @throws GalleryException 写真、または指定のアカウントが存在しなかった場合
   */
  @Override
  @Transactional(readOnly = true)
  public PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel)
      throws GalleryException {
    AccountModel accountModel =
        accountRepository.getByAccountId(photoDetailGetModel.getPhotoAccountId());
    if (Objects.isNull(accountModel)) {
      throw ErrorEnum.PHOTO_NOT_FOUND.toException();
    }

    return photoDetailRepository.getPhotoDetail(
        PhotoDetailSearchModel.of(photoDetailGetModel, accountModel.getAccountNo()));
  }

  /**
   * 写真を登録・更新する
   *
   * <p>新規登録分については、アカウント行のロックにより直列化したうえで登録枚数の上限を トランザクション内で再検証し、チェックと登録の間のレースによる上限バイパスを防ぐ
   *
   * @param photoDetailModelList {@link PhotoDetailModelList}
   * @throws GalleryException 以下のいずれかに該当する場合 ・新規登録時に画像ファイルが指定されていない場合 ・許可されていない拡張子のファイルの場合
   *     ・画像ファイルのContent-Typeが許可されていない場合 ・画像ファイルのマジックバイトが既知の画像フォーマットと一致しない場合 ・画像ファイルのサイズが上限を超えている場合
   *     ・同じファイル名のファイルが既に保存済みの場合 ・登録枚数の上限に達している場合 ・登録に失敗した場合 ・更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public PhotoSaveResultModel savePhotos(
      AccountId accountId, PhotoDetailModelList photoDetailModelList) throws GalleryException {
    if (Objects.isNull(photoDetailModelList)) return null;
    if (photoDetailModelList.isEmpty()) return null;

    AccountNo photoAccountNo = photoDetailModelList.getFirst().getAccountNo();
    accountRepository.lockForUpdate(photoAccountNo);

    Long photoNo = photoMstRepository.getNewPhotoNo(photoAccountNo).value();
    PhotoNo savedPhotoNo = new PhotoNo(photoNo);
    ImageFilePath savedImageFilePath = null;
    String filePath = photoConfig.getOutputPath() + accountId.value() + "/";

    // ファイルI/OはDBトランザクションの対象外のため、途中の登録失敗でDBがロールバックされても
    // 書き込み済みのファイルは自動的には戻らない。登録済みファイルを記録しておき、失敗時に補償削除する
    List<ImageFilePath> registeredImageFilePaths = new ArrayList<>();
    AccountModel accountModel = null;
    PhotoCount registeredCount = null;

    try {
      for (PhotoDetailModel photoDetailModel : photoDetailModelList) {
        if (Objects.isNull(photoDetailModel.getPhotoNo())) {
          if (Objects.isNull(accountModel)) {
            accountModel = accountRepository.getByAccountNo(photoAccountNo);
            registeredCount = new PhotoCount(photoMstRepository.count(photoAccountNo));
          }
          if (photoQuotaPolicy.isReached(accountModel.getAuthorityKbn(), registeredCount)) {
            throw ErrorEnum.REACHED_REGISTRATION_LIMIT.toException();
          }
          savedImageFilePath = registPhoto(photoDetailModel, new PhotoNo(photoNo), filePath);
          registeredImageFilePaths.add(savedImageFilePath);
          registeredCount = new PhotoCount(registeredCount.value() + 1);
          ++photoNo;
        } else {
          savedPhotoNo = photoDetailModel.getPhotoNo();
          updatePhoto(photoDetailModel);
        }
      }
    } catch (GalleryException e) {
      deleteOrphanedFiles(registeredImageFilePaths);
      throw e;
    } catch (RuntimeException e) {
      // GalleryException 以外（DBアクセスエラー等）でトランザクションがロールバックされる場合も、
      // 書き込み済みのファイルは自動的には戻らないため補償削除する
      deleteOrphanedFiles(registeredImageFilePaths);
      throw e;
    }
    return PhotoSaveResultModel.builder()
        .photoNo(savedPhotoNo)
        .imageFilePath(savedImageFilePath)
        .build();
  }

  /**
   * 写真を1件登録し、登録イベントを発行する
   *
   * @param photoDetailModel {@link PhotoDetailModel}
   * @param newPhotoNo 新規採番した写真番号
   * @param filePath 写真の保存先ディレクトリパス
   * @return 保存した画像ファイルパス
   * @throws GalleryException 以下のいずれかに該当する場合 ・画像ファイルが指定されていない場合 ・許可されていない拡張子のファイルの場合
   *     ・画像ファイルのContent-Typeが許可されていない場合 ・画像ファイルのマジックバイトが既知の画像フォーマットと一致しない場合 ・画像ファイルのサイズが上限を超えている場合
   *     ・同じファイル名のファイルが既に保存済みの場合 ・登録に失敗した場合
   */
  private ImageFilePath registPhoto(
      PhotoDetailModel photoDetailModel, PhotoNo newPhotoNo, String filePath)
      throws GalleryException {
    validateImageFile(photoDetailModel.getImageFile());

    if (!photoFileExtensionPolicy.isAllowedExtension(photoDetailModel.getImageFile())) {
      throw ErrorEnum.INVALID_PHOTO_FILE_EXTENSION.toException();
    }

    // クライアント送信値であるオリジナルファイル名からパストラバーサル対策としてベース名のみを抽出する
    // （Linux では '\\' がパス区切りとして扱われないため、先に '/' へ正規化してから抽出する）
    String filename =
        new File(photoDetailModel.getImageFile().value().getOriginalFilename().replace('\\', '/'))
            .getName();
    Photo photo =
        Photo.forRegist(photoDetailModel, newPhotoNo, new ImageFilePath(filePath + filename));
    photoAggregateRepository.regist(photo);
    fileRepository.save(FileModel.of(photo.getImageFilePath(), photo.getImageFile()));
    applicationEventPublisher.publishEvent(
        new PhotoRegisteredEvent(photo.getAccountNo(), photo.getPhotoNo()));
    return photo.getImageFilePath();
  }

  /**
   * 登録済みだがDBロールバック対象となった孤立ファイルを削除する
   *
   * <p>削除自体の失敗は元の例外の伝播を妨げないよう、ログ出力のみに留める
   *
   * @param imageFilePaths 削除対象の画像ファイルパスのリスト
   */
  private void deleteOrphanedFiles(List<ImageFilePath> imageFilePaths) {
    for (ImageFilePath imageFilePath : imageFilePaths) {
      try {
        fileRepository.delete(imageFilePath);
      } catch (RuntimeException e) {
        log.warn(
            "Failed to delete orphaned file after registration rollback. (imageFilePath: {})",
            imageFilePath.value(),
            e);
      }
    }
  }

  /**
   * 画像ファイルの物理削除をトランザクションのコミット後に遅延実行する
   *
   * <p>トランザクション内で先にファイルを消すと、後続処理の失敗でDBがロールバックされたときに 「レコードはあるが実体ファイルが無い」不整合が残る。コミット確定後に削除することでこれを防ぐ。
   * 削除自体の失敗はログ出力に留め、削除漏れは後続のクリーンアップに委ねる。 トランザクションが無い場合（単体実行等）は即時削除する
   *
   * @param imageFilePaths 削除対象の画像ファイルパスのリスト
   */
  private void deleteFilesAfterCommit(List<ImageFilePath> imageFilePaths) {
    if (imageFilePaths.isEmpty()) {
      return;
    }
    List<ImageFilePath> targets = List.copyOf(imageFilePaths);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              deleteFilesQuietly(targets);
            }
          });
    } else {
      deleteFilesQuietly(targets);
    }
  }

  /**
   * 画像ファイルを削除する。失敗しても例外を伝播させずログ出力に留める
   *
   * @param imageFilePaths 削除対象の画像ファイルパスのリスト
   */
  private void deleteFilesQuietly(List<ImageFilePath> imageFilePaths) {
    for (ImageFilePath imageFilePath : imageFilePaths) {
      try {
        fileRepository.delete(imageFilePath);
      } catch (RuntimeException e) {
        log.warn(
            "Failed to delete image file after commit. (imageFilePath: {})",
            imageFilePath.value(),
            e);
      }
    }
  }

  /**
   * 新規登録時の画像ファイルの実効性を検証する（Content-Type・マジックバイト・サイズ）
   *
   * <p>新規登録時は画像ファイルの指定が必須であり、拡張子やContent-Typeの偽装を見破るため実際のバイナリ内容も検証する
   *
   * @param imageFile {@link ImageFile}
   * @throws GalleryException 以下のいずれかに該当する場合 ・画像ファイルが指定されていない場合 ・画像ファイルのContent-Typeが許可されていない場合
   *     ・画像ファイルのマジックバイトが既知の画像フォーマットと一致しない場合 ・画像ファイルのサイズが上限を超えている場合
   */
  private void validateImageFile(ImageFile imageFile) throws GalleryException {
    if (Objects.isNull(imageFile)) {
      throw ErrorEnum.IMAGE_FILE_REQUIRED.toException();
    }
    // サイズ超過はバイナリ内容を読む前に弾く（大きなファイルの読み込みコストを避ける）
    if (imageFileValidationPolicy.isSizeExceeded(imageFile)) {
      throw ErrorEnum.IMAGE_FILE_SIZE_EXCEEDED.toException();
    }
    if (!imageFileValidationPolicy.isAllowedContentType(imageFile)) {
      throw ErrorEnum.UNSUPPORTED_IMAGE_CONTENT_TYPE.toException();
    }
    if (!imageFileValidationPolicy.isValidSignature(imageFile)) {
      throw ErrorEnum.INVALID_IMAGE_SIGNATURE.toException();
    }
  }

  /**
   * 写真を1件更新し、更新イベントを発行する
   *
   * <p>画像ファイルパスはリクエスト値を信用せず、DB上の既存値をそのまま引き継ぐ（クライアント入力によるファイルパス汚染を防ぐため）
   *
   * @param photoDetailModel {@link PhotoDetailModel}
   * @throws GalleryException 更新に失敗した場合
   */
  private void updatePhoto(PhotoDetailModel photoDetailModel) throws GalleryException {
    PhotoDetailModel existing =
        photoDetailRepository.getPhotoDetail(
            PhotoDetailSearchModel.builder()
                .photoAccountNo(photoDetailModel.getAccountNo())
                .photoNo(photoDetailModel.getPhotoNo())
                .build());
    Photo photo =
        Photo.forUpdate(
            photoDetailModel.toBuilder().imageFilePath(existing.getImageFilePath()).build());
    photoAggregateRepository.update(photo);
    applicationEventPublisher.publishEvent(
        new PhotoUpdatedEvent(photo.getAccountNo(), photo.getPhotoNo()));
  }

  /**
   * 写真を削除する
   *
   * @param accountId アカウントID
   * @param photoDeleteModelList {@link PhotoDeleteModelList}
   * @throws GalleryException 削除に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void deletePhotos(AccountId accountId, PhotoDeleteModelList photoDeleteModelList)
      throws GalleryException {
    String filePath = photoConfig.getOutputPath() + accountId.value() + "/";

    List<ImageFilePath> deletedImageFilePaths = new ArrayList<>();
    for (PhotoDeleteModel photoDeleteModel : photoDeleteModelList) {
      // クライアント送信値のファイルパスからベース名のみを抽出する（'\\' を '/' へ正規化してから）
      String fileName =
          new File(photoDeleteModel.getImageFilePath().value().replace('\\', '/')).getName();
      ImageFilePath imageFilePathForDelete = new ImageFilePath(filePath + fileName);
      Photo photo =
          Photo.forDelete(
              photoDeleteModel.getAccountNo(),
              photoDeleteModel.getPhotoNo(),
              imageFilePathForDelete);
      photoAggregateRepository.delete(photo);
      deletedImageFilePaths.add(imageFilePathForDelete);
      applicationEventPublisher.publishEvent(
          new PhotoDeletedEvent(photo.getAccountNo(), photo.getPhotoNo()));
    }

    // ファイルの物理削除はDBコミット確定後に行う（ロールバック時の不整合を防ぐ）
    deleteFilesAfterCommit(deletedImageFilePaths);
  }

  /**
   * 該当アカウントが写真の登録枚数の上限に達しているかチェックする
   *
   * @param accountNo アカウント番号
   * @return 上限に達している場合、true
   */
  @Override
  @Transactional(readOnly = true)
  public Boolean isReachedUpperLimit(AccountNo accountNo) {
    AccountModel accountModel = accountRepository.getByAccountNo(accountNo);
    Integer count = photoMstRepository.count(accountNo);

    return photoQuotaPolicy.isReached(accountModel.getAuthorityKbn(), new PhotoCount(count));
  }

  /**
   * 写真一覧の季節・時期順のComparatorを取得する
   *
   * <p>月日ベースの近似ソートのため、SQLのORDER BYではなくアプリケーション層で計算する
   *
   * @return {@link PhotoModel}のComparator
   */
  private Comparator<PhotoModel> getSeasonComparator() {
    return new Comparator<PhotoModel>() {
      @Override
      public int compare(PhotoModel photoModelA, PhotoModel photoModelB) {
        OffsetDateTime photoAtA =
            photoModelA.getPhotoAt().value().withOffsetSameInstant(Consts.JST);
        OffsetDateTime photoAtB =
            photoModelB.getPhotoAt().value().withOffsetSameInstant(Consts.JST);

        LocalDate dateA =
            LocalDate.of(2000, photoAtA.getMonth().getValue(), photoAtA.getDayOfMonth());
        LocalDate dateB =
            LocalDate.of(2000, photoAtB.getMonth().getValue(), photoAtB.getDayOfMonth());

        return (int) ChronoUnit.DAYS.between(dateA, dateB);
      }
    };
  }
}
