import path from "path";
import { test, expect } from "../fixtures/admin";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";
import { fillStable } from "../fixtures/form";

const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const PHOTO_2 = path.join(__dirname, "../fixtures/images/e2e-photo-2.png");

test.describe("ポートフォリオで使っている写真を管理者が非公開にした場合の表示", () => {
  test("公開ページのグリッドは非公開の写真の枠だけを空けて行の構成を保ち、編集画面では外すよう案内して外すまで保存できないこと", async ({
    adminPage,
    browser,
  }, testInfo) => {
    // 写真の枚数・ポートフォリオの状態を前提にするため、使い捨てのアカウントを使う
    const ownerAccountId = generateTestAccountId(testInfo.workerIndex);
    const theme = "E2E非公開写真入りグリッド";
    let editUrl = "";

    const ownerContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const ownerPage = await ownerContext.newPage();
    const visitorContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
    const visitor = await visitorContext.newPage();

    try {
      await test.step("所有者が写真を2枚アップロードし、グリッド型（2枚均等の1行）のポートフォリオを公開する", async () => {
        await registerAccount(ownerPage, ownerAccountId, "E2E Hidden Portfolio Owner");
        await login(ownerPage, ownerAccountId);

        await ownerPage.goto(`/photo/${ownerAccountId}/photo_setting`);
        await expect(ownerPage.getByTestId("image-input")).toBeAttached();
        await ownerPage.getByTestId("image-input").setInputFiles([PHOTO_1, PHOTO_2]);
        await expect(ownerPage.getByTestId("image-preview-item-1")).toBeVisible();
        await fillStable(ownerPage.getByTestId("japanese-title-input"), "E2E非公開ポートフォリオ写真");
        await ownerPage.getByTestId("submit-button").click();
        await expect(ownerPage.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
        await ownerPage.getByRole("button", { name: "閉じる" }).click();
        await expect(ownerPage).toHaveURL(new RegExp(`/photo/${ownerAccountId}/photo_list$`), {
          timeout: 10000,
        });

        await ownerPage.goto(`/${ownerAccountId}/portfolio_setting/edit`);
        await fillStable(ownerPage.locator("#portfolio-theme"), theme);
        await ownerPage.getByRole("radio", { name: "グリッド" }).check();

        await ownerPage.getByRole("button", { name: "写真を選択" }).click();
        const dialog = ownerPage.getByTestId("photo-picker-dialog");
        // 候補は写真番号の降順のため、新しい写真→古い写真の順に選ばれる
        const photoButtons = dialog.getByTestId("photo-picker-unselected").getByRole("button", {
          name: /^写真\d+.*を選択$/,
        });
        await expect(photoButtons).toHaveCount(2, { timeout: 10000 });
        await photoButtons.nth(0).click();
        await photoButtons.nth(0).click();
        await expect(dialog.getByTestId("photo-picker-selected").getByRole("button")).toHaveCount(2);
        await dialog.getByRole("button", { name: "完了" }).click();

        await ownerPage.getByRole("button", { name: "行を追加" }).click();
        await expect(ownerPage.getByTestId("grid-photo-count")).toContainText(
          "行の枚数の合計 2枚 / 写真 2枚"
        );

        const resultDialog = ownerPage.getByTestId("portfolio-result-dialog");
        await ownerPage.getByRole("button", { name: "下書きとして登録" }).click();
        await expect(resultDialog).toContainText("ポートフォリオを登録しました。", { timeout: 10000 });
        await resultDialog.getByRole("button", { name: "閉じる" }).click();
        await ownerPage.getByRole("button", { name: "公開する" }).click();
        await expect(resultDialog).toContainText("ポートフォリオを公開しました。", { timeout: 10000 });
        await resultDialog.getByRole("button", { name: "閉じる" }).click();
        editUrl = ownerPage.url();
      });

      const portfolioNo = new URL(editUrl).searchParams.get("portfolioNo");
      const publicUrl = `/portfolio/${ownerAccountId}/${portfolioNo}`;

      await test.step("管理者が、ポートフォリオの先頭の枠の写真（新しい方の写真）を非公開にする", async () => {
        await adminPage.goto("/admin/photo_management");
        await expect(adminPage.getByRole("heading", { name: "写真管理" })).toBeVisible({
          timeout: 10000,
        });
        await fillStable(adminPage.getByLabel("アカウントID"), ownerAccountId);
        await adminPage.getByRole("button", { name: "検索" }).click();

        // 絞り込み結果の反映を待つ（一覧は登録日時の降順のため、先頭の行が新しい方の写真）
        const rows = adminPage.locator("tbody tr");
        await expect(rows).toHaveCount(2, { timeout: 10000 });
        await expect(rows.first()).toContainText(ownerAccountId);

        await rows.first().getByRole("button", { name: "非公開にする" }).click();
        const confirmDialog = adminPage.getByTestId("visibility-confirm-dialog");
        await expect(confirmDialog).toBeVisible();
        await confirmDialog.getByLabel(/理由/).fill("E2E：不適切な画像との報告があったため");
        await confirmDialog.getByRole("button", { name: "実行" }).click();
        await expect(adminPage.getByRole("status")).toHaveText("写真を非公開にしました。", {
          timeout: 10000,
        });
      });

      await test.step("公開ページでは、非公開の写真の枠だけが空き、行の構成が保たれること", async () => {
        await visitor.goto(publicUrl);
        await expect(visitor.getByRole("heading", { level: 1, name: theme })).toBeVisible({
          timeout: 10000,
        });
        const row = visitor.getByTestId("grid-row");
        await expect(row).toHaveCount(1);
        await expect(row).toHaveAttribute("data-pattern", "TWO_EQUAL");
        // 先頭の枠が空き枠になり、残りの写真は2つ目の枠に表示されたまま
        await expect(row.locator("> *").first()).toHaveAttribute("data-testid", "grid-empty-cell");
        await expect(row.getByRole("img")).toHaveCount(1);
      });

      await test.step("編集画面では、非公開の写真にその旨と外すよう案内が表示され、外すまで保存できないこと", async () => {
        await ownerPage.goto(editUrl);
        await expect(ownerPage.getByTestId("portfolio-photo-item")).toHaveCount(2, { timeout: 10000 });
        const notices = ownerPage.locator('[data-testid^="portfolio-photo-hidden-notice-"]');
        await expect(notices).toHaveCount(1);
        await expect(notices.first()).toContainText("管理者により非公開");
        // 非公開の写真は先頭の枠（1つ目）の写真
        await expect(
          ownerPage
            .getByTestId("portfolio-photo-item")
            .first()
            .locator('[data-testid^="portfolio-photo-hidden-notice-"]')
        ).toHaveCount(1);

        await ownerPage.getByRole("button", { name: "保存する" }).click();
        await expect(
          ownerPage.getByText("管理者により非公開にされた写真が含まれています。外してから保存してください")
        ).toBeVisible();
        await expect(ownerPage.getByTestId("portfolio-result-dialog")).toHaveCount(0);

        // 非公開の写真を外すと案内が消える
        await ownerPage
          .getByTestId("portfolio-photo-item")
          .first()
          .getByRole("button", { name: /を外す$/ })
          .click();
        await expect(ownerPage.getByTestId("portfolio-photo-item")).toHaveCount(1);
        await expect(notices).toHaveCount(0);
      });

      await test.step("非公開の写真は、写真選択ダイアログの候補に表示されず選択できないこと", async () => {
        await ownerPage.getByRole("button", { name: "写真を選択" }).click();
        const dialog = ownerPage.getByTestId("photo-picker-dialog");
        // 候補の読み込み完了を待つ（候補は選択済みの1枚だけで、非公開の写真は未選択の候補に出てこない）
        await expect(dialog.getByTestId("photo-picker-selected").getByRole("button")).toHaveCount(1, {
          timeout: 10000,
        });
        await expect(dialog.getByText("未選択の写真はありません")).toBeVisible({ timeout: 10000 });
        await expect(
          dialog.getByTestId("photo-picker-unselected").getByRole("button", { name: /^写真\d+.*を選択$/ })
        ).toHaveCount(0, { timeout: 10000 });
        await dialog.getByRole("button", { name: "キャンセル" }).click();
      });
    } finally {
      await ownerContext.close();
      await visitorContext.close();
    }
  });
});
