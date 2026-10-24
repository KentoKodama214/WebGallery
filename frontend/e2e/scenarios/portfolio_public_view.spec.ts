import path from "path";
import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { generateTestAccountId, registerAndLogin } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

test.describe("ポートフォリオの公開ページ", () => {
  test("公開したグリッド型のポートフォリオを未ログインで閲覧でき、下書きに戻すとすぐに見られなくなること", async ({
    page,
    browser,
  }, testInfo) => {
    // 写真の枚数・ポートフォリオの状態を前提にするため、共有の workerPage ではなく使い捨てのアカウントを使う
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const theme = "E2E光と影";
    let editUrl = "";

    await test.step("アカウント登録・ログインし、写真を2枚アップロードする", async () => {
      await registerAndLogin(page, accountId, "E2E Public User");
      await page.goto(`/photo/${accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();
      await page.getByTestId("image-input").setInputFiles([PHOTO_1, PHOTO_2]);
      await expect(page.getByTestId("image-preview-item-1")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("E2E公開写真");
      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), { timeout: 10000 });
    });

    await test.step("グリッド型（2枚均等の1行）のポートフォリオを作成して公開する", async () => {
      await page.goto(`/${accountId}/portfolio_setting/edit`);
      await fillStable(page.locator("#portfolio-theme"), theme);
      await page.getByRole("radio", { name: "グリッド" }).check();

      await page.getByRole("button", { name: "写真を選択" }).click();
      const dialog = page.getByTestId("photo-picker-dialog");
      // 未選択の写真（上段）を押すと選択済み（下段）へ移るため、先頭を2回選ぶ
      const photoButtons = dialog.getByTestId("photo-picker-unselected").getByRole("button", {
        name: /^写真\d+.*を選択$/,
      });
      await expect(photoButtons).toHaveCount(2, { timeout: 10000 });
      await photoButtons.nth(0).click();
      await photoButtons.nth(0).click();
      await expect(dialog.getByTestId("photo-picker-selected").getByRole("button")).toHaveCount(2);
      await dialog.getByRole("button", { name: "完了" }).click();

      await page.getByRole("button", { name: "行を追加" }).click();
      await expect(page.getByTestId("grid-photo-count")).toContainText("行の枚数の合計 2枚 / 写真 2枚");

      await page.getByRole("button", { name: "下書きとして登録" }).click();
      await expect(page.getByText("ポートフォリオを登録しました。")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "公開する" }).click();
      await expect(page.getByText("ポートフォリオを公開しました。")).toBeVisible({ timeout: 10000 });
      editUrl = page.url();
    });

    const portfolioNo = new URL(editUrl).searchParams.get("portfolioNo");
    const publicUrl = `/portfolio/${accountId}/${portfolioNo}`;
    // 未ログインの閲覧者として、別のブラウザコンテキストで公開ページを開く
    const visitorContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const visitor = await visitorContext.newPage();

    try {
      await test.step("未ログインで一覧・詳細を閲覧でき、写真がグリッドで表示される", async () => {
        await visitor.goto(`/portfolio/${accountId}`);
        const card = visitor.getByTestId("public-portfolio-card");
        await expect(card).toHaveCount(1, { timeout: 10000 });
        await card.getByRole("link", { name: new RegExp(theme) }).click();

        await expect(visitor).toHaveURL(new RegExp(`${publicUrl}$`), { timeout: 10000 });
        await expect(visitor.getByRole("heading", { level: 1, name: theme })).toBeVisible();
        // タイトルはポートフォリオのテーマから生成する（メタデータはストリーミングで後から反映される）
        await expect(visitor).toHaveTitle(`${theme} - ${accountId} のポートフォリオ - WebGallery`, {
          timeout: 10000,
        });
        const row = visitor.getByTestId("grid-row");
        await expect(row).toHaveCount(1);
        await expect(row).toHaveAttribute("data-pattern", "TWO_EQUAL");

        // 署名付きURLの画像が実際に読み込めていること（naturalWidth が 0 なら読み込みに失敗している）
        const images = row.getByRole("img");
        await expect(images).toHaveCount(2);
        for (const image of await images.all()) {
          await expect
            .poll(() => image.evaluate((img: HTMLImageElement) => img.complete && img.naturalWidth > 0), {
              timeout: 10000,
            })
            .toBe(true);
        }
      });

      await test.step("詳細ページにアクセシビリティ違反がない", async () => {
        if (testInfo.project.name === "chromium") {
          await expectNoAccessibilityViolations(visitor);
        }
      });

      await test.step("設定画面で下書きに戻すと、公開ページはすぐに404になる", async () => {
        await page.goto(editUrl);
        await page.getByRole("button", { name: "下書きに戻す" }).click();
        await expect(page.getByText("ポートフォリオを下書きに戻しました。")).toBeVisible({ timeout: 10000 });

        // 公開ページは表示のたびに最新の公開状態を取得するため、待たずに反映される
        const response = await visitor.goto(publicUrl);
        expect(response?.status()).toBe(404);
        await visitor.goto(`/portfolio/${accountId}`);
        await expect(visitor.getByText("公開中のポートフォリオはありません")).toBeVisible();
      });
    } finally {
      await visitorContext.close();
    }
  });
});
