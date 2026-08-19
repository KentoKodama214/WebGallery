package com.web.gallery.service.impl;

import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModel;
import com.web.gallery.model.PhotoModelList;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.model.PhotoTagModelList;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.repository.PhotoDetailRepository;
import com.web.gallery.repository.PhotoFavoriteRepository;
import com.web.gallery.repository.PhotoMstRepository;
import com.web.gallery.repository.PhotoTagMstRepository;
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
	private final PhotoTagMstRepository photoTagMstRepository;
	private final PhotoFavoriteRepository photoFavoriteRepository;
	private final AccountRepository accountRepository;
	private final FileRepository fileRepository;
	private final PhotoConfig photoConfig;

	/**
	 * 写真一覧を取得する
	 *
	 * @param	photoListGetModel	{@link PhotoListGetModel}
	 * @return						{@link PhotoModelList}
	 */
	@Override
	@Transactional(readOnly = true)
	public PhotoModelList getPhotoList(PhotoListGetModel photoListGetModel) {
		AccountModel accountModel = accountRepository.getByAccountId(photoListGetModel.getPhotoAccountId());

		PhotoModelList photoModelList
			= photoDetailRepository.getPhotoList(
					PhotoGetModel.of(photoListGetModel.getAccountNo(), accountModel.getAccountNo()));

		return photoModelList
					.filterByDirectionKbn(photoListGetModel.getDirectionKbn())
					.filterByFavorite(photoListGetModel.getIsFavoriteOnly().value())
					.filterByTags(photoListGetModel.getTagList())
					.sorted(getComparator(photoListGetModel.getSortBy()));
	}

	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 *
	 * @param	photoDetailGetModel		{@link PhotoDetailGetModel}
	 * @return							{@link PhotoDetailModel}
	 * @throws	PhotoNotFoundException	写真が存在しなかった場合
	 */
	@Override
	@Transactional(readOnly = true)
	public PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws PhotoNotFoundException {
		return photoDetailRepository.getPhotoDetail(photoDetailGetModel);
	}

	/**
	 * 写真を登録・更新する
	 *
	 * @param	photoDetailModelList	{@link PhotoDetailModelList}
	 * @throws	FileDuplicateException 	同じファイル名のファイルが既に保存済みの場合
	 * @throws	RegistFailureException	登録に失敗した場合
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@Override
	@Transactional
	public PhotoNo savePhotos(AccountId accountId, PhotoDetailModelList photoDetailModelList) throws FileDuplicateException, RegistFailureException, UpdateFailureException {
		if(Objects.isNull(photoDetailModelList)) return null;
		if(photoDetailModelList.isEmpty()) return null;

		Long photoNo = photoMstRepository.getNewPhotoNo(photoDetailModelList.getFirst().getAccountNo()).value();
		PhotoNo savedPhotoNo = new PhotoNo(photoNo);
		String filePath = photoConfig.getOutputPath() + accountId.value() + "/";

		for(PhotoDetailModel photoDetailModel : photoDetailModelList){
			if(Objects.isNull(photoDetailModel.getPhotoNo())) {
				String filename = photoDetailModel.getImageFile().value().getOriginalFilename();
				if(photoMstRepository.isExistPhoto(photoDetailModel)) {
					log.warn("Duplicate image file (filename: {}}", filename);
					throw new FileDuplicateException(ErrorEnum.DUPLICATE_PHOTO_FILE);
				}

				photoMstRepository.regist(photoDetailModel, new ImageFilePath(filePath + filename), new PhotoNo(photoNo));
				registPhotoTags(photoDetailModel.getPhotoTagModelList(), photoNo++);
				uploadFile(new ImageFilePath(filePath + filename), photoDetailModel.getImageFile());
			} else {
				savedPhotoNo = photoDetailModel.getPhotoNo();
				photoMstRepository.update(photoDetailModel);
				deletePhotoTags(photoDetailModel.getAccountNo(), photoDetailModel.getPhotoNo());
				registPhotoTags(photoDetailModel.getPhotoTagModelList(), null);
			}
		}
		return savedPhotoNo;
	}

	/**
	 * 写真を削除する
	 *
	 * @param	accountId				アカウントID
	 * @param	photoDeleteModelList	{@link PhotoDeleteModelList}
	 * @throws	UpdateFailureException	削除に失敗した場合
	 */
	@Override
	@Transactional
	public void deletePhotos(AccountId accountId, PhotoDeleteModelList photoDeleteModelList) throws UpdateFailureException {
		String filePath = photoConfig.getOutputPath() + accountId.value() + "/";

		for(PhotoDeleteModel photoDeleteModel : photoDeleteModelList) {
			photoFavoriteRepository.clear(PhotoFavoriteDeleteModel.from(photoDeleteModel));
			deletePhotoTags(photoDeleteModel.getAccountNo(), photoDeleteModel.getPhotoNo());

			photoMstRepository.delete(photoDeleteModel);

			String fileName = new File(photoDeleteModel.getImageFilePath().value()).getName();
			fileRepository.delete(new ImageFilePath(filePath + fileName));
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

		switch(accountModel.getAuthorityKbn()) {
			case MINI:
				return count > (photoConfig.getMiniUserUpperLimit() - 1);
			case NORMAL:
				return count > (photoConfig.getNormalUserUpperLimit() - 1);
			case SPECIAL:
			case ADMINISTRATOR:
				return false;
			default:
				return true;
		}
	}

	/**
	 * 写真一覧の並び順のComparatorを取得する
	 *
	 * @param	sortBy	{@link SortPhotoEnum}
	 * @return			{@link PhotoModel}のComparator
	 */
	private Comparator<PhotoModel> getComparator(SortPhotoEnum sortBy) {
		switch(sortBy) {
			case PHOTO_AT:
				return Comparator.comparing(photoModel -> photoModel.getPhotoAt().value(), Comparator.reverseOrder());
			case FAVORITE:
				return Comparator.comparing((PhotoModel photoModel) -> photoModel.getFavoriteCount().value()).reversed();
			case SEASON:
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
			default:
				return Comparator.comparing(photoModel -> photoModel.getPhotoAt().value(), Comparator.reverseOrder());
		}
	}

	/**
	 * 写真タグを登録する
	 *
	 * @param	photoTagModelList		{@link PhotoTagModelList}
	 * @param	newPhotoNo				新規採番された写真番号
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	private void registPhotoTags(PhotoTagModelList photoTagModelList, Long newPhotoNo) throws RegistFailureException {
		if(Objects.isNull(photoTagModelList)) return;

		int tagNo = 1;
		for(PhotoTagModel photoTagModel : photoTagModelList) {
			PhotoTagModel photoTagRegistModel = PhotoTagModel.forRegist(
					photoTagModel,
					!Objects.isNull(newPhotoNo) ? new PhotoNo(newPhotoNo) : photoTagModel.getPhotoNo(),
					new TagNo((long) tagNo));
			photoTagMstRepository.regist(photoTagRegistModel);
			++tagNo;
		}
	}

	/**
	 * ファイルをアップロードする
	 *
	 * @param	filePath	アップロードのファイルパス
	 * @param	imageFile	アップロードするファイル
	 */
	private void uploadFile(ImageFilePath filePath, ImageFile imageFile) {
		fileRepository.save(FileModel.of(filePath, imageFile));
	}

	/**
	 * 写真タグを一括削除する
	 *
	 * @param	accountNo	削除する写真のアカウント番号
	 * @param	photoNo		削除する写真の写真番号
	 */
	private void deletePhotoTags(AccountNo accountNo, PhotoNo photoNo) {
		photoTagMstRepository.clear(PhotoTagDeleteModel.of(accountNo, photoNo));
	}
}
