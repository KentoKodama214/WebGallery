package com.web.gallery.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.entity.PhotoTagMstCondition;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;
import com.web.gallery.repository.PhotoTagMstRepository;

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
		PhotoTagMst photoTagMst = PhotoTagMst.from(photoTagModel);

		try {
			photoTagMstMapper.insert(photoTagMst);
		}
		catch (DuplicateKeyException e) {
			log.warn("PhotoTagMst: Duplicate Key (AccountNo: {}, PhototNo: {}, TagNo: {})",
					photoTagModel.getAccountNo().value(), photoTagModel.getPhotoNo().value(), photoTagModel.getTagNo().value(), e);
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
		PhotoTagMstCondition condition = PhotoTagMstCondition.from(photoTagDeleteModel);
		photoTagMstMapper.delete(condition);
	}

	/**
	 * アカウント番号で写真タグを全件削除する
	 *
	 * @param	accountNo	アカウント番号
	 */
	@Override
	public void deleteByAccountNo(AccountNo accountNo) {
		photoTagMstMapper.delete(PhotoTagMstCondition.byAccountNo(accountNo));
	}
}
