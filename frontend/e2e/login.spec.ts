import { test, expect } from "@playwright/test";

test.describe("ログインページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/login");
  });

  test("ログインフォームが表示されること", async ({ page }) => {
    await expect(page.getByPlaceholder("User ID")).toBeVisible();
    await expect(page.getByPlaceholder("Password")).toBeVisible();
    await expect(page.getByRole("button", { name: "Log in" })).toBeVisible();
    await expect(page.getByText("Forgot your password?")).toBeVisible();
    await expect(page.getByText("Create an account")).toBeVisible();
  });

  test("空のフォームで送信するとエラーが表示されること", async ({ page }) => {
    await page.getByRole("button", { name: "Log in" }).click();

    // APIへのリクエストが失敗し、エラーメッセージが表示される
    await expect(page.locator("text=ログインに失敗しました")).toBeVisible({
      timeout: 5000,
    });
  });

  test("不正な認証情報でエラーメッセージが表示されること", async ({ page }) => {
    await page.getByPlaceholder("User ID").fill("invaliduser");
    await page.getByPlaceholder("Password").fill("invalidpass");
    await page.getByRole("button", { name: "Log in" }).click();

    // バックエンドが起動していない場合はネットワークエラー、
    // 起動している場合は認証エラーが表示される
    await expect(
      page.locator("p").filter({ hasText: /(間違っています|失敗しました)/ })
    ).toBeVisible({ timeout: 5000 });
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/ログイン/);
  });
});
