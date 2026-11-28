package com.web.gallery.infrastructure.persistence.repository.photo;

import com.web.gallery.application.model.photo.PhotoTagDeleteModel;
import com.web.gallery.application.model.photo.PhotoTagModel;
import com.web.gallery.application.repository.photo.PhotoTagMstRepository;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoTagMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.photo.PhotoTagMstMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/** 写真タグマスタデータを永続化するRepositoryの実装クラス */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoTagMstRepositoryImpl implements PhotoTagMstRepository {

  private final PhotoTagMstMapper photoTagMstMapper;

  /**
   * 写真タグマスタを登録する
   *
   * @param photoTagModel {@link PhotoTagModel}
   * @throws GalleryException 登録に失敗した場合
   */
  @Override
  public void regist(PhotoTagModel photoTagModel) throws GalleryException {
    PhotoTagMst photoTagMst = PhotoTagMst.from(photoTagModel);

    try {
      photoTagMstMapper.insert(photoTagMst);
    } catch (DuplicateKeyException e) {
      log.warn(
          "PhotoTagMst: Duplicate Key (AccountNo: {}, PhototNo: {}, TagNo: {})",
          photoTagModel.getAccountNo().value(),
          photoTagModel.getPhotoNo().value(),
          photoTagModel.getTagNo().value(),
          e);
      throw ErrorEnum.FAIL_TO_REGIST_PHOTO_TAG.toException();
    }
  }

  /**
   * 該当写真の写真タグを全件削除する
   *
   * @param photoTagDeleteModel {@link PhotoTagDeleteModel}
   */
  @Override
  public void clear(PhotoTagDeleteModel photoTagDeleteModel) {
    PhotoTagMstCondition condition = PhotoTagMstCondition.from(photoTagDeleteModel);
    photoTagMstMapper.delete(condition);
  }

  /**
   * アカウント番号で写真タグを全件削除する
   *
   * @param accountNo アカウント番号
   */
  @Override
  public void deleteByAccountNo(AccountNo accountNo) {
    photoTagMstMapper.delete(PhotoTagMstCondition.byAccountNo(accountNo.value()));
  }
}
