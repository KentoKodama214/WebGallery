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

  test("一覧が表示されるか、取得エラーが表示されること", async ({ page }) => {
    const table = page.getByRole("columnheader", { name: "アカウント名" });
    const error = page.getByText("アカウント一覧の取得に失敗しました");
    await expect(table.or(error)).toBeVisible({ timeout: 10000 });
  });
});
