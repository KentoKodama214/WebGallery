import { test, expect, type Browser, type Page } from "@playwright/test";
import {
  test as adminTest,
  expect as adminExpect,
} from "../fixtures/admin";
import { generateSortEarlyTestAccountId, TEST_USER_PASSWORD } from "../fixtures/auth";

test.describe("管理者用アカウント管理ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/admin/account_management");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/アカウント管理/);
  });

  test("未ログイン状態では管理者権限エラーが表示されること", async ({ page }) => {
    await expect(page.locator("text=管理者権限がありません")).toBeVisible({
      timeout: 5000,
    });
  });
});

/**
 * 一覧の対象アカウント行を探す。ローカルで繰り返しE2Eを実行すると一覧の件数が
 * 増えていき、目的の行が1ページ目に収まらない場合があるため、
 * 「＋もっと見る」で最終ページまで読み進めながら探す。
 */
async function revealAccountRow(page: Page, accountId: string) {
  // 初回表示・権限変更後の再取得中は一覧テーブル自体が「読み込み中...」に置き換わるため、
  // テーブルが再表示されるのを待ってから行を探す
  await page.locator("table").first().waitFor({ state: "visible", timeout: 10000 });

  const row = page.getByRole("row", { name: new RegExp(accountId) });
  const showMoreButton = page.getByTestId("show-more-button");
  for (let i = 0; i < 50; i++) {
    if ((await row.count()) > 0) return row;
    if ((await showMoreButton.count()) === 0) break;
    await showMoreButton.click();
    // 読み込み中はボタンがdisabledのまま。次ページの反映（ボタンが再度有効になる、
    // または最終ページで消滅する）を待つ
    await expect(async () => {
      const isStillLoading = await showMoreButton.isDisabled().catch(() => false);
      expect(isStillLoading).toBe(false);
    }).toPass({ timeout: 20000 });
  }
  return row;
}

/** 管理対象として操作するための、専用の一般アカウントを登録・ログインする */
async function registerTargetAccount(
  browser: Browser,
  baseURL: string | undefined,
  workerIndex: number
): Promise<{ page: Page; accountId: string }> {
  const accountId = generateSortEarlyTestAccountId(workerIndex);
  const context = await browser.newContext({ baseURL });
  const page = await context.newPage();

  await page.goto("/register");
  await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
  await page.locator('label:has-text("アカウント名") + input').fill("E2E Admin Target User");
  await page
    .getByPlaceholder("英字と数字を含む半角8〜72文字")
    .fill(TEST_USER_PASSWORD);
  await page.getByRole("button", { name: "登録" }).click();
  await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
    timeout: 10000,
  });

  return { page, accountId };
}

// ローカルで繰り返しE2Eを実行すると一覧の件数（1ページ20件）が増え、目的の行に
// 辿り着くまでの「もっと見る」ページ送りが多くなる。既定の30秒テストタイムアウトでは
// 不足する場合があるため、この describe 内のテストは長めのタイムアウトを設定する
adminTest.describe("管理者用アカウント管理ページ（ログイン済み・管理者）", () => {
  adminTest.describe.configure({ timeout: 60_000 });

  adminTest(
    "ログインページへリダイレクトされず、アカウント一覧が表示されること",
    async ({ adminPage: page, adminUser }) => {
      await page.goto("/admin/account_management");

      await adminExpect(page).toHaveTitle(/アカウント管理/);
      await adminExpect(page.getByText("管理者権限がありません")).toHaveCount(0);
      const ownRow = await revealAccountRow(page, adminUser.accountId);
      await adminExpect(ownRow).toBeVisible();
      // 自分自身の行には権限編集ボタンを表示しない
      await adminExpect(ownRow.getByRole("button", { name: "編集" })).toHaveCount(0);
    }
  );

  adminTest(
    "対象アカウントの権限を変更すると、一覧の表示に反映されること",
    async ({ adminPage: page, browser }, testInfo) => {
      const target = await registerTargetAccount(
        browser,
        testInfo.project.use.baseURL,
        testInfo.workerIndex
      );

      await page.goto("/admin/account_management");
      const targetRow = await revealAccountRow(page, target.accountId);
      // 新規登録直後の権限区分は既定でMINI（簡易ユーザー）
      await adminExpect(targetRow.getByText("簡易ユーザー")).toBeVisible();

      await targetRow.getByRole("button", { name: "編集" }).click();
      const dialog = page.getByRole("dialog", { name: "権限の編集" });
      await adminExpect(dialog).toBeVisible();
      await dialog.locator("select").selectOption("special-user");
      await dialog.getByRole("button", { name: "登録" }).click();

      await adminExpect(page.getByText("アカウントの権限を変更しました")).toBeVisible({
        timeout: 10000,
      });
      const updatedRow = await revealAccountRow(page, target.accountId);
      await adminExpect(updatedRow.getByText("特別ユーザー")).toBeVisible();

      await target.page.close();
    }
  );

  adminTest(
    "対象アカウントを強制ロックすると状態がロック中になり、ロック解除で有効に戻ること",
    async ({ adminPage: page, browser }, testInfo) => {
      const target = await registerTargetAccount(
        browser,
        testInfo.project.use.baseURL,
        testInfo.workerIndex
      );

      await page.goto("/admin/account_management");
      let targetRow = await revealAccountRow(page, target.accountId);
      await adminExpect(targetRow.getByText("有効")).toBeVisible();

      await targetRow.getByRole("button", { name: "強制ロック" }).click();
      await adminExpect(page.getByTestId("lock-confirm-dialog")).toBeVisible();
      await page.getByRole("button", { name: "実行" }).click();

      await adminExpect(page.getByText("アカウントをロックしました")).toBeVisible({
        timeout: 10000,
      });
      targetRow = await revealAccountRow(page, target.accountId);
      await adminExpect(targetRow.getByText("ロック中")).toBeVisible();

      await targetRow.getByRole("button", { name: "ロック解除" }).click();
      await adminExpect(page.getByTestId("lock-confirm-dialog")).toBeVisible();
      await page.getByRole("button", { name: "実行" }).click();

      await adminExpect(page.getByText("アカウントのロックを解除しました")).toBeVisible({
        timeout: 10000,
      });
      targetRow = await revealAccountRow(page, target.accountId);
      await adminExpect(targetRow.getByText("有効")).toBeVisible();

      await target.page.close();
    }
  );
});
