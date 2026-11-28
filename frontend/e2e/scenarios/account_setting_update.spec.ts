import { test, expect } from "@playwright/test";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";
import { cancelNextPasskeyOperation, selectPasskeyAccount } from "../fixtures/passkey";

test.describe("アカウント設定の更新（プロフィール変更〜アカウント削除）", () => {
  // アカウント登録・ログイン・パスキーによる本人確認・アカウント削除を1テストで連続して行うため、
  // 既定（60秒）では並列実行時の負荷で不足する
  test.describe.configure({ timeout: 120_000 });

  test("プロフィールを変更でき、パスキーで本人確認してアカウントを削除すると、そのパスキーでログインできなくなること", async ({
    page,
  }, testInfo) => {
    // アカウント削除という破壊的な操作を含むため、他テストと共有する
    // workerPage（fixtures/auth.ts）ではなく、このテスト専用の使い捨てアカウントを使う
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const updatedAccountName = "E2E Updated Name";

    await test.step("アカウント登録・ログイン", async () => {
      await registerAccount(page, accountId, "E2E Setting User");
      await login(page, accountId);
    });

    await test.step("アカウント名を変更して保存すると完了モーダルが表示され、リロード後も反映されていること", async () => {
      await page.goto(`/${accountId}/account_setting`);
      await page.getByLabel("アカウント名").fill(updatedAccountName);
      await page.getByRole("button", { name: "登録", exact: true }).click();

      await expect(page.getByRole("dialog", { name: "アカウント更新完了" })).toBeVisible({
        timeout: 10000,
      });
      await page.getByRole("button", { name: "閉じる" }).click();

      await page.reload();
      await expect(page.getByLabel("アカウント名")).toHaveValue(updatedAccountName);
      // パスワードの変更欄は無い
      await expect(page.getByLabel("新しいパスワード")).toHaveCount(0);
    });

    await test.step("削除確認ダイアログをキャンセルすると削除されないこと", async () => {
      await page.getByRole("button", { name: "アカウント削除" }).click();

      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await expect(deleteDialog).toBeVisible();
      await deleteDialog.getByRole("button", { name: "いいえ" }).click();
      await expect(deleteDialog).toBeHidden();
      await expect(page).toHaveURL(new RegExp(`/${accountId}/account_setting$`));
    });

    await test.step("本人確認のパスキー操作をキャンセルすると、削除されずダイアログが開いたままであること", async () => {
      await page.getByRole("button", { name: "アカウント削除" }).click();
      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await expect(deleteDialog).toBeVisible();

      cancelNextPasskeyOperation();
      await deleteDialog.getByRole("button", { name: "はい" }).click();

      await expect(deleteDialog.getByRole("button", { name: "はい" })).toBeEnabled({
        timeout: 10000,
      });
      await expect(deleteDialog.getByRole("alert").filter({ hasText: /\S/ })).toHaveCount(0);
      await expect(page).toHaveURL(new RegExp(`/${accountId}/account_setting$`));
    });

    await test.step("パスキーで本人確認して削除すると、削除完了のログインページへ遷移すること", async () => {
      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await deleteDialog.getByRole("button", { name: "はい" }).click();

      await expect(page).toHaveURL(/\/login\?deleted=1/, { timeout: 10000 });
      await expect(page.getByRole("status")).toHaveText("アカウントを削除しました");
    });

    await test.step("削除後は端末に残ったパスキーでもログインできないこと", async () => {
      selectPasskeyAccount(accountId);
      await page.getByRole("button", { name: "パスキーでログイン" }).click();

      await expect(
        page.getByRole("alert").filter({ hasText: "パスキーによる認証に失敗しました。" })
      ).toBeVisible({ timeout: 10000 });
    });
  });
});
