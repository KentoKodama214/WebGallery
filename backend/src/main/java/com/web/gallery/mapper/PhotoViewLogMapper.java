package com.web.gallery.mapper;

import com.web.gallery.entity.PhotoViewLog;
import com.web.gallery.entity.PhotoViewLogCondition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 写真詳細閲覧ログテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PhotoViewLogMapper {
  /**
   * 写真詳細閲覧ログを登録する
   *
   * @param photoViewLog {@link PhotoViewLog}
   * @return 登録件数
   */
  public Integer insert(PhotoViewLog photoViewLog);

  /**
   * 抽出条件に該当する写真詳細閲覧ログを削除する
   *
   * <p>アカウント削除時、写真マスタの物理削除に先立って外部キー制約に抵触しないよう削除する
   *
   * @param condition {@link PhotoViewLogCondition}
   * @return 削除件数
   */
  public Integer delete(PhotoViewLogCondition condition);
}
