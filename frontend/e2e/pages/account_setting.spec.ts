import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { test as authTest, expect as authExpect } from "../fixtures/auth";

test.describe("アカウント設定ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    // accountId はアカウントID形式（半角英数字8〜16文字）でないとページ側で
    // 「ページが見つかりません」になり、フォーム（＝未ログイン時の /login 誘導）に到達しない
    await page.goto("/e2etestaccount/account_setting");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });

  test("アカウントID形式でない accountId は『ページが見つかりません』を表示する", async ({
    page,
  }) => {
    await page.goto("/e2e-test-account/account_setting");

    await expect(page.getByText("ページが見つかりません")).toBeVisible();
  });
});

authTest.describe("アカウント設定ページ（ログイン済み・本人）", () => {
  authTest(
    "ログインページへリダイレクトされず、自分のアカウント情報が表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/${testUser.accountId}/account_setting`);

      await authExpect(page).toHaveURL(new RegExp(`/${testUser.accountId}/account_setting$`));
      await authExpect(page).toHaveTitle(/アカウント設定/);
      await authExpect(page.getByLabel("アカウントID")).toHaveValue(testUser.accountId);
      await authExpect(page.getByLabel("アカウント名")).toHaveValue("E2E Auth User");

      // ヘッダーが認証済み状態のメニュー（Sign Out）になっていること
      await page.getByTestId("hamburger-button").click();
      await authExpect(page.getByTestId("logout-button")).toBeVisible();
    }
  );

  authTest(
    "アクセシビリティ違反がないこと",
    async ({ workerPage: page, testUser }, testInfo) => {
      authTest.skip(
        testInfo.project.name !== "chromium",
        "a11y検証はchromiumプロジェクトのみで実施する"
      );

      await page.goto(`/${testUser.accountId}/account_setting`);
      await authExpect(page.getByLabel("アカウントID")).toHaveValue(testUser.accountId);

      await expectNoAccessibilityViolations(page);
    }
  );

  authTest(
    "アカウント更新APIがサーバーエラーを返した場合、エラーメッセージが表示され更新完了にならないこと",
    async ({ workerPage: page, testUser }) => {
      // workerPage は同一ワーカー内の他のspecファイルとも使い回す共有ページのため、
      // ここで登録したルートモックを外し忘れると、後続の別テストが行う本物のアカウント更新
      // リクエストまで横取りしてしまう。必ず finally で unroute する
      const accountUrlPattern = `**/api/v1/accounts/${testUser.accountId}`;
      const mockServerError = (route: import("@playwright/test").Route) => {
        if (route.request().method() === "PUT") {
          return route.fulfill({
            status: 500,
            contentType: "application/json",
            body: JSON.stringify({ errorMessage: "内部サーバーエラーが発生しました。" }),
          });
        }
        return route.continue();
      };
      await page.route(accountUrlPattern, mockServerError);

      try {
        await page.goto(`/${testUser.accountId}/account_setting`);
        await authExpect(page.getByLabel("アカウント名")).toHaveValue("E2E Auth User");

        await page.getByLabel("アカウント名").fill("E2E Server Error Update");
        await page.getByRole("button", { name: "登録" }).click();

        // 5xx応答時はレスポンス本文のメッセージを使わず、固定のフォールバック文言を表示する仕様
        // （frontend/src/lib/api/client.ts の readErrorMessage）
        await authExpect(
          page.getByRole("alert").filter({ hasText: "アカウント情報の更新に失敗しました" })
        ).toBeVisible({ timeout: 10000 });
        await authExpect(page.getByRole("dialog", { name: "アカウント更新完了" })).toHaveCount(0);
        await authExpect(page).toHaveURL(new RegExp(`/${testUser.accountId}/account_setting$`));
      } finally {
        await page.unroute(accountUrlPattern, mockServerError);
      }
    }
  );
});
