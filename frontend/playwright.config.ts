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
  // 1テストあたりの上限。ほとんどのテストは前準備でアカウント登録（BCryptハッシュ化）と
  // ログインを行い、devサーバーは初回アクセス時にページをオンデマンドでコンパイルするため、
  // Playwright既定の30秒では並列実行時の負荷で頻繁に不足する（重いspecが個別に
  // describe.configure({ timeout: 60_000 }) で引き上げる運用になっていたが、引き上げ漏れの
  // specが負荷次第で落ちていた）。既定を60秒にし、さらに重いspecのみ個別に上書きする
  timeout: 60_000,
  retries: process.env.CI ? 2 : 0,
  // ワーカーごとに使い捨てアカウントを使う設計（fixtures/auth.ts・fixtures/admin.ts）のため
  // 並列実行してもテスト間の競合はない。直列実行（旧: 1）から引き上げて実行時間を短縮する。
  //
  // ローカルもCIと同じ2を既定にする。既定（undefined＝論理CPU数の半分）では、backendのJVM・
  // Docker（PostgreSQL・MinIO）・devサーバー・ブラウザが同居するローカル環境でCPUが飽和し
  // （8コア機でロードアベレージ102を観測）、ハイドレーション待ちやアップロード後の遷移待ちが
  // 5〜10秒のアサーションに間に合わず不定期に失敗していた。実測では2に下げた方が失敗が
  // 消えるうえ実行時間も短い（chromium単体: 4ワーカーで5.5〜7.0分・4件失敗 →
  // 2ワーカーで3.0分・0件失敗）。過負荷によるスラッシングで並列度が逆効果になっていた。
  // この2は8コア機での実測値のため、コア数の多いマシン・CIランナーでは環境変数
  // `PW_WORKERS` で引き上げられるようにしておく
  workers: Number(process.env.PW_WORKERS) || 2,
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
