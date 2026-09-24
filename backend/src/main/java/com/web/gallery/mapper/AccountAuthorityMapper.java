package com.web.gallery.mapper;

import com.web.gallery.entity.account.AccountAuthority;
import com.web.gallery.entity.account.AccountAuthorityCondition;
import com.web.gallery.entity.account.AccountAuthorityUpdateTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** アカウント権限テーブルのMapperクラス */
@Mapper
public interface AccountAuthorityMapper {
  /**
   * アカウント権限を登録する
   *
   * @param accountAuthority {@link AccountAuthority}
   * @return 登録件数
   */
  public Integer insert(AccountAuthority accountAuthority);

  /**
   * アカウント権限を更新する
   *
   * @param condition 更新対象の抽出条件
   * @param target 更新内容
   * @return 更新件数
   */
  public Integer update(
      @Param("condition") AccountAuthorityCondition condition,
      @Param("target") AccountAuthorityUpdateTarget target);

  /**
   * アカウント権限を削除する
   *
   * @param condition 削除対象の抽出条件
   * @return 削除件数
   */
  public Integer delete(AccountAuthorityCondition condition);
}
