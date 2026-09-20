import { test, expect } from "@playwright/test";
import { test as authTest, expect as authExpect } from "../fixtures/auth";

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

authTest.describe("アカウント設定ページ（ログイン済み・本人）", () => {
  authTest(
    "ログインページへリダイレクトされず、自分のアカウント情報が表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/${testUser.accountId}/account_setting`);

      await authExpect(page).toHaveURL(new RegExp(`/${testUser.accountId}/account_setting$`));
      await authExpect(page).toHaveTitle(/アカウント設定/);
      await authExpect(page.getByLabel("アカウントID")).toHaveValue(testUser.accountId);
      await authExpect(page.getByLabel("アカウント名")).toHaveValue("E2E Auth User");

      // ヘッダーが認証済み状態のメニュー（Sign Out）になっていること
      await page.getByTestId("hamburger-button").click();
      await authExpect(page.getByTestId("logout-button")).toBeVisible();
    }
  );
});
