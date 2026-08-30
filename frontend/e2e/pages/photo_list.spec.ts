import { test, expect } from "@playwright/test";

test.describe("写真一覧ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/photo/e2etestaccount/photo_list");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/写真一覧/);
  });

  test("フィルタートリガーが表示されること", async ({ page }) => {
    await expect(page.getByTestId("filter-trigger")).toBeVisible();
  });

  test("フィルターパネルの開閉ができること", async ({ page }) => {
    const filterPanel = page.getByTestId("filter-panel");

    await page.getByTestId("filter-trigger").click();
    await expect(filterPanel).toHaveClass(/filterOpen/);

    await page.getByTestId("filter-close-button").click();
    await expect(filterPanel).not.toHaveClass(/filterOpen/);
  });

  test("写真一覧が表示されるか、取得エラーが表示されること", async ({ page }) => {
    const empty = page.getByText("写真がありません");
    // バックエンドの「写真が存在しません。」またはフロントの既定文言のいずれか
    const error = page.getByText(/写真が存在しません|写真一覧の取得に失敗しました/);
    await expect(empty.or(error)).toBeVisible({ timeout: 10000 });
  });

  test("アカウントID形式でない photoAccountId は『ギャラリーが見つかりません』を表示する", async ({
    page,
  }) => {
    // `;` を含む細工されたセグメント（Cookie名インジェクション対策の検証）
    await page.goto("/photo/aaaa1111%3B%20x/photo_list");
    await expect(page.getByText("ギャラリーが見つかりません")).toBeVisible();
    await expect(page.getByTestId("filter-trigger")).toHaveCount(0);
  });
});
