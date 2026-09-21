import { test, expect } from "@playwright/test";

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
