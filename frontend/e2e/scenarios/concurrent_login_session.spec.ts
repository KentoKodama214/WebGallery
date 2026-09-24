import { test, expect } from "@playwright/test";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

/**
 * バックエンドはログイン時に同一アカウントの既存セッションをすべて失効させる
 * （`AuthServiceImpl#login` の `revokeAllByAccountNo`、`fixtures/auth.ts` のコメント参照）。
 * この仕様自体は各specファイルが`workerPage`を使い回す設計の前提になっているが、
 * 「同一アカウントで別ブラウザ（別Cookie）から再ログインすると、先にログインしていた側は
 * 実際にセッション切れとなり、保護ページで再ログインへ誘導されること」自体は未検証だった。
 * 本シナリオでは2つの独立したブラウザコンテキストで再現し、この挙動を検証する。
 *
 * アクセストークンはメモリ保持のみのため、ページの完全な再読み込み（`page.goto`）で失われ、
 * `AuthProvider`のマウント時リフレッシュ（Cookieのリフレッシュトークンを使用）が走る。
 * 先にログインしていた側のリフレッシュトークンは後発ログインで失効済みのため、この
 * リフレッシュが401で失敗し、未ログイン確定としてログインページへ誘導される。
 */
test.describe("同一アカウントの二重ログインによるセッション競合", () => {
  test("別ブラウザで再ログインすると、先にログインしていた側は保護ページの再読み込みでログインページへ誘導されること", async ({
    browser,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);

    const contextA = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const pageA = await contextA.newPage();
    const contextB = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const pageB = await contextB.newPage();

    try {
      await test.step("ブラウザAでアカウント登録・ログインする", async () => {
        await registerAccount(pageA, accountId, "E2E Concurrent Session User");
        await login(pageA, accountId);
      });

      await test.step("同じアカウントでブラウザBから再ログインする", async () => {
        await login(pageB, accountId);
      });

      await test.step("ブラウザAで保護ページを再読み込みすると、ログインページへ誘導されること", async () => {
        // ソフトナビゲーション（リンククリック）ではアクセストークンがメモリに残ったままの
        // ため再現しない。完全な再読み込みを再現するには `page.goto` で実際のページ遷移を行う
        // 必要がある（`AuthProvider`のマウント時リフレッシュを踏ませるため）
        await pageA.goto("/inquiry");
        await expect(pageA).toHaveURL(/\/login(\?|$)/, { timeout: 10000 });
        await expect(pageA).toHaveTitle(/ログイン/);
      });

      await test.step("ブラウザBは引き続きログイン状態を維持していること", async () => {
        await pageB.goto("/inquiry");
        await expect(pageB).toHaveURL(/\/inquiry$/);
        await expect(pageB.getByLabel("件名")).toBeVisible();
      });
    } finally {
      await contextA.close();
      await contextB.close();
    }
  });
});
