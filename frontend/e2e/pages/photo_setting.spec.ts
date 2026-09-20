import { test, expect } from "@playwright/test";
import { test as authTest, expect as authExpect } from "../fixtures/auth";

test.describe("写真設定ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/photo/e2etestaccount/photo_setting");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

authTest.describe("写真設定ページ（ログイン済み・本人）", () => {
  authTest(
    "ログインページへリダイレクトされず、新規登録フォームが表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);

      await authExpect(page).toHaveURL(new RegExp(`/photo/${testUser.accountId}/photo_setting$`));
      await authExpect(page).toHaveTitle(/写真設定/);
      await authExpect(page.getByText("この操作を行う権限がありません")).toHaveCount(0);
      await authExpect(page.getByTestId("image-input")).toBeAttached();
      await authExpect(page.getByTestId("japanese-title-input")).toBeVisible();
    }
  );

  authTest("他人のphotoAccountIdでは権限エラーが表示されること", async ({
    workerPage: page,
  }) => {
    await page.goto("/photo/e2eotheraccount1/photo_setting");

    await authExpect(page.getByText("この操作を行う権限がありません")).toBeVisible({
      timeout: 10000,
    });
  });
});
