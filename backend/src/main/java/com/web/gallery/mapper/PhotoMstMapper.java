package com.web.gallery.mapper;

import com.web.gallery.dto.PhotoDeletionDto;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoMstCondition;
import com.web.gallery.entity.PhotoMstUpdateTarget;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 写真マスタテーブルのMapperクラス */
@Mapper
public interface PhotoMstMapper {
  /**
   * 条件に該当する写真マスタの件数を取得する
   *
   * @param condition カウント条件
   * @return 抽出件数
   */
  public Integer count(PhotoMstCondition condition);

  /**
   * 写真マスタを登録する
   *
   * @param photoMst {@link PhotoMst}
   * @return 登録件数
   */
  public Integer insert(PhotoMst photoMst);

  /**
   * 写真マスタを更新する
   *
   * @param condition 更新対象の抽出条件
   * @param target 更新内容
   * @return 更新件数
   */
  public Integer update(
      @Param("condition") PhotoMstCondition condition,
      @Param("target") PhotoMstUpdateTarget target);

  /**
   * アカウントが登録済みの最大の写真番号を取得する
   *
   * @param accountNo アカウント番号
   * @return 最大の写真番号
   */
  public Long getMaxPhotoNo(Long accountNo);

  /**
   * ファイル名から登録済みの写真か判定する
   *
   * @param condition {@link PhotoMstCondition}
   * @return 登録有無
   */
  public Boolean isExistPhoto(PhotoMstCondition condition);

  /**
   * アカウント番号に紐づく写真マスタを物理削除し、削除した行を返す
   *
   * @param accountNo アカウント番号
   * @return 削除した{@link PhotoDeletionDto}のリスト
   */
  public List<PhotoDeletionDto> deletePhotosByAccountNo(Long accountNo);
}
