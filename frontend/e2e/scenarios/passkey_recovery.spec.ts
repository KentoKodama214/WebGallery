import { test, expect } from "@playwright/test";
import { generateTestAccountId, login, registerAndLogin } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";
import {
  fakePasskeyCount,
  forgetFakePasskeys,
  installFakeAuthenticator,
  selectPasskeyAccount,
  useFakeAuthenticatorDevice,
} from "../fixtures/passkey";

/**
 * パスキーの追加・削除、端末紛失時のリカバリーコードによる復旧、リカバリーコードの再発行を通しで検証する
 *
 * パスキーの作成・署名は擬似認証器（fixtures/passkey.ts）で行う。端末の紛失は、擬似認証器から
 * そのアカウントのパスキーを消す（サーバー側の登録は残る）ことで再現する。
 * パスキー・リカバリーコードを追加・削除・消費するため、共有の workerPage ではなく使い捨てのアカウントを使う。
 */
test.describe("パスキーの管理とリカバリーコードによる復旧", () => {
  test.describe.configure({ timeout: 120_000 });

  test.afterEach(() => {
    // 端末の切り替えはワーカー内で共有されるため、テストごとに既定の端末へ戻す
    useFakeAuthenticatorDevice();
  });

  test("パスキーを追加・削除でき、最後の1件は削除できないこと", async ({ page }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAndLogin(page, accountId, "E2E Passkey Manage User");
    await page.goto(`/${accountId}/account_setting`);
    const section = page.getByRole("region", { name: "パスキー" });
    const list = section.getByRole("list", { name: "登録済みのパスキー" });

    await test.step("パスキーが1件のときは削除できず、案内が表示されること", async () => {
      await expect(section.getByRole("button", { name: "最初のパスキーを削除" })).toBeDisabled({
        timeout: 30000,
      });
      await expect(section.getByText(/最後のパスキーは削除できません/)).toBeVisible();
    });

    await test.step("登録済みのパスキーで本人確認のうえ、別の端末に2つ目のパスキーを登録できること", async () => {
      // 同じ端末の認証器は登録済みのパスキー（excludeCredentials）があると作成を拒否するため、別の端末で作成する
      useFakeAuthenticatorDevice("second");
      await section.getByRole("button", { name: "パスキーを登録" }).click();
      const dialog = page.getByRole("dialog", { name: "パスキーの登録" });
      await fillStable(dialog.getByLabel("パスキー名"), "E2Eの2台目");
      await dialog.getByRole("button", { name: "登録" }).click();

      await expect(section.getByRole("status")).toHaveText("パスキーを登録しました", {
        timeout: 15000,
      });
      await expect(list.getByText("E2Eの2台目")).toBeVisible();
      expect(fakePasskeyCount(accountId)).toBe(2);
    });

    await test.step("パスキーで本人確認のうえ、最初のパスキーを削除できること", async () => {
      await section.getByRole("button", { name: "最初のパスキーを削除" }).click();
      const dialog = page.getByRole("dialog", { name: "パスキーの削除の確認" });
      await dialog.getByRole("button", { name: "削除" }).click();

      await expect(section.getByRole("status")).toHaveText("パスキーを削除しました", {
        timeout: 15000,
      });
      await expect(list.getByText("最初のパスキー")).toHaveCount(0);
      await expect(section.getByRole("button", { name: "E2Eの2台目を削除" })).toBeDisabled();
    });
  });

  test("端末を失っても、リカバリーコードで新しいパスキーを登録して復旧でき、使ったコードは再利用できないこと", async ({
    page,
    browser,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const recoveryCodes = await registerAndLogin(page, accountId, "E2E Recovery User");

    await test.step("端末を紛失するとパスキーを選べず、ログインできないこと", async () => {
      forgetFakePasskeys(accountId);
      await page.context().clearCookies();
      selectPasskeyAccount(accountId);
      await page.goto("/login");
      const button = page.getByRole("button", { name: "パスキーでログイン" });
      await button.click();

      // 端末にこのアカウントのパスキーが無いため、選択画面をキャンセルしたのと同じになり、ログイン画面に留まる
      await expect(button).toBeEnabled({ timeout: 10000 });
      await expect(page).toHaveURL(/\/login(\?|$)/);
    });

    await test.step("ログイン画面から復旧ページへ進み、リカバリーコードで新しいパスキーを登録してログインできること", async () => {
      await page
        .getByRole("link", { name: "パスキーを失った場合（リカバリーコードで復旧）" })
        .click();
      await expect(page).toHaveURL(/\/recovery$/, { timeout: 10000 });

      await fillStable(page.getByLabel("アカウントID"), accountId);
      // ハイフン無し・小文字でも受け付ける
      await fillStable(
        page.getByLabel("リカバリーコード"),
        recoveryCodes[0].replace(/-/g, "").toLowerCase()
      );
      await fillStable(page.getByLabel("新しいパスキーの名前"), "E2Eの新しい端末");
      await page.getByRole("button", { name: "新しいパスキーを登録してログイン" }).click();

      // 復旧後はパスキーの見直し・リカバリーコードの再発行ができるアカウント設定へ遷移する
      await expect(page).toHaveURL(new RegExp(`/${accountId}/account_setting$`), {
        timeout: 30000,
      });
      const section = page.getByRole("region", { name: "パスキー" });
      await expect(section.getByText("E2Eの新しい端末")).toBeVisible({ timeout: 15000 });
      const recoverySection = page.getByRole("region", { name: "リカバリーコード" });
      await expect(recoverySection.getByText("9 / 10")).toBeVisible();
    });

    await test.step("復旧で登録したパスキーで、別の端末（ブラウザ）からもログインできること", async () => {
      const context = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
      const otherPage = await context.newPage();
      await login(otherPage, accountId);
      await context.close();
    });

    await test.step("使用済みのリカバリーコードは再利用できないこと", async () => {
      const context = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
      await installFakeAuthenticator(context);
      const otherPage = await context.newPage();
      await otherPage.goto("/recovery");
      await fillStable(otherPage.getByLabel("アカウントID"), accountId);
      await fillStable(otherPage.getByLabel("リカバリーコード"), recoveryCodes[0]);
      await otherPage.getByRole("button", { name: "新しいパスキーを登録してログイン" }).click();

      await expect(
        otherPage
          .getByRole("alert")
          .filter({ hasText: "アカウントIDまたはリカバリーコードが正しくありません。" })
      ).toBeVisible({ timeout: 10000 });
      await context.close();
    });
  });

  test("リカバリーコードを再発行すると、新しいコードが一度だけ表示され、以前のコードは使えなくなること", async ({
    page,
    browser,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const oldCodes = await registerAndLogin(page, accountId, "E2E Regenerate User");
    await page.goto(`/${accountId}/account_setting`);
    const section = page.getByRole("region", { name: "リカバリーコード" });
    await expect(section.getByText("10 / 10")).toBeVisible({ timeout: 30000 });

    let newCodes: string[] = [];
    await test.step("パスキーで本人確認のうえ再発行すると、新しいコードが表示されること", async () => {
      await section.getByRole("button", { name: "リカバリーコードを再発行" }).click();
      const confirm = page.getByRole("dialog", { name: "リカバリーコードの再発行の確認" });
      await confirm.getByRole("button", { name: "再発行" }).click();

      const codesDialog = page.getByRole("dialog", { name: "新しいリカバリーコード" });
      await expect(codesDialog).toBeVisible({ timeout: 15000 });
      newCodes = await codesDialog
        .getByRole("list", { name: "リカバリーコード" })
        .getByRole("listitem")
        .allInnerTexts();
      expect(newCodes).toHaveLength(10);
      expect(newCodes).not.toContain(oldCodes[0]);

      await codesDialog.getByRole("button", { name: "保管しました" }).click();
      await expect(section.getByRole("status")).toHaveText("リカバリーコードを再発行しました");
    });

    await test.step("再発行前のコードでは復旧できないこと", async () => {
      const context = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
      await installFakeAuthenticator(context);
      const otherPage = await context.newPage();
      await otherPage.goto("/recovery");
      await fillStable(otherPage.getByLabel("アカウントID"), accountId);
      await fillStable(otherPage.getByLabel("リカバリーコード"), oldCodes[0]);
      await otherPage.getByRole("button", { name: "新しいパスキーを登録してログイン" }).click();

      await expect(
        otherPage
          .getByRole("alert")
          .filter({ hasText: "アカウントIDまたはリカバリーコードが正しくありません。" })
      ).toBeVisible({ timeout: 10000 });
      await context.close();
    });
  });
});
