package com.web.gallery.infrastructure.persistence.repository.account.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.web.gallery.application.aggregate.Account;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.infrastructure.persistence.entity.account.AccountCondition;
import com.web.gallery.infrastructure.persistence.mapper.account.AccountMapper;
import com.web.gallery.infrastructure.persistence.repository.account.AccountAggregateRepositoryImpl;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class AccountAggregateRepositoryImplIntegrationTest {
  @Autowired private AccountAggregateRepositoryImpl accountAggregateRepositoryImpl;

  @Autowired private JdbcTemplate jdbcTemplate;

  /**
   * アカウント本体の物理削除だけを意図的に失敗させ、ロールバックを検証するためのスパイ
   *
   * <p>スタブしない限り実装どおりに動作するため、他のテストケースの挙動は変わらない
   */
  @MockitoSpyBean private AccountMapper accountMapper;

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

      // ロケーションマスタが削除されたことを確認
      Integer locationMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.location_mst where account_no=1", Integer.class);
      assertEquals(0, locationMstCount);

      // account_no=2のロケーションマスタは残っていること
      Integer otherLocationMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.location_mst where account_no=2", Integer.class);
      assertEquals(1, otherLocationMstCount);

      // 自分が登録したお問い合わせが削除されたことを確認
      Integer inquiryMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_mst where account_no=1", Integer.class);
      assertEquals(0, inquiryMstCount);

      // account_no=2のお問い合わせは残っていること
      Integer otherInquiryMstCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_mst where account_no=2", Integer.class);
      assertEquals(1, otherInquiryMstCount);

      // 自分が管理者として投稿した返信が削除されたことを確認（admin_account_no 参照の解消）
      Integer replyByAccount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.inquiry_reply_mst where admin_account_no=1",
              Integer.class);
      assertEquals(0, replyByAccount);

      // 自分のお問い合わせに紐づく返信が削除されたことを確認（inquiry_id 参照の解消）
      Integer replyForAccount =
          jdbcTemplate.queryForObject(
              """
              SELECT COUNT(*) FROM common.inquiry_reply_mst r
              WHERE EXISTS (SELECT 1 FROM common.inquiry_mst i WHERE i.id = r.inquiry_id AND i.account_no = 1)
              """,
              Integer.class);
      assertEquals(0, replyForAccount);

      // 削除時点で未削除だった写真番号が記録されていること
      assertFalse(account.getDeletedPhotoNoList().isEmpty());
      assertTrue(account.getDeletedPhotoNoList().toList().contains(new PhotoNo(1L)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：common.accountを参照する全テーブルから、削除対象アカウントを参照する行が消えていること")
    void delete_removesAllForeignKeyReferences() {
      Account account = Account.forDelete(new AccountNo(1L));
      accountAggregateRepositoryImpl.delete(account);

      // common.account(account_no) を参照する外部キーはいずれも ON DELETE RESTRICT / NO ACTION のため、
      // 参照元テーブルの削除漏れはアカウント本体の物理削除を外部キー違反で失敗させる。
      // information_schema から参照元を動的に列挙し、テーブル追加時の削除漏れを機械的に検出する
      List<Map<String, Object>> referencingColumns =
          jdbcTemplate.queryForList(
              """
              SELECT
                  src_ns.nspname AS schema_name,
                  src.relname    AS table_name,
                  src_att.attname AS column_name
              FROM pg_constraint c
              JOIN pg_class src ON src.oid = c.conrelid
              JOIN pg_namespace src_ns ON src_ns.oid = src.relnamespace
              JOIN pg_class tgt ON tgt.oid = c.confrelid
              JOIN pg_namespace tgt_ns ON tgt_ns.oid = tgt.relnamespace
              JOIN pg_attribute src_att
                ON src_att.attrelid = c.conrelid AND src_att.attnum = c.conkey[1]
              JOIN pg_attribute tgt_att
                ON tgt_att.attrelid = c.confrelid AND tgt_att.attnum = c.confkey[1]
              WHERE c.contype = 'f'
                AND tgt_ns.nspname = 'common'
                AND tgt.relname = 'account'
                AND tgt_att.attname = 'account_no'
              """);

      assertFalse(referencingColumns.isEmpty(), "common.accountを参照する外部キーが1件も取得できていません");
      for (Map<String, Object> reference : referencingColumns) {
        String table = reference.get("schema_name") + "." + reference.get("table_name");
        String column = (String) reference.get("column_name");
        Integer remaining =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = 1", Integer.class);
        assertEquals(0, remaining, table + "." + column + " に削除対象アカウントを参照する行が残っています");
      }
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
    @Order(4)
    @DisplayName("異常系：処理途中で例外が発生した場合、それまでの削除を含めてすべてロールバックされること")
    void delete_rollbackOnFailure() {
      // @Sqlによるフィクスチャ投入はこのテストメソッドのトランザクション内で行われるため、
      // 後段でTestTransaction.flagForRollback()するとフィクスチャ投入自体も巻き戻ってしまう。
      // それを避けるため、一度物理コミットしてフィクスチャを確定させてから新しいトランザクションを開始する
      TestTransaction.flagForCommit();
      TestTransaction.end();
      TestTransaction.start();

      // アカウント本体の物理削除で例外が発生するようにする。
      // 以前は common.location_mst の外部キー違反（ON DELETE RESTRICT）を利用していたが、
      // ロケーションマスタは削除対象に含まれるようになったため意図的な失敗を作れない。
      // 参照元テーブルに依存せず「最後のステップで落ちたら全部戻る」ことだけを検証する
      doThrow(new DataIntegrityViolationException("意図的な削除失敗"))
          .when(accountMapper)
          .delete(any(AccountCondition.class));

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
						common.inquiry_reply_mst,
						common.inquiry_mst,
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
