package com.web.gallery.repository.impl;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.PhotoViewLog;
import com.web.gallery.entity.PhotoViewLogCondition;
import com.web.gallery.mapper.PhotoViewLogMapper;
import com.web.gallery.model.PhotoViewLogModel;
import com.web.gallery.repository.PhotoViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 写真詳細閲覧ログデータを永続化するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class PhotoViewLogRepositoryImpl implements PhotoViewLogRepository {
  private final PhotoViewLogMapper photoViewLogMapper;

  @Override
  public void save(PhotoViewLogModel photoViewLogModel) {
    PhotoViewLog photoViewLog = PhotoViewLog.from(photoViewLogModel);
    photoViewLogMapper.insert(photoViewLog);
  }

  @Override
  public void deleteByPhotoAccountNo(AccountNo photoAccountNo) {
    photoViewLogMapper.delete(PhotoViewLogCondition.byPhotoAccountNo(photoAccountNo.value()));
  }
}
