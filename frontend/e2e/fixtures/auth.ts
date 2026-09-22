import { test as base, expect, type Page } from "@playwright/test";

/** `workerPage` が登録するテストアカウントのパスワード（E2E専用、本番では使用しない） */
export const TEST_USER_PASSWORD = "E2eTestPass123";

/** ワーカーごとに使い捨てで登録するテストアカウントの認証情報 */
export interface TestUserCredentials {
  accountId: string;
  password: string;
}

/**
 * `generateTestAccountId`が生成するアカウントIDの接頭辞。
 * `global-setup.ts`でE2E生成アカウントを判別してクリーンアップする際にも使う。
 */
export const TEST_ACCOUNT_ID_PREFIX = "e2ew";

/**
 * アカウントID制約（半角英数字8〜20文字）を満たす、ワーカー単位で一意なアカウントIDを生成する
 *
 * @param workerIndex Playwrightのワーカー番号
 * @returns 生成されたアカウントID
 */
export function generateTestAccountId(workerIndex: number): string {
  const randomPart = Math.random().toString(36).slice(2, 8);
  return `${TEST_ACCOUNT_ID_PREFIX}${workerIndex}${randomPart}`;
}

/** `generateSortEarlyTestAccountId`が生成するアカウントIDの接頭辞（`TEST_ACCOUNT_ID_PREFIX`を参照） */
export const SORT_EARLY_TEST_ACCOUNT_ID_PREFIX = "00e2w";

/**
 * account_id ASC で並ぶ管理者用アカウント一覧（1ページ20件）で、ローカルに蓄積した
 * 過去のE2E実行分を含めても1ページ目に収まりやすいアカウントIDを生成する。
 *
 * 先頭を数字にすることで、`e2ew...`等の英字始まりの既存アカウントより辞書順で先に来る
 * （PostgreSQLの既定コレーションでは数字は英字より小さい）。
 *
 * @param workerIndex Playwrightのワーカー番号
 * @returns 生成されたアカウントID
 */
export function generateSortEarlyTestAccountId(workerIndex: number): string {
  const randomPart = Math.random().toString(36).slice(2, 8);
  return `${SORT_EARLY_TEST_ACCOUNT_ID_PREFIX}${workerIndex}${randomPart}`;
}

/**
 * アカウント登録ページで新規アカウントを登録し、登録完了モーダルが表示されるまで待つ
 *
 * @param page 操作対象のページ
 * @param accountId 登録するアカウントID（半角英数字8〜20文字）
 * @param accountName 登録するアカウント名
 * @param password 登録するパスワード（省略時は`TEST_USER_PASSWORD`）
 */
export async function registerAccount(
  page: Page,
  accountId: string,
  accountName: string,
  password: string = TEST_USER_PASSWORD
): Promise<void> {
  await page.goto("/register");
  await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
  await page.locator('label:has-text("アカウント名") + input').fill(accountName);
  await page.getByPlaceholder("英字と数字を含む半角8〜72文字").fill(password);
  await page.getByRole("button", { name: "登録" }).click();
  await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
    timeout: 10000,
  });
}

/**
 * ログインページで指定したアカウントにログインし、写真一覧への遷移を待つ
 *
 * @param page 操作対象のページ
 * @param accountId ログインするアカウントID
 * @param password ログインするパスワード（省略時は`TEST_USER_PASSWORD`）
 */
export async function login(
  page: Page,
  accountId: string,
  password: string = TEST_USER_PASSWORD
): Promise<void> {
  await page.goto("/login");
  await page.getByPlaceholder("User ID").fill(accountId);
  await page.getByPlaceholder("Password").fill(password);
  await page.getByRole("button", { name: "Log in" }).click();
  await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
    timeout: 10000,
  });
}

/**
 * 新規アカウントを登録し、続けてそのアカウントでログインする（`registerAccount` + `login`）
 *
 * @param page 操作対象のページ
 * @param accountId 登録・ログインするアカウントID
 * @param accountName 登録するアカウント名
 * @param password 登録・ログインするパスワード（省略時は`TEST_USER_PASSWORD`）
 */
export async function registerAndLogin(
  page: Page,
  accountId: string,
  accountName: string,
  password: string = TEST_USER_PASSWORD
): Promise<void> {
  await registerAccount(page, accountId, accountName, password);
  await login(page, accountId, password);
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

      await registerAndLogin(page, testUser.accountId, "E2E Auth User", testUser.password);

      await use(page);
      await context.close();
    },
    { scope: "worker" },
  ],
});

export { expect };
