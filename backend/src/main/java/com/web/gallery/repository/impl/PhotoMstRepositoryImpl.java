package com.web.gallery.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoMstCondition;
import com.web.gallery.entity.PhotoMstUpdateTarget;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.repository.PhotoMstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真マスタデータを永続化するRepositoryの実装クラス
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoMstRepositoryImpl implements PhotoMstRepository {

	private final PhotoMstMapper photoMstMapper;

	/**
	 * 写真マスタを登録する
	 *
	 * @param	photoDetailModel		{@link PhotoDetailModel}
	 * @param	filePath				写真の保存ファイルパス
	 * @param	newPhotoNo			新規採番した写真番号
	 * @throws	GalleryException	登録に失敗した場合
	 */
	@Override
	public void regist(PhotoDetailModel photoDetailModel, ImageFilePath filePath, PhotoNo newPhotoNo) throws GalleryException {
		PhotoMst photoMst = PhotoMst.fromForRegist(photoDetailModel, filePath.value(), newPhotoNo.value());

		try {
			photoMstMapper.insert(photoMst);
		}
		catch (DuplicateKeyException e) {
			log.warn("PhotoMst: Duplicate Key (AccountNo: {}, PhotoNo: {})", photoDetailModel.getAccountNo().value(), newPhotoNo.value(), e);
			throw ErrorEnum.FAIL_TO_REGIST_PHOTO.toException();
		}
	}

	/**
	 * 写真マスタを更新する
	 *
	 * @param	photoDetailModel	{@link PhotoDetailModel}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	public void update(PhotoDetailModel photoDetailModel) throws GalleryException {
		PhotoMstCondition condition = PhotoMstCondition.byAccountAndPhoto(photoDetailModel.getAccountNo().value(), photoDetailModel.getPhotoNo().value());
		PhotoMstUpdateTarget target = PhotoMstUpdateTarget.fromForUpdate(photoDetailModel);

		if (photoMstMapper.update(condition, target) < 1) {
			log.warn("PhotoMst: Update Failed (AccountNo: {}, PhotoNo: {})", photoDetailModel.getAccountNo().value(), photoDetailModel.getPhotoNo().value());
			throw ErrorEnum.FAIL_TO_UPDATE_PHOTO.toException();
		}
	}

	/**
	 * 写真マスタを削除する
	 *
	 * @param	photoDeleteModel	{@link PhotoDeleteModel}
	 * @throws	GalleryException	削除に失敗した場合
	 */
	@Override
	public void delete(PhotoDeleteModel photoDeleteModel) throws GalleryException {
		PhotoMstCondition condition = PhotoMstCondition.byAccountAndPhoto(photoDeleteModel.getAccountNo().value(), photoDeleteModel.getPhotoNo().value());
		PhotoMstUpdateTarget target = PhotoMstUpdateTarget.forDelete(photoDeleteModel);

		if (photoMstMapper.update(condition, target) < 1) {
			log.warn("PhotoMst: Delete Failed (AccountNo: {}, PhotoNo: {})", photoDeleteModel.getAccountNo().value(), photoDeleteModel.getPhotoNo().value());
			throw ErrorEnum.FAIL_TO_DELETE_PHOTO.toException();
		}
	}

	/**
	 * アカウント番号から新しい写真番号を発番する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				新規採番した写真番号
	 */
	@Override
	public PhotoNo getNewPhotoNo(AccountNo accountNo) {
		Long maxPhotoNo = photoMstMapper.getMaxPhotoNo(accountNo.value());
		return PhotoNo.next(maxPhotoNo);
	}

	/**
	 * 同じファイル名の写真が存在するかチェックする
	 *
	 * @param	photoDetailModel	{@link PhotoDetailModel}
	 * @return						写真が存在する場合、true
	 */
	@Override
	public Boolean isExistPhoto(PhotoDetailModel photoDetailModel) {
		return photoMstMapper.isExistPhoto(PhotoMstCondition.forExistCheck(photoDetailModel));
	}

	/**
	 * アカウントに登録されている写真の件数を取得する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				登録件数
	 */
	@Override
	public Integer count(AccountNo accountNo) {
		return photoMstMapper.count(PhotoMstCondition.forCount(accountNo.value()));
	}

	/**
	 * アカウント番号で写真マスタを物理削除する
	 *
	 * @param	accountNo	アカウント番号
	 */
	@Override
	public void deleteByAccountNo(AccountNo accountNo) {
		photoMstMapper.delete(PhotoMstCondition.byAccountNo(accountNo));
	}
}
