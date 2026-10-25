import { Client } from "pg";
import { SORT_EARLY_TEST_ACCOUNT_ID_PREFIX, TEST_ACCOUNT_ID_PREFIX } from "./auth";
import { DB_CONFIG } from "./db";

/**
 * `generateTestAccountId`・`generateSortEarlyTestAccountId`（`fixtures/auth.ts`）で生成した
 * E2E用の使い捨てアカウントと、その関連データ（写真・ポートフォリオ・お問い合わせ・ログイン履歴等）を
 * まとめて削除する
 *
 * 削除は1トランザクションで行い、途中で失敗した場合はロールバックする。
 */
export async function deleteE2eTestAccounts(): Promise<void> {
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
    // ポートフォリオ写真は写真マスタ・ポートフォリオを、ポートフォリオタグはポートフォリオを参照するため、
    // 写真マスタ・ポートフォリオより先に削除する
    await client.query(
      `DELETE FROM photo.portfolio_photo
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM photo.portfolio_tag_mst
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
    );
    await client.query(
      `DELETE FROM photo.portfolio_mst
        WHERE account_no IN (SELECT account_no FROM target_accounts)`
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
