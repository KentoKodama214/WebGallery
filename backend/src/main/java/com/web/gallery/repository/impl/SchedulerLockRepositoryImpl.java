package com.web.gallery.repository.impl;

import com.web.gallery.enumeration.SchedulerLockName;
import com.web.gallery.mapper.SchedulerLockMapper;
import com.web.gallery.repository.SchedulerLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * スケジューラの多重実行防止用ロックを管理するRepositoryの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class SchedulerLockRepositoryImpl implements SchedulerLockRepository {
  private final SchedulerLockMapper schedulerLockMapper;

  @Override
  public boolean tryLock(SchedulerLockName lockName) {
    return Boolean.TRUE.equals(schedulerLockMapper.tryAdvisoryXactLock(lockName.getLockKey()));
  }
}
