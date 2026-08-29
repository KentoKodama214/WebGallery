package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

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

import com.web.gallery.aggregate.Account;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.repository.impl.AccountAggregateRepositoryImpl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class AccountAggregateRepositoryImplIntegrationTest {
	@Autowired
	private AccountAggregateRepositoryImpl accountAggregateRepositoryImpl;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	@Sql("/sql/service/AccountServiceImplDeleteAccountIntegrationTest.sql")
	class delete {
		@Test
		@Order(1)
		@DisplayName("正常系：お気に入り・タグ・写真・リフレッシュトークン・アカウント本体が削除され、削除された写真番号が記録されること")
		void delete_success() {
			Account account = Account.forDelete(new AccountNo(1L));
			accountAggregateRepositoryImpl.delete(account);

			// アカウントが削除されたことを確認
			Integer accountCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
			assertEquals(0, accountCount);

			// account_no=2のアカウントは残っていること
			Integer otherAccountCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.account where account_no=2", Integer.class);
			assertEquals(1, otherAccountCount);

			// 写真マスタが削除されたことを確認
			Integer photoMstCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
			assertEquals(0, photoMstCount);

			// 写真タグが削除されたことを確認
			Integer photoTagCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_tag_mst where account_no=1", Integer.class);
			assertEquals(0, photoTagCount);

			// 自分が登録したお気に入りが削除されたことを確認
			Integer favoriteByAccount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_favorite where account_no=1", Integer.class);
			assertEquals(0, favoriteByAccount);

			// 他人が自分の写真に対して登録したお気に入りが削除されたことを確認
			Integer favoriteForAccount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM photo.photo_favorite where favorite_photo_account_no=1", Integer.class);
			assertEquals(0, favoriteForAccount);

			// リフレッシュトークンが失効・削除されたことを確認
			Integer refreshTokenCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM common.refresh_token where account_no=1", Integer.class);
			assertEquals(0, refreshTokenCount);

			// 削除時点で未削除だった写真番号が記録されていること
			assertFalse(account.getDeletedPhotoNoList().isEmpty());
			assertTrue(account.getDeletedPhotoNoList().toList().contains(new PhotoNo(1L)));
		}
	}
}
