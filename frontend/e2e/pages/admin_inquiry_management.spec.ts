import { test, expect, type Browser, type Page } from "@playwright/test";
import {
  test as adminTest,
  expect as adminExpect,
} from "../fixtures/admin";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

test.describe("管理者用お問い合わせ管理ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/admin/inquiry_management");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/お問い合わせ管理/);
  });

  test("未ログイン状態では管理者権限エラーが表示されること", async ({ page }) => {
    await expect(page.locator("text=管理者権限がありません")).toBeVisible({
      timeout: 5000,
    });
  });
});

test.describe("管理者用お問い合わせ詳細ページ", () => {
  test("不正なinquiryIdクエリの場合は『お問い合わせが見つかりません』を表示する", async ({
    page,
  }) => {
    await page.goto("/admin/inquiry_management/detail?inquiryId=abc");

    await expect(page.getByText("お問い合わせが見つかりません")).toBeVisible();
  });

  test("未ログイン状態では管理者権限エラーが表示されること", async ({ page }) => {
    await page.goto("/admin/inquiry_management/detail?inquiryId=1");

    await expect(page.locator("text=管理者権限がありません")).toBeVisible({
      timeout: 5000,
    });
  });
});

/** 専用の一般アカウントを登録・ログインし、指定した件名でお問い合わせを送信する */
async function submitInquiryAsNewAccount(
  browser: Browser,
  baseURL: string | undefined,
  workerIndex: number,
  subject: string
): Promise<{ page: Page; accountId: string }> {
  const accountId = generateTestAccountId(workerIndex);
  const context = await browser.newContext({ baseURL });
  const page = await context.newPage();

  await registerAccount(page, accountId, "E2E Inquiry Sender");
  await login(page, accountId);

  await page.goto("/inquiry");
  await page.getByLabel("件名").fill(subject);
  await page.getByLabel("本文").fill("E2E管理者テストからのお問い合わせ本文です。");
  await page.getByRole("button", { name: "送信" }).click();
  await expect(page.getByText("お問い合わせを受け付けました")).toBeVisible({
    timeout: 10000,
  });

  return { page, accountId };
}

adminTest.describe("管理者用お問い合わせ管理ページ（ログイン済み・管理者）", () => {
  adminTest(
    "対象の問い合わせが一覧に未対応で表示され、ステータスで絞り込めること",
    async ({ adminPage: page, browser }, testInfo) => {
      const subject = `E2E管理者一覧テスト${testInfo.workerIndex}${Date.now()}`;
      const sender = await submitInquiryAsNewAccount(
        browser,
        testInfo.project.use.baseURL,
        testInfo.workerIndex,
        subject
      );

      await page.goto("/admin/inquiry_management");
      const row = page.getByRole("row", { name: new RegExp(subject) });
      await adminExpect(row).toBeVisible({ timeout: 10000 });
      await adminExpect(row.getByText("未対応")).toBeVisible();

      await page.selectOption("#status-filter", "unreplied");
      await adminExpect(row).toBeVisible();

      await page.selectOption("#status-filter", "replied");
      await adminExpect(row).toHaveCount(0);

      await sender.page.close();
    }
  );

  adminTest(
    "問い合わせ詳細で返信すると回答済みになり、一覧にも反映されること",
    async ({ adminPage: page, browser }, testInfo) => {
      const subject = `E2E管理者返信テスト${testInfo.workerIndex}${Date.now()}`;
      const replyBody = "E2E管理者テストからの返信です。";
      const sender = await submitInquiryAsNewAccount(
        browser,
        testInfo.project.use.baseURL,
        testInfo.workerIndex,
        subject
      );

      await page.goto("/admin/inquiry_management");
      await page.getByRole("link", { name: subject }).click();

      await adminExpect(page.getByRole("heading", { name: subject })).toBeVisible();
      await adminExpect(page.getByText("未対応")).toBeVisible();

      await page.getByLabel("返信する").fill(replyBody);
      await page.getByRole("button", { name: "返信を送信" }).click();

      await adminExpect(page.getByText("返信を送信しました")).toBeVisible({
        timeout: 10000,
      });
      await adminExpect(page.getByText(replyBody)).toBeVisible();
      await adminExpect(page.getByText("回答済み").first()).toBeVisible();

      await page.goto("/admin/inquiry_management");
      const row = page.getByRole("row", { name: new RegExp(subject) });
      await adminExpect(row.getByText("回答済み")).toBeVisible({ timeout: 10000 });

      await sender.page.close();
    }
  );
});
