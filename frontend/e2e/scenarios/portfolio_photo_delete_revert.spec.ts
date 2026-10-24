import path from "path";
import { test, expect } from "@playwright/test";
import { generateTestAccountId, registerAndLogin } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

test.describe("ポートフォリオの公開と、写真削除による下書きへの差し戻し", () => {
  test("公開中のポートフォリオで使っている写真を削除すると、下書きに戻った旨が表示され、確認して公開し直せること", async ({
    page,
  }, testInfo) => {
    // 写真の枚数・ポートフォリオの状態を前提にするため、共有の workerPage ではなく使い捨てのアカウントを使う
    const accountId = generateTestAccountId(testInfo.workerIndex);
    const theme = "E2E日本の自然";

    await test.step("アカウント登録・ログインし、写真を2枚アップロードする", async () => {
      await registerAndLogin(page, accountId, "E2E Portfolio User");
      await page.goto(`/photo/${accountId}/photo_setting`);
      await expect(page.getByTestId("image-input")).toBeAttached();
      await page.getByTestId("image-input").setInputFiles([PHOTO_1, PHOTO_2]);
      await expect(page.getByTestId("image-preview-item-1")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("E2Eポートフォリオ写真");
      await page.getByTestId("submit-button").click();
      await expect(page.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page).toHaveURL(new RegExp(`/photo/${accountId}/photo_list$`), {
        timeout: 10000,
      });
    });

    await test.step("2枚の写真でフィーチャー型のポートフォリオを作成して公開する", async () => {
      await page.goto(`/${accountId}/portfolio_setting/edit`);
      await fillStable(page.locator("#portfolio-theme"), theme);

      await page.getByRole("button", { name: "写真を選択" }).click();
      const dialog = page.getByTestId("photo-picker-dialog");
      // 上段が未選択の写真、下段が選択済みの写真
      const photoButtons = dialog.getByTestId("photo-picker-unselected").getByRole("button", {
        name: /^写真\d+.*を選択$/,
      });
      const selectedButtons = dialog.getByTestId("photo-picker-selected").getByRole("button", {
        name: /^写真\d+.*の選択を解除$/,
      });
      await expect(photoButtons).toHaveCount(2, { timeout: 10000 });
      await expect(selectedButtons).toHaveCount(0);

      // 1つの検索語（タイトル・場所・住所・タグ）で絞り込める
      const keywordInput = dialog.getByLabel("検索語（タイトル・場所・住所・タグ）");
      await keywordInput.fill("該当しないタイトル");
      await dialog.getByRole("button", { name: "検索" }).click();
      await expect(dialog.getByText("条件に一致する写真がありません")).toBeVisible({ timeout: 10000 });
      await keywordInput.fill("E2Eポートフォリオ");
      await dialog.getByRole("button", { name: "検索" }).click();
      await expect(photoButtons).toHaveCount(2, { timeout: 10000 });

      // キャンセルすると、ダイアログ内で選んだ写真は反映されない
      await photoButtons.nth(0).click();
      await expect(selectedButtons).toHaveCount(1);
      await dialog.getByRole("button", { name: "キャンセル" }).click();
      await expect(page.getByText("写真が選択されていません")).toBeVisible();

      await page.getByRole("button", { name: "写真を選択" }).click();
      await expect(photoButtons).toHaveCount(2, { timeout: 10000 });
      // 未選択の写真を押すと選択済みへ移り、選択済みの写真を押すと未選択へ戻る
      await photoButtons.nth(0).click();
      await expect(photoButtons).toHaveCount(1);
      await expect(selectedButtons).toHaveCount(1);
      await selectedButtons.nth(0).click();
      await expect(photoButtons).toHaveCount(2);
      await expect(selectedButtons).toHaveCount(0);
      await photoButtons.nth(0).click();
      await photoButtons.nth(0).click();
      await expect(selectedButtons).toHaveCount(2);
      await dialog.getByRole("button", { name: "完了" }).click();

      // フィーチャー型は全写真にテーマが必要
      const themeInputs = page.getByPlaceholder("テーマ（公開時は必須）");
      await expect(themeInputs).toHaveCount(2);
      await themeInputs.nth(0).fill("京都");
      await themeInputs.nth(1).fill("静岡");

      // 登録・公開の結果はダイアログで通知されるため、確認したら閉じて次の操作へ進む
      const resultDialog = page.getByTestId("portfolio-result-dialog");
      await page.getByRole("button", { name: "下書きとして登録" }).click();
      await expect(resultDialog).toContainText("ポートフォリオを登録しました。", {
        timeout: 10000,
      });
      await resultDialog.getByRole("button", { name: "閉じる" }).click();
      await expect(resultDialog).toHaveCount(0);

      await page.getByRole("button", { name: "公開する" }).click();
      await expect(resultDialog).toContainText("ポートフォリオを公開しました。", {
        timeout: 10000,
      });
      await resultDialog.getByRole("button", { name: "閉じる" }).click();
      await expect(page.getByTestId("portfolio-status")).toHaveText("公開中");
    });

    await test.step("一覧をテーマで絞り込める", async () => {
      await page.goto(`/${accountId}/portfolio_setting`);
      await expect(page.getByTestId("portfolio-row")).toHaveCount(1, { timeout: 10000 });

      await page.getByLabel("テーマ").fill("該当しないテーマ");
      await page.getByRole("button", { name: "検索" }).click();
      await expect(page.getByText("条件に一致するポートフォリオはありません")).toBeVisible({
        timeout: 10000,
      });

      await page.getByLabel("テーマ").fill("日本");
      await page.getByRole("button", { name: "検索" }).click();
      await expect(page.getByTestId("portfolio-row").filter({ hasText: theme })).toHaveCount(1, {
        timeout: 10000,
      });
    });

    await test.step("ポートフォリオで使っている写真を削除すると、下書きに戻った旨とリンクが表示される", async () => {
      await page.goto(`/photo/${accountId}/photo_list`);
      const detailLink = page.locator('a[href*="/photo_detail?photoNo="]').first();
      await expect(detailLink).toBeAttached({ timeout: 10000 });
      await page.goto((await detailLink.getAttribute("href"))!);

      await page.getByRole("button", { name: "削除" }).click();
      await page.getByRole("button", { name: "削除する" }).click();

      const revertedDialog = page.getByTestId("reverted-portfolio-dialog");
      await expect(revertedDialog).toBeVisible({ timeout: 10000 });
      await expect(revertedDialog).toContainText(
        `この写真を使用していたポートフォリオ（${theme}）を下書きに戻しました。内容を確認して公開してください。`
      );
      await revertedDialog.getByRole("link", { name: theme }).click();
    });

    await test.step("編集画面で下書きに戻ったことを確認し、公開し直せる", async () => {
      await expect(page).toHaveURL(/\/portfolio_setting\/edit\?portfolioNo=\d+$/, {
        timeout: 10000,
      });
      await expect(page.getByTestId("portfolio-status")).toHaveText("下書き", { timeout: 10000 });
      await expect(page.getByTestId("portfolio-photo-item")).toHaveCount(1);

      const resultDialog = page.getByTestId("portfolio-result-dialog");
      await page.getByRole("button", { name: "公開する" }).click();
      await expect(resultDialog).toContainText("ポートフォリオを公開しました。", {
        timeout: 10000,
      });
      await resultDialog.getByRole("button", { name: "閉じる" }).click();
      await expect(page.getByTestId("portfolio-status")).toHaveText("公開中");
    });

    await test.step("ポートフォリオを削除すると、復元できない旨を確認したうえで削除され、一覧から消える", async () => {
      await page.getByRole("button", { name: "削除", exact: true }).click();
      await expect(page.getByText("削除したポートフォリオは復元できません。")).toBeVisible();
      await page.getByRole("button", { name: "削除する" }).click();

      await expect(page).toHaveURL(new RegExp(`/${accountId}/portfolio_setting$`), {
        timeout: 10000,
      });
      await expect(page.getByText("ポートフォリオはありません")).toBeVisible({
        timeout: 10000,
      });
    });
  });
});
