package com.web.gallery.application.repository.photo;

import com.web.gallery.application.aggregate.Portfolio;
import com.web.gallery.application.model.photo.PortfolioNoList;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;

/** ポートフォリオ集約（{@link Portfolio}）を永続化するRepositoryクラス */
public interface PortfolioAggregateRepository {
  /**
   * ポートフォリオ集約（ポートフォリオ・写真・タグ）を新規登録する
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException 登録に失敗した場合
   */
  void regist(Portfolio portfolio) throws GalleryException;

  /**
   * ポートフォリオ集約を更新する
   *
   * <p>ポートフォリオ本体を更新し、写真・タグは全件を削除してから登録し直す
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException 更新に失敗した場合
   */
  void update(Portfolio portfolio) throws GalleryException;

  /**
   * ポートフォリオの公開状態を更新する
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException 更新に失敗した場合
   */
  void updateStatus(Portfolio portfolio) throws GalleryException;

  /**
   * アカウントのポートフォリオの表示順を、指定したポートフォリオ番号の並び順で1から振り直す
   *
   * @param accountNo アカウント番号
   * @param portfolioNoList 表示順に並べた、アカウントの全ポートフォリオのポートフォリオ番号
   * @throws GalleryException 更新に失敗した場合
   */
  void updateDisplayOrder(AccountNo accountNo, PortfolioNoList portfolioNoList)
      throws GalleryException;

  /**
   * ポートフォリオ集約を削除する
   *
   * <p>写真・タグ・ポートフォリオ本体を物理削除する（復元はできない）
   *
   * @param portfolio {@link Portfolio}
   * @throws GalleryException ポートフォリオが存在しない（未登録・削除済み）場合
   */
  void delete(Portfolio portfolio) throws GalleryException;
}
