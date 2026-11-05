import path from "path";
import type { Page } from "@playwright/test";
import { test, expect } from "../fixtures/admin";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");

/**
 * 管理者用写真管理画面で、指定アカウントの写真の公開状態を変更する
 *
 * @param adminPage 管理者でログイン済みのページ
 * @param ownerAccountId 写真所有者のアカウントID
 * @param buttonName 操作ボタンの名前（「非公開にする」/「非公開を解除」）
 * @param reason 理由
 * @param expectedMessage 成功時のメッセージ
 */
async function changeVisibility(
  adminPage: Page,
  ownerAccountId: string,
  buttonName: string,
  reason: string,
  expectedMessage: string
): Promise<void> {
  await adminPage.goto("/admin/photo_management");
  await expect(adminPage.getByRole("heading", { name: "写真管理" })).toBeVisible({ timeout: 10000 });
  await fillStable(adminPage.getByLabel("アカウントID"), ownerAccountId);
  await adminPage.getByRole("button", { name: "検索" }).click();

  // 絞り込み結果の反映を待つ（反映前の全アカウントの一覧に対して操作しないよう、所有者の1行だけになるまで待つ）
  const rows = adminPage.locator("tbody tr");
  await expect(rows).toHaveCount(1, { timeout: 10000 });
  await expect(rows.first()).toContainText(ownerAccountId);

  await rows.first().getByRole("button", { name: buttonName }).click();
  const dialog = adminPage.getByTestId("visibility-confirm-dialog");
  await expect(dialog).toBeVisible();
  await dialog.getByLabel(/理由/).fill(reason);
  await dialog.getByRole("button", { name: "実行" }).click();

  await expect(adminPage.getByRole("status")).toHaveText(expectedMessage, { timeout: 10000 });
}

test.describe("管理者による写真の非公開（管理者が非公開にする〜所有者以外から見えなくなる〜解除する）", () => {
  test("管理者が非公開にした写真は所有者本人にだけ表示され、解除すると再び誰にでも表示されること", async ({
    adminPage,
    browser,
  }, testInfo) => {
    const ownerAccountId = generateTestAccountId(testInfo.workerIndex);
    const japaneseTitle = "E2E管理者非公開テスト写真";
    const caption = "E2E管理者非公開テストのキャプション";

    const ownerContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const ownerPage = await ownerContext.newPage();
    const visitorContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const visitorPage = await visitorContext.newPage();

    try {
      let detailHref: string | null = null;

      await test.step("所有者が写真を1枚アップロードする", async () => {
        await registerAccount(ownerPage, ownerAccountId, "E2E Hidden Photo Owner");
        await login(ownerPage, ownerAccountId);

        await ownerPage.goto(`/photo/${ownerAccountId}/photo_setting`);
        await expect(ownerPage.getByTestId("image-input")).toBeAttached();
        await ownerPage.getByTestId("image-input").setInputFiles([PHOTO_1]);
        await expect(ownerPage.getByTestId("image-preview-item-0")).toBeVisible();
        await fillStable(ownerPage.getByTestId("japanese-title-input"), japaneseTitle);
        await fillStable(ownerPage.getByTestId("caption-input"), caption);
        await ownerPage.getByTestId("submit-button").click();
        await expect(ownerPage.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
        await ownerPage.getByRole("button", { name: "閉じる" }).click();
        await expect(ownerPage).toHaveURL(new RegExp(`/photo/${ownerAccountId}/photo_list$`), {
          timeout: 10000,
        });

        const detailLinks = ownerPage.locator('a[href*="/photo_detail?photoNo="]');
        await expect(detailLinks).toHaveCount(1, { timeout: 10000 });
        detailHref = await detailLinks.first().getAttribute("href");
        expect(detailHref).toBeTruthy();
      });

      await test.step("非公開にする前は、未ログインの訪問者にも写真が表示されること", async () => {
        await visitorPage.goto(`/photo/${ownerAccountId}/photo_list`);
        await expect(visitorPage.getByAltText(caption)).toHaveCount(1, { timeout: 10000 });
      });

      await test.step("管理者が写真を非公開にする", async () => {
        await changeVisibility(
          adminPage,
          ownerAccountId,
          "非公開にする",
          "E2E：お問い合わせで不適切な画像との報告があったため",
          "写真を非公開にしました。"
        );
        await expect(
          adminPage.locator("tbody tr").first().getByRole("button", { name: "非公開を解除" })
        ).toBeVisible();
      });

      await test.step("未ログインの訪問者には、一覧にも詳細にも表示されないこと", async () => {
        await visitorPage.goto(`/photo/${ownerAccountId}/photo_list`);
        await expect(visitorPage.getByText("写真がありません")).toBeVisible({ timeout: 10000 });
        await expect(visitorPage.getByAltText(caption)).toHaveCount(0);

        // URLを知っていても詳細は取得できない
        await visitorPage.goto(detailHref!);
        await expect(visitorPage.getByText(/写真が(存在しません|見つかりません)/)).toBeVisible({
          timeout: 10000,
        });
        await expect(visitorPage.getByText(japaneseTitle)).toHaveCount(0);
      });

      await test.step("所有者本人には、非公開の表示付きで一覧・詳細に表示されること", async () => {
        await ownerPage.goto(`/photo/${ownerAccountId}/photo_list`);
        await expect(ownerPage.getByAltText(caption)).toHaveCount(1, { timeout: 10000 });
        await expect(ownerPage.getByText("管理者により非公開")).toBeVisible();

        await ownerPage.goto(detailHref!);
        await expect(ownerPage.getByText(japaneseTitle)).toBeVisible({ timeout: 10000 });
        await expect(ownerPage.getByTestId("hidden-by-admin-notice")).toBeVisible();
      });

      await test.step("管理者が非公開を解除すると、未ログインの訪問者にも再び表示されること", async () => {
        await changeVisibility(
          adminPage,
          ownerAccountId,
          "非公開を解除",
          "E2E：確認の結果、問題がなかったため",
          "写真の非公開を解除しました。"
        );

        await visitorPage.goto(`/photo/${ownerAccountId}/photo_list`);
        await expect(visitorPage.getByAltText(caption)).toHaveCount(1, { timeout: 10000 });

        await ownerPage.goto(`/photo/${ownerAccountId}/photo_list`);
        await expect(ownerPage.getByAltText(caption)).toHaveCount(1, { timeout: 10000 });
        await expect(ownerPage.getByText("管理者により非公開")).toHaveCount(0);
      });
    } finally {
      await ownerContext.close();
      await visitorContext.close();
    }
  });
});
