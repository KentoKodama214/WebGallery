import { test, expect, type ConsoleMessage } from "@playwright/test";

/**
 * 本番ビルドでの CSP スモークテスト。
 *
 * - 公開ページ（ログイン・登録）で CSP 違反がコンソールに出ないこと
 * - Tailwind の CSS が実際に適用されること（`style-src-elem` が Next.js の
 *   スタイルシートを誤ってブロックしていないことの確認）
 *
 * バックエンド・DB を必要としないページに絞り、ビルド＋起動のみで完結させる。
 */

/** CSP 違反・リソース拒否を示すコンソール／ページエラー文言 */
const CSP_VIOLATION_PATTERN =
  /Content Security Policy|Refused to (load|apply|execute|connect)|violates the following Content Security Policy/i;

/** 公開（未認証・バックエンド不要で到達可能な）ページ */
const PUBLIC_PATHS = ["/login", "/register"];

for (const path of PUBLIC_PATHS) {
  test(`${path} で CSP 違反が発生しないこと`, async ({ page }) => {
    const violations: string[] = [];
    const collect = (msg: ConsoleMessage) => {
      if (msg.type() === "error" && CSP_VIOLATION_PATTERN.test(msg.text())) {
        violations.push(msg.text());
      }
    };
    page.on("console", collect);
    page.on("pageerror", (err) => {
      if (CSP_VIOLATION_PATTERN.test(err.message)) violations.push(err.message);
    });

    // networkidle は AuthProvider がバックエンド不在の refresh をリトライする分だけ遅延しうるため
    // load を待ち、CSS/スクリプト評価が落ち着くよう短く待機してから検証する
    await page.goto(path, { waitUntil: "load" });
    await page.waitForTimeout(500);

    expect(violations, `CSP 違反:\n${violations.join("\n")}`).toEqual([]);
  });
}

test("ログインページで Tailwind のスタイルが適用されること", async ({ page }) => {
  await page.goto("/login", { waitUntil: "load" });

  const button = page.getByRole("button", { name: "Log in" });
  await expect(button).toBeVisible();

  // bg-[#2196F3] が効いていれば rgb(33, 150, 243)。CSS がブロックされると透明になる。
  const backgroundColor = await button.evaluate(
    (el) => getComputedStyle(el).backgroundColor
  );
  expect(backgroundColor).toBe("rgb(33, 150, 243)");
});
