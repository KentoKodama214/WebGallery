import { test, expect } from "@playwright/test";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

/**
 * 既存のテストは「ログイン済み状態ではヘッダーメニューがSign Outになる（＝ボタンが
 * 表示される）」ことまでしか検証しておらず、実際にSign Outをクリックした後の
 * ・未ログイン状態へ戻ること（ヘッダーメニューがSign Inに変わる）
 * ・保護ページへの再アクセスがログインページへリダイレクトされること（＝セッションが
 *   クライアント側の見た目だけでなくサーバー側でも実際に失効していること）
 * は未検証だった。本シナリオではログアウトの実処理を検証する。
 *
 * 破壊的な操作（セッション失効）を含むため、他テストと共有する`fixtures/auth.ts`の
 * `workerPage`ではなく、この検証専用の使い捨てアカウントを使う。
 */
test.describe("ログアウト", () => {
  test("Sign Outをクリックすると未ログイン状態に戻り、保護ページへのアクセスがログインページへリダイレクトされること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);

    await test.step("アカウント登録・ログインする", async () => {
      await registerAccount(page, accountId, "E2E Logout User");
      await login(page, accountId);
    });

    await test.step("Sign Outをクリックすると、ログインページへ遷移すること", async () => {
      await page.getByTestId("hamburger-button").click();
      await page.getByTestId("logout-button").click();

      await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    });

    await test.step("ヘッダーメニューが未ログイン状態（Sign In）になっていること", async () => {
      // ログインページ自体にはヘッダーがないため、ヘッダーを持つ公開ページで確認する
      await page.goto("/account_list");
      await page.getByTestId("hamburger-button").click();
      const menu = page.getByTestId("overlay-menu");
      await expect(menu.getByText("Sign In")).toBeVisible();
      await expect(menu.getByTestId("logout-button")).toHaveCount(0);
      await page.keyboard.press("Escape");
    });

    await test.step("ログアウト後は、保護ページへ直接アクセスするとログインページへリダイレクトされること", async () => {
      await page.goto(`/${accountId}/account_setting`);
      await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    });
  });
});
