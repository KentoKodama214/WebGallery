import path from "path";
import { Client } from "pg";
import { test, expect, type Page } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";
import { DB_CONFIG } from "../fixtures/db";

/**
 * `photo_detail.spec.ts` は写真不存在時の表示のみを検証しており、`PhotoDetail.tsx` が持つ
 * 以下の表示ロジックは一切検証されていなかった。
 * ・英語タイトルの表示
 * ・EXIF設定テキスト（焦点距離・F値・シャッタースピード・ISOを結合した表示）
 * ・撮影場所の地図表示。特に、位置情報が非公開の場合に閲覧者が所有者本人でない限り
 *   秘匿される仕様（`PhotoServiceImpl#isLocationHiddenFor`）
 * 本ファイルではこれらを検証する。
 */
const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");

/**
 * `PhotoSettingForm`には撮影場所（緯度経度・ロケーション名）を入力するUIが存在せず、
 * バックエンドもEXIFのGPS情報からの自動抽出は行わない（`PhotoExifExtractor`は焦点距離・
 * F値・シャッタースピード・ISOのみを対象とする）。撮影場所はAPI経由でのみ設定可能なため、
 * この検証専用にDBへ直接ロケーションマスタを作成し、対象の写真に紐付ける
 * （`fixtures/admin.ts`の`grantAdministratorAuthority`と同様、UI操作だけでは到達できない
 * 状態を用意するための直接更新）。
 *
 * なお、位置情報の公開設定もこのヘルパーで直接更新する。編集フォーム経由の保存は
 * locationNoを一切送信しないため、`LocationNo.getOrDefault`によりlocation_noが0
 * （紐付けなし）へ巻き戻ってしまい、UI操作では地図を維持したまま公開設定だけを
 * 切り替えられない（本アプリの現在の仕様上の制約）
 */
async function linkLocationToPhoto(
  accountId: string,
  photoNo: number,
  locationName: string,
  isLocationPublic: boolean
): Promise<void> {
  const client = new Client(DB_CONFIG);
  await client.connect();
  try {
    const {
      rows: [{ account_no: accountNo }],
    } = await client.query<{ account_no: number }>(
      "SELECT account_no FROM common.account WHERE account_id = $1",
      [accountId]
    );

    await client.query(
      `INSERT INTO common.location_mst
         (account_no, location_no, created_by, created_at, updated_by, updated_at,
          is_deleted, location_name, address, latitude, longitude)
       VALUES ($1, 1, $1, now(), $1, now(), false, $2, 'テスト住所', 35.6595, 139.7005)`,
      [accountNo, locationName]
    );
    await client.query(
      `UPDATE photo.photo_mst SET location_no = 1, is_location_public = $3
        WHERE account_no = $1 AND photo_no = $2`,
      [accountNo, photoNo, isLocationPublic]
    );
  } finally {
    await client.end();
  }
}

/** 写真の位置情報公開フラグを直接更新する（`linkLocationToPhoto`と同じ理由でUI経由では切り替えられないため） */
async function setLocationPublic(
  accountId: string,
  photoNo: number,
  isLocationPublic: boolean
): Promise<void> {
  const client = new Client(DB_CONFIG);
  await client.connect();
  try {
    await client.query(
      `UPDATE photo.photo_mst SET is_location_public = $3
        WHERE account_no = (SELECT account_no FROM common.account WHERE account_id = $1)
          AND photo_no = $2`,
      [accountId, photoNo, isLocationPublic]
    );
  } finally {
    await client.end();
  }
}

/** 新規登録で1枚アップロードし、詳細URLを取得する */
async function uploadAndGetDetailUrl(
  page: Page,
  accountId: string,
  japaneseTitle: string
): Promise<{ detailUrl: string; photoNo: number }> {
  await page.goto(`/photo/${accountId}/photo_setting`);
  await expect(page.getByTestId("image-input")).toBeAttached();
  await page.getByTestId("image-input").setInputFiles(PHOTO_1);
  await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
  await page.getByTestId("japanese-title-input").fill(japaneseTitle);

  await page.getByTestId("submit-button").click();
  await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
  await page.getByRole("button", { name: "閉じる" }).click();
  await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
    timeout: 10000,
  });

  const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
  await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
  const href = (await detailLinks.first().getAttribute("href"))!;
  const photoNo = Number(new URL(href, "http://localhost").searchParams.get("photoNo"));
  return { detailUrl: href, photoNo };
}

