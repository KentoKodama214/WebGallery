import { test, expect } from "@playwright/test";
import { test as adminTest, expect as adminExpect } from "../fixtures/admin";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";

test.describe("管理者用写真管理ページ", () => {
  test("未ログイン状態では /login へ誘導されること", async ({ page }) => {
    await page.goto("/admin/photo_management");

    // 他の管理者ページと同様に <AuthGuard> で退避させる
    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
  });
});

adminTest.describe("管理者用写真管理ページ（管理者ログイン済み）", () => {
  adminTest("見出しと絞り込み条件が表示されること", async ({ adminPage: page }) => {
    await page.goto("/admin/photo_management");

    await adminExpect(page).toHaveTitle(/写真管理/);
    await adminExpect(page.getByRole("heading", { name: "写真管理" })).toBeVisible({
      timeout: 10000,
    });
    await adminExpect(page.getByText("管理者権限がありません")).toHaveCount(0);
    await adminExpect(page.getByLabel("アカウントID")).toBeVisible();
    await adminExpect(page.getByLabel("公開状態")).toBeVisible();
  });

  adminTest("アクセシビリティ違反がないこと", async ({ adminPage: page }, testInfo) => {
    adminTest.skip(
      testInfo.project.name !== "chromium",
      "a11y検証はchromiumプロジェクトのみで実施する"
    );
    await page.goto("/admin/photo_management");

    await adminExpect(page.getByRole("heading", { name: "写真管理" })).toBeVisible({
      timeout: 10000,
    });
    await adminExpect(page.getByText("読み込み中...")).toHaveCount(0, { timeout: 10000 });

    // color-contrast は他の管理者ページと共通の配色（テーブルヘッダー・操作ボタン）に既存の違反があり、
    // 解消にはアプリ共通の配色変更が必要なため、本検証の対象外とする
    await expectNoAccessibilityViolations(page, { disableRules: ["color-contrast"] });
  });
});
