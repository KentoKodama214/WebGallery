package com.web.gallery.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.web.gallery.entity.RefreshToken;

/**
 * リフレッシュトーク��テーブルのMapperクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Mapper
public interface RefreshTokenMapper {
	/**
	 * リフレッシュトークンを登録する
	 *
	 * @param	refreshToken	{@link RefreshToken}
	 * @return					登録件数
	 */
	public Integer insert(RefreshToken refreshToken);

	/**
	 * トークンハッシュに該当するリフレッシュトークンを取得する
	 *
	 * @param	tokenHash	トーク��ハッシュ
	 * @return				{@link RefreshToken}
	 */
	public RefreshToken selectByTokenHash(@Param("tokenHash") String tokenHash);

	/**
	 * アカウント番号に該当するリフレッシュトークンをすべて無効化する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	updatedBy	更新者
	 * @return				更新件数
	 */
	public Integer revokeAllByAccountNo(@Param("accountNo") Long accountNo, @Param("updatedBy") Long updatedBy);

	/**
	 * トークンハッシュに該当するリフレッシュトークンを無効化する
	 *
	 * @param	tokenHash	トークンハッシュ
	 * @param	updatedBy	更新者
	 * @return				更新件数
	 */
	public Integer revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("updatedBy") Long updatedBy);

	/**
	 * 有効期限切れのリフレッシュトークンを削除する
	 *
	 * @return	削除件数
	 */
	public Integer deleteExpired();
}
