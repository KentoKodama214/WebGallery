import { test, expect } from "@playwright/test";

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
