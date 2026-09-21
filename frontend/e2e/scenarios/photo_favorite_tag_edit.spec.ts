import path from "path";
import { test, expect } from "../fixtures/auth";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");

test.describe("写真のお気に入り・タグ・編集", () => {
  test("タグ付きで登録した写真をお気に入り登録し、編集後もタグ・お気に入り絞り込みが正しく機能すること", async ({
    workerPage: page,
    testUser,
  }) => {
    const originalTitle = "お気に入りタグ編集テスト写真";
    const originalCaption = "編集前のキャプションです";
    const originalTag = "風景";
    const editedTitle = "編集後のタイトル";
    const editedCaption = "編集後のキャプションです";
    const addedTag = "編集後タグ";

    await test.step("タグを1つ付けて写真を1枚登録する", async () => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles(PHOTO_1);
      await expect(page.getByTestId("image-preview-item-0")).toBeVisible();

      await page.getByTestId("japanese-title-input").fill(originalTitle);
      await page.getByTestId("caption-input").fill(originalCaption);

      await page.getByTestId("add-tag-button").click();
      await page.getByTestId("tag-japanese-1").fill(originalTag);

      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await expect(page.getByText("1枚の写真を保存しました")).toBeVisible();
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${testUser.accountId}/photo_list$`), {
        timeout: 10000,
      });
    });

    const detailLinks = page.locator('a[href*="/photo_detail?photoNo="]');
    let detailUrl = "";

    await test.step("登録した写真の詳細URLを取得し、タグが表示されていることを確認する", async () => {
      await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
      const href = await detailLinks.first().getAttribute("href");
      expect(href).toBeTruthy();
      detailUrl = href!;

      await page.goto(detailUrl);
      await expect(page).toHaveTitle(/写真詳細/);
      await expect(page.getByText(originalTag)).toBeVisible();
    });

    await test.step("詳細ページでお気に入り登録し、リロード後も維持されること", async () => {
      const favoriteButton = page.getByTestId("favorite-button");
      await expect(favoriteButton).toHaveAttribute("aria-pressed", "false");
      await expect(favoriteButton).toHaveAttribute("aria-label", "お気に入り登録");

      await favoriteButton.click();
      await expect(favoriteButton).toHaveAttribute("aria-pressed", "true");
      await expect(favoriteButton).toHaveAttribute("aria-label", "お気に入り解除");

      await page.reload();
      await expect(page.getByTestId("favorite-button")).toHaveAttribute(
        "aria-pressed",
        "true"
      );
    });

    await test.step("編集画面でタイトル・キャプションを変更し、タグを追加する", async () => {
      await page.getByRole("button", { name: "編集" }).click();
      await expect(page).toHaveTitle(/写真設定/);
      await expect(page.getByTestId("image-preview")).toBeVisible();

      const existingTagInput = page
        .locator('[data-testid^="tag-japanese-"]')
        .first();
      await expect(existingTagInput).toHaveValue(originalTag);

      await page.getByTestId("japanese-title-input").fill(editedTitle);
      await page.getByTestId("caption-input").fill(editedCaption);

      await page.getByTestId("add-tag-button").click();
      await page
        .locator('[data-testid^="tag-japanese-"]')
        .last()
        .fill(addedTag);

      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await expect(page.getByText("写真を保存しました")).toBeVisible();
      await page.getByRole("button", { name: "閉じる" }).click();
    });

    await test.step("詳細ページで編集内容が反映されていること", async () => {
      await page.goto(detailUrl);
      await expect(page.getByText(editedTitle)).toBeVisible();
      await expect(page.getByText(editedCaption)).toBeVisible();
      await expect(page.getByText(originalTag)).toBeVisible();
      await expect(page.getByText(addedTag)).toBeVisible();
    });

    await test.step("一覧のタグ絞り込みで編集後のタグにヒットすること", async () => {
      await page.goto(`/photo/${testUser.accountId}/photo_list`);
      await page.getByTestId("filter-trigger").click();
      await page.getByPlaceholder("キーワードを入力").fill(addedTag);
      await page.getByRole("button", { name: "絞り込み" }).click();

      await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
    });

    await test.step("お気に入り絞り込みでヒットすること（お気に入り登録中）", async () => {
      await page.getByTestId("filter-trigger").click();
      await page.getByPlaceholder("キーワードを入力").fill("");
      await page.getByTestId("favorite-filter-select").selectOption("true");
      await page.getByRole("button", { name: "絞り込み" }).click();

      await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
    });

    await test.step("一覧からお気に入りを解除すると、お気に入り絞り込みでヒットしなくなること", async () => {
      const listFavoriteButton = page.locator(
        '[data-testid^="list-favorite-button-"]'
      );
      await expect(listFavoriteButton).toHaveAttribute("aria-pressed", "true");
      await listFavoriteButton.click();
      await expect(listFavoriteButton).toHaveAttribute("aria-pressed", "false");

      await page.getByTestId("filter-trigger").click();
      await page.getByRole("button", { name: "絞り込み" }).click();

      await expect(detailLinks).toHaveCount(0, { timeout: 10000 });
      await expect(page.getByText("写真がありません")).toBeVisible();
    });
  });
});
