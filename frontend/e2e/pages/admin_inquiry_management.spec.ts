import { test, expect } from "@playwright/test";

test.describe("管理者用お問い合わせ管理ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/admin/inquiry_management");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/お問い合わせ管理/);
  });

  test("未ログイン状態では管理者権限エラーが表示されること", async ({ page }) => {
    await expect(page.locator("text=管理者権限がありません")).toBeVisible({
      timeout: 5000,
    });
  });
});

test.describe("管理者用お問い合わせ詳細ページ", () => {
  test("不正なinquiryIdクエリの場合は『お問い合わせが見つかりません』を表示する", async ({
    page,
  }) => {
    await page.goto("/admin/inquiry_management/detail?inquiryId=abc");

    await expect(page.getByText("お問い合わせが見つかりません")).toBeVisible();
  });

  test("未ログイン状態では管理者権限エラーが表示されること", async ({ page }) => {
    await page.goto("/admin/inquiry_management/detail?inquiryId=1");

    await expect(page.locator("text=管理者権限がありません")).toBeVisible({
      timeout: 5000,
    });
  });
});
