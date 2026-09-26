package com.web.gallery.infrastructure.persistence.repository;

import com.web.gallery.application.model.photo.PhotoListFilterLogModel;
import com.web.gallery.application.repository.PhotoListFilterLogRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoListFilterLog;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoListFilterLogCondition;
import com.web.gallery.infrastructure.persistence.mapper.PhotoListFilterLogMapper;
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
