package com.web.gallery.repository.impl;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.PhotoListFilterLog;
import com.web.gallery.entity.PhotoListFilterLogCondition;
import com.web.gallery.mapper.PhotoListFilterLogMapper;
import com.web.gallery.model.PhotoListFilterLogModel;
import com.web.gallery.repository.PhotoListFilterLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 写真一覧絞り込みログデータを永続化するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class PhotoListFilterLogRepositoryImpl implements PhotoListFilterLogRepository {
  private final PhotoListFilterLogMapper photoListFilterLogMapper;

  /**
   * {@inheritDoc}
   *
   * <p>呼び出し元（写真一覧取得）は{@code readOnly = true}のトランザクションで実行されるため、 REQUIRES_NEWで独立した書き込みトランザクションとして保存する
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void save(PhotoListFilterLogModel photoListFilterLogModel) {
    PhotoListFilterLog photoListFilterLog = PhotoListFilterLog.from(photoListFilterLogModel);
    photoListFilterLogMapper.insert(photoListFilterLog);
  }

  @Override
  public void deleteByPhotoAccountNo(AccountNo photoAccountNo) {
    photoListFilterLogMapper.delete(
        PhotoListFilterLogCondition.byPhotoAccountNo(photoAccountNo.value()));
  }
}
