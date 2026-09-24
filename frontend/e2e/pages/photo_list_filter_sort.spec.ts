import fs from "fs";
import path from "path";
import { Client } from "pg";
import { test, expect, type Page } from "@playwright/test";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";
import { DB_CONFIG } from "../fixtures/db";

/**
 * `photo_favorite_tag_edit.spec.ts` はタグ・お気に入りの絞り込みのみを検証しており、
 * `PhotoList.tsx` が持つ以下の機能は一切検証されていなかった。
 * ・向きフィルター（オーナーのみ表示、縦写真/横写真）
 * ・並び順セレクト（撮影日順/お気に入り数順/季節・時期順）
 * ・「+もっと見る」によるページング（一覧自体では0件・不存在ケースしか検証されていなかった）
 * 本ファイルではこれらを検証する。
 */
const PHOTO_1_BUFFER = fs.readFileSync(path.join(__dirname, "../fixtures/images/e2e-photo-1.png"));

// バックエンドは同一アカウント内でのファイル名重複を検知して登録を拒否するため
// （`PhotoServiceImpl#registPhoto`）、同じ画像バイナリを使い回すこの検証ファイルでは
// アップロードのたびに一意なファイル名を採番する
let uploadFileNameSeq = 0;
function nextUploadFileName(): string {
  uploadFileNameSeq += 1;
  return `e2e-photo-list-${uploadFileNameSeq}.png`;
}

/** 写真一覧のフィルターパネル内は向き/並び順ともに data-testid を持たないため、選択肢のテキストで特定する */
function filterSelectByOptionText(page: Page, optionText: string) {
  return page.getByTestId("filter-panel").locator("select").filter({ hasText: optionText });
}

/** 新規登録で1枚アップロードし、一覧に反映されるまで待つ（画像・タイトル・キャプションを指定） */
async function uploadSinglePhoto(
  page: Page,
  accountId: string,
  japaneseTitle: string,
  caption: string
): Promise<void> {
  await page.getByTestId("image-input").setInputFiles({
    name: nextUploadFileName(),
    mimeType: "image/png",
    buffer: PHOTO_1_BUFFER,
  });
  await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
  await page.getByTestId("japanese-title-input").fill(japaneseTitle);
  await page.getByTestId("caption-input").fill(caption);

  await page.getByTestId("submit-button").click();
  await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
  await page.getByRole("button", { name: "閉じる" }).click();
  await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
    timeout: 10000,
  });
}

test.describe("写真一覧ページ（向きフィルター）", () => {
  test("向きで絞り込むと、指定した向きの写真のみ表示されること", async ({ page }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const verticalCaption = "向きフィルター検証用（縦）写真";
    const horizontalCaption = "向きフィルター検証用（横）写真";

    await registerAccount(page, accountId, "E2E Direction Filter User");
    await login(page, accountId);

    let verticalDetailUrl = "";
    await test.step("縦写真を1枚登録し、編集画面で向きを縦に設定する", async () => {
      await page.goto(`/photo/${accountId}/photo_setting`);
      await uploadSinglePhoto(page, accountId, "向きテスト（縦）", verticalCaption);

      const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
      await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
      verticalDetailUrl = (await detailLinks.first().getAttribute("href"))!;

      await page.goto(verticalDetailUrl);
      await page.getByRole("button", { name: "編集" }).click();
      await page.getByTestId("direction-select").selectOption("vertical");
      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
    });

    await test.step("横写真をもう1枚登録し、編集画面で向きを横に設定する", async () => {
      await page.goto(`/photo/${accountId}/photo_list`);
      await page.getByRole("link", { name: "＋写真追加" }).click();
      await uploadSinglePhoto(page, accountId, "向きテスト（横）", horizontalCaption);

      const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
      await expect(detailLinks).toHaveCount(2, { timeout: 10000 });
      const hrefs = await detailLinks.evaluateAll((els) =>
        els.map((el) => el.getAttribute("href"))
      );
      const horizontalDetailUrl = hrefs.find((href) => href !== verticalDetailUrl)!;

      await page.goto(horizontalDetailUrl);
      await page.getByRole("button", { name: "編集" }).click();
      await page.getByTestId("direction-select").selectOption("horizontal");
      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
    });

    await test.step("向き=縦で絞り込むと縦写真のみ表示されること", async () => {
      await page.goto(`/photo/${accountId}/photo_list`);
      await page.getByTestId("filter-trigger").click();
      await filterSelectByOptionText(page, "縦写真").selectOption("vertical");
      await page.getByRole("button", { name: "絞り込み" }).click();

      await expect(page.getByAltText(verticalCaption)).toBeVisible({ timeout: 10000 });
      await expect(page.getByAltText(horizontalCaption)).toHaveCount(0);
    });

    await test.step("向き=横で絞り込むと横写真のみ表示されること", async () => {
      await page.getByTestId("filter-trigger").click();
      await filterSelectByOptionText(page, "横写真").selectOption("horizontal");
      await page.getByRole("button", { name: "絞り込み" }).click();

      await expect(page.getByAltText(horizontalCaption)).toBeVisible({ timeout: 10000 });
      await expect(page.getByAltText(verticalCaption)).toHaveCount(0);
    });
  });
});

