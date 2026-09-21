import { test as base, expect, type Page } from "@playwright/test";
import { Client } from "pg";
import { generateSortEarlyTestAccountId, TEST_USER_PASSWORD } from "./auth";
import { DB_CONFIG } from "./db";

/**
 * 指定したアカウントIDの権限区分を管理者（administrator）に書き換える
 *
 * バックエンドは管理者権限のAPI（アカウント権限変更）自体に`@RequireAdminAuthority`を要求するため、
 * UI操作だけでは最初の管理者アカウントを作れない（鶏と卵）。E2E専用にDBを直接更新して初期の
 * 管理者アカウントを用意する。
 *
 * @param accountId 管理者権限を付与するアカウントID
 */
async function grantAdministratorAuthority(accountId: string): Promise<void> {
  const client = new Client(DB_CONFIG);
  await client.connect();
  try {
    await client.query(
      `UPDATE common.account_authority
          SET authority_kbn = 'administrator', updated_at = now()
        WHERE account_no = (SELECT account_no FROM common.account WHERE account_id = $1)`,
      [accountId]
    );
  } finally {
    await client.end();
  }
}

type AdminFixtures = {
  adminUser: { accountId: string; password: string };
  adminPage: Page;
};

/**
 * 管理者権限が必要なフローのE2Eテスト用に拡張した `test`
 *
 * `fixtures/auth.ts`と同様、ワーカーごとに専用の使い捨て管理者アカウントを1つだけ登録・ログインし、
 * そのワーカー内の全テストで同一の `adminPage` を使い回す（cookie・セッション競合を避けるため）。
 * 管理者権限は登録直後にDBを直接更新して付与する（JWT検証時のアカウント情報キャッシュ（既定TTL10秒）は
 * アカウントIDキー方式のため、この付与後に初めてログインする本フィクスチャでは古い権限を拾わない）。
 *
 * 使い方: 管理者ログイン済み状態が必要な `describe` ブロックでのみ、
 * `@playwright/test` の代わりにこのモジュールから `test`/`expect` をimportし、
 * テスト引数の `page` は使わず `adminPage`（と必要なら `adminUser`）を使う。
 */
export const test = base.extend<object, AdminFixtures>({
  adminUser: [
    async ({}, use, workerInfo) => {
      await use({
        accountId: generateSortEarlyTestAccountId(workerInfo.workerIndex),
        password: TEST_USER_PASSWORD,
      });
    },
    { scope: "worker" },
  ],

  adminPage: [
    async ({ browser, adminUser }, use, workerInfo) => {
      const context = await browser.newContext({
        baseURL: workerInfo.project.use.baseURL,
      });
      const page: Page = await context.newPage();

      await page.goto("/register");
      await page.getByPlaceholder("半角英数字で8〜20文字").fill(adminUser.accountId);
      await page.locator('label:has-text("アカウント名") + input').fill("E2E Admin User");
      await page
        .getByPlaceholder("英字と数字を含む半角8〜72文字")
        .fill(adminUser.password);
      await page.getByRole("button", { name: "登録" }).click();
      await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
        timeout: 10000,
      });

      await grantAdministratorAuthority(adminUser.accountId);

      await page.goto("/login");
      await page.getByPlaceholder("User ID").fill(adminUser.accountId);
      await page.getByPlaceholder("Password").fill(adminUser.password);
      await page.getByRole("button", { name: "Log in" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${adminUser.accountId}/photo_list`), {
        timeout: 10000,
      });

      await use(page);
      await context.close();
    },
    { scope: "worker" },
  ],
});

export { expect };
