package com.web.gallery.service.impl;

import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.web.gallery.aggregate.Photo;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoCount;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.event.PhotoDeletedEvent;
import com.web.gallery.event.PhotoRegisteredEvent;
import com.web.gallery.event.PhotoUpdatedEvent;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
import com.web.gallery.policy.PhotoQuotaPolicy;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.repository.PhotoAggregateRepository;
import com.web.gallery.repository.PhotoDetailRepository;
import com.web.gallery.repository.PhotoMstRepository;
import com.web.gallery.service.PhotoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真に関するビジネスロジックを行うServiceの実装クラス
 */
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
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 写真一覧を取得する
	 *
	 * @param	photoListGetModel	{@link PhotoListGetModel}
	 * @return						{@link PhotoModelList}
	 * @throws	GalleryException	指定のアカウントが存在しなかった場合
	 */
	@Override
	@Transactional(readOnly = true)
	public PhotoModelList getPhotoList(PhotoListGetModel photoListGetModel) throws GalleryException {
		AccountModel accountModel = accountRepository.getByAccountId(photoListGetModel.getPhotoAccountId());
		if (Objects.isNull(accountModel)) {
			throw ErrorEnum.PHOTO_NOT_FOUND.toException();
		}

		PhotoModelList photoModelList
			= photoDetailRepository.getPhotoList(
					PhotoGetModel.of(photoListGetModel, accountModel.getAccountNo()));

		if(SortPhotoEnum.SEASON.equals(photoListGetModel.getSortBy())) {
			return photoModelList.sorted(getSeasonComparator());
		}
		return photoModelList;
	}

	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 *
	 * @param	photoDetailGetModel	{@link PhotoDetailGetModel}
	 * @return						{@link PhotoDetailModel}
	 * @throws	GalleryException	写真、または指定のアカウントが存在しなかった場合
	 */
	@Override
	@Transactional(readOnly = true)
	public PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws GalleryException {
		AccountModel accountModel = accountRepository.getByAccountId(photoDetailGetModel.getPhotoAccountId());
		if (Objects.isNull(accountModel)) {
			throw ErrorEnum.PHOTO_NOT_FOUND.toException();
		}

		return photoDetailRepository.getPhotoDetail(
				PhotoDetailSearchModel.of(photoDetailGetModel, accountModel.getAccountNo()));
	}

	/**
	 * 写真を登録・更新する
	 *
	 * @param	photoDetailModelList	{@link PhotoDetailModelList}
	 * @throws	GalleryException		以下のいずれかに該当する場合
	 *                              	・同じファイル名のファイルが既に保存済みの場合
	 *                              	・登録に失敗した場合
	 *                              	・更新に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public PhotoNo savePhotos(AccountId accountId, PhotoDetailModelList photoDetailModelList) throws GalleryException {
		if(Objects.isNull(photoDetailModelList)) return null;
		if(photoDetailModelList.isEmpty()) return null;

		AccountNo photoAccountNo = photoDetailModelList.getFirst().getAccountNo();
		accountRepository.lockForUpdate(photoAccountNo);

		Long photoNo = photoMstRepository.getNewPhotoNo(photoAccountNo).value();
		PhotoNo savedPhotoNo = new PhotoNo(photoNo);
		String filePath = photoConfig.getOutputPath() + accountId.value() + "/";

		for(PhotoDetailModel photoDetailModel : photoDetailModelList){
			if(Objects.isNull(photoDetailModel.getPhotoNo())) {
				registPhoto(photoDetailModel, new PhotoNo(photoNo), filePath);
				++photoNo;
			} else {
				savedPhotoNo = photoDetailModel.getPhotoNo();
				updatePhoto(photoDetailModel);
			}
		}
		return savedPhotoNo;
	}

	/**
	 * 写真を1件登録し、登録イベントを発行する
	 *
	 * @param	photoDetailModel	{@link PhotoDetailModel}
	 * @param	newPhotoNo			新規採番した写真番号
	 * @param	filePath			写真の保存先ディレクトリパス
	 * @throws	GalleryException	以下のいずれかに該当する場合
	 *                              	・同じファイル名のファイルが既に保存済みの場合
	 *                              	・登録に失敗した場合
	 */
	private void registPhoto(PhotoDetailModel photoDetailModel, PhotoNo newPhotoNo, String filePath) throws GalleryException {
		String filename = photoDetailModel.getImageFile().value().getOriginalFilename();
		Photo photo = Photo.forRegist(photoDetailModel, newPhotoNo, new ImageFilePath(filePath + filename));
		photoAggregateRepository.regist(photo);
		fileRepository.save(FileModel.of(photo.getImageFilePath(), photo.getImageFile()));
		applicationEventPublisher.publishEvent(new PhotoRegisteredEvent(photo.getAccountNo(), photo.getPhotoNo()));
	}

	/**
	 * 写真を1件更新し、更新イベントを発行する
	 *
	 * @param	photoDetailModel	{@link PhotoDetailModel}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	private void updatePhoto(PhotoDetailModel photoDetailModel) throws GalleryException {
		Photo photo = Photo.forUpdate(photoDetailModel);
		photoAggregateRepository.update(photo);
		applicationEventPublisher.publishEvent(new PhotoUpdatedEvent(photo.getAccountNo(), photo.getPhotoNo()));
	}

	/**
	 * 写真を削除する
	 *
	 * @param	accountId				アカウントID
	 * @param	photoDeleteModelList	{@link PhotoDeleteModelList}
	 * @throws	GalleryException		削除に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public void deletePhotos(AccountId accountId, PhotoDeleteModelList photoDeleteModelList) throws GalleryException {
		String filePath = photoConfig.getOutputPath() + accountId.value() + "/";

		for(PhotoDeleteModel photoDeleteModel : photoDeleteModelList) {
			String fileName = new File(photoDeleteModel.getImageFilePath().value()).getName();
			ImageFilePath imageFilePathForDelete = new ImageFilePath(filePath + fileName);
			Photo photo = Photo.forDelete(photoDeleteModel.getAccountNo(), photoDeleteModel.getPhotoNo(), imageFilePathForDelete);
			photoAggregateRepository.delete(photo);
			fileRepository.delete(imageFilePathForDelete);
			applicationEventPublisher.publishEvent(new PhotoDeletedEvent(photo.getAccountNo(), photo.getPhotoNo()));
		}
	}

	/**
	 * 該当アカウントが写真の登録枚数の上限に達しているかチェックする
	 *
	 * @param	accountNo	アカウント番号
	 * @return				上限に達している場合、true
	 */
	@Override
	@Transactional(readOnly = true)
	public Boolean isReachedUpperLimit(AccountNo accountNo) {
		AccountModel accountModel = accountRepository.getByAccountNo(accountNo);
		Integer count = photoMstRepository.count(accountNo);

		return photoQuotaPolicy.isReached(accountModel.getAuthorityKbn(), new PhotoCount(count));
	}

	/**
	 * 写真一覧の季節・時期順のComparatorを取得する<p>
	 * 月日ベースの近似ソートのため、SQLのORDER BYではなくアプリケーション層で計算する
	 *
	 * @return	{@link PhotoModel}のComparator
	 */
	private Comparator<PhotoModel> getSeasonComparator() {
		return new Comparator<PhotoModel>() {
			@Override
			public int compare(PhotoModel photoModelA, PhotoModel photoModelB) {
				OffsetDateTime photoAtA = photoModelA.getPhotoAt().value().withOffsetSameInstant(Consts.JST);
				OffsetDateTime photoAtB = photoModelB.getPhotoAt().value().withOffsetSameInstant(Consts.JST);

				LocalDate dateA = LocalDate.of(2000, photoAtA.getMonth().getValue(), photoAtA.getDayOfMonth());
				LocalDate dateB = LocalDate.of(2000, photoAtB.getMonth().getValue(), photoAtB.getDayOfMonth());

				return (int) ChronoUnit.DAYS.between(dateA, dateB);
			}
		};
	}
}
