package com.web.gallery.mapper;

import com.web.gallery.entity.PhotoListFilterLog;
import com.web.gallery.entity.PhotoListFilterLogCondition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 写真一覧絞り込みログテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PhotoListFilterLogMapper {
  /**
   * 写真一覧絞り込みログを登録する
   *
   * @param photoListFilterLog {@link PhotoListFilterLog}
   * @return 登録件数
   */
  public Integer insert(PhotoListFilterLog photoListFilterLog);

  /**
   * 抽出条件に該当する写真一覧絞り込みログを削除する
   *
   * <p>アカウント削除時に、外部キー制約に抵触しないよう先に削除する
   *
   * @param condition {@link PhotoListFilterLogCondition}
   * @return 削除件数
   */
  public Integer delete(PhotoListFilterLogCondition condition);
}
