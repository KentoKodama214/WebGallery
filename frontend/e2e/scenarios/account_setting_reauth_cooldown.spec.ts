import { test, expect } from "@playwright/test";
import { generateTestAccountId, registerAndLogin, TEST_USER_PASSWORD } from "../fixtures/auth";

/**
 * `scenarios/account_setting_update.spec.ts` は「現在のパスワード」を1回だけ間違えた場合の
 * エラー表示のみを検証しており、`AccountSettingForm` が持つクライアント側の連打抑止
 * （`REAUTH_MAX_ATTEMPTS`＝3回連続失敗でクールダウン、`sessionStorage`で画面遷移・リロードを
 * またいで維持）は未検証だった。バックエンドの再認証ロック（`ReauthenticationThrottle`、
 * 既定`max-failures`＝5）より先にクライアント側のクールダウンに達することを前提に、
 * パスワード変更・アカウント削除の両操作が共有する再認証カウンタの挙動を検証する。
 */
test.describe("アカウント設定の再認証クールダウン（現在のパスワード連続失敗による一時停止）", () => {
  test("3回連続で現在のパスワードを間違えるとクールダウンし、リロード後も維持され、クールダウン明けには解除されること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const newPassword = "NewE2ePass456";

    await test.step("対象アカウントを登録・ログインする", async () => {
      await registerAndLogin(page, accountId, "E2E Reauth Cooldown User");
      await page.goto(`/${accountId}/account_setting`);
    });

    await test.step("現在のパスワードを3回連続で間違えると、3回目でクールダウンメッセージが表示され送信ボタンが無効化されること", async () => {
      await page.getByLabel("新しいパスワード").fill(newPassword);

      for (let i = 0; i < 3; i++) {
        await page.getByLabel("現在のパスワード").fill("WrongPass999");
        await page.getByRole("button", { name: "登録" }).click();

        await expect(
          page.getByRole("alert").filter({ hasText: "現在のパスワードが正しくありません" })
        ).toBeVisible({ timeout: 10000 });
      }

      await expect(
        page.getByRole("alert").filter({
          hasText: "現在のパスワードの確認に連続して失敗しました。しばらく待ってから再度お試しください。",
        })
      ).toBeVisible();
      await expect(page.getByRole("button", { name: "登録" })).toBeDisabled();
    });

    await test.step("クールダウン中は、パスワード変更と共有される再認証カウンタによりアカウント削除の実行ボタンも無効化されること", async () => {
      await page.getByRole("button", { name: "アカウント削除" }).click();
      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await expect(deleteDialog).toBeVisible();

      await expect(deleteDialog.getByRole("button", { name: "はい" })).toBeDisabled();

      await deleteDialog.getByRole("button", { name: "いいえ" }).click();
      await expect(deleteDialog).toBeHidden();
    });

    await test.step("リロードしてもクールダウンが維持されること（sessionStorageによる永続化）", async () => {
      await page.reload();

      await expect(
        page.getByRole("alert").filter({
          hasText: "現在のパスワードの確認に連続して失敗しました。しばらく待ってから再度お試しください。",
        })
      ).toBeVisible();

      await page.getByRole("button", { name: "アカウント削除" }).click();
      const deleteDialog = page.getByRole("dialog", { name: "アカウント削除の確認" });
      await expect(deleteDialog.getByRole("button", { name: "はい" })).toBeDisabled();
      await deleteDialog.getByRole("button", { name: "いいえ" }).click();
    });

    await test.step("クールダウン明け後は解除され、正しい現在のパスワードでの変更が再び行えること", async () => {
      // 60秒の実待機を避けるため、クライアント側の永続化先（sessionStorage）を直接
      // 期限切れ状態へ書き換える。これは AccountSettingForm が実際に読み書きしているキーであり、
      // プロダクションコードの「期限切れなら解除する」経路（loadReauthState）をそのまま通す。
      await page.evaluate((id) => {
        const key = `webgallery.reauthCooldown.${id}`;
        window.sessionStorage.setItem(
          key,
          JSON.stringify({ failCount: 0, cooldownUntil: Date.now() - 1000 })
        );
      }, accountId);
      await page.reload();

      await expect(
        page.getByText("現在のパスワードの確認に連続して失敗しました。しばらく待ってから再度お試しください。")
      ).toBeHidden();

      await page.getByLabel("新しいパスワード").fill(newPassword);
      await page.getByLabel("現在のパスワード").fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "登録" }).click();

      await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    });
  });
});
