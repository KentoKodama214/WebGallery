import { test, expect } from "@playwright/test";
import {
  test as adminTest,
  expect as adminExpect,
} from "../fixtures/admin";
import { generateSortEarlyTestAccountId, TEST_USER_PASSWORD } from "../fixtures/auth";

/**
 * 管理者用アカウント管理ページ（`pages/admin_account_management.spec.ts`）は
 * 「強制ロック操作で一覧の表示状態が『ロック中』に変わること」までしか検証していない。
 * 本シナリオでは、そのロック操作が実際に対象アカウントのログイン可否へ反映されることまで
 * 一般ユーザー側の画面操作で確認する（管理者操作の実効性の横断検証）。
 */
adminTest.describe("管理者による強制ロックがログイン可否に反映されること", () => {
  adminTest.describe.configure({ timeout: 60_000 });

  adminTest(
    "強制ロック中はログインできず、ロック解除後は再びログインできること",
    async ({ adminPage, browser }, testInfo) => {
      const targetAccountId = generateSortEarlyTestAccountId(testInfo.workerIndex);

      await test.step("対象アカウントを登録する", async () => {
        await adminPage.goto("/register");
        await adminPage
          .getByPlaceholder("半角英数字で8〜20文字")
          .fill(targetAccountId);
        await adminPage
          .locator('label:has-text("アカウント名") + input')
          .fill("E2E Lock Target User");
        await adminPage
          .getByPlaceholder("英字と数字を含む半角8〜72文字")
          .fill(TEST_USER_PASSWORD);
        await adminPage.getByRole("button", { name: "登録" }).click();
        await adminExpect(
          adminPage.getByRole("dialog", { name: "アカウント登録完了" })
        ).toBeVisible({ timeout: 10000 });
      });

      await test.step("ロック前は対象アカウントでログインできること", async () => {
        const context = await browser.newContext({
          baseURL: testInfo.project.use.baseURL,
        });
        const targetPage = await context.newPage();
        await targetPage.goto("/login");
        await targetPage.getByPlaceholder("User ID").fill(targetAccountId);
        await targetPage.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
        await targetPage.getByRole("button", { name: "Log in" }).click();
        await expect(targetPage).toHaveURL(
          new RegExp(`/photo/${targetAccountId}/photo_list`),
          { timeout: 10000 }
        );
        await context.close();
      });

      // 管理者アカウントの新規登録直後は一覧の再取得前のため、ログイン操作を挟んだ後に開き直す
      await adminPage.goto("/admin/account_management");
      await adminPage.locator("table").first().waitFor({ state: "visible", timeout: 10000 });

      const targetRow = adminPage.getByRole("row", { name: new RegExp(targetAccountId) });
      const showMoreButton = adminPage.getByTestId("show-more-button");
      for (let i = 0; i < 50; i++) {
        if ((await targetRow.count()) > 0) break;
        if ((await showMoreButton.count()) === 0) break;
        await showMoreButton.click();
        await adminExpect(async () => {
          const isStillLoading = await showMoreButton.isDisabled().catch(() => false);
          expect(isStillLoading).toBe(false);
        }).toPass({ timeout: 20000 });
      }
      await adminExpect(targetRow).toBeVisible();

      await test.step("管理者が対象アカウントを強制ロックする", async () => {
        await adminExpect(targetRow.getByText("有効")).toBeVisible();
        await targetRow.getByRole("button", { name: "強制ロック" }).click();
        await adminExpect(adminPage.getByTestId("lock-confirm-dialog")).toBeVisible();
        await adminPage.getByRole("button", { name: "実行" }).click();
        await adminExpect(adminPage.getByText("アカウントをロックしました")).toBeVisible({
          timeout: 10000,
        });
      });

      await test.step("ロック中は対象アカウントでログインできず、ロック中である旨のエラーが表示されること", async () => {
        const context = await browser.newContext({
          baseURL: testInfo.project.use.baseURL,
        });
        const targetPage = await context.newPage();
        await targetPage.goto("/login");
        await targetPage.getByPlaceholder("User ID").fill(targetAccountId);
        await targetPage.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
        await targetPage.getByRole("button", { name: "Log in" }).click();

        await expect(
          targetPage.getByRole("alert").filter({ hasText: "アカウントがロックされています。" })
        ).toBeVisible({ timeout: 10000 });
        // ログイン失敗のため写真一覧へは遷移しない
        await expect(targetPage).toHaveURL(/\/login(\?|$)/);
        await context.close();
      });

      await test.step("管理者がロックを解除する", async () => {
        const lockedRow = adminPage.getByRole("row", { name: new RegExp(targetAccountId) });
        await adminExpect(lockedRow.getByText("ロック中")).toBeVisible();
        await lockedRow.getByRole("button", { name: "ロック解除" }).click();
        await adminExpect(adminPage.getByTestId("lock-confirm-dialog")).toBeVisible();
        await adminPage.getByRole("button", { name: "実行" }).click();
        await adminExpect(
          adminPage.getByText("アカウントのロックを解除しました")
        ).toBeVisible({ timeout: 10000 });
      });

      await test.step("ロック解除後は対象アカウントで再びログインできること", async () => {
        const context = await browser.newContext({
          baseURL: testInfo.project.use.baseURL,
        });
        const targetPage = await context.newPage();
        await targetPage.goto("/login");
        await targetPage.getByPlaceholder("User ID").fill(targetAccountId);
        await targetPage.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
        await targetPage.getByRole("button", { name: "Log in" }).click();
        await expect(targetPage).toHaveURL(
          new RegExp(`/photo/${targetAccountId}/photo_list`),
          { timeout: 10000 }
        );
        await context.close();
      });
    }
  );
});
