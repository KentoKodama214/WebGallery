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

      // 「閲覧者」として他人のギャラリーに残したログは、行を消さず個人データだけを匿名化すること
      // （閲覧者カラムは外部キーを持たないため、放置してもアカウント削除は成功してしまう）
      Map<String, Object> anonymizedViewLog =
          jdbcTemplate.queryForMap(
              "SELECT account_no, ip_address, country, region FROM photo.photo_view_log WHERE photo_view_log_no=2");
      assertEquals(0L, anonymizedViewLog.get("account_no"));
      assertEquals("", anonymizedViewLog.get("ip_address"));
      assertEquals("", anonymizedViewLog.get("country"));
      assertEquals("", anonymizedViewLog.get("region"));

      Map<String, Object> anonymizedFilterLog =
          jdbcTemplate.queryForMap(
              "SELECT account_no, tag_list, referer, ip_address, country, region FROM photo.photo_list_filter_log WHERE photo_list_filter_log_no=2");
      assertEquals(0L, anonymizedFilterLog.get("account_no"));
      assertEquals("", anonymizedFilterLog.get("tag_list"));
      assertEquals("", anonymizedFilterLog.get("referer"));
      assertEquals("", anonymizedFilterLog.get("ip_address"));
      assertEquals("", anonymizedFilterLog.get("country"));
      assertEquals("", anonymizedFilterLog.get("region"));

      // 所有者（account_no=2）側の分析データとしての行自体は残ること
      Integer remainingViewLogForOther =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_view_log where photo_account_no=2", Integer.class);
      assertEquals(1, remainingViewLogForOther);
      Integer remainingFilterLogForOther =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM photo.photo_list_filter_log where photo_account_no=2",
              Integer.class);
      assertEquals(1, remainingFilterLogForOther);

      // 削除時点で未削除だった写真番号が記録されていること
      assertFalse(account.getDeletedPhotoNoList().isEmpty());
      assertTrue(account.getDeletedPhotoNoList().toList().contains(new PhotoNo(1L)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：common.accountを参照する全テーブルから、削除対象アカウントを参照する行が消えていること")
    void delete_removesAllForeignKeyReferences() {
      // common.account(account_no) を参照する外部キーはいずれも ON DELETE RESTRICT / NO ACTION のため、
      // 参照元テーブルの削除漏れはアカウント本体の物理削除を外部キー違反で失敗させる。
      // pg_constraint から参照元を動的に列挙し、テーブル追加時の削除漏れを機械的に検出する
      List<Map<String, Object>> referencingColumns = selectColumnsReferencingAccountNo();
      assertFalse(referencingColumns.isEmpty(), "common.accountを参照する外部キーが1件も取得できていません");

      // 削除前に参照が存在することを確かめる。これが無いと、フィクスチャに行が無いテーブルは
      // 削除処理が抜けていても「削除後0件」で通ってしまい、テーブル追加時の漏れを検出できない
      for (Map<String, Object> reference : referencingColumns) {
        String table = tableNameOf(reference);
        String column = columnNameOf(reference);
        if (isGuardedByDeletionBlock(table, column)) {
          // 削除をブロックする仕様の参照は、そもそもフィクスチャに存在させられない
          assertEquals(
              0,
              countReferencing(table, column),
              table + "." + column + " は削除ブロック対象のため、フィクスチャに行を作ってはいけません");
          continue;
        }
        assertTrue(
            countReferencing(table, column) > 0,
            table + "." + column + " を参照する行がフィクスチャに存在しません。参照を作ってから削除を検証してください");
      }

      Account account = Account.forDelete(new AccountNo(1L));
      accountAggregateRepositoryImpl.delete(account);

      for (Map<String, Object> reference : referencingColumns) {
        String table = tableNameOf(reference);
        String column = columnNameOf(reference);
        assertEquals(
            0, countReferencing(table, column), table + "." + column + " に削除対象アカウントを参照する行が残っています");
      }
    }

    /**
     * {@code common.account(account_no)}を参照する外部キーの参照元カラムを列挙する
     *
     * <p>複合外部キーでも{@code account_no}を参照する列を取り逃さないよう、{@code conkey}を全要素展開する
     *
     * @return スキーマ名・テーブル名・カラム名のリスト
     */
    private List<Map<String, Object>> selectColumnsReferencingAccountNo() {
      return jdbcTemplate.queryForList(
          """
          SELECT DISTINCT
              src_ns.nspname  AS schema_name,
              src.relname     AS table_name,
              src_att.attname AS column_name
          FROM pg_constraint c
          JOIN pg_class src ON src.oid = c.conrelid
          JOIN pg_namespace src_ns ON src_ns.oid = src.relnamespace
          JOIN pg_class tgt ON tgt.oid = c.confrelid
          JOIN pg_namespace tgt_ns ON tgt_ns.oid = tgt.relnamespace
          JOIN LATERAL unnest(c.conkey, c.confkey) AS k(conkey, confkey) ON true
          JOIN pg_attribute src_att
            ON src_att.attrelid = c.conrelid AND src_att.attnum = k.conkey
          JOIN pg_attribute tgt_att
            ON tgt_att.attrelid = c.confrelid AND tgt_att.attnum = k.confkey
          WHERE c.contype = 'f'
            AND tgt_ns.nspname = 'common'
            AND tgt.relname = 'account'
            AND tgt_att.attname = 'account_no'
          """);
    }

    /**
     * 参照元テーブルの完全修飾名を取り出す
     *
     * @param reference {@link #selectColumnsReferencingAccountNo()}の1行
     * @return {@code スキーマ名.テーブル名}
     */
    private String tableNameOf(Map<String, Object> reference) {
      return reference.get("schema_name") + "." + reference.get("table_name");
    }

    /**
     * 参照元カラム名を取り出す
     *
     * @param reference {@link #selectColumnsReferencingAccountNo()}の1行
     * @return カラム名
     */
    private String columnNameOf(Map<String, Object> reference) {
      return (String) reference.get("column_name");
    }

    /**
     * 削除対象アカウント（account_no=1）を参照する行数を数える
     *
     * @param table 参照元テーブルの完全修飾名
     * @param column 参照元カラム名
     * @return 行数
     */
    private Integer countReferencing(String table, String column) {
      return jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = 1", Integer.class);
    }

    /**
     * 「参照が残っている場合は削除自体をブロックする」仕様の外部キーかどうかを判定する
     *
     * <p>{@code common.inquiry_reply_mst.admin_account_no}だけは、参照を解消する（＝返信を削除する）と
     * 無関係な第三者のお問い合わせスレッドから回答本文だけが消えてしまうため、Repositoryでは削除せず {@code
     * AccountServiceImpl#deleteAccount}がアカウント削除自体を拒否する。 ブロックされることの検証は{@code
     * AccountServiceImplIntegrationTest}側で行う
     *
     * @param table 参照元テーブルの完全修飾名
     * @param column 参照元カラム名
     * @return 削除ブロックで守る参照の場合、true
     */
    private boolean isGuardedByDeletionBlock(String table, String column) {
      return "common.inquiry_reply_mst".equals(table) && "admin_account_no".equals(column);
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
