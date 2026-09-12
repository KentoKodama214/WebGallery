package com.web.gallery.mapper;

import com.web.gallery.entity.LoginHistory;
import com.web.gallery.entity.LoginHistoryCondition;
import org.apache.ibatis.annotations.Mapper;

/**
 * ログイン履歴テーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface LoginHistoryMapper {
  /**
   * ログイン履歴を登録する
   *
   * @param loginHistory {@link LoginHistory}
   * @return 登録件数
   */
  public Integer insert(LoginHistory loginHistory);

  /**
   * 抽出条件に該当するログイン履歴を削除する
   *
   * <p>アカウント削除時に、外部キー制約に抵触しないよう先に削除する
   *
   * @param condition {@link LoginHistoryCondition}
   * @return 削除件数
   */
  public Integer delete(LoginHistoryCondition condition);
}
