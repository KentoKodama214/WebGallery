package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.entity.photo.PhotoModerationLog;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoModerationLogCondition;
import org.apache.ibatis.annotations.Mapper;

/** 写真モデレーションログテーブルのMapperクラス */
@Mapper
public interface PhotoModerationLogMapper {
  /**
   * 写真モデレーションログを登録する
   *
   * @param photoModerationLog {@link PhotoModerationLog}
   * @return 登録件数
   */
  public Integer insert(PhotoModerationLog photoModerationLog);

  /**
   * 抽出条件に該当する写真モデレーションログを削除する
   *
   * <p>アカウント削除時、写真マスタの物理削除に先立って外部キー制約に抵触しないよう削除する
   *
   * @param condition {@link PhotoModerationLogCondition}
   * @return 削除件数
   */
  public Integer delete(PhotoModerationLogCondition condition);
}
