package com.web.gallery.application.service.photo;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioListGetModel;
import com.web.gallery.application.model.photo.PortfolioModelList;
import com.web.gallery.application.model.photo.PortfolioNoList;
import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import com.web.gallery.application.model.photo.PortfolioPhotoSearchModel;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PortfolioNo;

/** ポートフォリオの設定に関するビジネスロジックを扱うServiceクラス */
public interface PortfolioService {
  /**
   * 本人のポートフォリオ一覧（下書きを含む）を、検索語のテーマ・タグ名いずれかへの部分一致で絞り込んで取得する
   *
   * @param portfolioListGetModel {@link PortfolioListGetModel}（絞り込み条件は未指定可）
   * @return {@link PortfolioModelList}
   */
  PortfolioModelList getPortfolioList(PortfolioListGetModel portfolioListGetModel);

  /**
   * 作成済みのポートフォリオ（下書きを含む）の件数が、アカウントの権限区分ごとの上限に達しているかを判定する
   *
   * @param accountNo アカウント番号
   * @return 上限に達している（これ以上作成できない）場合、true
   */
  Boolean isReachedUpperLimit(AccountNo accountNo);

  /**
   * ポートフォリオに設定する写真を、本人の未削除の写真から、タイトル・撮影場所の名前・撮影場所の住所・タグ名のいずれかに検索語が部分一致するものを検索する
   *
   * <p>写真の画像ファイルパスは署名付きURLに差し替えて返す
   *
   * @param portfolioPhotoSearchModel {@link PortfolioPhotoSearchModel}
   * @return {@link PortfolioPhotoPageModel}
   */
  PortfolioPhotoPageModel searchPhotos(PortfolioPhotoSearchModel portfolioPhotoSearchModel);

  /**
   * 本人のポートフォリオの詳細情報（写真・タグを含む）を取得する
   *
   * <p>写真の画像ファイルパスは署名付きURLに差し替えて返す
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @return {@link PortfolioDetailModel}
   * @throws GalleryException ポートフォリオが存在しない場合
   */
  PortfolioDetailModel getPortfolioDetail(AccountNo accountNo, PortfolioNo portfolioNo)
      throws GalleryException;

  /**
   * ポートフォリオを下書きとして新規登録する
   *
   * <p>作成済みのポートフォリオ（下書きを含む）の件数が、アカウントの権限区分ごとの上限に達している場合は登録しない
   *
   * @param requestDetail 登録リクエストのポートフォリオ詳細情報
   * @return 新規採番したポートフォリオ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・作成数が上限に達している場合 ・設定内容が不正な場合 ・設定できない写真が含まれる場合 ・登録に失敗した場合
   */
  PortfolioNo registPortfolio(PortfolioDetailModel requestDetail) throws GalleryException;

  /**
   * ポートフォリオを更新する
   *
   * <p>公開状態は変更しない。公開中のポートフォリオは、更新後の内容が公開できる状態であることを要求する
   *
   * @param requestDetail 更新リクエストのポートフォリオ詳細情報（ポートフォリオ番号を含む）
   * @throws GalleryException 以下のいずれかに該当する場合 ・ポートフォリオが存在しない場合 ・設定内容が不正な場合 ・設定できない写真が含まれる場合
   *     ・公開中で、更新後の内容が公開できない場合 ・更新に失敗した場合
   */
  void updatePortfolio(PortfolioDetailModel requestDetail) throws GalleryException;

  /**
   * ポートフォリオを公開する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・ポートフォリオが存在しない場合 ・公開に必要な設定が不足している場合 ・更新に失敗した場合
   */
  void publishPortfolio(AccountNo accountNo, PortfolioNo portfolioNo) throws GalleryException;

  /**
   * ポートフォリオを下書きに戻す
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・ポートフォリオが存在しない場合 ・更新に失敗した場合
   */
  void unpublishPortfolio(AccountNo accountNo, PortfolioNo portfolioNo) throws GalleryException;

  /**
   * 本人の全ポートフォリオ（下書きを含む）の表示順を、指定したポートフォリオ番号の並び順に更新する
   *
   * @param accountNo アカウント番号
   * @param portfolioNoList 表示順に並べたポートフォリオ番号（アカウントの全ポートフォリオを過不足・重複なく含むこと）
   * @throws GalleryException 以下のいずれかに該当する場合 ・指定したポートフォリオ番号がアカウントの全ポートフォリオと一致しない場合 ・更新に失敗した場合
   */
  void updateDisplayOrder(AccountNo accountNo, PortfolioNoList portfolioNoList)
      throws GalleryException;

  /**
   * ポートフォリオを削除する
   *
   * @param accountNo アカウント番号
   * @param portfolioNo ポートフォリオ番号
   * @throws GalleryException ポートフォリオが存在しない場合
   */
  void deletePortfolio(AccountNo accountNo, PortfolioNo portfolioNo) throws GalleryException;
}
