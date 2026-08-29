import { test, expect } from "@playwright/test";

test.describe("管理者用アカウント管理ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/admin/account_management");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});
