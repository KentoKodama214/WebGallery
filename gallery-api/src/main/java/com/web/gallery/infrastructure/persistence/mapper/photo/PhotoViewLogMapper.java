package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.entity.photo.PhotoViewLog;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoViewLogCondition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 写真詳細閲覧ログテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PhotoViewLogMapper {
  /**
   * 写真詳細閲覧ログを登録する
   *
   * @param photoViewLog {@link PhotoViewLog}
   * @return 登録件数
   */
  public Integer insert(PhotoViewLog photoViewLog);

  /**
   * 指定アカウントが「閲覧者」として残したログを匿名化する
   *
   * <p>閲覧者のアカウント番号を未ログイン相当のセンチネル値（0）へ、IPアドレス・国・地域を空文字へ更新する。
   * 退会時に、所有者側の閲覧数の分析データを残したまま退会者の個人データだけを消すために用いる
   *
   * @param accountNo 閲覧者のアカウント番号
   * @return 更新件数
   */
  public Integer anonymizeViewer(Long accountNo);

  /**
   * 抽出条件に該当する写真詳細閲覧ログを削除する
   *
   * <p>アカウント削除時、写真マスタの物理削除に先立って外部キー制約に抵触しないよう削除する
   *
   * @param condition {@link PhotoViewLogCondition}
   * @return 削除件数
   */
  public Integer delete(PhotoViewLogCondition condition);
}
