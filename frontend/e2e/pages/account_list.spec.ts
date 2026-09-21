import { test, expect, type Page } from "@playwright/test";
import { generateSortEarlyTestAccountId, TEST_USER_PASSWORD } from "../fixtures/auth";

test.describe("アカウント一覧ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/account_list");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/アカウント一覧/);
  });

  test("未ログイン状態ではヘッダーメニューにSign Inが表示されること", async ({ page }) => {
    await page.getByTestId("hamburger-button").click();
    const menu = page.getByTestId("overlay-menu");
    await expect(menu.getByText("Photographers")).toBeVisible();
    await expect(menu.getByText("Sign In")).toBeVisible();
  });

  test("一覧テーブルが表示されること", async ({ page }) => {
    // e2e.sh実行時はバックエンドが必ず起動しており、登録アカウント数によらず
    // テーブルのヘッダーは必ず描画される（0件でも空のtbodyでエラーにはならない）
    await expect(page.getByRole("columnheader", { name: "アカウント名" })).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByRole("columnheader", { name: "ギャラリー" })).toBeVisible();
  });
});

/**
 * 一覧の対象アカウント行を探す。ローカルで繰り返しE2Eを実行すると一覧の件数が
 * 増えていき、目的の行が1ページ目に収まらない場合があるため、
 * 「＋もっと見る」で最終ページまで読み進めながら探す
 * （`pages/admin_account_management.spec.ts`と同様のロジック）。
 */
async function revealAccountRow(page: Page, accountName: string) {
  await page.locator("table").first().waitFor({ state: "visible", timeout: 10000 });

  const row = page.getByRole("row", { name: new RegExp(accountName) });
  const showMoreButton = page.getByTestId("show-more-button");
  for (let i = 0; i < 50; i++) {
    if ((await row.count()) > 0) return row;
    if ((await showMoreButton.count()) === 0) break;
    await showMoreButton.click();
    await expect(async () => {
      const isStillLoading = await showMoreButton.isDisabled().catch(() => false);
      expect(isStillLoading).toBe(false);
    }).toPass({ timeout: 20000 });
  }
  return row;
}

test.describe("アカウント一覧ページ（ログイン済み）", () => {
  test.describe.configure({ timeout: 60_000 });

  test("ログイン済み状態ではヘッダーメニューがSign Outになり、自分の行からギャラリーへ遷移できること", async ({
    page,
  }, testInfo) => {
    // account_list はアカウントID昇順で表示され、ローカルの反復実行で件数が増えるほど
    // 目的の行を探すページ送りが増える。他テストの実アカウントと衝突しないよう、
    // ここでも sort-early な専用アカウントを新規登録して使う
    const accountId = generateSortEarlyTestAccountId(testInfo.workerIndex);
    const accountName = `E2E List User ${testInfo.workerIndex}${Date.now()}`;

    await page.goto("/register");
    await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
    await page.locator('label:has-text("アカウント名") + input').fill(accountName);
    await page.getByPlaceholder("英字と数字を含む半角8〜72文字").fill(TEST_USER_PASSWORD);
    await page.getByRole("button", { name: "登録" }).click();
    await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
      timeout: 10000,
    });

    await page.goto("/login");
    await page.getByPlaceholder("User ID").fill(accountId);
    await page.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
    await page.getByRole("button", { name: "Log in" }).click();
    await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
      timeout: 10000,
    });

    await page.goto("/account_list");
    await page.getByTestId("hamburger-button").click();
    const menu = page.getByTestId("overlay-menu");
    await expect(menu.getByTestId("logout-button")).toBeVisible();
    await expect(menu.getByText("Sign In")).toHaveCount(0);
    await page.keyboard.press("Escape");

    const row = await revealAccountRow(page, accountName);
    await expect(row).toBeVisible();

    await row.getByRole("link", { name: "ギャラリーを見る" }).click();
    await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
      timeout: 10000,
    });
  });
});
