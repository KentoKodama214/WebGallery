package com.web.gallery.repository;

import com.web.gallery.model.RefreshTokenModel;

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
	 * @param	refreshTokenModel	{@link RefreshTokenModel}
	 */
	void save(RefreshTokenModel refreshTokenModel);

	/**
	 * トークンハッシュに該当するリフレッシュトークンを取得する
	 *
	 * @param	tokenHash	トークンハッシュ
	 * @return				{@link RefreshTokenModel}、取得できない場合はnull
	 */
	RefreshTokenModel findByTokenHash(String tokenHash);

	/**
	 * アカウント番号に該当するリフレッシュトークンをすべて無効化する
	 *
	 * @param	accountNo	アカウント番号
	 */
	void revokeAllByAccountNo(Long accountNo);

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
