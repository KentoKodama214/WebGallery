import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { generateTestAccountId, registerAccount } from "../fixtures/auth";
import {
  cancelNextPasskeyOperation,
  installFakeAuthenticator,
  selectPasskeyAccount,
} from "../fixtures/passkey";

test.describe("ログインページ", () => {
  test.beforeEach(async ({ page }) => {
    await installFakeAuthenticator(page.context());
    await page.goto("/login");
  });

  test("パスワードの入力欄が無く、パスキーでのログインボタンと新規登録・復旧への導線が表示されること", async ({
    page,
  }) => {
    await expect(page.getByRole("button", { name: "パスキーでログイン" })).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByPlaceholder("Password")).toHaveCount(0);
    await expect(page.getByPlaceholder("User ID")).toHaveCount(0);
    await expect(page.getByRole("link", { name: "Create an account" })).toBeVisible();
    await expect(
      page.getByRole("link", { name: "パスキーを失った場合（リカバリーコードで復旧）" })
    ).toBeVisible();
  });

  test("登録済みのパスキーでログインでき、写真一覧へ遷移すること", async ({ page }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Login Page User");
    selectPasskeyAccount(accountId);

    await page.goto("/login");
    await page.getByRole("button", { name: "パスキーでログイン" }).click();

    await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
      timeout: 30000,
    });
  });

  test("サーバーに登録されていないパスキーではログインできず、エラーメッセージが表示されること", async ({
    page,
  }) => {
    // どのアカウントにも登録していない（サーバーが知らない）パスキーを端末にだけ作っておく
    await page.evaluate(async () => {
      await navigator.credentials.create({
        publicKey: {
          rp: { id: location.hostname, name: "WebGallery" },
          user: { id: new Uint8Array([1, 2, 3]), name: "unregistered", displayName: "unregistered" },
          challenge: new Uint8Array(32),
          pubKeyCredParams: [{ type: "public-key", alg: -7 }],
        },
      });
    });
    selectPasskeyAccount("unregistered");

    await page.getByRole("button", { name: "パスキーでログイン" }).click();

    await expect(
      page.getByRole("alert").filter({ hasText: "パスキーによる認証に失敗しました。" })
    ).toBeVisible({ timeout: 10000 });
    await expect(page).toHaveURL(/\/login(\?|$)/);
  });

  test("パスキーの選択をキャンセルした場合は、エラーを表示せずログイン画面に留まること", async ({
    page,
  }) => {
    cancelNextPasskeyOperation();
    const button = page.getByRole("button", { name: "パスキーでログイン" });
    await button.click();

    await expect(button).toBeEnabled({ timeout: 10000 });
    // Next.js のルートアナウンサー（中身が空の role=alert）は除き、エラー文言の表示が無いことを確かめる
    await expect(page.getByRole("alert").filter({ hasText: /\S/ })).toHaveCount(0);
    await expect(page).toHaveURL(/\/login(\?|$)/);
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
