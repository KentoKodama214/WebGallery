package com.web.gallery.infrastructure.persistence.mapper.auth;

import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandle;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyUserHandleCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * パスキーユーザーハンドルテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PasskeyUserHandleMapper {
  /**
   * ユーザーハンドルを登録する。同一アカウントのユーザーハンドルが既に存在する場合は何もしない
   *
   * @param passkeyUserHandle {@link PasskeyUserHandle}
   * @return 登録件数（既に存在した場合は0）
   */
  public Integer insertIfAbsent(PasskeyUserHandle passkeyUserHandle);

  /**
   * 抽出条件に該当するユーザーハンドルを取得する
   *
   * @param condition {@link PasskeyUserHandleCondition}
   * @return {@link PasskeyUserHandle}のリスト
   */
  public List<PasskeyUserHandle> select(PasskeyUserHandleCondition condition);

  /**
   * 抽出条件に該当するユーザーハンドルを削除する
   *
   * @param condition {@link PasskeyUserHandleCondition}
   * @return 削除件数
   */
  public Integer delete(PasskeyUserHandleCondition condition);
}
