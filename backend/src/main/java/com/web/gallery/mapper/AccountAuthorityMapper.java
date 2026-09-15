package com.web.gallery.mapper;

import com.web.gallery.entity.AccountAuthority;
import com.web.gallery.entity.AccountAuthorityCondition;
import org.apache.ibatis.annotations.Mapper;

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
   * アカウント権限を削除する
   *
   * @param condition 削除対象の抽出条件
   * @return 削除件数
   */
  public Integer delete(AccountAuthorityCondition condition);
}
