package com.web.gallery.application.repository.photo;

import com.web.gallery.application.model.photo.PhotoNoList;
import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioDetailModelList;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioNo;

/** ポートフォリオマスタデータ（写真・タグを含む）を参照するRepositoryクラス */
public interface PortfolioMstRepository {
  /**
   * アカウント番号から新しいポートフォリオ番号を発番する
   *
   * @param accountNo アカウント番号
   * @return 新規採番したポートフォリオ番号
   */
  PortfolioNo getNewPortfolioNo(AccountNo accountNo);

  /**
   * アカウントが作成済みのポートフォリオの件数（下書きを含む）を取得する
   *
   * @param accountNo アカウント番号
   * @return 作成済みの件数
   */
  Integer count(AccountNo accountNo);

  /**
   * 本人のポートフォリオ一覧（下書きを含む）を、テーマ・タグ名の部分一致で絞り込み、更新日時の新しい順に取得する
   *
   * @param portfolioListGetModel {@link PortfolioListGetModel}（絞り込み条件は未指定可）
   * @return {@link PortfolioModelList}
   */
  PortfolioModelList getPortfolioList(PortfolioListGetModel portfolioListGetModel);

  /**
   * 本人の未削除の写真を、検索語がタイトル・撮影場所の名前・撮影場所の住所・タグ名のいずれかに部分一致するもので絞り込み、写真番号の降順に1ページ分取得する
   *
   * @param portfolioPhotoSearchModel {@link PortfolioPhotoSearchModel}（1ページあたりの件数を設定済み）
   * @return {@link PortfolioPhotoPageModel}（画像ファイルパスはオブジェクトキーのまま）
   */
  PortfolioPhotoPageModel searchAvailablePhotos(
      PortfolioPhotoSearchModel portfolioPhotoSearchModel);

  /**
   * 本人のポートフォリオの詳細情報（写真・タグを含む）を取得する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioDetailModel}
   * @throws GalleryException ポートフォリオが存在しない（未登録・削除済み）場合
   */
  PortfolioDetailModel getPortfolioDetail(AccountNo accountNo, PortfolioNo portfolioNo)
      throws GalleryException;

  /**
   * 指定した写真を使用している未削除のポートフォリオの詳細情報（写真・タグを含む）を取得する
   *
   * @param accountNo アカウント番号
   * @param photoNo 写真番号
   * @return {@link PortfolioDetailModelList}
   */
  PortfolioDetailModelList getPortfolioDetailListByPhoto(AccountNo accountNo, PhotoNo photoNo);

  /**
   * 指定した写真番号のうち、アカウントが所有する未削除の写真の件数を取得する
   *
   * @param accountNo アカウント番号
   * @param photoNoList 写真番号の一覧
   * @return 件数（写真番号の一覧が空の場合は0）
   */
  Integer countAvailablePhotos(AccountNo accountNo, PhotoNoList photoNoList);
}
