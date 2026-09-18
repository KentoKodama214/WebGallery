import { test, expect } from "@playwright/test";

test.describe("お問い合わせページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

test.describe("お問い合わせ一覧ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry/list");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

test.describe("お問い合わせ詳細ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry/detail?inquiryNo=1");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});
