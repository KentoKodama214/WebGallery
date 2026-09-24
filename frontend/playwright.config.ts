import { defineConfig, devices } from "@playwright/test";

/**
 * 主要導線のみ、Chromium以外のブラウザ・モバイルビューポートでも実行する。全specを
 * 複数ブラウザ化すると実行時間が大きく伸びるため、レイアウト崩れの影響が大きい代表的な
 * ページに絞る。
 *
 * `photo_detail_display.spec.ts`は対象外：認証Cookie（`AuthController`が発行する
 * リフレッシュトークンCookie）は本番のHTTPS環境向けに`Secure`属性を付与しているが、
 * WebKitは`http://localhost`（非HTTPS）ではChromiumと異なり`Secure`Cookieを保存しない。
 * そのため、ログイン後に`page.goto`でフルページ遷移すると（クライアント側メモリの
 * アクセストークンが失われ、Cookie頼みの再認証に失敗し）WebKit/モバイル（iPhoneは
 * WebKitエンジン）で恒常的に失敗する。同ページの視覚回帰・a11y検証はchromiumのみで行う
 */
const CROSS_BROWSER_SPECS = ["pages/login.spec.ts", "pages/photo_list.spec.ts"];

export default defineConfig({
  testDir: "./e2e",
  // 本番ビルド専用のスモークテストは playwright.prod.config.ts で実行する
  testIgnore: "**/prod-smoke/**",
  // ローカル実行時に蓄積するE2E生成アカウントを実行前にクリーンアップする（e2e/global-setup.ts）
  globalSetup: require.resolve("./e2e/global-setup"),
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  // ワーカーごとに使い捨てアカウントを使う設計（fixtures/auth.ts・fixtures/admin.ts）のため
  // 並列実行してもテスト間の競合はない。CIランナーのCPU数を踏まえ、直列実行（旧: 1）から
  // 引き上げて実行時間を短縮する
  workers: process.env.CI ? 2 : undefined,
  // CIではPlaywrightの公式GitHub Actions向けレポーターも併用し、失敗箇所をジョブサマリー・
  // チェックにアノテーションとして表示する（htmlレポートのartifactダウンロードのみに頼らない）
  reporter: process.env.CI ? [["html"], ["github"]] : "html",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
    {
      name: "webkit-smoke",
      use: { ...devices["Desktop Safari"] },
      testMatch: CROSS_BROWSER_SPECS,
    },
    {
      name: "mobile-smoke",
      use: { ...devices["iPhone 13"] },
      testMatch: CROSS_BROWSER_SPECS,
    },
  ],
  webServer: {
    command: "pnpm dev",
    url: "http://localhost:3000",
    reuseExistingServer: !process.env.CI,
  },
});
