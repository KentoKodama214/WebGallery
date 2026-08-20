import { test, expect } from "@playwright/test";

test.describe("アカウント設定ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/e2e-test-account/account_setting");

    await expect(page).toHaveURL(/\/login$/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});
