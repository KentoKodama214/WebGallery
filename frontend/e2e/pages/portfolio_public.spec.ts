import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";

test.describe("ポートフォリオの公開ページ（未ログイン）", () => {
  test("アカウントIDの形式が不正な場合は404が表示されること", async ({ page }) => {
    const response = await page.goto("/portfolio/bad");

    expect(response?.status()).toBe(404);
    await expect(page.getByRole("heading", { name: "ポートフォリオが見つかりません" })).toBeVisible();
  });

  test("ポートフォリオ番号が正の整数でない・存在しない場合は404が表示されること", async ({ page }) => {
    for (const path of ["/portfolio/e2enoaccount1/abc", "/portfolio/e2enoaccount1/1"]) {
      const response = await page.goto(path);

      expect(response?.status()).toBe(404);
      await expect(page.getByRole("heading", { name: "ポートフォリオが見つかりません" })).toBeVisible();
    }
  });

  test("存在しないアカウントの一覧は、公開中のポートフォリオがない旨が表示されること", async ({ page }) => {
    // アカウントの存在有無を推測させないため、ポートフォリオがないアカウントと区別しない
    const response = await page.goto("/portfolio/e2enoaccount1");

    expect(response?.status()).toBe(200);
    await expect(page.getByText("公開中のポートフォリオはありません")).toBeVisible();
  });

  test("一覧ページにアクセシビリティ違反がないこと", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");

    await page.goto("/portfolio/e2enoaccount1");
    await expect(page.getByText("公開中のポートフォリオはありません")).toBeVisible();

    await expectNoAccessibilityViolations(page);
  });
});
