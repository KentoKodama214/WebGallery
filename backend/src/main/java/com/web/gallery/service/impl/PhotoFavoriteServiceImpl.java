package com.web.gallery.service.impl;

import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.repository.PhotoDetailRepository;
import com.web.gallery.repository.PhotoFavoriteRepository;
import com.web.gallery.service.PhotoFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 写真お気に入りに関するビジネスロジックを行うServiceの実装クラス */
@Service
@RequiredArgsConstructor
public class PhotoFavoriteServiceImpl implements PhotoFavoriteService {

  private final PhotoFavoriteRepository photoFavoriteRepository;
  private final PhotoDetailRepository photoDetailRepository;

  /**
   * お気に入りを追加する
   *
   * <p>実在しない写真に対する登録は拒否する（自分自身の写真に対する登録は許可する）
   *
   * @param photoFavoriteModel {@link PhotoFavoriteModel}
   * @throws GalleryException 以下のいずれかに該当する場合 ・対象の写真が存在しない場合 ・登録に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void addFavorite(PhotoFavoriteModel photoFavoriteModel) throws GalleryException {
    // 対象写真の存在を検証する（存在しない場合はPHOTO_NOT_FOUNDが送出される）
    photoDetailRepository.getPhotoDetail(
        PhotoDetailSearchModel.builder()
            .photoAccountNo(photoFavoriteModel.getFavoritePhotoAccountNo())
            .photoNo(photoFavoriteModel.getFavoritePhotoNo())
            .build());

    photoFavoriteRepository.regist(photoFavoriteModel);
  }

  /**
   * お気に入りを解除する
   *
   * @param photoFavoriteModel {@link PhotoFavoriteModel}
   * @throws GalleryException 解除に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void deleteFavorite(PhotoFavoriteModel photoFavoriteModel) throws GalleryException {
    photoFavoriteRepository.delete(PhotoFavoriteDeleteModel.from(photoFavoriteModel));
  }
}
