package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.dto.photo.PortfolioPhotoDto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhoto;
import com.web.gallery.infrastructure.persistence.entity.photo.PortfolioPhotoCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** ポートフォリオ写真テーブルのMapperクラス */
@Mapper
public interface PortfolioPhotoMapper {
  /**
   * ポートフォリオ写真を一括登録する
   *
   * @param portfolioPhotoList {@link PortfolioPhoto}のリスト
   * @return 登録件数
   */
  public Integer insertBulk(List<PortfolioPhoto> portfolioPhotoList);

  /**
   * 条件に該当するポートフォリオ写真を物理削除する
   *
   * @param condition 削除対象の抽出条件
   * @return 削除件数
   */
  public Integer delete(PortfolioPhotoCondition condition);

  /**
   * 条件に該当するポートフォリオ写真を、写真マスタ（{@code photo.photo_mst}）の表示用の情報と結合して表示順に取得する
   *
   * @param condition {@link PortfolioPhotoCondition}
   * @return {@link PortfolioPhotoDto}のリスト
   */
  public List<PortfolioPhotoDto> selectList(PortfolioPhotoCondition condition);

  /**
   * 指定した写真番号のうち、アカウントが所有する未削除の写真の件数を取得する
   *
   * @param accountNo アカウント番号
   * @param photoNoList 写真番号のリスト（1件以上）
   * @return 件数
   */
  public Integer countAvailablePhotos(
      @Param("accountNo") Long accountNo, @Param("photoNoList") List<Long> photoNoList);
}
