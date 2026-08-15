package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.IsRevoked;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.domain.common.TokenId;
import com.web.gallery.entity.RefreshToken;
import com.web.gallery.model.RefreshTokenModel;
import com.web.gallery.repository.impl.RefreshTokenRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class RefreshTokenRepositoryImplIntegrationTest {
	@Autowired
	private RefreshTokenRepositoryImpl refreshTokenRepositoryImpl;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private List<RefreshToken> getRefreshTokenData(String tokenHash) {
		return jdbcTemplate.query(
			"SELECT * FROM common.refresh_token WHERE token_hash = ?",
			(rs, rowNum) -> RefreshToken.builder()
				.tokenId(new TokenId(rs.getLong("token_id")))
				.accountNo(new AccountNo(rs.getLong("account_no")))
				.tokenHash(new TokenHash(rs.getString("token_hash")))
				.expiresAt(new ExpiresAt(rs.getObject("expires_at", OffsetDateTime.class)))
				.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
				.isRevoked(new IsRevoked(rs.getBoolean("is_revoked")))
				.build(),
			tokenHash
		);
	}

	private List<RefreshToken> getRefreshTokensByAccountNo(Long accountNo) {
		return jdbcTemplate.query(
			"SELECT * FROM common.refresh_token WHERE account_no = ?",
			(rs, rowNum) -> RefreshToken.builder()
				.tokenId(new TokenId(rs.getLong("token_id")))
				.accountNo(new AccountNo(rs.getLong("account_no")))
				.tokenHash(new TokenHash(rs.getString("token_hash")))
				.expiresAt(new ExpiresAt(rs.getObject("expires_at", OffsetDateTime.class)))
				.createdAt(new CreatedAt(rs.getObject("created_at", OffsetDateTime.class)))
				.isRevoked(new IsRevoked(rs.getBoolean("is_revoked")))
				.build(),
			accountNo
		);
	}

	private Integer countAllRefreshTokens() {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM common.refresh_token", Integer.class);
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/RefreshTokenRepositoryImplIntegrationTest.sql")
	class save {
		@Test
		@Order(1)
		@DisplayName("正常系：リフレッシュトークンを保存する")
		void save_success() {
			RefreshTokenModel refreshToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("new_token_hash"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.build();

			refreshTokenRepositoryImpl.save(refreshToken);

			List<RefreshToken> actualData = getRefreshTokenData("new_token_hash");
			assertEquals(1, actualData.size());
			assertEquals(new AccountNo(1L), actualData.getFirst().getAccountNo());
			assertEquals(new TokenHash("new_token_hash"), actualData.getFirst().getTokenHash());
			assertFalse(actualData.getFirst().getIsRevoked().value());
			assertNotNull(actualData.getFirst().getCreatedAt());
			assertTrue(actualData.getFirst().getExpiresAt().value().isAfter(OffsetDateTime.now()));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/RefreshTokenRepositoryImplIntegrationTest.sql")
	class findByTokenHash {
		@Test
		@Order(1)
		@DisplayName("正常系：トークンハッシュに該当するリフレッシュトークンを取得する")
		void findByTokenHash_success() {
			RefreshTokenModel actual = refreshTokenRepositoryImpl.findByTokenHash("valid_token_hash_1");

			assertNotNull(actual);
			assertEquals(new AccountNo(1L), actual.getAccountNo());
			assertEquals(new TokenHash("valid_token_hash_1"), actual.getTokenHash());
			assertFalse(actual.getIsRevoked().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：無効化済みのトークンも取得できる")
		void findByTokenHash_revoked() {
			RefreshTokenModel actual = refreshTokenRepositoryImpl.findByTokenHash("revoked_token_hash_1");

			assertNotNull(actual);
			assertTrue(actual.getIsRevoked().value());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：該当するトークンが存在しない場合、nullを返す")
		void findByTokenHash_not_found() {
			RefreshTokenModel actual = refreshTokenRepositoryImpl.findByTokenHash("nonexistent_hash");

			assertNull(actual);
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/RefreshTokenRepositoryImplIntegrationTest.sql")
	class revokeAllByAccountNo {
		@Test
		@Order(1)
		@DisplayName("正常系：アカウント番号に該当する有効なリフレッシュトークンをすべて無効化する")
		void revokeAllByAccountNo_success() {
			// アカウント1の有効なトークンが2件（token_id=1,5）、無効化済み1件（token_id=2）、期限切れ1件（token_id=3）
			refreshTokenRepositoryImpl.revokeAllByAccountNo(1L);

			List<RefreshToken> account1Tokens = getRefreshTokensByAccountNo(1L);
			// アカウント1のトークンはすべて無効化されている
			for (RefreshToken token : account1Tokens) {
				assertTrue(token.getIsRevoked().value());
			}

			// アカウント2のトークンは影響を受けない
			RefreshTokenModel account2Token = refreshTokenRepositoryImpl.findByTokenHash("valid_token_hash_2");
			assertNotNull(account2Token);
			assertFalse(account2Token.getIsRevoked().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：該当するトークンが存在しない場合もエラーにならない")
		void revokeAllByAccountNo_no_tokens() {
			assertDoesNotThrow(() -> refreshTokenRepositoryImpl.revokeAllByAccountNo(999L));
		}
	}

	@Nested
	@Order(4)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/RefreshTokenRepositoryImplIntegrationTest.sql")
	class revokeByTokenHash {
		@Test
		@Order(1)
		@DisplayName("正常系：トークンハッシュに該当するリフレッシュトークンを無効化する")
		void revokeByTokenHash_success() {
			// 無効化前は有効
			RefreshTokenModel before = refreshTokenRepositoryImpl.findByTokenHash("valid_token_hash_1");
			assertFalse(before.getIsRevoked().value());

			refreshTokenRepositoryImpl.revokeByTokenHash("valid_token_hash_1");

			// 無効化後はis_revoked=true
			RefreshTokenModel after = refreshTokenRepositoryImpl.findByTokenHash("valid_token_hash_1");
			assertTrue(after.getIsRevoked().value());

			// 他のトークンは影響を受けない
			RefreshTokenModel otherToken = refreshTokenRepositoryImpl.findByTokenHash("valid_token_hash_1b");
			assertFalse(otherToken.getIsRevoked().value());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：該当するトークンが存在しない場合もエラーにならない")
		void revokeByTokenHash_not_found() {
			assertDoesNotThrow(() -> refreshTokenRepositoryImpl.revokeByTokenHash("nonexistent_hash"));
		}
	}

	@Nested
	@Order(5)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/repository/RefreshTokenRepositoryImplIntegrationTest.sql")
	class deleteExpired {
		@Test
		@Order(1)
		@DisplayName("正常系：有効期限切れのリフレッシュトークンを削除する")
		void deleteExpired_success() {
			// 削除前は5件（うち期限切れ1件: token_id=3）
			Integer beforeCount = countAllRefreshTokens();
			assertEquals(5, beforeCount);

			refreshTokenRepositoryImpl.deleteExpired();

			// 期限切れトークンが削除されている
			Integer afterCount = countAllRefreshTokens();
			assertEquals(4, afterCount);

			// 期限切れトークン（token_id=3）が削除されていることを検証
			RefreshTokenModel deletedToken = refreshTokenRepositoryImpl.findByTokenHash("expired_token_hash_1");
			assertNull(deletedToken);

			// 有効なトークンは残っている
			RefreshTokenModel validToken = refreshTokenRepositoryImpl.findByTokenHash("valid_token_hash_1");
			assertNotNull(validToken);
		}
	}
}
