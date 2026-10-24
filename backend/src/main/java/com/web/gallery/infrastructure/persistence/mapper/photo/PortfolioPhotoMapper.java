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
   * アカウントが所有する未削除の写真を、タイトル・撮影場所の名前・タグ名の部分一致で絞り込み、撮影日時の新しい順に取得する
   *
   * <p>ポートフォリオに設定する写真を選ぶために使用する（本人の写真のみを対象とするため、非公開の撮影場所も検索対象とする）
   *
   * @param accountNo アカウント番号
   * @param titlePattern タイトル（日本語・英語）のLIKEパターン（未指定はnull）
   * @param locationPattern 撮影場所の名前（管理名・表示名）のLIKEパターン（未指定はnull）
   * @param tagPattern タグ名（日本語・英語）のLIKEパターン（未指定はnull）
   * @param limit 取得件数
   * @param offset 取得開始位置
   * @return {@link PortfolioPhotoDto}のリスト（写真番号・タイトル・画像ファイルパス・向きのみ）
   */
  public List<PortfolioPhotoDto> selectAvailablePhotos(
      @Param("accountNo") Long accountNo,
      @Param("titlePattern") String titlePattern,
      @Param("locationPattern") String locationPattern,
      @Param("tagPattern") String tagPattern,
      @Param("limit") Integer limit,
      @Param("offset") Integer offset);

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
