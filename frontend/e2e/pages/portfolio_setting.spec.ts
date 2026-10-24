import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { test as authTest, expect as authExpect } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

test.describe("ポートフォリオ設定ページ", () => {
  test("未ログイン状態では、一覧・編集ともにログインページへリダイレクトされること", async ({
    page,
  }) => {
    await page.goto("/e2etestaccount/portfolio_setting");
    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });

    await page.goto("/e2etestaccount/portfolio_setting/edit");
    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
  });

  test("ポートフォリオ番号が正の整数でない場合は、未ログインでも404が表示されること", async ({
    page,
  }) => {
    const response = await page.goto("/e2etestaccount/portfolio_setting/edit?portfolioNo=abc");

    expect(response?.status()).toBe(404);
  });
});

authTest.describe("ポートフォリオ設定ページ（ログイン済み）", () => {
  authTest(
    "本人の一覧ページが表示され、ヘッダーのメニューから遷移できること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_list`);
      await page.getByRole("button", { name: "メニュー" }).click();
      await page.getByRole("link", { name: "My Portfolio" }).click();

      await authExpect(page).toHaveURL(new RegExp(`/${testUser.accountId}/portfolio_setting$`), {
        timeout: 10000,
      });
      await authExpect(page).toHaveTitle(/ポートフォリオ設定/);
      await authExpect(page.getByRole("heading", { name: "ポートフォリオ設定" })).toBeVisible({
        timeout: 10000,
      });
      await authExpect(page.getByRole("link", { name: "新規作成" })).toBeVisible();
    }
  );

  authTest(
    "テーマを入力せずに登録しようとするとエラーが表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/${testUser.accountId}/portfolio_setting/edit`);
      await authExpect(page.getByRole("heading", { name: "ポートフォリオ作成" })).toBeVisible({
        timeout: 10000,
      });

      await page.getByRole("button", { name: "下書きとして登録" }).click();

      await authExpect(page.getByText("テーマを入力してください")).toBeVisible();
    }
  );

  authTest(
    "写真を選択せずに下書き登録しようとするとエラーが表示され、登録されないこと",
    async ({ workerPage: page, testUser }) => {
      const theme = `E2E下書き${Date.now()}`;
      await page.goto(`/${testUser.accountId}/portfolio_setting/edit`);
      await fillStable(page.locator("#portfolio-theme"), theme);

      await page.getByRole("button", { name: "下書きとして登録" }).click();

      await authExpect(page.getByText("写真を1枚以上選択してください")).toBeVisible();
      await authExpect(page).toHaveURL(/\/portfolio_setting\/edit$/);

      // 左上の「一覧へ戻る」から一覧へ戻ると、登録されていないことを確認できる
      await page.getByRole("link", { name: "ポートフォリオ一覧へ戻る" }).click();
      await authExpect(page).toHaveURL(new RegExp(`/${testUser.accountId}/portfolio_setting$`), {
        timeout: 10000,
      });
      await authExpect(page.getByRole("link", { name: "新規作成" })).toBeVisible({
        timeout: 10000,
      });
      await authExpect(page.getByTestId("portfolio-row").filter({ hasText: theme })).toHaveCount(0);
    }
  );

  authTest(
    "他人のアカウントIDのポートフォリオ設定を開くと、権限がない旨が表示されること",
    async ({ workerPage: page }) => {
      await page.goto("/e2eotheraccount/portfolio_setting");

      await authExpect(page.getByText("この操作を行う権限がありません")).toBeVisible({
        timeout: 10000,
      });
    }
  );

  authTest(
    "一覧・作成画面にアクセシビリティ違反がないこと",
    async ({ workerPage: page, testUser }, testInfo) => {
      authTest.skip(
        testInfo.project.name !== "chromium",
        "a11y検証はchromiumプロジェクトのみで実施する"
      );

      await page.goto(`/${testUser.accountId}/portfolio_setting`);
      await authExpect(page.getByRole("link", { name: "新規作成" })).toBeVisible({
        timeout: 10000,
      });
      await expectNoAccessibilityViolations(page);

      await page.goto(`/${testUser.accountId}/portfolio_setting/edit`);
      await authExpect(page.getByRole("heading", { name: "ポートフォリオ作成" })).toBeVisible({
        timeout: 10000,
      });
      await page.getByRole("radio", { name: "グリッド" }).check();
      await page.getByRole("button", { name: "＋行を追加" }).click();
      await page.getByRole("button", { name: "＋タグを追加" }).click();
      await expectNoAccessibilityViolations(page);

      await page.getByRole("button", { name: "写真を選択" }).click();
      const picker = page.getByTestId("photo-picker-dialog");
      await authExpect(picker.getByRole("button", { name: "検索" })).toBeVisible();
      await authExpect(picker.getByText("読み込み中...")).toHaveCount(0, { timeout: 10000 });
      await expectNoAccessibilityViolations(page);
      await picker.getByRole("button", { name: "キャンセル" }).click();

      await page.getByRole("button", { name: "プレビューを表示" }).click();
      await authExpect(page.getByRole("dialog", { name: "プレビュー" })).toBeVisible();
      await expectNoAccessibilityViolations(page);
      await page.getByRole("button", { name: "閉じる" }).click();
      await authExpect(page.getByRole("dialog", { name: "プレビュー" })).toHaveCount(0);
    }
  );
});
