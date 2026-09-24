import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";

test.describe("ログインページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/login");
  });

  test("ログインフォームが表示されること", async ({ page }) => {
    await expect(page.getByPlaceholder("User ID")).toBeVisible();
    await expect(page.getByPlaceholder("Password")).toBeVisible();
    await expect(page.getByRole("button", { name: "Log in" })).toBeVisible();
    await expect(page.getByText("Create an account")).toBeVisible();
  });

  test("空のフォームで送信するとバリデーションエラーが表示されること", async ({ page }) => {
    await page.getByRole("button", { name: "Log in" }).click();

    // e2e.sh実行時はバックエンドが必ず起動しているため、
    // 明確にバックエンドのバリデーションエラー文言のみを検証する
    await expect(
      page.getByRole("alert").filter({ hasText: "入力内容に誤りがあります。再度入力してください。" })
    ).toBeVisible({ timeout: 10000 });
  });

  test("不正な認証情報でエラーメッセージが表示されること", async ({ page }) => {
    await page.getByPlaceholder("User ID").fill("invaliduser");
    await page.getByPlaceholder("Password").fill("invalidpass");
    await page.getByRole("button", { name: "Log in" }).click();

    await expect(
      page.getByRole("alert").filter({ hasText: "アカウントIDまたはパスワードが間違っています。" })
    ).toBeVisible({ timeout: 10000 });
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/ログイン/);
  });

  test("アクセシビリティ違反がないこと", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");
    await expectNoAccessibilityViolations(page);
  });

  test("画面表示が崩れていないこと（視覚回帰）", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "視覚回帰はchromiumプロジェクトのみで検証する");
    await expect(page).toHaveScreenshot("login.png", { fullPage: true, maxDiffPixelRatio: 0.02 });
  });
});
