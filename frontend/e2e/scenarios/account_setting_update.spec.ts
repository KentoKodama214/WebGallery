import { test, expect } from "@playwright/test";
import { generateTestAccountId, TEST_USER_PASSWORD } from "../fixtures/auth";

test.describe("アカウント設定の更新（プロフィール変更〜パスワード変更〜アカウント削除）", () => {
  test("プロフィールを変更でき、パスワード変更後は再ログインが必要になり、アカウント削除後はログインできなくなること", async ({
    page,
  }, testInfo) => {
    // パスワード変更・アカウント削除という破壊的な操作を含むため、他テストと共有する
    // workerPage（fixtures/auth.ts）ではなく、このテスト専用の使い捨てアカウントを使う
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const updatedAccountName = "E2E Updated Name";
    const newPassword = "NewE2ePass456";

    await test.step("アカウント登録・ログイン", async () => {
      await page.goto("/register");
      await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
      await page
        .locator('label:has-text("アカウント名") + input')
        .fill("E2E Setting User");
      await page
        .getByPlaceholder("英字と数字を含む半角8〜72文字")
        .fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "登録" }).click();
      await expect(page.getByRole("dialog", { name: "アカウント登録完了" })).toBeVisible({
        timeout: 10000,
      });

      await page.goto("/login");
      await page.getByPlaceholder("User ID").fill(accountId);
      await page.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "Log in" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
        timeout: 10000,
      });
    });

    await test.step("アカウント名を変更して保存すると完了モーダルが表示され、リロード後も反映されていること", async () => {
      await page.goto(`/${accountId}/account_setting`);
      await page.getByLabel("アカウント名").fill(updatedAccountName);
      await page.getByRole("button", { name: "登録" }).click();

      await expect(page.getByRole("dialog", { name: "アカウント更新完了" })).toBeVisible({
        timeout: 10000,
      });
      await page.getByRole("button", { name: "閉じる" }).click();

      await page.reload();
      await expect(page.getByLabel("アカウント名")).toHaveValue(updatedAccountName);
    });

    await test.step("現在のパスワードを入力せずに新しいパスワードだけ入力すると検証エラーが表示されること", async () => {
      await page.getByLabel("新しいパスワード").fill(newPassword);
      await page.getByRole("button", { name: "登録" }).click();

      await expect(page.getByText("現在のパスワードを入力してください")).toBeVisible();
      // クライアント側検証で送信されないため、プロフィール変更ページに留まる
      await expect(page).toHaveURL(new RegExp(`/${accountId}/account_setting$`));
    });

    await test.step("誤った現在のパスワードでパスワード変更しようとするとエラーが表示されること", async () => {
      await page.getByLabel("現在のパスワード").fill("WrongPass999");
      await page.getByRole("button", { name: "登録" }).click();

      await expect(
        page.getByRole("alert").filter({ hasText: "現在のパスワードが正しくありません" })
      ).toBeVisible({ timeout: 10000 });
      await expect(page).toHaveURL(new RegExp(`/${accountId}/account_setting$`));
    });

    await test.step("正しい現在のパスワードでパスワードを変更すると、ログインページへ遷移すること", async () => {
      await page.getByLabel("現在のパスワード").fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "登録" }).click();

      await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    });

    await test.step("旧パスワードではログインできず、新パスワードでログインできること", async () => {
      await page.goto("/login");
      await page.getByPlaceholder("User ID").fill(accountId);
      await page.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "Log in" }).click();
      await expect(
        page.getByRole("alert").filter({ hasText: "アカウントIDまたはパスワードが間違っています。" })
      ).toBeVisible({ timeout: 10000 });

      await page.getByPlaceholder("Password").fill(newPassword);
      await page.getByRole("button", { name: "Log in" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list`), {
        timeout: 10000,
      });
    });

    await test.step("削除確認ダイアログをキャンセルすると削除されないこと", async () => {
      await page.goto(`/${accountId}/account_setting`);
      await page.getByRole("button", { name: "アカウント削除" }).click();

      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await expect(deleteDialog).toBeVisible();
      await deleteDialog.getByRole("button", { name: "いいえ" }).click();
      await expect(deleteDialog).toBeHidden();
      await expect(page).toHaveURL(new RegExp(`/${accountId}/account_setting$`));
    });

    await test.step("誤ったパスワードでの削除は失敗し、ダイアログが開いたままであること", async () => {
      await page.getByRole("button", { name: "アカウント削除" }).click();
      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await expect(deleteDialog).toBeVisible();

      await deleteDialog.getByLabel("現在のパスワード").fill("WrongPass999");
      await deleteDialog.getByRole("button", { name: "はい" }).click();

      await expect(
        deleteDialog.getByRole("alert").filter({ hasText: "現在のパスワードが正しくありません" })
      ).toBeVisible({ timeout: 10000 });
      await expect(deleteDialog).toBeVisible();
    });

    await test.step("正しいパスワードで削除すると、削除完了のログインページへ遷移すること", async () => {
      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await deleteDialog.getByLabel("現在のパスワード").fill(newPassword);
      await deleteDialog.getByRole("button", { name: "はい" }).click();

      await expect(page).toHaveURL(/\/login\?deleted=1/, { timeout: 10000 });
    });

    await test.step("削除後は同じ認証情報でログインできないこと", async () => {
      await page.getByPlaceholder("User ID").fill(accountId);
      await page.getByPlaceholder("Password").fill(newPassword);
      await page.getByRole("button", { name: "Log in" }).click();

      await expect(
        page.getByRole("alert").filter({ hasText: "アカウントIDまたはパスワードが間違っています。" })
      ).toBeVisible({ timeout: 10000 });
    });
  });
});
