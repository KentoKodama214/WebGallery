import { test, expect } from "@playwright/test";

test.describe("アカウント登録ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/register");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/アカウント登録/);
  });

  test("登録フォームが表示されること", async ({ page }) => {
    await expect(page.getByText("Create an Account")).toBeVisible();
    await expect(page.getByPlaceholder("半角英数字で8〜16文字")).toBeVisible();
    await expect(page.getByPlaceholder("英字と数字を含む半角8〜72文字")).toBeVisible();
    await expect(page.getByRole("button", { name: "登録" })).toBeVisible();
    await expect(page.getByText("← back")).toBeVisible();
  });

  test("空のフォームで送信するとバリデーションエラーが表示されること", async ({ page }) => {
    await page.getByRole("button", { name: "登録" }).click();

    await expect(
      page.getByText("半角英数字で8〜16文字で入力してください")
    ).toBeVisible();
    await expect(page.getByText("アカウント名を入力してください")).toBeVisible();
    await expect(
      page.getByText("英字と数字を含む半角8〜72文字で入力してください")
    ).toBeVisible();
  });

  test("不正な形式のアカウントIDを入力するとエラーが表示されること", async ({ page }) => {
    const accountIdInput = page.getByPlaceholder("半角英数字で8〜16文字");
    await accountIdInput.fill("abc");
    await accountIdInput.blur();

    await expect(
      page.getByText("半角英数字で8〜16文字で入力してください")
    ).toBeVisible();
  });

  test("未来の日付を生年月日に入力して送信するとエラーが表示されること", async ({ page }) => {
    await page.getByPlaceholder("半角英数字で8〜16文字").fill("testuser01");
    await page.locator('label:has-text("アカウント名") + input').fill("テストユーザー");
    await page.getByPlaceholder("英字と数字を含む半角8〜72文字").fill("testpass123");
    await page.locator('label:has-text("生年月日") + input').fill("2099-01-01");

    await page.getByRole("button", { name: "登録" }).click();

    await expect(page.getByText("過去の日付を入力してください")).toBeVisible();
  });
});
