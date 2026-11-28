import fs from "fs";
import path from "path";
import { test, expect, type Page } from "@playwright/test";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

/**
 * 写真一覧の拡大表示（PhotoSwipe）で使うキャプションは、長文でも画面を占有しないよう
 * 100文字までに切り詰めて表示する（`PhotoList.tsx`の`truncateCaption`）。
 * 本ファイルではその切り詰めを、実際のアップロード〜一覧表示まで通して検証する。
 */
const PHOTO_1_BUFFER = fs.readFileSync(path.join(__dirname, "../fixtures/images/e2e-photo-1.png"));

/** 拡大表示時に表示されるキャプションの最大文字数（`PhotoList.tsx`の`CAPTION_MAX_LENGTH`と一致させる） */
const CAPTION_MAX_LENGTH = 100;

/** 新規登録で1枚アップロードし、一覧に反映されるまで待つ */
async function uploadPhotoWithCaption(
  page: Page,
  accountId: string,
  japaneseTitle: string,
  caption: string
): Promise<void> {
  await page.goto(`/photo/${accountId}/photo_setting`);
  await expect(page.getByTestId("image-input")).toBeAttached();
  await page.getByTestId("image-input").setInputFiles({
    name: "e2e-photo-list-caption.png",
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

test.describe("写真一覧ページ（拡大表示のキャプション）", () => {
  test("キャプションが101文字以上の場合は100文字までに切り詰められて末尾が...になること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Caption Truncate User");
    await login(page, accountId);

    const caption = `${"あ".repeat(CAPTION_MAX_LENGTH)}いうえお`;
    await uploadPhotoWithCaption(page, accountId, "キャプション切り詰め検証用写真", caption);

    // 拡大表示のキャプションは、一覧の非表示要素（.hidden-caption-content）を複製して生成される
    const captionContent = page.locator(".hidden-caption-content .caption_content").first();
    await expect(captionContent).toHaveText(`${"あ".repeat(CAPTION_MAX_LENGTH)}...`, {
      timeout: 10000,
    });
  });

  test("キャプションが100文字以内の場合は切り詰められずに全文表示されること", async ({
    page,
  }, testInfo) => {
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Caption Full User");
    await login(page, accountId);

    const caption = "あ".repeat(CAPTION_MAX_LENGTH);
    await uploadPhotoWithCaption(page, accountId, "キャプション全文表示検証用写真", caption);

    const captionContent = page.locator(".hidden-caption-content .caption_content").first();
    await expect(captionContent).toHaveText(caption, { timeout: 10000 });
  });
});
