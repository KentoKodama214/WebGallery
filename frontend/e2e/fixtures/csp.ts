import type { ConsoleMessage, Page } from "@playwright/test";

/**
 * CSP 違反・リソース拒否を示すコンソール／ページエラー文言
 *
 * ブラウザごとに文言が異なる（Chromium: `Refused to frame ... violates the following
 * Content Security Policy directive`、Firefox: `Content-Security-Policy: ...`）ため、
 * 共通して現れる語で拾う
 */
export const CSP_VIOLATION_PATTERN =
  /Content Security Policy|Content-Security-Policy|Refused to (load|apply|execute|connect|frame)|violates the following Content Security Policy/i;

/**
 * ページで発生した CSP 違反の収集を開始する
 *
 * 返す配列はテストの進行に伴って追記されるため、検証したい時点で中身を確認する。
 *
 * @param page 対象ページ
 * @returns 収集された違反メッセージ（呼び出し時点では空）
 */
export function collectCspViolations(page: Page): string[] {
  const violations: string[] = [];
  page.on("console", (msg: ConsoleMessage) => {
    if (msg.type() === "error" && CSP_VIOLATION_PATTERN.test(msg.text())) {
      violations.push(msg.text());
    }
  });
  page.on("pageerror", (err) => {
    if (CSP_VIOLATION_PATTERN.test(err.message)) violations.push(err.message);
  });
  return violations;
}

/**
 * CSP ヘッダーから指定ディレクティブの許可ソースを取り出す
 *
 * @param csp `Content-Security-Policy` ヘッダーの値
 * @param directive ディレクティブ名（例: `frame-src`）
 * @returns 許可ソースの配列。ディレクティブが無い場合は空配列
 */
export function cspDirectiveSources(csp: string, directive: string): string[] {
  const found = csp
    .split(";")
    .map((part) => part.trim())
    .find((part) => part === directive || part.startsWith(`${directive} `));
  if (!found) return [];
  return found.split(/\s+/).slice(1);
}
