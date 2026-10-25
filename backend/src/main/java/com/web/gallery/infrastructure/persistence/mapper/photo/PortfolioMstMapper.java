package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioDto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMst;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstCondition;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioMstUpdateTarget;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** ポートフォリオマスタテーブルのMapperクラス */
@Mapper
public interface PortfolioMstMapper {
  /**
   * ポートフォリオマスタを登録する
   *
   * @param portfolioMst {@link PortfolioMst}
   * @return 登録件数
   */
  public Integer insert(PortfolioMst portfolioMst);

  /**
   * ポートフォリオマスタを更新する
   *
   * @param condition 更新対象の抽出条件
   * @param target 更新内容
   * @return 更新件数
   */
  public Integer update(
      @Param("condition") PortfolioMstCondition condition,
      @Param("target") PortfolioMstUpdateTarget target);

  /**
   * アカウントのポートフォリオの表示順を、指定したポートフォリオ番号の並び順で1から振り直す
   *
   * @param accountNo アカウント番号
   * @param portfolioNoList 表示順に並べたポートフォリオ番号のリスト（空でないこと）
   * @return 更新件数
   */
  public Integer updateDisplayOrder(
      @Param("accountNo") Long accountNo, @Param("portfolioNoList") List<Long> portfolioNoList);

  /**
   * 条件に該当するポートフォリオマスタを物理削除する
   *
   * @param condition 削除対象の抽出条件
   * @return 削除件数
   */
  public Integer delete(PortfolioMstCondition condition);

  /**
   * アカウントが登録済みの最大のポートフォリオ番号を取得する
   *
   * @param accountNo アカウント番号
   * @return 最大のポートフォリオ番号
   */
  public Long getMaxPortfolioNo(Long accountNo);

  /**
   * 条件に該当するポートフォリオマスタの件数を取得する
   *
   * @param condition {@link PortfolioMstCondition}
   * @return 件数
   */
  public Integer count(PortfolioMstCondition condition);

  /**
   * 条件に該当するポートフォリオ一覧を、写真の枚数とあわせて取得する
   *
   * @param condition {@link PortfolioMstCondition}
   * @return {@link PortfolioDto}のリスト
   */
  public List<PortfolioDto> selectList(PortfolioMstCondition condition);

  /**
   * アカウントが作成済みのポートフォリオ番号（下書きを含む）をすべて取得する
   *
   * @param accountNo アカウント番号
   * @return ポートフォリオ番号のリスト（昇順）
   */
  public List<Long> selectPortfolioNoList(Long accountNo);

  /**
   * 条件に該当するポートフォリオマスタを取得する
   *
   * @param condition {@link PortfolioMstCondition}
   * @return {@link PortfolioMst}のリスト
   */
  public List<PortfolioMst> select(PortfolioMstCondition condition);

  /**
   * 指定した写真を使用している、未削除のポートフォリオマスタを取得する
   *
   * @param accountNo アカウント番号
   * @param photoNo 写真番号
   * @return {@link PortfolioMst}のリスト
   */
  public List<PortfolioMst> selectByPhoto(
      @Param("accountNo") Long accountNo, @Param("photoNo") Long photoNo);
}
