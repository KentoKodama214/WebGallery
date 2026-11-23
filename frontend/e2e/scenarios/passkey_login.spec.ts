import { test, expect, type CDPSession, type Page } from "@playwright/test";
import { TEST_USER_PASSWORD, generateTestAccountId, registerAndLogin } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

/**
 * パスキーの登録 → パスキーでのログイン → 削除までを、Chromium の仮想認証器
 * （Chrome DevTools Protocol の WebAuthn ドメイン）で通しで検証する。
 *
 * 仮想認証器は「生体認証・PIN によるユーザー検証済み」「ユーザー操作を自動で承認」に設定し、
 * OS のパスキーダイアログを出さずに navigator.credentials.create / get を完了させる。
 * CDP は Chromium 専用のため、他のブラウザプロジェクトでは実行しない。
 *
 * ログインのたびに既存セッションが失効し、パスキーも登録・削除するため、
 * 共有の `workerPage` ではなくこの検証専用の使い捨てアカウントを使う。
 */

/**
 * ページに仮想認証器（端末内蔵のパスキー対応認証器相当）を取り付ける
 *
 * @param page 操作対象のページ
 * @returns CDP セッションと認証器ID
 */
async function addVirtualAuthenticator(
  page: Page
): Promise<{ cdp: CDPSession; authenticatorId: string }> {
  const cdp = await page.context().newCDPSession(page);
  await cdp.send("WebAuthn.enable");
  const { authenticatorId } = await cdp.send("WebAuthn.addVirtualAuthenticator", {
    options: {
      protocol: "ctap2",
      transport: "internal",
      hasResidentKey: true,
      hasUserVerification: true,
      isUserVerified: true,
      automaticPresenceSimulation: true,
    },
  });
  return { cdp, authenticatorId };
}

test.describe("パスキーによるログイン", () => {
  test("アカウント設定でパスキーを登録すると、パスワードを入力せずにパスキーでログインでき、削除後はログインできなくなること", async ({
    page,
  }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "仮想認証器（CDP）はchromiumのみで利用できる");
    test.setTimeout(120_000);

    const accountId = generateTestAccountId(testInfo.workerIndex);
    // 仮想認証器は、ログイン画面のオートフィル待ち受け（Conditional UI）にも利用者の選択を待たずに応答し、
    // ボタンを押す前にログインが完了してしまう。ボタンからの導線を確実に検証するため、このテストでは
    // オートフィル待ち受けを無効にする（オートフィル経由の挙動は LoginForm の単体テストで検証する）
    await page.addInitScript(() => {
      if (typeof window.PublicKeyCredential === "function") {
        Object.defineProperty(window.PublicKeyCredential, "isConditionalMediationAvailable", {
          configurable: true,
          value: async () => false,
        });
      }
    });
    const { cdp, authenticatorId } = await addVirtualAuthenticator(page);

    await test.step("アカウント登録・パスワードでログインする", async () => {
      await registerAndLogin(page, accountId, "E2E Passkey User");
    });

    await test.step("アカウント設定で、本人確認のうえパスキーを登録できること", async () => {
      await page.goto(`/${accountId}/account_setting`);
      const section = page.getByRole("region", { name: "パスキー" });
      await expect(section.getByText("登録済みのパスキーはありません")).toBeVisible({
        timeout: 30000,
      });

      await section.getByRole("button", { name: "パスキーを登録" }).click();
      const dialog = page.getByRole("dialog", { name: "パスキーの登録" });
      await fillStable(dialog.getByLabel("パスキー名"), "E2Eの仮想認証器");
      await fillStable(dialog.getByLabel("現在のパスワード"), TEST_USER_PASSWORD);
      await dialog.getByRole("button", { name: "登録" }).click();

      await expect(section.getByRole("status")).toHaveText("パスキーを登録しました", {
        timeout: 15000,
      });
      await expect(
        section.getByRole("list", { name: "登録済みのパスキー" }).getByText("E2Eの仮想認証器")
      ).toBeVisible();

      // 仮想認証器にパスキー（discoverable credential）が1件作られている
      const { credentials } = await cdp.send("WebAuthn.getCredentials", { authenticatorId });
      expect(credentials).toHaveLength(1);
      expect(credentials[0].isResidentCredential).toBe(true);
    });

    await test.step("未ログイン状態にすると、保護ページはログインページへ誘導されること", async () => {
      await page.context().clearCookies();
      await page.goto("/login");
      await expect(page.getByPlaceholder("User ID")).toBeVisible();
    });

    await test.step("「パスキーでログイン」でパスワードを入力せずにログインできること", async () => {
      await page.getByRole("button", { name: "パスキーでログイン" }).click();

      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
        timeout: 30000,
      });
      // ヘッダーが認証済み状態のメニュー（Sign Out）になっていること
      await page.getByTestId("hamburger-button").click();
      await expect(page.getByTestId("logout-button")).toBeVisible();
      await page.keyboard.press("Escape");
    });

    await test.step("アカウント設定で、本人確認のうえパスキーを削除できること", async () => {
      await page.goto(`/${accountId}/account_setting`);
      const section = page.getByRole("region", { name: "パスキー" });
      await section.getByRole("button", { name: "E2Eの仮想認証器を削除" }).click({
        timeout: 30000,
      });
      const dialog = page.getByRole("dialog", { name: "パスキーの削除の確認" });
      await fillStable(dialog.getByLabel("現在のパスワード"), TEST_USER_PASSWORD);
      await dialog.getByRole("button", { name: "削除" }).click();

      await expect(section.getByRole("status")).toHaveText("パスキーを削除しました", {
        timeout: 15000,
      });
      await expect(section.getByText("登録済みのパスキーはありません")).toBeVisible();
    });

    await test.step("削除したパスキーではログインできず、エラーが表示されること", async () => {
      await page.context().clearCookies();
      await page.goto("/login");
      await expect(page.getByPlaceholder("User ID")).toBeVisible();

      // 仮想認証器にはパスキーが残っているが、サーバーからは削除済みのため認証に失敗する
      await page.getByRole("button", { name: "パスキーでログイン" }).click();

      await expect(
        page.getByRole("alert").filter({ hasText: "パスキーによる認証に失敗しました。" })
      ).toBeVisible({ timeout: 15000 });
      await expect(page).toHaveURL(/\/login(\?|$)/);
    });
  });
});
