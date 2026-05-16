package com.web.gallary.repository.impl;

import org.springframework.stereotype.Repository;

import com.web.gallary.entity.RefreshToken;
import com.web.gallary.mapper.RefreshTokenMapper;
import com.web.gallary.model.RefreshTokenModel;
import com.web.gallary.repository.RefreshTokenRepository;

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
		RefreshToken refreshToken = RefreshToken.builder()
				.accountNo(refreshTokenModel.getAccountNo())
				.tokenHash(refreshTokenModel.getTokenHash())
				.expiresAt(refreshTokenModel.getExpiresAt())
				.build();
		refreshTokenMapper.insert(refreshToken);
	}

	@Override
	public RefreshTokenModel findByTokenHash(String tokenHash) {
		RefreshToken refreshToken = refreshTokenMapper.selectByTokenHash(tokenHash);
		if (refreshToken == null) {
			return null;
		}
		return RefreshTokenModel.builder()
				.accountNo(refreshToken.getAccountNo())
				.tokenHash(refreshToken.getTokenHash())
				.expiresAt(refreshToken.getExpiresAt())
				.isRevoked(refreshToken.getIsRevoked())
				.build();
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
