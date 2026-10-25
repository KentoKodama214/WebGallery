import path from "path";
import { test, expect, type Page } from "@playwright/test";
import { generateTestAccountId, registerAndLogin } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

/**
 * グリッド型（2枚均等の1行）のポートフォリオを作成して公開する
 *
 * @param page ログイン済みのページ
 * @param accountId アカウントID
 * @param theme テーマ
 * @param tag タグ（日本語）。未指定の場合はタグを付けない
 */
async function createPublishedPortfolio(page: Page, accountId: string, theme: string, tag?: string) {
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

  if (tag) {
    await page.getByRole("button", { name: "＋タグを追加" }).click();
    await fillStable(page.getByRole("textbox", { name: "タグ1（日本語）" }), tag);
  }

  const resultDialog = page.getByTestId("portfolio-result-dialog");
  await page.getByRole("button", { name: "下書きとして登録" }).click();
  await expect(resultDialog).toContainText("ポートフォリオを登録しました。", { timeout: 10000 });
  await resultDialog.getByRole("button", { name: "閉じる" }).click();
  await page.getByRole("button", { name: "公開する" }).click();
  await expect(resultDialog).toContainText("ポートフォリオを公開しました。", { timeout: 10000 });
  await resultDialog.getByRole("button", { name: "閉じる" }).click();
}

/**
 * 公開ページの検索欄に検索語を入力して検索し、検索結果のページへの遷移を待つ
 *
 * 遷移前の一覧に対して検証してしまわないよう、URLの検索語が入力した値になるまで待つ
 *
 * @param page 公開ページを開いているページ
 * @param keyword 検索語
 */
async function searchPublicPortfolios(page: Page, keyword: string) {
  await page.getByRole("searchbox", { name: "テーマ・タグ" }).fill(keyword);
  await page.getByRole("button", { name: "検索" }).click();
  await expect
    .poll(() => new URL(page.url()).searchParams.get("keyword"), { timeout: 10000 })
    .toBe(keyword);
}

/** 公開ページの一覧に並んでいるポートフォリオのテーマ（表示順） */
async function publicThemes(page: Page): Promise<string[]> {
  return page.getByTestId("public-portfolio-card").getByRole("heading", { level: 2 }).allTextContents();
}

test.describe("ポートフォリオの並び順と公開ページの検索", () => {
  test("設定画面で並び替えた順に公開ページの一覧が並び、テーマ・タグを1つの検索欄でOR条件で検索できること", async ({
    page,
    browser,
  }, testInfo) => {
    // ポートフォリオの件数・並び順を前提にするため、共有の workerPage ではなく使い捨てのアカウントを使う
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const springTheme = "E2E春の風景";
    const autumnTheme = "E2E秋の夕日";

    await test.step("アカウント登録・ログインし、写真を2枚アップロードする", async () => {
      await registerAndLogin(page, accountId, "E2E Order User");
      await page.goto(`/photo/${accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();
      await page.getByTestId("image-input").setInputFiles([PHOTO_1, PHOTO_2]);
      await expect(page.getByTestId("image-preview-item-1")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("E2E並び順写真");
      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), { timeout: 10000 });
    });

    await test.step("ポートフォリオを2件作成して公開する（作成した順に並ぶ）", async () => {
      await createPublishedPortfolio(page, accountId, springTheme, "桜");
      await createPublishedPortfolio(page, accountId, autumnTheme);

      await page.goto(`/${accountId}/portfolio_setting`);
      await expect(page.getByTestId("portfolio-row")).toHaveCount(2, { timeout: 10000 });
      await expect(page.getByTestId("portfolio-row").nth(0)).toContainText(springTheme);
      await expect(page.getByTestId("portfolio-row").nth(1)).toContainText(autumnTheme);
      await expect(page.getByRole("button", { name: `${springTheme}を上へ移動` })).toBeDisabled();
    });

    await test.step("上へ移動すると並び順が保存され、再読み込みしても保たれる", async () => {
      await page.getByRole("button", { name: `${autumnTheme}を上へ移動` }).click();
      await expect(page.getByRole("status")).toContainText(`「${autumnTheme}」の並び順を変更しました`, {
        timeout: 10000,
      });
      await expect(page.getByTestId("portfolio-row").nth(0)).toContainText(autumnTheme);

      await page.reload();
      await expect(page.getByTestId("portfolio-row")).toHaveCount(2, { timeout: 10000 });
      await expect(page.getByTestId("portfolio-row").nth(0)).toContainText(autumnTheme);
      await expect(page.getByTestId("portfolio-row").nth(1)).toContainText(springTheme);
    });

    // 未ログインの閲覧者として、別のブラウザコンテキストで公開ページを開く
    const visitorContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const visitor = await visitorContext.newPage();
    const refreshStatuses: number[] = [];
    visitor.on("response", (response) => {
      if (response.url().includes("/api/v1/auth/refresh")) {
        refreshStatuses.push(response.status());
      }
    });

    try {
      await test.step("未ログインで一覧を開くと、設定した並び順で表示され、セッション復元は401にならない", async () => {
        await visitor.goto(`/portfolio/${accountId}`);
        await expect(visitor.getByTestId("public-portfolio-card")).toHaveCount(2, { timeout: 10000 });
        expect(await publicThemes(visitor)).toEqual([autumnTheme, springTheme]);

        // 未ログインのセッション復元はエラー（401）ではなく 204 で返る
        await expect.poll(() => refreshStatuses.length, { timeout: 10000 }).toBeGreaterThan(0);
        expect(refreshStatuses.every((status) => status === 204)).toBe(true);
      });

      await test.step("タグに一致する検索語で、そのタグを持つポートフォリオだけに絞り込める", async () => {
        await searchPublicPortfolios(visitor, "桜");
        await expect(visitor.getByTestId("public-portfolio-card")).toHaveCount(1, { timeout: 10000 });
        expect(await publicThemes(visitor)).toEqual([springTheme]);
      });

      await test.step("テーマに一致する検索語で、そのテーマのポートフォリオだけに絞り込める", async () => {
        await searchPublicPortfolios(visitor, "秋の");
        await expect(visitor.getByTestId("public-portfolio-card")).toHaveCount(1, { timeout: 10000 });
        expect(await publicThemes(visitor)).toEqual([autumnTheme]);
      });

      await test.step("テーマ・タグのいずれにも一致する検索語では両方が並び順のまま表示される（OR条件）", async () => {
        await searchPublicPortfolios(visitor, "E2E");
        await expect(visitor.getByTestId("public-portfolio-card")).toHaveCount(2, { timeout: 10000 });
        expect(await publicThemes(visitor)).toEqual([autumnTheme, springTheme]);
      });

      await test.step("一致しない場合はその旨を表示し、クリアで全件に戻る", async () => {
        await searchPublicPortfolios(visitor, "該当なしの検索語");
        await expect(visitor.getByText("条件に一致するポートフォリオはありません")).toBeVisible({
          timeout: 10000,
        });

        await visitor.getByRole("link", { name: "クリア" }).click();
        await expect(visitor).toHaveURL(new RegExp(`/portfolio/${accountId}$`), { timeout: 10000 });
        await expect(visitor.getByTestId("public-portfolio-card")).toHaveCount(2, { timeout: 10000 });
      });
    } finally {
      await visitorContext.close();
    }
  });
});
