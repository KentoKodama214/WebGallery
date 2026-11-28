package com.web.gallery.infrastructure.persistence.mapper.auth;

import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredential;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialCondition;
import com.web.gallery.infrastructure.persistence.entity.auth.PasskeyCredentialUpdateTarget;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * パスキーテーブルのMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PasskeyCredentialMapper {
  /**
   * パスキーを登録する
   *
   * @param passkeyCredential {@link PasskeyCredential}
   * @return 登録件数
   */
  public Integer insert(PasskeyCredential passkeyCredential);

  /**
   * 抽出条件に該当するパスキーを登録日時の昇順で取得する
   *
   * @param condition {@link PasskeyCredentialCondition}
   * @return {@link PasskeyCredential}のリスト
   */
  public List<PasskeyCredential> select(PasskeyCredentialCondition condition);

  /**
   * 抽出条件に該当するパスキーを行ロック付きで取得する
   *
   * <p>同一パスキーによる同時認証を直列化し、署名カウンタの検証と更新の間の競合を防ぐ
   *
   * @param condition {@link PasskeyCredentialCondition}
   * @return {@link PasskeyCredential}のリスト
   */
  public List<PasskeyCredential> selectForUpdate(PasskeyCredentialCondition condition);

  /**
   * 抽出条件に該当するパスキーの件数を取得する
   *
   * @param condition {@link PasskeyCredentialCondition}
   * @return 件数
   */
  public Integer count(PasskeyCredentialCondition condition);

  /**
   * 抽出条件に該当するパスキーを更新する
   *
   * @param condition {@link PasskeyCredentialCondition}
   * @param target {@link PasskeyCredentialUpdateTarget}
   * @return 更新件数
   */
  public Integer update(
      @Param("condition") PasskeyCredentialCondition condition,
      @Param("target") PasskeyCredentialUpdateTarget target);

  /**
   * 抽出条件に該当するパスキーを削除する
   *
   * @param condition {@link PasskeyCredentialCondition}
   * @return 削除件数
   */
  public Integer delete(PasskeyCredentialCondition condition);
}
