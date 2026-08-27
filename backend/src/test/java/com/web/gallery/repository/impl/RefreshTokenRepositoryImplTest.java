package com.web.gallery.repository.impl;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.entity.RefreshToken;
import com.web.gallery.mapper.RefreshTokenMapper;
import com.web.gallery.model.RefreshTokenModel;

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
			OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7);
			RefreshTokenModel refreshTokenModel = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("abc123hash"))
					.expiresAt(new ExpiresAt(expiresAt))
					.build();

			doReturn(1).when(refreshTokenMapper).insert(any(RefreshToken.class));

			refreshTokenRepositoryImpl.save(refreshTokenModel);

			ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
			verify(refreshTokenMapper, times(1)).insert(refreshTokenCaptor.capture());
			assertEquals(1L, refreshTokenCaptor.getValue().getAccountNo());
			assertEquals("abc123hash", refreshTokenCaptor.getValue().getTokenHash());
			assertEquals(expiresAt, refreshTokenCaptor.getValue().getExpiresAt());
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
			OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7);
			RefreshToken mapperResult = RefreshToken.builder()
					.tokenId(1L)
					.accountNo(1L)
					.tokenHash("abc123hash")
					.expiresAt(expiresAt)
					.isRevoked(false)
					.build();

			doReturn(mapperResult).when(refreshTokenMapper).selectByTokenHash("abc123hash");

			RefreshTokenModel actual = refreshTokenRepositoryImpl.findByTokenHash(new TokenHash("abc123hash"));

			assertNotNull(actual);
			assertEquals(new AccountNo(1L), actual.getAccountNo());
			assertEquals(new TokenHash("abc123hash"), actual.getTokenHash());
			assertEquals(new ExpiresAt(expiresAt), actual.getExpiresAt());
			assertFalse(actual.getIsRevoked().value());
			verify(refreshTokenMapper, times(1)).selectByTokenHash("abc123hash");
		}

		@Test
		@Order(2)
		@DisplayName("正常系：該当するリフレッシュトークンが存在しない場合、nullを返す")
		void findByTokenHash_not_found() {
			doReturn(null).when(refreshTokenMapper).selectByTokenHash("nonexistent");

			RefreshTokenModel actual = refreshTokenRepositoryImpl.findByTokenHash(new TokenHash("nonexistent"));

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
			doReturn(2).when(refreshTokenMapper).revokeAllByAccountNo(1L, 1L);

			refreshTokenRepositoryImpl.revokeAllByAccountNo(new AccountNo(1L));

			verify(refreshTokenMapper, times(1)).revokeAllByAccountNo(1L, 1L);
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
			RefreshToken mapperResult = RefreshToken.builder()
					.tokenId(1L)
					.accountNo(1L)
					.tokenHash("abc123hash")
					.build();
			doReturn(mapperResult).when(refreshTokenMapper).selectByTokenHash("abc123hash");
			doReturn(1).when(refreshTokenMapper).revokeByTokenHash("abc123hash", 1L);

			refreshTokenRepositoryImpl.revokeByTokenHash(new TokenHash("abc123hash"));

			verify(refreshTokenMapper, times(1)).revokeByTokenHash("abc123hash", 1L);
		}

		@Test
		@Order(2)
		@DisplayName("正常系：該当するリフレッシュトークンが存在しない場合、無効化処理を行わない")
		void revokeByTokenHash_not_found() {
			doReturn(null).when(refreshTokenMapper).selectByTokenHash("nonexistent");

			refreshTokenRepositoryImpl.revokeByTokenHash(new TokenHash("nonexistent"));

			verify(refreshTokenMapper, never()).revokeByTokenHash(anyString(), anyLong());
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
