import { Client } from "pg";
import {
  SORT_EARLY_TEST_ACCOUNT_ID_PREFIX,
  TEST_ACCOUNT_ID_PREFIX,
} from "./fixtures/auth";
import { DB_CONFIG } from "./fixtures/db";

/**
 * `scripts/e2e.sh`のローカル実行はdocker-composeの永続的なPostgreSQLコンテナを使うため、
 * `generateTestAccountId`・`generateSortEarlyTestAccountId`（`fixtures/auth.ts`）で
 * 生成する使い捨てアカウントが実行のたびに蓄積し続け、管理者用一覧等のページング検証が
 * 徐々に遅くなっていく。テスト実行前に、それらE2E生成アカウントとその関連データを
 * まとめて削除する。
 *
 * CI（`.github/workflows/test.yml`）はジョブごとに新規のPostgreSQLコンテナを使うため、
 * ここでの削除は常に0件で空振りになるだけで実害はない。
 */
export default async function globalSetup(): Promise<void> {
  const client = new Client(DB_CONFIG);
  await client.connect();
  try {
    await client.query("BEGIN");

    await client.query(
      `CREATE TEMP TABLE target_accounts ON COMMIT DROP AS
         SELECT account_no FROM common.account
        WHERE account_id LIKE $1 OR account_id LIKE $2`,
      [`${TEST_ACCOUNT_ID_PREFIX}%`, `${SORT_EARLY_TEST_ACCOUNT_ID_PREFIX}%`]
    );
    await client.query(
      `CREATE TEMP TABLE target_photos ON COMMIT DROP AS
         SELECT account_no, photo_no FROM photo.photo_mst
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );

    // 外部キー制約に違反しないよう、参照される側（account・photo_mst）より先に
    // 参照する側のテーブルから削除する
    await client.query(
      `DELETE FROM photo.photo_view_log
        WHERE (photo_account_no, photo_no) IN (SELECT account_no, photo_no FROM target_photos)
           OR account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM photo.photo_favorite
        WHERE account_no IN (SELECT account_no FROM target_accounts)
           OR (favorite_photo_account_no, favorite_photo_no)
               IN (SELECT account_no, photo_no FROM target_photos)`
    );
    await client.query(
      `DELETE FROM photo.photo_tag_mst
        WHERE (account_no, photo_no) IN (SELECT account_no, photo_no FROM target_photos)`
    );
    await client.query(
      `DELETE FROM photo.photo_mst
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM photo.photo_list_filter_log
        WHERE photo_account_no IN (SELECT account_no FROM target_accounts)
           OR account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM common.inquiry_reply_mst
        WHERE admin_account_no IN (SELECT account_no FROM target_accounts)
           OR inquiry_id IN (
                SELECT id FROM common.inquiry_mst
                 WHERE account_no IN (SELECT account_no FROM target_accounts)
              )`
    );
    await client.query(
      `DELETE FROM common.inquiry_mst
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM common.refresh_token
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM common.login_history
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM common.location_mst
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM common.account_authority
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM common.account
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );

    await client.query("COMMIT");
  } catch (error) {
    await client.query("ROLLBACK").catch(() => {});
    throw error;
  } finally {
    await client.end();
  }
}
