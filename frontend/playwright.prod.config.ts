import { defineConfig } from "@playwright/test";

/**
 * 本番ビルド（`next build` + `next start`）に対する CSP スモークテスト用の Playwright 設定。
 *
 * 通常の E2E（`playwright.config.ts`）は `next dev` で起動するため、本番でのみ付与される
 * CSP ディレクティブ（`src/proxy.ts` の `style-src-elem` 等）と、Next.js が本番ビルドで
 * 生成する `<style>` / `<link>` への nonce 付与が検証されない。この設定は本番ビルドを
 * 起動し、公開ページで CSP 違反が発生しないこと・スタイルが適用されることを確認する。
 */
export default defineConfig({
  testDir: "./e2e/prod-smoke",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: "html",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
  },
  webServer: {
    command: "pnpm build && pnpm start",
    url: "http://localhost:3000",
    reuseExistingServer: !process.env.CI,
    timeout: 180_000,
  },
});
