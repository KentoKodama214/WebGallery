import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

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

  test("存在しないアカウントの場合は『写真が存在しません。』が表示されること", async ({
    page,
  }) => {
    // e2e.sh実行時はバックエンドが必ず起動しているため、アカウント不存在時の
    // 明確なエラー文言のみを検証する（getByRole("alert")で明示的に絞り込む必要はなく、
    // このページのエラーはページ本文に直接テキストとして表示される）
    await expect(page.getByText("写真が存在しません。")).toBeVisible({ timeout: 10000 });
  });

  test("アカウントID形式でない photoAccountId は『ギャラリーが見つかりません』を表示する", async ({
    page,
  }) => {
    // `;` を含む細工されたセグメント（Cookie名インジェクション対策の検証）
    await page.goto("/photo/aaaa1111%3B%20x/photo_list");
    await expect(page.getByText("ギャラリーが見つかりません")).toBeVisible();
    await expect(page.getByTestId("filter-trigger")).toHaveCount(0);
  });

  test("アクセシビリティ違反がないこと", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");
    await expect(page.getByText("写真が存在しません。")).toBeVisible({ timeout: 10000 });
    await expectNoAccessibilityViolations(page);
  });
});

test.describe("写真一覧ページ（ログイン済み・写真0件の実アカウント）", () => {
  test("写真が1枚もない場合は『写真がありません』が表示されること", async ({
    page,
  }, testInfo) => {
    // fixtures/auth.ts の workerPage は同一ワーカー内の他ファイルと使い回すため、
    // 「写真0件」を前提とするこのテストでは使わず、ここで使い捨てアカウントを
    // 個別に登録する（他テストの写真登録と実行順で競合しないようにするため）
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E No Photo User");
    await login(page, accountId);

    await expect(page.getByText("写真がありません")).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("filter-trigger")).toBeVisible();
  });

  test("画面表示が崩れていないこと（視覚回帰）", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "視覚回帰はchromiumプロジェクトのみで検証する");

    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Visual Regression User");
    await login(page, accountId);

    await expect(page.getByText("写真がありません")).toBeVisible({ timeout: 10000 });
    await expect(page).toHaveScreenshot("photo_list_empty.png", {
      fullPage: true,
      maxDiffPixelRatio: 0.02,
    });
  });
});