test.describe("写真詳細ページ（英語タイトル・EXIF設定テキストの表示）", () => {
  test("英語タイトルとEXIF設定テキストが表示されること", async ({ page }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Detail Display User");
    await login(page, accountId);

    await page.goto(`/photo/${accountId}/photo_setting`);
    await expect(page.getByTestId("image-input")).toBeAttached();
    await page.getByTestId("image-input").setInputFiles(PHOTO_1);
    await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
    await page.getByTestId("japanese-title-input").fill("EXIF表示検証用写真");
    await page.getByTestId("english-title-input").fill("Exif Display Test Photo");
    await page.getByTestId("focal-length-input").fill("50");
    await page.getByTestId("f-value-input").fill("1.8");
    await page.getByTestId("shutter-speed-input").fill("0.005");
    await page.getByTestId("iso-input").fill("200");

    await page.getByTestId("submit-button").click();
    await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
    await page.getByRole("button", { name: "閉じる" }).click();

    const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
    await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
    // 一覧の詳細リンクはPhotoSwipeキャプション用の非表示要素のため、クリックではなくhrefで遷移する
    const href = await detailLinks.first().getAttribute("href");
    expect(href).toBeTruthy();
    await page.goto(href!);

    await expect(page).toHaveTitle(/写真詳細/);
    await expect(page.getByText("Exif Display Test Photo")).toBeVisible();
    await expect(page.getByText("50mm F1.8 0.005sec iso200")).toBeVisible();
  });

  test("アクセシビリティ違反がないこと", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");

    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E A11y Detail User");
    await login(page, accountId);
    const { detailUrl } = await uploadAndGetDetailUrl(page, accountId, "A11y検証用写真");
    await page.goto(detailUrl);

    await expectNoAccessibilityViolations(page);
  });

  test("画面表示が崩れていないこと（視覚回帰）", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "視覚回帰はchromiumプロジェクトのみで検証する");

    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Visual Regression Detail User");
    await login(page, accountId);
    const { detailUrl } = await uploadAndGetDetailUrl(page, accountId, "視覚回帰検証用写真");
    await page.goto(detailUrl);

    await expect(page).toHaveScreenshot("photo_detail.png", {
      fullPage: true,
      maxDiffPixelRatio: 0.02,
    });
  });
});

test.describe("写真詳細ページ（撮影場所の地図表示）", () => {
  test.describe.configure({ timeout: 60_000 });

  test("位置情報が非公開の間は所有者にのみ地図が表示され、公開にすると他アカウントからも見えるようになること", async ({
    page,
    browser,
  }, testInfo) => {
    const ownerAccountId = generateTestAccountId(testInfo.workerIndex);
    const visitorAccountId = `${generateTestAccountId(testInfo.workerIndex)}v`;
    const locationName = `E2Eテスト地点${testInfo.workerIndex}${Date.now()}`;

    await registerAccount(page, ownerAccountId, "E2E Location Owner");
    await login(page, ownerAccountId);

    let detailUrl = "";
    let photoNo = 0;
    await test.step("写真を1枚登録し、ロケーションを紐付ける（位置情報公開設定は既定でオフ）", async () => {
      const result = await uploadAndGetDetailUrl(page, ownerAccountId, "地図表示検証用写真");
      detailUrl = result.detailUrl;
      photoNo = result.photoNo;
      await linkLocationToPhoto(ownerAccountId, photoNo, locationName, false);
    });

    const visitorContext = await browser.newContext({
      baseURL: testInfo.project.use.baseURL,
    });
    const visitorPage = await visitorContext.newPage();
    await registerAccount(visitorPage, visitorAccountId, "E2E Location Visitor");
    await login(visitorPage, visitorAccountId);

    await test.step("非公開の間は、所有者本人には地図・ロケーション名が表示されること", async () => {
      await page.goto(detailUrl);
      await expect(page.getByText(locationName)).toBeVisible();
      await expect(page.locator('iframe[title="撮影場所の地図"]')).toBeVisible();
    });

    await test.step("非公開の間は、所有者以外には地図・ロケーション名が表示されないこと", async () => {
      await visitorPage.goto(detailUrl);
      await expect(visitorPage.getByText(locationName)).toHaveCount(0);
      await expect(visitorPage.locator('iframe[title="撮影場所の地図"]')).toHaveCount(0);
    });

    await test.step("位置情報を公開に変更する", async () => {
      await setLocationPublic(ownerAccountId, photoNo, true);
    });

    await test.step("公開後は、所有者以外にも地図・ロケーション名が表示されること", async () => {
      await visitorPage.goto(detailUrl);
      await expect(visitorPage.getByText(locationName)).toBeVisible({ timeout: 10000 });
      await expect(visitorPage.locator('iframe[title="撮影場所の地図"]')).toBeVisible();
    });

    await visitorContext.close();
  });
});
