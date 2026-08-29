package com.web.gallery.repository.impl;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.entity.PhotoFavoriteCondition;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoMstCondition;
import com.web.gallery.entity.PhotoMstUpdateTarget;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.repository.PhotoAggregateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真集約（{@link Photo}）を永続化するRepositoryの実装クラス<p>
 * PhotoMst・PhotoTagMst・PhotoFavoriteの3テーブルへの永続化を、写真の登録・更新・削除という
 * ユースケース単位で整合性のある1操作としてまとめる。他のRepositoryには依存せず、Mapperを直接操作する
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoAggregateRepositoryImpl implements PhotoAggregateRepository {

	private final PhotoMstMapper photoMstMapper;
	private final PhotoTagMstMapper photoTagMstMapper;
	private final PhotoFavoriteMapper photoFavoriteMapper;

	/**
	 * 写真集約を新規登録する
	 *
	 * @param	photo				{@link Photo}
	 * @throws	GalleryException	以下のいずれかに該当する場合
	 *                              ・同じファイル名の写真が既に保存済みの場合
	 *                              ・登録に失敗した場合
	 */
	@Override
	public void regist(Photo photo) throws GalleryException {
		String filename = photo.getImageFile().value().getOriginalFilename();
		if (photoMstMapper.isExistPhoto(PhotoMstCondition.forExistCheck(photo.getDetail()))) {
			log.warn("Duplicate image file (filename: {})", filename);
			throw ErrorEnum.DUPLICATE_PHOTO_FILE.toException();
		}

		PhotoMst photoMst = PhotoMst.fromForRegist(photo.getDetail(), photo.getImageFilePath().value(), photo.getPhotoNo().value());
		try {
			photoMstMapper.insert(photoMst);
		}
		catch (DuplicateKeyException e) {
			log.warn("PhotoMst: Duplicate Key (AccountNo: {}, PhotoNo: {})", photo.getAccountNo().value(), photo.getPhotoNo().value(), e);
			throw ErrorEnum.FAIL_TO_REGIST_PHOTO.toException();
		}

		registTags(photo);
	}

	/**
	 * 写真集約を更新する
	 *
	 * @param	photo				{@link Photo}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	public void update(Photo photo) throws GalleryException {
		PhotoMstCondition condition = PhotoMstCondition.byAccountAndPhotoNotDeleted(photo.getAccountNo().value(), photo.getPhotoNo().value());
		PhotoMstUpdateTarget target = PhotoMstUpdateTarget.fromForUpdate(photo.getDetail());

		if (photoMstMapper.update(condition, target) < 1) {
			log.warn("PhotoMst: Update Failed (AccountNo: {}, PhotoNo: {})", photo.getAccountNo().value(), photo.getPhotoNo().value());
			throw ErrorEnum.FAIL_TO_UPDATE_PHOTO.toException();
		}

		photoTagMstMapper.delete(PhotoTagMstCondition.from(PhotoTagDeleteModel.of(photo.getAccountNo(), photo.getPhotoNo())));
		registTags(photo);
	}

	/**
	 * 写真集約を削除する<p>
	 * 対象写真への全アカウントからのお気に入り・タグを削除したうえで写真マスタを論理削除する
	 *
	 * @param	photo				{@link Photo}
	 * @throws	GalleryException	削除に失敗した場合
	 */
	@Override
	public void delete(Photo photo) throws GalleryException {
		PhotoFavoriteDeleteModel favoriteDeleteModel = PhotoFavoriteDeleteModel.builder()
				.favoritePhotoAccountNo(photo.getAccountNo())
				.favoritePhotoNo(photo.getPhotoNo())
				.build();
		photoFavoriteMapper.delete(PhotoFavoriteCondition.forClear(favoriteDeleteModel));

		photoTagMstMapper.delete(PhotoTagMstCondition.from(PhotoTagDeleteModel.of(photo.getAccountNo(), photo.getPhotoNo())));

		PhotoMstCondition condition = PhotoMstCondition.byAccountAndPhotoNotDeleted(photo.getAccountNo().value(), photo.getPhotoNo().value());
		PhotoDeleteModel photoDeleteModel = PhotoDeleteModel.builder()
				.accountNo(photo.getAccountNo())
				.photoNo(photo.getPhotoNo())
				.imageFilePath(photo.getImageFilePathForDelete())
				.build();
		PhotoMstUpdateTarget target = PhotoMstUpdateTarget.forDelete(photoDeleteModel);

		if (photoMstMapper.update(condition, target) < 1) {
			log.warn("PhotoMst: Delete Failed (AccountNo: {}, PhotoNo: {})", photo.getAccountNo().value(), photo.getPhotoNo().value());
			throw ErrorEnum.FAIL_TO_DELETE_PHOTO.toException();
		}
	}

	/**
	 * 写真タグを登録する
	 *
	 * @param	photo				{@link Photo}
	 * @throws	GalleryException	登録に失敗した場合
	 */
	private void registTags(Photo photo) throws GalleryException {
		if (photo.getPhotoTagModelList() == null || photo.getPhotoTagModelList().isEmpty()) {
			return;
		}

		List<PhotoTagMst> photoTagMstList = photo.getPhotoTagModelList().stream()
				.map(PhotoTagMst::from)
				.toList();
		try {
			photoTagMstMapper.insertBulk(photoTagMstList);
		}
		catch (DuplicateKeyException e) {
			log.warn("PhotoTagMst: Duplicate Key (AccountNo: {}, PhotoNo: {})", photo.getAccountNo().value(), photo.getPhotoNo().value(), e);
			throw ErrorEnum.FAIL_TO_REGIST_PHOTO_TAG.toException();
		}
	}
}
