import { test, expect } from "@playwright/test";

test.describe("アカウント設定ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    // accountId はアカウントID形式（半角英数字8〜16文字）でないとページ側で
    // 「ページが見つかりません」になり、フォーム（＝未ログイン時の /login 誘導）に到達しない
    await page.goto("/e2etestaccount/account_setting");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });

  test("アカウントID形式でない accountId は『ページが見つかりません』を表示する", async ({
    page,
  }) => {
    await page.goto("/e2e-test-account/account_setting");

    await expect(page.getByText("ページが見つかりません")).toBeVisible();
  });
});
