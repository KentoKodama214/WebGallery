package com.web.gallery.repository.impl;

import org.springframework.stereotype.Repository;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.FileModel;
import com.web.gallery.model.PhotoDeleteModel;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.repository.PhotoAggregateRepository;
import com.web.gallery.repository.PhotoFavoriteRepository;
import com.web.gallery.repository.PhotoMstRepository;
import com.web.gallery.repository.PhotoTagMstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真集約（{@link Photo}）を永続化するRepositoryの実装クラス<p>
 * 単票のPhotoMstRepository・PhotoTagMstRepository・PhotoFavoriteRepository・FileRepositoryを
 * 合成し、写真の登録・更新・削除というユースケース単位で整合性のある永続化を行う
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoAggregateRepositoryImpl implements PhotoAggregateRepository {

	private final PhotoMstRepository photoMstRepository;
	private final PhotoTagMstRepository photoTagMstRepository;
	private final PhotoFavoriteRepository photoFavoriteRepository;
	private final FileRepository fileRepository;

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
		if (photoMstRepository.isExistPhoto(photo.getDetail())) {
			log.warn("Duplicate image file (filename: {})", filename);
			throw ErrorEnum.DUPLICATE_PHOTO_FILE.toException();
		}

		photoMstRepository.regist(photo.getDetail(), photo.getImageFilePath(), photo.getPhotoNo());
		registTags(photo);
		fileRepository.save(FileModel.of(photo.getImageFilePath(), photo.getImageFile()));
	}

	/**
	 * 写真集約を更新する
	 *
	 * @param	photo				{@link Photo}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	public void update(Photo photo) throws GalleryException {
		photoMstRepository.update(photo.getDetail());
		photoTagMstRepository.clear(PhotoTagDeleteModel.of(photo.getAccountNo(), photo.getPhotoNo()));
		registTags(photo);
	}

	/**
	 * 写真集約を削除する<p>
	 * 対象写真への全アカウントからのお気に入り・タグを削除したうえで写真マスタを論理削除し、実ファイルを削除する
	 *
	 * @param	photo				{@link Photo}
	 * @throws	GalleryException	削除に失敗した場合
	 */
	@Override
	public void delete(Photo photo) throws GalleryException {
		photoFavoriteRepository.clear(PhotoFavoriteDeleteModel.builder()
				.favoritePhotoAccountNo(photo.getAccountNo())
				.favoritePhotoNo(photo.getPhotoNo())
				.build());
		photoTagMstRepository.clear(PhotoTagDeleteModel.of(photo.getAccountNo(), photo.getPhotoNo()));

		photoMstRepository.delete(PhotoDeleteModel.builder()
				.accountNo(photo.getAccountNo())
				.photoNo(photo.getPhotoNo())
				.imageFilePath(photo.getImageFilePathForDelete())
				.build());

		fileRepository.delete(photo.getImageFilePathForDelete());
	}

	/**
	 * 写真タグを登録する
	 *
	 * @param	photo				{@link Photo}
	 * @throws	GalleryException	登録に失敗した場合
	 */
	private void registTags(Photo photo) throws GalleryException {
		if (photo.getPhotoTagModelList() == null) {
			return;
		}

		for (PhotoTagModel photoTagModel : photo.getPhotoTagModelList()) {
			photoTagMstRepository.regist(photoTagModel);
		}
	}
}
