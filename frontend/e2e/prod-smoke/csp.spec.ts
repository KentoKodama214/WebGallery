import { test, expect } from "@playwright/test";
import { collectCspViolations, cspDirectiveSources } from "../fixtures/csp";

/**
 * 本番ビルドでの CSP スモークテスト。
 *
 * - 公開ページ（ログイン・登録）で CSP 違反がコンソールに出ないこと
 * - 本番のみ付与されるディレクティブ（`style-src-elem`）が実際にヘッダーへ入っていること
 * - Tailwind の CSS が実際に適用されること（`style-src-elem` が Next.js の
 *   スタイルシートを誤ってブロックしていないことの確認）
 *
 * バックエンド・DB を必要としないページに絞り、ビルド＋起動のみで完結させる。
 */

/** 公開（未認証・バックエンド不要で到達可能な）ページ */
const PUBLIC_PATHS = ["/login", "/register"];

for (const path of PUBLIC_PATHS) {
  test(`${path} で CSP 違反が発生しないこと`, async ({ page }) => {
    const violations = collectCspViolations(page);

    // networkidle は AuthProvider がバックエンド不在の refresh をリトライする分だけ遅延しうるため
    // load を待ち、フォームの描画完了（＝CSS/スクリプトの評価が一巡した決定的なシグナル）を待ってから検証する
    await page.goto(path, { waitUntil: "load" });
    await page.locator("form").first().waitFor({ state: "visible" });

    expect(violations, `CSP 違反:\n${violations.join("\n")}`).toEqual([]);
  });
}

test("本番ビルドでは style-src-elem が nonce と自オリジンに限定されていること", async ({
  page,
}) => {
  // `style-src-elem` は `src/proxy.ts` が本番（NODE_ENV !== "development"）でのみ付与する。
  // 通常のE2Eは `next dev` 起動のため、このディレクティブの有無を検証できるのはここだけ。
  // 本番分岐が壊れて付与されなくなっても、CSP違反が出ないため上のテストでは気づけない
  const response = await page.goto("/login", { waitUntil: "load" });
  const csp = response?.headers()["content-security-policy"] ?? "";

  const styleSrcElem = cspDirectiveSources(csp, "style-src-elem");
  expect(styleSrcElem).toContain("'self'");
  expect(styleSrcElem.some((source) => source.startsWith("'nonce-"))).toBe(true);
  // <style>/<link> 要素側に 'unsafe-inline' を許してしまうと注入された <style> を防げない
  expect(styleSrcElem).not.toContain("'unsafe-inline'");

  // style 属性（React の style={{}}）向けのフォールバックは維持されていること
  expect(cspDirectiveSources(csp, "style-src")).toContain("'unsafe-inline'");
});

test("ログインページで Tailwind のスタイルが適用されること", async ({ page }) => {
  await page.goto("/login", { waitUntil: "load" });

  const button = page.getByRole("button", { name: "Log in" });
  await expect(button).toBeVisible();

  // bg-[#1565C0] が効いていれば rgb(21, 101, 192)。CSS がブロックされると透明になる。
  const backgroundColor = await button.evaluate(
    (el) => getComputedStyle(el).backgroundColor
  );
  expect(backgroundColor).toBe("rgb(21, 101, 192)");
});
