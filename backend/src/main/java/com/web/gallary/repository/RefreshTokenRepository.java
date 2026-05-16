package com.web.gallary.repository;

import com.web.gallary.entity.RefreshToken;

/**
 * リフレッシュトークンデータを永続化するRepositoryクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
public interface RefreshTokenRepository {
	/**
	 * リフレッシュトークンを保存する
	 *
	 * @param	refreshToken	{@link RefreshToken}
	 */
	void save(RefreshToken refreshToken);

	/**
	 * トークンハッシュに該当するリフレッシュトークンを取得する
	 *
	 * @param	tokenHash	トークンハッシュ
	 * @return				{@link RefreshToken}、取得できない場合はnull
	 */
	RefreshToken findByTokenHash(String tokenHash);

	/**
	 * アカウント番号に該当するリフレッシュトークンをすべて無効化する
	 *
	 * @param	accountNo	アカウント番号
	 */
	void revokeAllByAccountNo(Integer accountNo);

	/**
	 * トークンハッシュに該当するリフレッシュトークンを無効化する
	 *
	 * @param	tokenHash	トークンハッシュ
	 */
	void revokeByTokenHash(String tokenHash);

	/**
	 * 有効期限切れのリフレッシュトークンを削除する
	 */
	void deleteExpired();
}
