package com.web.gallery.mapper;

import com.web.gallery.entity.KbnMst;
import com.web.gallery.entity.KbnMstCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** 区分マスタテーブルのMapperクラス */
@Mapper
public interface KbnMstMapper {
  /**
   * 条件に該当する区分マスタの一覧を取得する
   *
   * @param condition 抽出条件
   * @return {@link KbnMst}
   */
  public List<KbnMst> select(KbnMstCondition condition);
}
