package com.web.gallery.infrastructure.persistence.mapper.photo;

import com.web.gallery.infrastructure.persistence.entity.photo.PhotoListFilterLog;
import com.web.gallery.infrastructure.persistence.entity.photo.PhotoListFilterLogCondition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 写真一覧絞り込みログテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PhotoListFilterLogMapper {
  /**
   * 写真一覧絞り込みログを登録する
   *
   * @param photoListFilterLog {@link PhotoListFilterLog}
   * @return 登録件数
   */
  public Integer insert(PhotoListFilterLog photoListFilterLog);

  /**
   * 指定アカウントが「閲覧者」として残したログを匿名化する
   *
   * <p>閲覧者のアカウント番号を未ログイン相当のセンチネル値（0）へ、検索キーワード・リファラ・ IPアドレス・国・地域を空文字へ更新する。退会時に、所有者側の分析データを残したまま
   * 退会者の個人データだけを消すために用いる
   *
   * @param accountNo 閲覧者のアカウント番号
   * @return 更新件数
   */
  public Integer anonymizeViewer(Long accountNo);

  /**
   * 抽出条件に該当する写真一覧絞り込みログを削除する
   *
   * <p>アカウント削除時に、外部キー制約に抵触しないよう先に削除する
   *
   * @param condition {@link PhotoListFilterLogCondition}
   * @return 削除件数
   */
  public Integer delete(PhotoListFilterLogCondition condition);
}
