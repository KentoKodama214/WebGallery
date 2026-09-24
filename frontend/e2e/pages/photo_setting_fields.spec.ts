import path from "path";
import { test as authTest, expect as authExpect } from "../fixtures/auth";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

/**
 * `photo_setting.spec.ts`・`photo_setting_validation.spec.ts` は画像・タイトル・容量/形式検証のみを
 * 対象としており、`PhotoSettingForm.tsx` の以下の項目は一切検証されていなかった。
 * 本ファイルではそれらのエッジケース・入出力の往復を検証する。
 * ・タイトル（英語）・撮影日時・向き・EXIF数値4項目（焦点距離/F値/シャッタースピード/ISO）・
 *   位置情報公開設定の入力と保存後の再読み込みでの反映
 * ・EXIF数値項目の「正の数値を入力してください」バリデーション
 * ・撮影日時の未来日付バリデーション
 * ・タグの日本語名必須・スペース禁止バリデーション
 * ・追加した画像・タグの個別削除（保存前のクライアント側操作）
 */
const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

authTest.describe("写真設定ページ（画像・タグの個別削除）", () => {
  authTest(
    "選択済みの画像を個別に削除でき、残りの画像だけがプレビューされること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles([PHOTO_1, PHOTO_2]);
      await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await authExpect(page.getByTestId("image-preview-item-1")).toBeVisible();

      await page.getByTestId("remove-image-0").click();

      await authExpect(page.getByTestId("image-preview-item-1")).toHaveCount(0);
      await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
    }
  );

  authTest(
    "追加したタグを個別に削除でき、削除したタグの入力欄が消えること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("add-tag-button").click();
      await page.getByTestId("tag-japanese-1").fill("削除される予定のタグ");
      await page.getByTestId("add-tag-button").click();
      await page.getByTestId("tag-japanese-2").fill("残るタグ");

      await page.getByTestId("remove-tag-1").click();

      await authExpect(page.getByTestId("tag-entry-1")).toHaveCount(0);
      await authExpect(page.getByTestId("tag-japanese-2")).toHaveValue("残るタグ");
    }
  );
});

authTest.describe("写真設定ページ（バリデーション未検証項目）", () => {
  authTest(
    "EXIF数値項目（焦点距離・F値・シャッタースピード・ISO）に0以下の値を入力すると、それぞれ正の数値エラーが表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles(PHOTO_1);
      await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("EXIF検証用写真");

      await page.getByTestId("focal-length-input").fill("-1");
      await page.getByTestId("f-value-input").fill("-1");
      await page.getByTestId("shutter-speed-input").fill("-1");
      await page.getByTestId("iso-input").fill("-1");

      await page.getByTestId("submit-button").click();

      const errors = page.getByTestId("validation-errors");
      await authExpect(errors).toBeVisible();
      await authExpect(errors.getByText("焦点距離は正の数値を入力してください")).toBeVisible();
      await authExpect(errors.getByText("F値は正の数値を入力してください")).toBeVisible();
      await authExpect(
        errors.getByText("シャッタースピードは正の数値を入力してください")
      ).toBeVisible();
      await authExpect(errors.getByText("ISOは正の数値を入力してください")).toBeVisible();
      // バリデーションで弾かれるため、保存API自体は呼ばれず一覧へは遷移しない
      await authExpect(page).toHaveURL(new RegExp(`/photo/${testUser.accountId}/photo_setting$`));
    }
  );

  authTest(
    "撮影日時に未来の日時を入力すると、過去日時を指定するよう促すエラーが表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles(PHOTO_1);
      await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("撮影日時検証用写真");
      await page.getByTestId("photo-at-input").fill("2099-01-01T00:00");

      await page.getByTestId("submit-button").click();

      await authExpect(
        page.getByTestId("validation-errors").getByText("撮影日時は過去の日時を指定してください")
      ).toBeVisible();
    }
  );

  authTest(
    "タグの日本語名が未入力または半角/全角スペースを含む場合、保存できずエラーが表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles(PHOTO_1);
      await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("タグ検証用写真");

      await authTest.step("日本語名が未入力のタグがあると保存できないこと", async () => {
        await page.getByTestId("add-tag-button").click();
        await page.getByTestId("tag-english-1").fill("english only");

        await page.getByTestId("submit-button").click();

        await authExpect(
          page.getByTestId("validation-errors").getByText("タグの日本語名は必須です")
        ).toBeVisible();
      });

      await authTest.step("日本語名にスペースが含まれると保存できないこと", async () => {
        await page.getByTestId("tag-japanese-1").fill("風 景");

        await page.getByTestId("submit-button").click();

        await authExpect(
          page.getByTestId("validation-errors").getByText("タグの日本語名にスペースは使用できません")
        ).toBeVisible();
      });
    }
  );
});

