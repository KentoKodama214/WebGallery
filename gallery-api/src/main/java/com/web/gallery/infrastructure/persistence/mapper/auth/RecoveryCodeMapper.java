package com.web.gallery.infrastructure.persistence.mapper.auth;

import com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCode;
import com.web.gallery.infrastructure.persistence.entity.auth.RecoveryCodeCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * リカバリーコードテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface RecoveryCodeMapper {
  /**
   * リカバリーコードをまとめて登録する
   *
   * @param recoveryCodeList {@link RecoveryCode}のリスト
   * @return 登録件数
   */
  public Integer bulkInsert(List<RecoveryCode> recoveryCodeList);

  /**
   * 抽出条件に該当するリカバリーコードの件数を取得する
   *
   * @param condition {@link RecoveryCodeCondition}
   * @return 件数
   */
  public Integer count(RecoveryCodeCondition condition);

  /**
   * 抽出条件に該当するリカバリーコードを削除する
   *
   * <p>アカウント番号・コードハッシュを指定すると照合と削除を1文で行うため、同じコードを並行して2回消費することはできない
   *
   * @param condition {@link RecoveryCodeCondition}
   * @return 削除件数
   */
  public Integer delete(RecoveryCodeCondition condition);
}
