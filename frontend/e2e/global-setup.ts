import type { FullConfig } from "@playwright/test";
import { Client } from "pg";
import {
  SORT_EARLY_TEST_ACCOUNT_ID_PREFIX,
  TEST_ACCOUNT_ID_PREFIX,
} from "./fixtures/auth";
import { DB_CONFIG } from "./fixtures/db";

/**
 * `next dev`が初回リクエスト時にオンデマンドでコンパイルするルートを、テスト開始前に一度叩いて
 * コンパイル済みにしておく
 *
 * ブラウザ別にE2Eジョブを分割した結果、`webkit-smoke`・`mobile-smoke`は先行するchromiumの
 * 実行による暖機を当てにできなくなり、未コンパイルのルートを最初に叩くようになった。
 * 暖機しない場合、次の2つの形で不定期に失敗する。
 *
 * - ページルート（`/login`等）が未コンパイルだとハイドレーション完了までの時間が延び、
 *   `page.goto`直後の`fill`が上書きされる（`fixtures/form.ts`の`fillStable`参照）
 * - APIプロキシルート（`src/app/api/[...path]/route.ts`）が未コンパイルだと、
 *   最初のフォーム送信の往復に10秒以上かかり、送信ボタンがdisabledのまま
 *   アサーションがタイムアウトする
 *
 * 暖機は最適化であり検証対象ではないため、失敗しても警告のみでテストは続行する。
 *
 * @param baseURL 対象アプリケーションのベースURL
 */
async function warmUpRoutes(baseURL: string): Promise<void> {
  // フォーム入力を伴うページ・cross-browser対象の写真一覧ページと、
  // バックエンドへの中継を行うAPIプロキシルート（認証不要かつ読み取り専用の都道府県一覧で代表させる）
  const paths = [
    "/login",
    "/register",
    "/photo/warmup/photo_list",
    "/api/v1/prefectures",
  ];
  const deadline = Date.now() + 60000;

  for (const path of paths) {
    const url = new URL(path, baseURL).toString();
    while (Date.now() < deadline) {
      try {
        const response = await fetch(url);
        // ボディまで読み切ってレンダリングを完了させる
        await response.text();
        break;
      } catch {
        // 開発サーバーの起動待ち。少し待って再試行する
        await new Promise((resolve) => setTimeout(resolve, 1000));
      }
    }
    if (Date.now() >= deadline) {
      console.warn(`[global-setup] ルートの暖機に失敗しました（続行します）: ${url}`);
      return;
    }
  }
  console.log(`[global-setup] ルートを暖機しました: ${paths.join(", ")}`);
}

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
export default async function globalSetup(config: FullConfig): Promise<void> {
  const baseURL = config.projects[0]?.use?.baseURL;
  if (baseURL) {
    await warmUpRoutes(baseURL);
  }

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
