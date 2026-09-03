import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  // 本番ビルド専用のスモークテストは playwright.prod.config.ts で実行する
  testIgnore: "**/prod-smoke/**",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: "html",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
  },
  webServer: {
    command: "pnpm dev",
    url: "http://localhost:3000",
    reuseExistingServer: !process.env.CI,
  },
});
