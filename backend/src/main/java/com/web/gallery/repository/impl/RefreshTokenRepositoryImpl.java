package com.web.gallery.repository.impl;

import org.springframework.stereotype.Repository;

import com.web.gallery.entity.RefreshToken;
import com.web.gallery.mapper.RefreshTokenMapper;
import com.web.gallery.model.RefreshTokenModel;
import com.web.gallery.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * リフレッシュトークンデータを永続化するRepositoryの実装クラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
	private final RefreshTokenMapper refreshTokenMapper;

	@Override
	public void save(RefreshTokenModel refreshTokenModel) {
		RefreshToken refreshToken = RefreshToken.from(refreshTokenModel);
		refreshTokenMapper.insert(refreshToken);
	}

	@Override
	public RefreshTokenModel findByTokenHash(String tokenHash) {
		RefreshToken refreshToken = refreshTokenMapper.selectByTokenHash(tokenHash);
		if (refreshToken == null) {
			return null;
		}
		return RefreshTokenModel.from(refreshToken);
	}

	@Override
	public void revokeAllByAccountNo(Integer accountNo) {
		refreshTokenMapper.revokeAllByAccountNo(accountNo);
	}

	@Override
	public void revokeByTokenHash(String tokenHash) {
		refreshTokenMapper.revokeByTokenHash(tokenHash);
	}

	@Override
	public void deleteExpired() {
		refreshTokenMapper.deleteExpired();
	}
}
