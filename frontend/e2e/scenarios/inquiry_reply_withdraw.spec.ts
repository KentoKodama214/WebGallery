import type { Browser, Page } from "@playwright/test";
import { test as adminTest, expect as adminExpect } from "../fixtures/admin";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

/**
 * `pages/inquiry.spec.ts` はユーザー側のお問い合わせ送信フォームの表示・送信のみ、
 * `pages/admin_inquiry_management.spec.ts` は管理者側の一覧・返信フローのみを検証しており、
 * 「管理者の返信がユーザー側の詳細・一覧に反映されること」と「ユーザー自身によるお問い合わせの
 * 取り下げ（`InquiryDetail#handleWithdraw`）」はどちらも未検証だった。
 * 本シナリオでは、管理者・一般ユーザー双方の画面操作をまたいでこれらを確認する。
 */

/** 専用の一般アカウントを登録・ログインし、指定した件名でお問い合わせを送信する */
async function registerAndSubmitInquiry(
  browser: Browser,
  baseURL: string | undefined,
  workerIndex: number,
  subject: string
): Promise<Page> {
  const accountId = generateTestAccountId(workerIndex);
  const context = await browser.newContext({ baseURL });
  const page = await context.newPage();

  await registerAccount(page, accountId, "E2E Inquiry User");
  await login(page, accountId);

  await page.goto("/inquiry");
  await page.getByLabel("件名").fill(subject);
  await page.getByLabel("本文").fill("E2Eシナリオテストからのお問い合わせ本文です。");
  await page.getByRole("button", { name: "送信" }).click();
  await page.getByText("お問い合わせを受け付けました").waitFor({ timeout: 10000 });
  await page.getByRole("button", { name: "お問い合わせ一覧へ" }).click();
  await page.waitForURL(/\/inquiry\/list$/, { timeout: 10000 });

  return page;
}

adminTest.describe("お問い合わせのユーザー側返信確認・取り下げ", () => {
  adminTest.describe.configure({ timeout: 60_000 });

  adminTest(
    "管理者の返信がユーザー側の詳細・一覧に反映され、未対応の問い合わせは取り下げられること",
    async ({ adminPage, browser }, testInfo) => {
      const repliedSubject = `E2E返信反映確認${testInfo.workerIndex}${Date.now()}`;
      const withdrawSubject = `E2E取り下げ確認${testInfo.workerIndex}${Date.now()}`;
      const replyBody = "E2Eシナリオテストからの返信です。";

      const userPage = await registerAndSubmitInquiry(
        browser,
        testInfo.project.use.baseURL,
        testInfo.workerIndex,
        repliedSubject
      );
      await userPage.goto("/inquiry");
      await userPage.getByLabel("件名").fill(withdrawSubject);
      await userPage
        .getByLabel("本文")
        .fill("E2Eシナリオテストからの取り下げ確認用の問い合わせ本文です。");
      await userPage.getByRole("button", { name: "送信" }).click();
      await userPage.getByText("お問い合わせを受け付けました").waitFor({ timeout: 10000 });
      await userPage.getByRole("button", { name: "お問い合わせ一覧へ" }).click();

      let repliedDetailUrl = "";
      await adminTest.step("一覧から返信確認用の問い合わせの詳細URLを取得する", async () => {
        const link = userPage.getByRole("link", { name: repliedSubject });
        await adminExpect(link).toBeVisible({ timeout: 10000 });
        const href = await link.getAttribute("href");
        adminExpect(href).toBeTruthy();
        repliedDetailUrl = href!;
      });

      await adminTest.step("管理者が返信する", async () => {
        await adminPage.goto("/admin/inquiry_management");
        await adminPage.getByRole("link", { name: repliedSubject }).click();
        await adminExpect(adminPage.getByRole("heading", { name: repliedSubject })).toBeVisible({
          timeout: 10000,
        });
        await adminPage.getByLabel("返信する").fill(replyBody);
        await adminPage.getByRole("button", { name: "返信を送信" }).click();
        await adminExpect(adminPage.getByText("返信を送信しました")).toBeVisible({
          timeout: 10000,
        });
      });

      await adminTest.step("ユーザー側の詳細ページに返信内容とステータスが反映されること", async () => {
        await userPage.goto(repliedDetailUrl);
        await adminExpect(userPage.getByText(replyBody)).toBeVisible({ timeout: 10000 });
        await adminExpect(userPage.getByText("回答済み")).toBeVisible();
        // 回答済みでも取り下げ済みでない限り、取り下げ導線は表示されたままである
        await adminExpect(
          userPage.getByRole("button", { name: "このお問い合わせを取り下げる" })
        ).toBeVisible();
      });

      await adminTest.step("ユーザー側の一覧にも返信ありのステータスが反映されること", async () => {
        await userPage.goto("/inquiry/list");
        const row = userPage.getByRole("row", { name: new RegExp(repliedSubject) });
        await adminExpect(row.getByText("回答あり")).toBeVisible({ timeout: 10000 });
      });

      await adminTest.step("未対応の問い合わせで取り下げ確認をキャンセルすると取り下げられないこと", async () => {
        const link = userPage.getByRole("link", { name: withdrawSubject });
        await adminExpect(link).toBeVisible({ timeout: 10000 });
        await link.click();

        await adminExpect(
          userPage.getByRole("heading", { name: withdrawSubject })
        ).toBeVisible({ timeout: 10000 });
        await adminExpect(userPage.getByText("未対応")).toBeVisible();

        await userPage.getByRole("button", { name: "このお問い合わせを取り下げる" }).click();
        const dialog = userPage.getByTestId("withdraw-confirm-dialog");
        await adminExpect(dialog).toBeVisible();
        await dialog.getByRole("button", { name: "キャンセル" }).click();
        await adminExpect(dialog).toBeHidden();
        await adminExpect(userPage.getByText("未対応")).toBeVisible();
      });

      await adminTest.step("取り下げを実行すると、ステータスが取り下げになり取り下げ導線が消えること", async () => {
        await userPage.getByRole("button", { name: "このお問い合わせを取り下げる" }).click();
        const dialog = userPage.getByTestId("withdraw-confirm-dialog");
        await adminExpect(dialog).toBeVisible();
        await dialog.getByRole("button", { name: "取り下げる" }).click();

        await adminExpect(dialog).toBeHidden({ timeout: 10000 });
        await adminExpect(userPage.getByText("取り下げ", { exact: true })).toBeVisible();
        await adminExpect(
          userPage.getByRole("button", { name: "このお問い合わせを取り下げる" })
        ).toHaveCount(0);
      });

      await adminTest.step("一覧にも取り下げ後のステータスが反映されること", async () => {
        await userPage.goto("/inquiry/list");
        const row = userPage.getByRole("row", { name: new RegExp(withdrawSubject) });
        await adminExpect(row.getByText("取り下げ", { exact: true })).toBeVisible({
          timeout: 10000,
        });
      });

      await userPage.close();
    }
  );
});
