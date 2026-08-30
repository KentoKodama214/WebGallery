package com.web.gallery.repository.impl;

import org.springframework.stereotype.Repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.TokenHash;
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
	public RefreshTokenModel findByTokenHash(TokenHash tokenHash) {
		RefreshToken refreshToken = refreshTokenMapper.selectByTokenHash(tokenHash.value());
		if (refreshToken == null) {
			return null;
		}
		return RefreshTokenModel.from(refreshToken);
	}

	@Override
	public RefreshTokenModel findByTokenHashForUpdate(TokenHash tokenHash) {
		RefreshToken refreshToken = refreshTokenMapper.selectByTokenHashForUpdate(tokenHash.value());
		if (refreshToken == null) {
			return null;
		}
		return RefreshTokenModel.from(refreshToken);
	}

	@Override
	public void revokeAllByAccountNo(AccountNo accountNo) {
		refreshTokenMapper.revokeAllByAccountNo(accountNo.value(), accountNo.value());
	}

	@Override
	public void revokeByTokenHash(TokenHash tokenHash) {
		RefreshToken refreshToken = refreshTokenMapper.selectByTokenHash(tokenHash.value());
		if (refreshToken == null) {
			return;
		}
		refreshTokenMapper.revokeByTokenHash(tokenHash.value(), refreshToken.getAccountNo());
	}

	@Override
	public void deleteExpired() {
		refreshTokenMapper.deleteExpired();
	}
}
