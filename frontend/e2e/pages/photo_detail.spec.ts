import { test, expect } from "@playwright/test";

test.describe("写真詳細ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(
      "/photo/e2etestaccount/photo_detail?photoNo=999999999"
    );
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/写真詳細/);
  });

  test("ヘッダーメニューが表示されること", async ({ page }) => {
    await expect(page.getByTestId("hamburger-button")).toBeVisible();
  });

  test("存在しないアカウントの場合は『写真が存在しません。』が表示されること", async ({
    page,
  }) => {
    // e2e.sh実行時はバックエンドが必ず起動しているため、アカウント不存在時の
    // 明確なエラー文言のみを検証する
    await expect(page.getByText("写真が存在しません。")).toBeVisible({ timeout: 10000 });
  });
});
