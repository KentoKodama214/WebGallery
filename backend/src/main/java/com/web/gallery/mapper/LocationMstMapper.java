package com.web.gallery.mapper;

import com.web.gallery.entity.common.LocationMst;
import com.web.gallery.entity.common.LocationMstCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** ロケーションマスタのMapperクラス */
@Mapper
public interface LocationMstMapper {
  /**
   * 条件に該当するロケーションマスタの一覧を取得する
   *
   * @param condition 抽出条件
   * @return {@link LocationMst}のリスト
   */
  public List<LocationMst> select(LocationMstCondition condition);

  /**
   * ロケーションマスタを登録する
   *
   * @param locationMst {@link LocationMst}
   * @return 登録件数
   */
  public Integer insert(LocationMst locationMst);

  /**
   * アカウントが登録済みの最大のロケーション番号を取得する
   *
   * @param accountNo アカウント番号
   * @return 最大のロケーション番号
   */
  public Long getMaxLocationNo(Long accountNo);
}
