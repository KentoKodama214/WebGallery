import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { generateTestAccountId, registerAccount } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";
import { fakePasskeyCount, installFakeAuthenticator } from "../fixtures/passkey";

test.describe("アカウント登録ページ", () => {
  test.beforeEach(async ({ page }) => {
    await installFakeAuthenticator(page.context());
    await page.goto("/register");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/アカウント登録/);
  });

  test("パスワード欄が無く、パスキー名の欄を含む登録フォームが表示されること", async ({ page }) => {
    await expect(page.getByText("Create an Account")).toBeVisible();
    await expect(page.getByPlaceholder("半角英数字で8〜20文字")).toBeVisible();
    await expect(page.getByLabel("パスキー名")).toHaveValue("最初のパスキー");
    await expect(page.getByLabel("パスワード")).toHaveCount(0);
    await expect(page.getByRole("button", { name: "登録", exact: true })).toBeVisible();
    await expect(page.getByText("← back")).toBeVisible();
  });

  test("空のフォームで送信するとバリデーションエラーが表示されること", async ({ page }) => {
    await page.getByRole("button", { name: "登録", exact: true }).click();

    await expect(
      page.getByText("半角英数字で8〜20文字で入力してください")
    ).toBeVisible();
    await expect(page.getByText("アカウント名を入力してください")).toBeVisible();
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
    await page.locator('label:has-text("生年月日") + input').fill("2099-01-01");

    await page.getByRole("button", { name: "登録", exact: true }).click();

    await expect(page.getByText("過去の日付を入力してください")).toBeVisible();
  });

  test("アクセシビリティ違反がないこと（バリデーションエラー表示時）", async ({
    page,
  }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");

    await page.getByRole("button", { name: "登録", exact: true }).click();
    await expect(page.getByText("アカウント名を入力してください")).toBeVisible();

    await expectNoAccessibilityViolations(page);
  });
});

test.describe("アカウント登録ページ（登録完了）", () => {
  test("登録するとパスキーが作成され、10個のリカバリーコードが表示され、保管の確認でログインページへ移ること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);

    const recoveryCodes = await registerAccount(page, accountId, "E2E Register Complete User");

    expect(recoveryCodes).toHaveLength(10);
    for (const code of recoveryCodes) {
      expect(code).toMatch(/^[0-9A-Z]{4}-[0-9A-Z]{4}-[0-9A-Z]{4}-[0-9A-Z]{4}$/);
    }
    expect(fakePasskeyCount(accountId)).toBe(1);
    const dialog = page.getByRole("dialog", { name: "アカウント登録完了" });
    await expect(dialog.getByRole("button", { name: "ダウンロード" })).toBeVisible();

    await dialog.getByRole("button", { name: "保管しました（ログインへ）" }).click();
    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
  });

  test("アクセシビリティ違反がないこと（リカバリーコード表示時）", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");

    await registerAccount(page, generateTestAccountId(testInfo.workerIndex), "E2E A11y User");

    await expectNoAccessibilityViolations(page);
  });
});

test.describe("アカウント登録ページ（重複アカウントID）", () => {
  test("既に使用されているアカウントIDで登録すると、パスキーを作成せずエラーメッセージが表示され登録完了モーダルは出ないこと", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Duplicate User 1");

    // 同じアカウントIDで再度登録を試みる
    await page.goto("/register");
    await fillStable(page.getByPlaceholder("半角英数字で8〜20文字"), accountId);
    await fillStable(page.locator('label:has-text("アカウント名") + input'), "E2E Duplicate User 2");
    await page.getByRole("button", { name: "登録", exact: true }).click();

    await expect(page.getByText("このアカウントIDは既に使われています")).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toHaveCount(0);
    await expect(page).toHaveURL(/\/register$/);
    // 重複は新規登録オプションの発行時点で弾くため、2つ目のパスキーは作られない
    expect(fakePasskeyCount(accountId)).toBe(1);
  });
});
