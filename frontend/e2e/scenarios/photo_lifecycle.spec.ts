import path from "path";
import { test, expect } from "@playwright/test";
import { generateTestAccountId, TEST_USER_PASSWORD } from "../fixtures/auth";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

test.describe("写真ライフサイクル（アカウント登録〜ログイン〜アップロード〜一覧〜詳細〜削除）", () => {
  test("2枚まとめてアップロードした写真を一覧・詳細で確認し、1枚削除できること", async ({
    page,
  }, testInfo) => {
    // ワーカー単位で使い捨てのアカウントを登録する（fixtures/auth.ts の workerPage とは別に、
    // 登録〜ログイン自体もこのシナリオの検証対象に含めるため、ここで直接アカウントを作る）
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const accountName = "E2E Scenario User";
    const japaneseTitle = "E2Eシナリオテスト写真";
    const caption = "E2Eシナリオテストで一括登録した写真です";

    await test.step("アカウント登録", async () => {
      await page.goto("/register");
      await page.getByPlaceholder("半角英数字で8〜20文字").fill(accountId);
      await page
        .locator('label:has-text("アカウント名") + input')
        .fill(accountName);
      await page
        .getByPlaceholder("英字と数字を含む半角8〜72文字")
        .fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "登録" }).click();

      await expect(
        page.getByRole("dialog", { name: "アカウント登録完了" })
      ).toBeVisible({ timeout: 10000 });
    });

    await test.step("ログイン", async () => {
      await page.goto("/login");
      await page.getByPlaceholder("User ID").fill(accountId);
      await page.getByPlaceholder("Password").fill(TEST_USER_PASSWORD);
      await page.getByRole("button", { name: "Log in" }).click();

      await expect(page).toHaveURL(
        new RegExp(`/photo/${accountId}/photo_list`),
        { timeout: 10000 }
      );
    });

    await test.step("写真を2枚まとめてアップロードする", async () => {
      await page.goto(`/photo/${accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles([PHOTO_1, PHOTO_2]);
      await expect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await expect(page.getByTestId("image-preview-item-1")).toBeVisible();

      await page.getByTestId("japanese-title-input").fill(japaneseTitle);
      await page.getByTestId("caption-input").fill(caption);

      await page.getByTestId("submit-button").click();

      await expect(page.getByTestId("success-modal")).toBeVisible({
        timeout: 10000,
      });
      await expect(page.getByText("2枚の写真を保存しました")).toBeVisible();

      // 新規一括登録の成功モーダルを閉じると一覧へ自動的に戻る
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(
        new RegExp(`/photo/${accountId}/photo_list$`),
        { timeout: 10000 }
      );
    });

    // 一覧のDOM上には写真ごとに詳細ページへの隠しリンク（PhotoSwipeキャプション用）が
    // 存在するため、そのhrefから写真ごとの詳細URLを特定する
    const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');

    await test.step("一覧に2枚表示されること", async () => {
      await expect(detailLinks).toHaveCount(2, { timeout: 10000 });
      await expect(page.getByAltText(caption)).toHaveCount(2);
    });

    let firstPhotoHref: string | null = null;
    await test.step("1枚目の詳細を確認する", async () => {
      firstPhotoHref = await detailLinks.first().getAttribute("href");
      expect(firstPhotoHref).toBeTruthy();
      await page.goto(firstPhotoHref!);

      await expect(page).toHaveTitle(/写真詳細/);
      await expect(page.getByText(japaneseTitle)).toBeVisible();
      await expect(page.getByText(caption)).toBeVisible();
      await expect(page.getByRole("button", { name: "編集" })).toBeVisible();
      await expect(page.getByRole("button", { name: "削除" })).toBeVisible();
    });

    await test.step("1枚削除する", async () => {
      await page.getByRole("button", { name: "削除" }).click();
      await expect(page.getByTestId("delete-confirm-dialog")).toBeVisible();

      await page.getByRole("button", { name: "削除する" }).click();

      await expect(page).toHaveURL(
        new RegExp(`/photo/${accountId}/photo_list$`),
        { timeout: 10000 }
      );
    });

    await test.step("一覧に残り1枚だけ表示されること", async () => {
      await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
    });
  });
});
