import { test, expect } from "@playwright/test";

test.describe("写真詳細ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(
      "/photo/e2etestaccount/photo_detail?accountNo=999999999&photoNo=999999999"
    );
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/写真詳細/);
  });

  test("ヘッダーメニューが表示されること", async ({ page }) => {
    await expect(page.getByTestId("hamburger-button")).toBeVisible();
  });

  test("存在しない写真の場合はエラーが表示されること", async ({ page }) => {
    await expect(
      page.getByText("写真詳細の取得に失敗しました")
    ).toBeVisible({ timeout: 10000 });
  });
});
