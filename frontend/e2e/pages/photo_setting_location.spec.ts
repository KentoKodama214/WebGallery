import path from "path";
import { test, expect } from "@playwright/test";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

/**
 * 写真設定ページの「撮影場所」入力（`PhotoSettingForm.tsx`の新規ロケーション登録・既存ロケーション選択・
 * 地図クリックでの緯度経度取得）を検証する。ロケーションはアカウント単位のマスタ（`common.location_mst`）
 * として永続化され、同名での再登録は既存のロケーション番号を再利用する仕様（`PhotoAggregateRepositoryImpl`）
 * のため、「既存ロケーションから選択」の検証は事前にUI経由で1件登録したロケーションを使う
 * （DB直接投入は行わない。UI操作で到達できる状態のため）。
 *
 * 地図のクリック位置からの逆ジオコーディング（住所自動補完）は外部サービス（Nominatim）への
 * 実通信に依存し結果が非決定的なため、住所の内容自体はアサーションしない。緯度・経度が
 * クリックにより入力されることのみを検証する
 */
const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

test.describe("写真設定ページ（撮影場所：新規ロケーション登録）", () => {
  test.describe.configure({ timeout: 60_000 });

  test("地図をクリックすると緯度・経度が自動入力され、新規ロケーションとして保存できること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Location Regist User");
    await login(page, accountId);

    await page.goto(`/photo/${accountId}/photo_setting`);
    await expect(page.getByTestId("image-input")).toBeAttached();
    await page.getByTestId("image-input").setInputFiles(PHOTO_1);
    await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
    await page.getByTestId("japanese-title-input").fill("新規ロケーション検証用写真");

    await page.getByTestId("location-mode-new").check();
    await page.getByTestId("new-location-management-name-input").fill("E2E新規ロケーション_管理用");
    await page.getByTestId("new-location-display-name-input").fill("E2E新規ロケーション");

    const map = page.getByTestId("location-map-picker");
    await expect(map).toBeVisible();
    // Leafletの初期化（動的import→L.map()）は非同期のため、地図コンテナ自身への
    // クラス付与（leaflet-container、Leafletがコンテナに直接付与する）を待ってから
    // クリックする。空のdivが可視というだけではクリックリスナーの登録前にクリックしてしまう場合がある
    await expect(map).toHaveClass(/leaflet-container/, { timeout: 10000 });
    const box = (await map.boundingBox())!;
    await page.mouse.click(box.x + box.width / 2, box.y + box.height / 2);

    const latitudeInput = page.getByTestId("new-location-latitude-input");
    const longitudeInput = page.getByTestId("new-location-longitude-input");
    await expect(latitudeInput).not.toHaveValue("", { timeout: 10000 });
    await expect(longitudeInput).not.toHaveValue("");

    await page.getByTestId("submit-button").click();
    await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
  });

  test("新規ロケーション登録モードで管理名が未入力の場合、保存できずエラーが表示されること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Location Validation User");
    await login(page, accountId);

    await page.goto(`/photo/${accountId}/photo_setting`);
    await expect(page.getByTestId("image-input")).toBeAttached();
    await page.getByTestId("image-input").setInputFiles(PHOTO_1);
    await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
    await page.getByTestId("japanese-title-input").fill("管理名未入力検証用写真");

    await page.getByTestId("location-mode-new").check();
    await page.getByTestId("submit-button").click();

    await expect(
      page.getByTestId("validation-errors").getByText("管理名を入力してください")
    ).toBeVisible();
  });

  test("新規ロケーション登録モードで表示名が未入力の場合、保存できずエラーが表示されること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Location Display Validation User");
    await login(page, accountId);

    await page.goto(`/photo/${accountId}/photo_setting`);
    await expect(page.getByTestId("image-input")).toBeAttached();
    await page.getByTestId("image-input").setInputFiles(PHOTO_1);
    await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
    await page.getByTestId("japanese-title-input").fill("表示名未入力検証用写真");

    await page.getByTestId("location-mode-new").check();
    await page.getByTestId("new-location-management-name-input").fill("E2E管理名のみ");
    await page.getByTestId("submit-button").click();

    await expect(
      page.getByTestId("validation-errors").getByText("表示名を入力してください")
    ).toBeVisible();
  });

  test("既存のロケーションから選択モードで未選択のまま保存すると、エラーが表示されること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Location Unselected User");
    await login(page, accountId);

    await page.goto(`/photo/${accountId}/photo_setting`);
    await expect(page.getByTestId("image-input")).toBeAttached();
    await page.getByTestId("image-input").setInputFiles(PHOTO_1);
    await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
    await page.getByTestId("japanese-title-input").fill("ロケーション未選択検証用写真");

    await page.getByTestId("location-mode-existing").check();
    await page.getByTestId("submit-button").click();

    await expect(
      page.getByTestId("validation-errors").getByText("ロケーションを選択してください")
    ).toBeVisible();
  });
});

test.describe("写真設定ページ（撮影場所：既存ロケーションからの選択・再利用）", () => {
  test.describe.configure({ timeout: 60_000 });

  test("先に登録したロケーションを2枚目の写真で選択でき、編集画面を再読み込みしても選択状態が保持されること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Location Reuse User");
    await login(page, accountId);

    let detailUrl2 = "";
    await test.step("1枚目：新規ロケーションを登録する", async () => {
      await page.goto(`/photo/${accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();
      await page.getByTestId("image-input").setInputFiles(PHOTO_1);
      await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("1枚目：新規ロケーション登録");

      await page.getByTestId("location-mode-new").check();
      await page.getByTestId("new-location-management-name-input").fill("E2E再利用ロケーション");
      await page.getByTestId("new-location-display-name-input").fill("E2E再利用スポット表示名");
      await page.getByTestId("new-location-latitude-input").fill("35.6812");
      await page.getByTestId("new-location-longitude-input").fill("139.7671");

      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
        timeout: 10000,
      });
    });

    await test.step("2枚目：既存のロケーションから選択して登録する", async () => {
      await page.goto(`/photo/${accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();
      await page.getByTestId("image-input").setInputFiles(PHOTO_2);
      await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("2枚目：既存ロケーション選択");

      await page.getByTestId("location-mode-existing").check();
      const select = page.getByTestId("location-select");
      await expect(select.locator("option", { hasText: "E2E再利用ロケーション" })).toHaveCount(1, {
        timeout: 10000,
      });
      await select.selectOption({ label: "E2E再利用ロケーション" });

      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
        timeout: 10000,
      });

      const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
      await expect(detailLinks).toHaveCount(2, { timeout: 10000 });
      // 2枚目（新しい方）の詳細リンクを取得する
      const href = await detailLinks.first().getAttribute("href");
      expect(href).toBeTruthy();
      detailUrl2 = href!;
    });

    await test.step("2枚目の編集画面を開くと、既存選択モードでそのロケーションが選ばれていること", async () => {
      await page.goto(detailUrl2);
      await page.getByRole("button", { name: "編集" }).click();
      await expect(page).toHaveURL(/photo_setting\?accountNo=\d+&photoNo=\d+/, {
        timeout: 10000,
      });

      await expect(page.getByTestId("location-mode-existing")).toBeChecked();
      await expect(page.getByTestId("location-select")).toHaveValue(/.+/);

      await page.reload();
      await expect(page.getByTestId("location-mode-existing")).toBeChecked({ timeout: 10000 });
    });
  });
});
