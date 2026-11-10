import path from "path";
import { test, expect, type Locator, type Page } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { generateTestAccountId, registerAndLogin } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

/**
 * 数値の入力欄の値を取得する
 *
 * @param page ページ
 * @param label 入力欄のラベル
 * @returns 入力欄の値
 */
async function numberValue(page: Page, label: RegExp): Promise<number> {
  return Number(await page.getByLabel(label).inputValue());
}

/**
 * 要素の中心の画面上の座標を取得する
 *
 * @param locator 要素
 * @returns 中心の座標
 */
async function centerOf(locator: Locator): Promise<{ x: number; y: number }> {
  const box = await locator.boundingBox();
  if (!box) throw new Error("要素が表示されていません");
  return { x: box.x + box.width / 2, y: box.y + box.height / 2 };
}

test.describe("額縁テンプレートを使ったポートフォリオ", () => {
  test("額縁テンプレートをキャンバスで作成し、ポートフォリオで選んで写真とメッセージを当てはめると、公開ページに組写真として表示されること", async ({
    page,
    browser,
  }, testInfo) => {
    // 写真の枚数・額縁テンプレートの有無を前提にするため、共有の workerPage ではなく使い捨てのアカウントを使う
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const templateName = "E2E横長2枚組";
    const theme = "E2E組写真";
    const message = "E2E春の訪れ";
    let editUrl = "";

    await test.step("アカウント登録・ログインし、写真を2枚アップロードする", async () => {
      await registerAndLogin(page, accountId, "E2E Frame User");
      await page.goto(`/photo/${accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();
      await page.getByTestId("image-input").setInputFiles([PHOTO_1, PHOTO_2]);
      await expect(page.getByTestId("image-preview-item-1")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("E2E額縁写真");
      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), { timeout: 10000 });
    });

    await test.step("ポートフォリオ設定から額縁テンプレートの作成画面を開く", async () => {
      await page.goto(`/${accountId}/portfolio_setting`);
      await page.getByRole("link", { name: "額縁テンプレート" }).click();
      await expect(page).toHaveURL(new RegExp(`/${accountId}/portfolio_setting/frame_templates$`), {
        timeout: 10000,
      });
      await expect(page.getByText("額縁テンプレートがありません")).toBeVisible({ timeout: 10000 });
      await page.getByRole("link", { name: "新規作成" }).click();
      await expect(page.getByRole("heading", { name: "額縁テンプレート作成" })).toBeVisible({
        timeout: 10000,
      });
    });

    await test.step("キャンバスで写真枠を削除・移動・大きさの変更・回転し、額縁テンプレートを登録する", async () => {
      await fillStable(page.locator("#frame-template-name"), templateName);
      await expect(page.getByTestId("frame-element-count")).toContainText("写真枠 3/30");

      // 既定の写真枠3つのうち3つ目を Delete キーで削除し、2枚組にする
      await page.getByRole("button", { name: "写真枠3" }).focus();
      await page.keyboard.press("Delete");
      await expect(page.getByTestId("frame-element-count")).toContainText("写真枠 2/30");

      // 写真枠1をドラッグして右下へ移動する
      const slot1 = page.getByRole("button", { name: "写真枠1" });
      const start = await centerOf(slot1);
      await page.mouse.move(start.x, start.y);
      await page.mouse.down();
      await page.mouse.move(start.x + 40, start.y + 30, { steps: 5 });
      await page.mouse.up();
      await expect(page.getByRole("heading", { name: "写真枠1" })).toBeVisible();
      expect(await numberValue(page, /^左/)).toBeGreaterThan(6);
      expect(await numberValue(page, /^上/)).toBeGreaterThan(12);

      // 右下のつまみで大きさを変える
      const widthBefore = await numberValue(page, /^横幅/);
      const resize = await centerOf(slot1.getByTestId("frame-handle-resize"));
      await page.mouse.move(resize.x, resize.y);
      await page.mouse.down();
      await page.mouse.move(resize.x + 30, resize.y + 20, { steps: 5 });
      await page.mouse.up();
      expect(await numberValue(page, /^横幅/)).toBeGreaterThan(widthBefore);

      // 上のつまみを枠の右へ回して傾ける
      const rotate = await centerOf(slot1.getByTestId("frame-handle-rotate"));
      const center = await centerOf(slot1);
      await page.mouse.move(rotate.x, rotate.y);
      await page.mouse.down();
      await page.mouse.move(center.x + 200, center.y - 200, { steps: 5 });
      await page.mouse.up();
      expect(await numberValue(page, /^傾き/)).toBeGreaterThan(20);

      // メッセージ枠のフォントを変える
      await page.getByRole("button", { name: "メッセージ枠1" }).focus();
      await page.getByLabel("フォント").selectOption("script");

      const resultDialog = page.getByTestId("frame-template-result-dialog");
      await page.getByRole("button", { name: "登録する" }).click();
      await expect(resultDialog).toContainText("額縁テンプレートを登録しました。", { timeout: 10000 });
      await resultDialog.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(/frameTemplateNo=1$/);
    });

    await test.step("一覧に登録した額縁テンプレートが見本とともに表示される", async () => {
      await page.getByRole("link", { name: "額縁テンプレート一覧へ戻る" }).click();
      const item = page.getByTestId("frame-template-item");
      await expect(item).toHaveCount(1, { timeout: 10000 });
      await expect(item).toContainText(templateName);
      await expect(item).toContainText("写真枠 2・メッセージ枠 1");
    });

    await test.step("額縁型のポートフォリオを作成し、額縁を選んで写真とメッセージを当てはめて公開する", async () => {
      await page.goto(`/${accountId}/portfolio_setting/edit`);
      await fillStable(page.locator("#portfolio-theme"), theme);
      await page.getByRole("radio", { name: "額縁" }).check();
      await expect(page.getByRole("combobox", { name: "額縁テンプレート" })).toHaveValue("1", { timeout: 10000 });
      await page.getByRole("button", { name: "この額縁を使う" }).click();
      await expect(page.getByTestId("applied-frame")).toContainText(templateName);
      await page.getByLabel("メッセージ1").fill(message);

      await page.getByRole("button", { name: "写真を選択" }).click();
      const dialog = page.getByTestId("photo-picker-dialog");
      // 未選択の写真（上段）を押すと選択済み（下段）へ移るため、先頭を2回選ぶ
      const photoButtons = dialog.getByTestId("photo-picker-unselected").getByRole("button", {
        name: /^写真\d+.*を選択$/,
      });
      await expect(photoButtons).toHaveCount(2, { timeout: 10000 });
      await photoButtons.nth(0).click();
      await photoButtons.nth(0).click();
      await dialog.getByRole("button", { name: "完了" }).click();
      await expect(page.getByTestId("frame-photo-count")).toHaveText("写真枠 2枠 / 写真 2枚");
      await expect(page.getByTestId("frame-preview").getByText(message)).toBeVisible();

      const resultDialog = page.getByTestId("portfolio-result-dialog");
      await page.getByRole("button", { name: "下書きとして登録" }).click();
      await expect(resultDialog).toContainText("ポートフォリオを登録しました。", { timeout: 10000 });
      await resultDialog.getByRole("button", { name: "閉じる" }).click();
      await page.getByRole("button", { name: "公開する" }).click();
      await expect(resultDialog).toContainText("ポートフォリオを公開しました。", { timeout: 10000 });
      await resultDialog.getByRole("button", { name: "閉じる" }).click();
      editUrl = page.url();
    });

    await test.step("額縁テンプレートを削除しても、選択時にコピーした額縁で表示され続けること", async () => {
      await page.goto(`/${accountId}/portfolio_setting/frame_templates`);
      await page.getByRole("button", { name: `${templateName}を削除` }).click();
      await page.getByRole("button", { name: "削除する" }).click();
      await expect(page.getByText("額縁テンプレートがありません")).toBeVisible({ timeout: 10000 });
    });

    const portfolioNo = new URL(editUrl).searchParams.get("portfolioNo");
    // 未ログインの閲覧者として、別のブラウザコンテキストで公開ページを開く
    const visitorContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const visitor = await visitorContext.newPage();

    try {
      await test.step("未ログインで公開ページを開くと、額縁に写真2枚とメッセージが配置されて表示される", async () => {
        await visitor.goto(`/portfolio/${accountId}/${portfolioNo}`);
        await expect(visitor.getByRole("heading", { level: 1, name: theme })).toBeVisible({ timeout: 10000 });
        const frame = visitor.getByTestId("frame-template");
        await expect(frame).toBeVisible();
        await expect(frame.getByTestId("frame-slot")).toHaveCount(2);
        await expect(frame.getByText(message)).toBeVisible();

        // 署名付きURLの画像が実際に読み込めていること（naturalWidth が 0 なら読み込みに失敗している）
        const images = frame.getByTestId("frame-slot").locator("img");
        await expect(images).toHaveCount(2);
        for (const image of await images.all()) {
          await expect
            .poll(() => image.evaluate((img: HTMLImageElement) => img.complete && img.naturalWidth > 0), {
              timeout: 10000,
            })
            .toBe(true);
        }
        // 写真枠1はキャンバスで回転させたため、傾けて表示される
        await expect(frame.getByTestId("frame-slot").first()).toHaveCSS("transform", /matrix/);
      });

      await test.step("公開ページにアクセシビリティ違反がない", async () => {
        if (testInfo.project.name === "chromium") {
          await expectNoAccessibilityViolations(visitor);
        }
      });
    } finally {
      await visitorContext.close();
    }
  });
});
