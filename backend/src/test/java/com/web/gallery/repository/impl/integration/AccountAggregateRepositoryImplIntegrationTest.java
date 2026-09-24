package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.aggregate.Account;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.repository.impl.AccountAggregateRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class AccountAggregateRepositoryImplIntegrationTest {
  @Autowired private AccountAggregateRepositoryImpl accountAggregateRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

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
      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(0, accountCount);

      // account_no=2のアカウントは残っていること
      Integer otherAccountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=2", Integer.class);
      assertEquals(1, otherAccountCount);

      // 写真マスタが削除されたことを確認
      Integer photoMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
      assertEquals(0, photoMstCount);

      // 写真タグが削除されたことを確認
      Integer photoTagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_tag_mst where account_no=1", Integer.class);
      assertEquals(0, photoTagCount);

      // 自分が登録したお気に入りが削除されたことを確認
      Integer favoriteByAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite where account_no=1", Integer.class);
      assertEquals(0, favoriteByAccount);

      // 他人が自分の写真に対して登録したお気に入りが削除されたことを確認
      Integer favoriteForAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite where favorite_photo_account_no=1",
              Integer.class);
      assertEquals(0, favoriteForAccount);

      // リフレッシュトークンが失効・削除されたことを確認
      Integer refreshTokenCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.refresh_token where account_no=1", Integer.class);
      assertEquals(0, refreshTokenCount);

      // ログイン履歴が削除されたことを確認
      Integer loginHistoryCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.login_history where account_no=1", Integer.class);
      assertEquals(0, loginHistoryCount);

      // 写真一覧絞り込みログが削除されたことを確認
      Integer photoListFilterLogCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_list_filter_log where photo_account_no=1",
              Integer.class);
      assertEquals(0, photoListFilterLogCount);

      // 削除時点で未削除だった写真番号が記録されていること
      assertFalse(account.getDeletedPhotoNoList().isEmpty());
      assertTrue(account.getDeletedPhotoNoList().toList().contains(new PhotoNo(1L)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真・お気に入り等の関連データが0件のアカウントでも削除が成功すること")
    void delete_success_whenNoRelatedData() {
      Account account = Account.forDelete(new AccountNo(3L));
      accountAggregateRepositoryImpl.delete(account);

      // アカウントが削除されたことを確認
      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=3", Integer.class);
      assertEquals(0, accountCount);

      // 削除対象の写真が存在しなかったため、削除された写真番号が記録されないこと
      assertTrue(account.getDeletedPhotoNoList().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：処理途中で例外が発生した場合、それまでの削除を含めてすべてロールバックされること")
    void delete_rollbackOnFailure() {
      // @Sqlによるフィクスチャ投入はこのテストメソッドのトランザクション内で行われるため、
      // 後段でTestTransaction.flagForRollback()するとフィクスチャ投入自体も巻き戻ってしまう。
      // それを避けるため、一度物理コミットしてフィクスチャを確定させてから新しいトランザクションを開始する
      TestTransaction.flagForCommit();
      TestTransaction.end();
      TestTransaction.start();

      // common.location_mstにaccount_no=1を参照する行を用意し、
      // 外部キー制約（ON DELETE RESTRICT）によりアカウント本体の削除で例外が発生するようにする
      jdbcTemplate.update(
          "INSERT INTO common.location_mst"
              + " VALUES (DEFAULT, 1, 99, 1, now(), 1, now(), false, 'ロケーション99', '住所99', 0, 0)");

      Account account = Account.forDelete(new AccountNo(1L));
      assertThrows(
          DataIntegrityViolationException.class,
          () -> accountAggregateRepositoryImpl.delete(account));

      // 例外発生によりPostgreSQL上のトランザクションが中断状態になるため、
      // 一度ロールバックして新しいトランザクションを開始した上で状態を確認する
      TestTransaction.flagForRollback();
      TestTransaction.end();
      TestTransaction.start();

      // アカウント本体の削除より前に実行された写真マスタ等の削除も、まとめてロールバックされ残っていること
      Integer accountCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.account where account_no=1", Integer.class);
      assertEquals(1, accountCount);
      Integer photoMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_mst where account_no=1", Integer.class);
      assertEquals(2, photoMstCount);
      Integer photoTagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_tag_mst where account_no=1", Integer.class);
      assertEquals(3, photoTagCount);
      Integer favoriteByAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_favorite where account_no=1", Integer.class);
      assertEquals(2, favoriteByAccount);

      // 冒頭で物理コミットしたフィクスチャが、通常の@Transactionalによる自動ロールバックに
      // 乗らないまま他のテストクラスへ残留しないよう、明示的にTRUNCATE（CASCADE）して物理コミットする
      jdbcTemplate.execute(
          """
					TRUNCATE TABLE
						photo.photo_favorite,
						photo.photo_tag_mst,
						photo.photo_mst,
						common.refresh_token,
						common.location_mst,
						common.account,
						common.kbn_mst
					CASCADE
					""");
      TestTransaction.flagForCommit();
      TestTransaction.end();
    }
  }
}
