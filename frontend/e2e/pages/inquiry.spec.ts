import { test, expect } from "@playwright/test";
import { test as authTest, expect as authExpect } from "../fixtures/auth";

test.describe("お問い合わせページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

test.describe("お問い合わせ一覧ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry/list");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

test.describe("お問い合わせ詳細ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry/detail?inquiryNo=1");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

authTest.describe("お問い合わせページ（ログイン済み）", () => {
  authTest("ログインページへリダイレクトされず、フォームが表示されること", async ({
    workerPage: page,
  }) => {
    await page.goto("/inquiry");

    await authExpect(page).toHaveURL(/\/inquiry$/);
    await authExpect(page).toHaveTitle(/お問い合わせ/);
    await authExpect(page.getByLabel("件名")).toBeVisible();
    await authExpect(page.getByLabel("本文")).toBeVisible();
  });

  authTest("件名・本文を入力して送信すると受付完了メッセージが表示されること", async ({
    workerPage: page,
  }) => {
    await page.goto("/inquiry");

    await page.getByLabel("件名").fill("E2Eテストからのお問い合わせ");
    await page.getByLabel("本文").fill("E2Eテストの自動投稿本文です。");
    await page.getByRole("button", { name: "送信" }).click();

    await authExpect(page.getByText("お問い合わせを受け付けました")).toBeVisible({
      timeout: 10000,
    });
  });

  authTest("件名・本文が未入力の場合はバリデーションエラーが表示されること", async ({
    workerPage: page,
  }) => {
    await page.goto("/inquiry");

    await page.getByLabel("件名").focus();
    await page.getByLabel("件名").blur();
    await page.getByLabel("本文").focus();
    await page.getByLabel("本文").blur();

    await authExpect(page.getByText("件名を入力してください")).toBeVisible();
    await authExpect(page.getByText("本文を入力してください")).toBeVisible();
  });
});

authTest.describe("お問い合わせ一覧ページ（ログイン済み）", () => {
  authTest("ログインページへリダイレクトされないこと", async ({ workerPage: page }) => {
    await page.goto("/inquiry/list");

    await authExpect(page).toHaveURL(/\/inquiry\/list$/);
    await authExpect(page).toHaveTitle(/お問い合わせ一覧/);
  });
});
