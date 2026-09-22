import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { test as authTest, expect as authExpect } from "../fixtures/auth";

test.describe("お問い合わせページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

test.describe("お問い合わせ一覧ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry/list");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

test.describe("お問い合わせ詳細ページ", () => {
  test("未ログイン状態ではログインページへリダイレクトされること", async ({ page }) => {
    await page.goto("/inquiry/detail?inquiryNo=1");

    await expect(page).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
    await expect(page).toHaveTitle(/ログイン/);
  });
});

authTest.describe("お問い合わせページ（ログイン済み）", () => {
  authTest("ログインページへリダイレクトされず、フォームが表示されること", async ({
    workerPage: page,
  }) => {
    await page.goto("/inquiry");

    await authExpect(page).toHaveURL(/\/inquiry$/);
    await authExpect(page).toHaveTitle(/お問い合わせ/);
    await authExpect(page.getByLabel("件名")).toBeVisible();
    await authExpect(page.getByLabel("本文")).toBeVisible();
  });

  authTest("件名・本文を入力して送信すると受付完了メッセージが表示されること", async ({
    workerPage: page,
  }) => {
    await page.goto("/inquiry");

    await page.getByLabel("件名").fill("E2Eテストからのお問い合わせ");
    await page.getByLabel("本文").fill("E2Eテストの自動投稿本文です。");
    await page.getByRole("button", { name: "送信" }).click();

    await authExpect(page.getByText("お問い合わせを受け付けました")).toBeVisible({
      timeout: 10000,
    });
  });

  authTest("件名・本文が未入力の場合はバリデーションエラーが表示されること", async ({
    workerPage: page,
  }) => {
    await page.goto("/inquiry");

    await page.getByLabel("件名").focus();
    await page.getByLabel("件名").blur();
    await page.getByLabel("本文").focus();
    await page.getByLabel("本文").blur();

    await authExpect(page.getByText("件名を入力してください")).toBeVisible();
    await authExpect(page.getByText("本文を入力してください")).toBeVisible();
  });

  authTest(
    "アクセシビリティ違反がないこと（バリデーションエラー表示時）",
    async ({ workerPage: page }, testInfo) => {
      authTest.skip(
        testInfo.project.name !== "chromium",
        "a11y検証はchromiumプロジェクトのみで実施する"
      );

      await page.goto("/inquiry");
      await page.getByLabel("件名").focus();
      await page.getByLabel("件名").blur();
      await page.getByLabel("本文").focus();
      await page.getByLabel("本文").blur();
      await authExpect(page.getByText("件名を入力してください")).toBeVisible();

      await expectNoAccessibilityViolations(page);
    }
  );

  authTest(
    "お問い合わせ登録APIがサーバーエラーを返した場合、エラーメッセージが表示され受付完了にならないこと",
    async ({ workerPage: page }) => {
      // workerPage は同一ワーカー内の他のspecファイルとも使い回す共有ページのため、
      // ここで登録したルートモックを外し忘れると、後続の別テストが行う本物のお問い合わせ
      // 登録リクエストまで横取りしてしまう。必ず finally で unroute する
      const inquiriesUrlPattern = "**/api/v1/inquiries";
      const mockServerError = (route: import("@playwright/test").Route) => {
        if (route.request().method() === "POST") {
          return route.fulfill({
            status: 500,
            contentType: "application/json",
            body: JSON.stringify({ errorMessage: "内部サーバーエラーが発生しました。" }),
          });
        }
        return route.continue();
      };
      await page.route(inquiriesUrlPattern, mockServerError);

      try {
        await page.goto("/inquiry");
        await page.getByLabel("件名").fill("E2Eサーバーエラーテスト");
        await page.getByLabel("本文").fill("E2Eサーバーエラーテストの本文です。");
        await page.getByRole("button", { name: "送信" }).click();

        // 5xx応答時はレスポンス本文のメッセージを使わず、固定のフォールバック文言を表示する仕様
        // （frontend/src/lib/api/client.ts の readErrorMessage）
        await authExpect(
          page.getByRole("alert").filter({ hasText: "お問い合わせの登録に失敗しました" })
        ).toBeVisible({ timeout: 10000 });
        await authExpect(page.getByText("お問い合わせを受け付けました")).toHaveCount(0);
        await authExpect(page).toHaveURL(/\/inquiry$/);
      } finally {
        await page.unroute(inquiriesUrlPattern, mockServerError);
      }
    }
  );
});

authTest.describe("お問い合わせ一覧ページ（ログイン済み）", () => {
  authTest("ログインページへリダイレクトされないこと", async ({ workerPage: page }) => {
    await page.goto("/inquiry/list");

    await authExpect(page).toHaveURL(/\/inquiry\/list$/);
    await authExpect(page).toHaveTitle(/お問い合わせ一覧/);
  });
});
