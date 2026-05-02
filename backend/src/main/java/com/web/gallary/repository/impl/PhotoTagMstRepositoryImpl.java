package com.web.gallary.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.web.gallary.entity.PhotoTagMst;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.mapper.PhotoTagMstMapper;
import com.web.gallary.model.PhotoTagDeleteModel;
import com.web.gallary.model.PhotoTagModel;
import com.web.gallary.repository.PhotoTagMstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真タグマスタデータを永続化するRepositoryの実装クラス
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoTagMstRepositoryImpl implements PhotoTagMstRepository {
	
	private final PhotoTagMstMapper photoTagMstMapper;
	
	/**
	 * 写真タグマスタを登録する
	 * 
	 * @param	photoTagModel			{@link PhotoTagModel}
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	@Override
	public void regist(PhotoTagModel photoTagModel) throws RegistFailureException {
		PhotoTagMst photoTagMst = PhotoTagMst.builder()
				.accountNo(photoTagModel.getAccountNo())
				.photoNo(photoTagModel.getPhotoNo())
				.tagNo(photoTagModel.getTagNo())
				.createdBy(photoTagModel.getAccountNo())
				.tagJapaneseName(photoTagModel.getTagJapaneseName())
				.tagEnglishName(photoTagModel.getTagEnglishName())
				.build();
		
		try {
			photoTagMstMapper.insert(photoTagMst);
		}
		catch (DuplicateKeyException e) {
			log.warn("PhotoTagMst: Duplicate Key (AccountNo: {}, PhototNo: {}, TagNo: {})",
					photoTagModel.getAccountNo(), photoTagModel.getPhotoNo(), photoTagModel.getTagNo(), e);
			throw new RegistFailureException(ErrorEnum.FAIL_TO_REGIST_PHOTO_TAG);
		}
	}
	
	/**
	 * 該当写真の写真タグを全件削除する
	 * 
	 * @param	photoTagDeleteModel	{@link PhotoTagDeleteModel}
	 */
	@Override
	public void clear(PhotoTagDeleteModel photoTagDeleteModel) {
		PhotoTagMst photoTagMst = PhotoTagMst.builder()
				.accountNo(photoTagDeleteModel.getAccountNo())
				.photoNo(photoTagDeleteModel.getPhotoNo())
				.build();
		
		photoTagMstMapper.delete(photoTagMst);
	}
}