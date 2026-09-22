import { test, expect } from "@playwright/test";
import { generateTestAccountId, registerAccount, TEST_USER_PASSWORD } from "../fixtures/auth";

/**
 * `pages/login.spec.ts` は誤った認証情報1回分のエラー表示のみを検証しており、
 * `application.yml` の `auth.login.failCount`（既定3回）に達した際の自動ロック
 * （`AuthServiceImpl#isLocked`）は未検証だった。本シナリオでは、連続したログイン
 * 失敗が実際にアカウントロックへつながることを一般ユーザーの画面操作で確認する。
 *
 * ロックの自動解除（`lockDurationMinutes`＝既定30分）はE2Eの実行時間内で検証できないため対象外。
 */
test.describe("ログイン失敗の連続によるアカウントロック", () => {
  test("failCount回のログイン失敗でロックされ、正しいパスワードでもログインできなくなること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);

    await test.step("対象アカウントを登録する", async () => {
      await registerAccount(page, accountId, "E2E Lockout User");
    });

    await test.step("誤ったパスワードで3回連続ログインに失敗する（application.ymlのfailCount=3）", async () => {
      await page.goto("/login");
      for (let i = 0; i < 3; i++) {
        await page.getByPlaceholder("User ID").fill(accountId);
        await page.getByPlaceholder("Password").fill("WrongPass999");
        await page.getByRole("button", { name: "Log in" }).click();

        await expect(
          page
            .getByRole("alert")
            .filter({ hasText: "アカウントIDまたはパスワードが間違っています。" })
        ).toBeVisible({ timeout: 10000 });
      }
    });

    await test.step("上限到達後は、正しいパスワードでもロック中である旨のエラーが表示されログインできないこと", async () => {
      await page.getByPlaceholder("User ID").fill(accountId);
      await page.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "Log in" }).click();

      await expect(
        page.getByRole("alert").filter({ hasText: "アカウントがロックされています。" })
      ).toBeVisible({ timeout: 10000 });
      await expect(page).toHaveURL(/\/login(\?|$)/);
    });
  });
});