test.describe("写真一覧ページ（並び順）", () => {
  test("並び順をお気に入り数順にすると、お気に入り登録済みの写真が優先表示されること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const favoriteCaption = "並び順検証用（お気に入り）写真";
    const otherCaption = "並び順検証用（通常）写真";

    await registerAccount(page, accountId, "E2E Sort Order User");
    await login(page, accountId);

    let favoriteDetailUrl = "";
    await test.step("2枚登録する（先に登録した方をお気に入り登録する）", async () => {
      await page.goto(`/photo/${accountId}/photo_setting`);
      await uploadSinglePhoto(page, accountId, "並び順テスト1枚目", favoriteCaption);

      const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
      await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
      favoriteDetailUrl = (await detailLinks.first().getAttribute("href"))!;

      await page.getByRole("link", { name: "＋写真追加" }).click();
      await uploadSinglePhoto(page, accountId, "並び順テスト2枚目", otherCaption);
      await expect(detailLinks).toHaveCount(2, { timeout: 10000 });
    });

    await test.step("1枚目をお気に入り登録する", async () => {
      await page.goto(favoriteDetailUrl);
      const favoriteButton = page.getByTestId("favorite-button");
      await favoriteButton.click();
      await expect(favoriteButton).toHaveAttribute("aria-pressed", "true");
    });

    const detailLinks = () => page.locator('a[href*="/photo_detail?photoNo="]');

    await test.step("並び順をお気に入り数順に変更すると、お気に入り登録した1枚目が先頭に表示されること", async () => {
      await page.goto(`/photo/${accountId}/photo_list`);
      await expect(detailLinks()).toHaveCount(2, { timeout: 10000 });

      await page.getByTestId("filter-trigger").click();
      await filterSelectByOptionText(page, "お気に入り数順").selectOption("favorite");
      await page.getByRole("button", { name: "絞り込み" }).click();

      await expect(async () => {
        const firstHref = await detailLinks().first().getAttribute("href");
        expect(firstHref).toBe(favoriteDetailUrl);
      }).toPass({ timeout: 10000 });
    });
  });
});

/**
 * 写真の登録上限は権限区分によって異なり（`PhotoQuotaPolicy`）、新規登録直後の既定権限
 * （MINI）は既定で10枚までしか登録できない。1ページ20件のページング検証には最低でも
 * 21枚必要なため、この検証専用にDBを直接更新して上限のないnormal権限を付与する
 * （`fixtures/admin.ts`の`grantAdministratorAuthority`と同様の理由・手法）。
 */
async function grantNormalAuthority(accountId: string): Promise<void> {
  const client = new Client(DB_CONFIG);
  await client.connect();
  try {
    await client.query(
      `UPDATE common.account_authority
          SET authority_kbn = 'normal-user', updated_at = now()
        WHERE account_no = (SELECT account_no FROM common.account WHERE account_id = $1)`,
      [accountId]
    );
  } finally {
    await client.end();
  }
}

/** 新規登録で複数枚まとめてアップロードし、一覧に戻るまで待つ（バルク上限10枚/回のため分割送信用） */
async function uploadBulkPhotos(page: Page, accountId: string, count: number): Promise<void> {
  await page.getByTestId("image-input").setInputFiles(
    Array.from({ length: count }, () => ({
      name: nextUploadFileName(),
      mimeType: "image/png",
      buffer: PHOTO_1_BUFFER,
    }))
  );
  await expect(page.getByTestId(`image-preview-item-${count - 1}`)).toBeVisible();
  await page.getByTestId("japanese-title-input").fill("ページングテスト写真");

  await page.getByTestId("submit-button").click();
  await expect(page.getByText(`${count}枚の写真を保存しました`)).toBeVisible({ timeout: 15000 });
  await page.getByRole("button", { name: "閉じる" }).click();
  await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
    timeout: 10000,
  });
}

test.describe("写真一覧ページ（もっと見るページネーション）", () => {
  test.describe.configure({ timeout: 120_000 });

  test("21枚登録すると、1ページ目は20枚+もっと見るボタンが表示され、押下すると21枚目まで表示されボタンが消えること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);

    await test.step("上限のないnormal権限で21枚登録する（10枚+10枚+1枚）", async () => {
      await registerAccount(page, accountId, "E2E Pagination User");
      await grantNormalAuthority(accountId);
      await login(page, accountId);

      await page.goto(`/photo/${accountId}/photo_setting`);
      await uploadBulkPhotos(page, accountId, 10);

      await page.getByRole("link", { name: "＋写真追加" }).click();
      await uploadBulkPhotos(page, accountId, 10);

      await page.getByRole("link", { name: "＋写真追加" }).click();
      await uploadBulkPhotos(page, accountId, 1);
    });

    const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
    const showMoreButton = page.getByRole("button", { name: "+もっと見る" });

    await test.step("1ページ目は20枚表示され、+もっと見るボタンが表示されること", async () => {
      await expect(detailLinks).toHaveCount(20, { timeout: 10000 });
      await expect(showMoreButton).toBeVisible();
    });

    await test.step("+もっと見るを押すと21枚目まで表示され、ボタンが消えること", async () => {
      await showMoreButton.click();

      await expect(detailLinks).toHaveCount(21, { timeout: 15000 });
      await expect(showMoreButton).toHaveCount(0);
    });
  });

  test("ちょうど20枚登録すると、1ページ目に全件表示され+もっと見るボタンは表示されないこと", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);

    await test.step("上限のないnormal権限で20枚登録する（10枚+10枚）", async () => {
      await registerAccount(page, accountId, "E2E Pagination Boundary User");
      await grantNormalAuthority(accountId);
      await login(page, accountId);

      await page.goto(`/photo/${accountId}/photo_setting`);
      await uploadBulkPhotos(page, accountId, 10);

      await page.getByRole("link", { name: "＋写真追加" }).click();
      await uploadBulkPhotos(page, accountId, 10);
    });

    await test.step("20枚全件表示され、+もっと見るボタンは表示されないこと", async () => {
      const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
      await expect(detailLinks).toHaveCount(20, { timeout: 10000 });
      await expect(page.getByRole("button", { name: "+もっと見る" })).toHaveCount(0);
    });
  });
});
