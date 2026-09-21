import { test, expect } from "@playwright/test";
import { generateTestAccountId, TEST_USER_PASSWORD } from "../fixtures/auth";

test.describe("アカウント登録ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/register");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/アカウント登録/);
  });

  test("登録フォームが表示されること", async ({ page }) => {
    await expect(page.getByText("Create an Account")).toBeVisible();
    await expect(page.getByPlaceholder("半角英数字で8〜20文字")).toBeVisible();
    await expect(page.getByPlaceholder("英字と数字を含む半角8〜72文字")).toBeVisible();
    await expect(page.getByRole("button", { name: "登録" })).toBeVisible();
    await expect(page.getByText("← back")).toBeVisible();
  });

  test("空のフォームで送信するとバリデーションエラーが表示されること", async ({ page }) => {
    await page.getByRole("button", { name: "登録" }).click();

    await expect(
      page.getByText("半角英数字で8〜20文字で入力してください")
    ).toBeVisible();
    await expect(page.getByText("アカウント名を入力してください")).toBeVisible();
    await expect(
      page.getByText("英字と数字を含む半角8〜72文字で入力してください")
    ).toBeVisible();
  });

  test("不正な形式のアカウントIDを入力するとエラーが表示されること", async ({ page }) => {
    const accountIdInput = page.getByPlaceholder("半角英数字で8〜20文字");
    await accountIdInput.fill("abc");
    await accountIdInput.blur();

    await expect(
      page.getByText("半角英数字で8〜20文字で入力してください")
    ).toBeVisible();
  });

  test("未来の日付を生年月日に入力して送信するとエラーが表示されること", async ({ page }) => {
    await page.getByPlaceholder("半角英数字で8〜20文字").fill("testuser01");
    await page.locator('label:has-text("アカウント名") + input').fill("テストユーザー");
    await page.getByPlaceholder("英字と数字を含む半角8〜72文字").fill("testpass123");
    await page.locator('label:has-text("生年月日") + input').fill("2099-01-01");

    await page.getByRole("button", { name: "登録" }).click();

    await expect(page.getByText("過去の日付を入力してください")).toBeVisible();
  });
});

test.describe("アカウント登録ページ（重複アカウントID）", () => {
  test("既に使用されているアカウントIDで登録すると、エラーメッセージが表示され登録完了モーダルは出ないこと", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);

    await page.goto("/register");
    await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
    await page
      .locator('label:has-text("アカウント名") + input')
      .fill("E2E Duplicate User 1");
    await page.getByPlaceholder("英字と数字を含む半角8〜72文字").fill(TEST_USER_PASSWORD);
    await page.getByRole("button", { name: "登録" }).click();
    await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
      timeout: 10000,
    });

    // 同じアカウントIDで再度登録を試みる
    await page.goto("/register");
    await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
    await page
      .locator('label:has-text("アカウント名") + input')
      .fill("E2E Duplicate User 2");
    await page.getByPlaceholder("英字と数字を含む半角8〜72文字").fill(TEST_USER_PASSWORD);
    await page.getByRole("button", { name: "登録" }).click();

    await expect(page.getByText("このアカウントIDは既に使われています")).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toHaveCount(0);
    await expect(page).toHaveURL(/\/register$/);
  });
});