authTest.describe("写真設定ページ（未検証フィールドの保存・再読み込み後の反映）", () => {
  authTest(
    "英語タイトル・撮影日時・EXIF数値・位置情報公開・向きを保存すると、編集画面の再読み込み後も値が保持されること",
    async ({ browser }, testInfo) => {
      // 他テストと共有する workerPage の写真件数を変えたくないため、この検証専用の
      // 使い捨てアカウントを個別に登録する
      const accountId = generateTestAccountId(testInfo.workerIndex);
      const context = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
      const page = await context.newPage();
      await registerAccount(page, accountId, "E2E Field Roundtrip User");
      await login(page, accountId);

      const englishTitle = "Exif Roundtrip Photo";
      const photoAt = "2024-06-01T10:30";

      let detailUrl = "";
      await authTest.step("英語タイトル・撮影日時・EXIF数値・位置情報公開をオンにして新規登録する", async () => {
        await page.goto(`/photo/${accountId}/photo_setting`);
        await authExpect(page.getByTestId("image-input")).toBeAttached();

        await page.getByTestId("image-input").setInputFiles(PHOTO_1);
        await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
        await page.getByTestId("japanese-title-input").fill("EXIF往復確認用写真");
        await page.getByTestId("english-title-input").fill(englishTitle);
        await page.getByTestId("photo-at-input").fill(photoAt);
        await page.getByTestId("focal-length-input").fill("50");
        await page.getByTestId("f-value-input").fill("1.8");
        await page.getByTestId("shutter-speed-input").fill("0.005");
        await page.getByTestId("iso-input").fill("200");
        await page.getByTestId("location-public-checkbox").check();

        await page.getByTestId("submit-button").click();
        await authExpect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
        await page.getByRole("button", { name: "閉じる" }).click();
        await authExpect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
          timeout: 10000,
        });

        const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
        await authExpect(detailLinks).toHaveCount(1, { timeout: 10000 });
        const href = await detailLinks.first().getAttribute("href");
        authExpect(href).toBeTruthy();
        detailUrl = href!;
      });

      let changedDirection = "";
      await authTest.step("編集画面を開くと、保存した値がそのまま表示されること", async () => {
        await page.goto(detailUrl);
        await page.getByRole("button", { name: "編集" }).click();
        await authExpect(page).toHaveURL(/photo_setting\?accountNo=\d+&photoNo=\d+/, {
          timeout: 10000,
        });

        await authExpect(page.getByTestId("english-title-input")).toHaveValue(englishTitle);
        await authExpect(page.getByTestId("photo-at-input")).toHaveValue(photoAt);
        await authExpect(page.getByTestId("focal-length-input")).toHaveValue("50");
        await authExpect(page.getByTestId("f-value-input")).toHaveValue("1.8");
        await authExpect(page.getByTestId("shutter-speed-input")).toHaveValue("0.005");
        await authExpect(page.getByTestId("iso-input")).toHaveValue("200");
        await authExpect(page.getByTestId("location-public-checkbox")).toBeChecked();

        // 向きは新規登録時にバックエンドが画像の実ピクセルサイズから判定するため、初期値を
        // 決め打ちせず、現在値と異なる方へ変更することで往復を検証する
        const directionSelect = page.getByTestId("direction-select");
        await authExpect(directionSelect).toBeVisible();
        const current = await directionSelect.inputValue();
        changedDirection = current === "vertical" ? "horizontal" : "vertical";
        await directionSelect.selectOption(changedDirection);

        await page.getByTestId("submit-button").click();
        await authExpect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
        await page.getByRole("button", { name: "閉じる" }).click();
      });

      await authTest.step("再読み込みしても、変更した向きと他の値が保持されていること", async () => {
        await page.reload();

        await authExpect(page.getByTestId("direction-select")).toHaveValue(changedDirection);
        await authExpect(page.getByTestId("english-title-input")).toHaveValue(englishTitle);
        await authExpect(page.getByTestId("photo-at-input")).toHaveValue(photoAt);
        await authExpect(page.getByTestId("location-public-checkbox")).toBeChecked();
      });

      await context.close();
    }
  );
});
