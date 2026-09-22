import { test, expect, type Page } from "@playwright/test";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

/**
 * 既存の`photo_list.spec.ts`は自分自身のギャラリー、または未存在アカウントの表示のみを
 * 検証しており、「他人のギャラリーを閲覧する際に、オーナー限定のUI（写真追加・向きフィルター）
 * が表示されないこと」「未認証の訪問者にはお気に入り関連UIも表示されないこと」は
 * 検証されていなかった。本ファイルでは、この権限依存のUI出し分けを検証する
 * （IDOR等の防御自体は`scenarios/cross_account_access_control.spec.ts`で別途検証済み）。
 */

/** 写真一覧のフィルターパネル内は向き/お気に入りともに data-testid のない<select>のため、選択肢のテキストで存在有無を判定する */
function directionFilterSelect(page: Page) {
  return page.getByTestId("filter-panel").locator("select").filter({ hasText: "縦写真" });
}

test.describe("写真一覧ページ（他人のギャラリー閲覧時のUI出し分け）", () => {
  test("所有者本人には写真追加ボタンと向きフィルターが表示されること", async ({
    page,
  }, testInfo) => {
    const ownerAccountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, ownerAccountId, "E2E Gallery Owner");
    await login(page, ownerAccountId);

    await page.goto(`/photo/${ownerAccountId}/photo_list`);
    await expect(page.getByRole("link", { name: "＋写真追加" })).toBeVisible();

    await page.getByTestId("filter-trigger").click();
    await expect(directionFilterSelect(page)).toBeVisible();
    await expect(page.getByTestId("favorite-filter-select")).toBeVisible();
  });

  test("別アカウントでログイン中の訪問者には、写真追加ボタンと向きフィルターが表示されないこと", async ({
    page,
    browser,
  }, testInfo) => {
    const ownerAccountId = generateTestAccountId(testInfo.workerIndex);
    const visitorAccountId = `${generateTestAccountId(testInfo.workerIndex)}v`;

    await registerAccount(page, ownerAccountId, "E2E Gallery Owner");

    const visitorContext = await browser.newContext({
      baseURL: testInfo.project.use.baseURL,
    });
    const visitorPage = await visitorContext.newPage();
    await registerAccount(visitorPage, visitorAccountId, "E2E Gallery Visitor");
    await login(visitorPage, visitorAccountId);

    await visitorPage.goto(`/photo/${ownerAccountId}/photo_list`);
    await expect(visitorPage.getByRole("link", { name: "＋写真追加" })).toHaveCount(0);

    await visitorPage.getByTestId("filter-trigger").click();
    await expect(directionFilterSelect(visitorPage)).toHaveCount(0);
    // 認証済みではあるため、お気に入りフィルターは表示される
    await expect(visitorPage.getByTestId("favorite-filter-select")).toBeVisible();

    await visitorContext.close();
  });

  test("未認証の訪問者には、写真追加ボタン・向きフィルター・お気に入り関連UIが表示されないこと", async ({
    page,
    browser,
  }, testInfo) => {
    const ownerAccountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, ownerAccountId, "E2E Gallery Owner");

    const anonymousContext = await browser.newContext({
      baseURL: testInfo.project.use.baseURL,
    });
    const anonymousPage = await anonymousContext.newPage();

    await anonymousPage.goto(`/photo/${ownerAccountId}/photo_list`);
    await expect(anonymousPage.getByRole("link", { name: "＋写真追加" })).toHaveCount(0);

    await anonymousPage.getByTestId("filter-trigger").click();
    await expect(directionFilterSelect(anonymousPage)).toHaveCount(0);
    await expect(anonymousPage.getByTestId("favorite-filter-select")).toHaveCount(0);

    await anonymousContext.close();
  });
});
