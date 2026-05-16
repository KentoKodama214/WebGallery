package com.web.gallary.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallary.entity.RefreshToken;
import com.web.gallary.mapper.RefreshTokenMapper;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class RefreshTokenRepositoryImplTest {
	@InjectMocks
	private RefreshTokenRepositoryImpl refreshTokenRepositoryImpl;

	@Mock
	private RefreshTokenMapper refreshTokenMapper;

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class save {
		@Test
		@Order(1)
		@DisplayName("正常系：リフレッシュトークンを保存する")
		void save_success() {
			RefreshToken refreshToken = RefreshToken.builder()
					.accountNo(1)
					.tokenHash("abc123hash")
					.expiresAt(OffsetDateTime.now().plusDays(7))
					.build();

			doReturn(1).when(refreshTokenMapper).insert(refreshToken);

			refreshTokenRepositoryImpl.save(refreshToken);

			verify(refreshTokenMapper, times(1)).insert(refreshToken);
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class findByTokenHash {
		@Test
		@Order(1)
		@DisplayName("正常系：トークンハッシュに該当するリフレッシュトークンを取得する")
		void findByTokenHash_success() {
			RefreshToken expected = RefreshToken.builder()
					.tokenId(1)
					.accountNo(1)
					.tokenHash("abc123hash")
					.expiresAt(OffsetDateTime.now().plusDays(7))
					.isRevoked(false)
					.build();

			doReturn(expected).when(refreshTokenMapper).selectByTokenHash("abc123hash");

			RefreshToken actual = refreshTokenRepositoryImpl.findByTokenHash("abc123hash");

			assertEquals(expected, actual);
			verify(refreshTokenMapper, times(1)).selectByTokenHash("abc123hash");
		}

		@Test
		@Order(2)
		@DisplayName("正常系：該当するリフレッシュトークンが存在しない場合、nullを返す")
		void findByTokenHash_not_found() {
			doReturn(null).when(refreshTokenMapper).selectByTokenHash("nonexistent");

			RefreshToken actual = refreshTokenRepositoryImpl.findByTokenHash("nonexistent");

			assertNull(actual);
			verify(refreshTokenMapper, times(1)).selectByTokenHash("nonexistent");
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class revokeAllByAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号に該当するリフレッシュトークンをすべて無効化する")
		void revokeAllByAccountNo_success() {
			doReturn(2).when(refreshTokenMapper).revokeAllByAccountNo(1);

			refreshTokenRepositoryImpl.revokeAllByAccountNo(1);

			verify(refreshTokenMapper, times(1)).revokeAllByAccountNo(1);
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class revokeByTokenHash {
		@Test
		@Order(1)
		@DisplayName("正常系：トークンハッシュに該当するリフレッシュトークンを無効化する")
		void revokeByTokenHash_success() {
			doReturn(1).when(refreshTokenMapper).revokeByTokenHash("abc123hash");

			refreshTokenRepositoryImpl.revokeByTokenHash("abc123hash");

			verify(refreshTokenMapper, times(1)).revokeByTokenHash("abc123hash");
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class deleteExpired {
		@Test
		@Order(1)
		@DisplayName("正常系：有効期限切れのリフレッシュトークンを削除する")
		void deleteExpired_success() {
			doReturn(3).when(refreshTokenMapper).deleteExpired();

			refreshTokenRepositoryImpl.deleteExpired();

			verify(refreshTokenMapper, times(1)).deleteExpired();
		}
	}
}
