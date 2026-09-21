import { test, expect } from "@playwright/test";
import { generateTestAccountId, TEST_USER_PASSWORD } from "../fixtures/auth";

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
});

test.describe("写真一覧ページ（ログイン済み・写真0件の実アカウント）", () => {
  test("写真が1枚もない場合は『写真がありません』が表示されること", async ({
    page,
  }, testInfo) => {
    // fixtures/auth.ts の workerPage は同一ワーカー内の他ファイルと使い回すため、
    // 「写真0件」を前提とするこのテストでは使わず、ここで使い捨てアカウントを
    // 個別に登録する（他テストの写真登録と実行順で競合しないようにするため）
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await page.goto("/register");
    await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
    await page.locator('label:has-text("アカウント名") + input').fill("E2E No Photo User");
    await page.getByPlaceholder("英字と数字を含む半角8〜72文字").fill(TEST_USER_PASSWORD);
    await page.getByRole("button", { name: "登録" }).click();
    await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
      timeout: 10000,
    });

    await page.goto("/login");
    await page.getByPlaceholder("User ID").fill(accountId);
    await page.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
    await page.getByRole("button", { name: "Log in" }).click();
    await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
      timeout: 10000,
    });

    await expect(page.getByText("写真がありません")).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("filter-trigger")).toBeVisible();
  });
});
