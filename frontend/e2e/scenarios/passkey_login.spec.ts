import { test, expect, type CDPSession, type Page } from "@playwright/test";
import { generateTestAccountId } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

/**
 * 新規登録（パスキー作成）→ パスキーでのログイン → パスキーで本人確認しての退会までを、
 * Chromium の仮想認証器（Chrome DevTools Protocol の WebAuthn ドメイン）で通しで検証する。
 *
 * 他の E2E は WebKit でも動くよう擬似認証器（fixtures/passkey.ts）で navigator.credentials を差し替えるが、
 * このテストだけは差し替えず、ブラウザ本来の WebAuthn 実装（clientDataJSON・attestationObject の生成）と
 * バックエンドの検証が噛み合うことを確かめる。
 *
 * 仮想認証器は「生体認証・PIN によるユーザー検証済み」「ユーザー操作を自動で承認」に設定し、
 * OS のパスキーダイアログを出さずに navigator.credentials.create / get を完了させる。
 * CDP は Chromium 専用のため、他のブラウザプロジェクトでは実行しない。
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

test.describe("ブラウザ本来のWebAuthnでのパスキー認証", () => {
  test("新規登録でパスキーを作成し、パスワード無しでログインでき、パスキーで本人確認して退会すると以後ログインできなくなること", async ({
    page,
  }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "仮想認証器（CDP）はchromiumのみで利用できる");
    test.setTimeout(120_000);

    const accountId = generateTestAccountId(testInfo.workerIndex);
    const { cdp, authenticatorId } = await addVirtualAuthenticator(page);

    await test.step("新規登録で端末にパスキーを作成し、リカバリーコードが表示されること", async () => {
      await page.goto("/register");
      await fillStable(page.getByPlaceholder("半角英数字で8〜20文字"), accountId);
      await fillStable(page.locator('label:has-text("アカウント名") + input'), "E2E Real Passkey User");
      await page.getByRole("button", { name: "登録", exact: true }).click();

      const dialog = page.getByRole("dialog", { name: "アカウント登録完了" });
      await expect(dialog).toBeVisible({ timeout: 30000 });
      await expect(
        dialog.getByRole("list", { name: "リカバリーコード" }).getByRole("listitem")
      ).toHaveCount(10);

      // 仮想認証器にパスキー（discoverable credential）が1件作られている
      const { credentials } = await cdp.send("WebAuthn.getCredentials", { authenticatorId });
      expect(credentials).toHaveLength(1);
      expect(credentials[0].isResidentCredential).toBe(true);

      await dialog.getByRole("button", { name: "保管しました（ログインへ）" }).click();
      await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    });

    await test.step("「パスキーでログイン」でアカウントIDもパスワードも入力せずにログインできること", async () => {
      await page.getByRole("button", { name: "パスキーでログイン" }).click();

      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
        timeout: 30000,
      });
      // ヘッダーが認証済み状態のメニュー（Sign Out）になっていること
      await page.getByTestId("hamburger-button").click();
      await expect(page.getByTestId("logout-button")).toBeVisible();
      await page.keyboard.press("Escape");
    });

    await test.step("アカウント設定で、パスキーによる本人確認のうえ退会できること", async () => {
      await page.goto(`/${accountId}/account_setting`);
      await page.getByRole("button", { name: "アカウント削除" }).click({ timeout: 30000 });
      const dialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await dialog.getByRole("button", { name: "はい" }).click();

      await expect(page).toHaveURL(/\/login\?deleted=1/, { timeout: 15000 });
    });

    await test.step("退会後は、端末に残ったパスキーではログインできずエラーが表示されること", async () => {
      // 仮想認証器にはパスキーが残っているが、サーバーからは削除済みのため認証に失敗する
      await page.getByRole("button", { name: "パスキーでログイン" }).click();

      await expect(
        page.getByRole("alert").filter({ hasText: "パスキーによる認証に失敗しました。" })
      ).toBeVisible({ timeout: 15000 });
      await expect(page).toHaveURL(/\/login(\?|$)/);
    });
  });
});
