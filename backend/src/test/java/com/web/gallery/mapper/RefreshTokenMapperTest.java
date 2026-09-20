package com.web.gallery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.auth.RefreshToken;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RefreshTokenMapperTest {
  @Autowired private RefreshTokenMapper refreshTokenMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  private List<RefreshToken> getRefreshTokenList(String condition) {
    return jdbcTemplate.query(
        "SELECT * FROM common.refresh_token WHERE " + condition,
        (rs, rowNum) ->
            RefreshToken.builder()
                .tokenId(rs.getLong("token_id"))
                .accountNo(rs.getLong("account_no"))
                .tokenHash(rs.getString("token_hash"))
                .expiresAt(rs.getObject("expires_at", OffsetDateTime.class))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .updatedBy(rs.getLong("updated_by"))
                .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                .isRevoked(rs.getBoolean("is_revoked"))
                .build());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RefreshTokenMapperTest.sql")
  @Sql("/sql/common/ResetRefreshTokenIdSeq.sql")
  class insert {
    @Test
    @Order(1)
    @DisplayName("正常系：登録成功")
    void insert_success() {
      OffsetDateTime expiresAt = OffsetDateTime.of(2099, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));
      RefreshToken insertRefreshToken =
          RefreshToken.builder()
              .accountNo(1L)
              .tokenHash("newHash")
              .expiresAt(expiresAt)
              .updatedBy(1L)
              .build();

      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);
      Integer actualCount = refreshTokenMapper.insert(insertRefreshToken);
      assertEquals(1, actualCount);

      List<RefreshToken> actualData = getRefreshTokenList("token_hash='newHash'");
      assertEquals(1, actualData.size());
      assertEquals(1L, actualData.getFirst().getAccountNo());
      assertEquals("newHash", actualData.getFirst().getTokenHash());
      assertEquals(
          expiresAt.withOffsetSameInstant(ZoneOffset.UTC), actualData.getFirst().getExpiresAt());
      assertEquals(transactionNow, actualData.getFirst().getCreatedAt());
      assertEquals(1L, actualData.getFirst().getUpdatedBy());
      assertEquals(transactionNow, actualData.getFirst().getUpdatedAt());
      assertFalse(actualData.getFirst().getIsRevoked());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RefreshTokenMapperTest.sql")
  class selectByTokenHash {
    @Test
    @Order(1)
    @DisplayName("正常系：トークンハッシュに該当するレコードがある場合、そのレコードを返すこと")
    void selectByTokenHash_found() {
      RefreshToken actual = refreshTokenMapper.selectByTokenHash("hash1");

      assertNotNull(actual);
      assertEquals(1L, actual.getTokenId());
      assertEquals(1L, actual.getAccountNo());
      assertEquals("hash1", actual.getTokenHash());
      assertFalse(actual.getIsRevoked());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：トークンハッシュに該当するレコードがない場合、nullを返すこと")
    void selectByTokenHash_notFound() {
      RefreshToken actual = refreshTokenMapper.selectByTokenHash("unknown-hash");

      assertNull(actual);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RefreshTokenMapperTest.sql")
  class selectByTokenHashForUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：トークンハッシュに該当するレコードがある場合、行ロック付きで取得できること")
    void selectByTokenHashForUpdate_found() {
      RefreshToken actual = refreshTokenMapper.selectByTokenHashForUpdate("hash1");

      assertNotNull(actual);
      assertEquals(1L, actual.getTokenId());
      assertEquals("hash1", actual.getTokenHash());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：トークンハッシュに該当するレコードがない場合、nullを返すこと")
    void selectByTokenHashForUpdate_notFound() {
      RefreshToken actual = refreshTokenMapper.selectByTokenHashForUpdate("unknown-hash");

      assertNull(actual);
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RefreshTokenMapperTest.sql")
  class revokeAllByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：該当アカウントの未無効化トークンのみ無効化され、他アカウント・既に無効化済みのトークンは影響を受けないこと")
    void revokeAllByAccountNo_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actual = refreshTokenMapper.revokeAllByAccountNo(1L, 9L);

      // account_no=1のうち、is_revoked=falseだったのはhash1・hash2・hash4の3件
      assertEquals(3, actual);

      List<RefreshToken> revoked =
          getRefreshTokenList("account_no=1 AND token_hash IN ('hash1','hash2','hash4')");
      assertEquals(3, revoked.size());
      for (RefreshToken token : revoked) {
        assertTrue(token.getIsRevoked());
        assertEquals(9L, token.getUpdatedBy());
        assertEquals(transactionNow, token.getUpdatedAt());
      }

      // 他アカウント（account_no=2）は影響を受けない
      List<RefreshToken> other = getRefreshTokenList("account_no=2");
      assertEquals(1, other.size());
      assertFalse(other.getFirst().getIsRevoked());

      // 既に無効化済み（hash5）は更新対象外なのでupdated_byは変わらない
      List<RefreshToken> alreadyRevoked = getRefreshTokenList("token_hash='hash5'");
      assertEquals(1, alreadyRevoked.size());
      assertEquals(1L, alreadyRevoked.getFirst().getUpdatedBy());
    }
  }

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RefreshTokenMapperTest.sql")
  class revokeByTokenHash {
    @Test
    @Order(1)
    @DisplayName("正常系：該当トークンのみ無効化され、他のトークンは影響を受けないこと")
    void revokeByTokenHash_success() {
      OffsetDateTime transactionNow =
          jdbcTemplate.queryForObject("SELECT NOW()", OffsetDateTime.class);

      Integer actual = refreshTokenMapper.revokeByTokenHash("hash1", 9L);

      assertEquals(1, actual);

      List<RefreshToken> revoked = getRefreshTokenList("token_hash='hash1'");
      assertEquals(1, revoked.size());
      assertTrue(revoked.getFirst().getIsRevoked());
      assertEquals(9L, revoked.getFirst().getUpdatedBy());
      assertEquals(transactionNow, revoked.getFirst().getUpdatedAt());

      List<RefreshToken> other = getRefreshTokenList("token_hash='hash2'");
      assertEquals(1, other.size());
      assertFalse(other.getFirst().getIsRevoked());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：既に無効化済みのトークンに対しては更新件数0を返すこと")
    void revokeByTokenHash_alreadyRevoked() {
      Integer actual = refreshTokenMapper.revokeByTokenHash("hash5", 9L);

      assertEquals(0, actual);
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  @Sql("/sql/mapper/RefreshTokenMapperTest.sql")
  class deleteExpired {
    @Test
    @Order(1)
    @DisplayName("正常系：有効期限切れのトークンのみ削除され、有効期限内のトークンは残ること")
    void deleteExpired_success() {
      Integer actual = refreshTokenMapper.deleteExpired();

      // 有効期限切れなのはhash4の1件のみ
      assertEquals(1, actual);

      List<RefreshToken> remaining = getRefreshTokenList("token_hash='hash4'");
      assertEquals(0, remaining.size());

      List<RefreshToken> notExpired = getRefreshTokenList("expires_at > NOW()");
      assertEquals(4, notExpired.size());
    }
  }
}
