import { test as base, expect, type Page } from "@playwright/test";

/** `workerPage` が登録するテストアカウントのパスワード（E2E専用、本番では使用しない） */
export const TEST_USER_PASSWORD = "E2eTestPass123";

/** ワーカーごとに使い捨てで登録するテストアカウントの認証情報 */
export interface TestUserCredentials {
  accountId: string;
  password: string;
}

/**
 * アカウントID制約（半角英数字8〜20文字）を満たす、ワーカー単位で一意なアカウントIDを生成する
 *
 * @param workerIndex Playwrightのワーカー番号
 * @returns 生成されたアカウントID
 */
export function generateTestAccountId(workerIndex: number): string {
  const randomPart = Math.random().toString(36).slice(2, 8);
  return `e2ew${workerIndex}${randomPart}`;
}

type AuthFixtures = {
  testUser: TestUserCredentials;
  workerPage: Page;
};

/**
 * 認証必須フローのE2Eテスト用に拡張した `test`
 *
 * バックエンドは（1）リフレッシュトークンを1回使い切りでローテーションし、
 * （2）ログイン時に同一アカウントの既存セッションを全て失効させる
 * （`AuthServiceImpl#login` の `revokeAllByAccountNo`）。そのため、
 * 単一の共有アカウント・storageStateファイルを複数テストや複数ワーカーで使い回すと、
 * 並列実行時に互いのセッションを奪い合って401になる。
 *
 * これを避けるため、ワーカーごとに専用の使い捨てアカウントを1つだけ登録・ログインし、
 * そのワーカー内の全テストで同一の `workerPage`（＝同一のブラウザコンテキスト・cookie）を
 * 使い回す。cookieのローテーションは同一コンテキスト内で自動的に追随するため、
 * テスト間の競合が起きない。
 *
 * 使い方: 認証済み状態が必要な `describe` ブロックでのみ、
 * `@playwright/test` の代わりにこのモジュールから `test`/`expect` をimportし、
 * テスト引数の `page` は使わず `workerPage`（と必要なら `testUser`）を使う。
 */
export const test = base.extend<object, AuthFixtures>({
  testUser: [
    async ({}, use, workerInfo) => {
      await use({
        accountId: generateTestAccountId(workerInfo.workerIndex),
        password: TEST_USER_PASSWORD,
      });
    },
    { scope: "worker" },
  ],

  workerPage: [
    async ({ browser, testUser }, use, workerInfo) => {
      const context = await browser.newContext({
        baseURL: workerInfo.project.use.baseURL,
      });
      const page: Page = await context.newPage();

      await page.goto("/register");
      await page.getByPlaceholder("半角英数字で8〜20文字").fill(testUser.accountId);
      await page.locator('label:has-text("アカウント名") + input').fill("E2E Auth User");
      await page
        .getByPlaceholder("英字と数字を含む半角8〜72文字")
        .fill(testUser.password);
      await page.getByRole("button", { name: "登録" }).click();
      await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
        timeout: 10000,
      });

      await page.goto("/login");
      await page.getByPlaceholder("User ID").fill(testUser.accountId);
      await page.getByPlaceholder("Password").fill(testUser.password);
      await page.getByRole("button", { name: "Log in" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${testUser.accountId}/photo_list`), {
        timeout: 10000,
      });

      await use(page);
      await context.close();
    },
    { scope: "worker" },
  ],
});

export { expect };
