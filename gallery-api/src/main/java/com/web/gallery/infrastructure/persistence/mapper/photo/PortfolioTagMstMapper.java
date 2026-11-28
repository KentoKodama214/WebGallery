package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioTagMstCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** ポートフォリオタグマスタテーブルのMapperクラス */
@Mapper
public interface PortfolioTagMstMapper {
  /**
   * ポートフォリオタグを一括登録する
   *
   * @param portfolioTagMstList {@link PortfolioTagMst}のリスト
   * @return 登録件数
   */
  public Integer insertBulk(List<PortfolioTagMst> portfolioTagMstList);

  /**
   * 条件に該当するポートフォリオタグを物理削除する
   *
   * @param condition 削除対象の抽出条件
   * @return 削除件数
   */
  public Integer delete(PortfolioTagMstCondition condition);

  /**
   * 条件に該当するポートフォリオタグをタグ番号順に取得する
   *
   * @param condition {@link PortfolioTagMstCondition}
   * @return {@link PortfolioTagMst}のリスト
   */
  public List<PortfolioTagMst> select(PortfolioTagMstCondition condition);
}
