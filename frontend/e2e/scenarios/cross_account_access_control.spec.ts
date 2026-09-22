import path from "path";
import {
  test as authTest,
  expect as authExpect,
  generateTestAccountId,
  login,
  registerAccount,
} from "../fixtures/auth";

/**
 * `pages/photo_setting.spec.ts`の「他人のphotoAccountIdでは権限エラー」は、URLの
 * パスセグメント（photoAccountId）自体が他人のケースのみを検証しており、「パスセグメントは
 * 自分のものだが、編集用クエリパラメータ（accountNo/photoNo）だけ他人の写真番号に差し替えた
 * 細工URL」は未検証だった。同様に、お問い合わせ詳細（`pages/inquiry.spec.ts`）も自分の
 * お問い合わせへのアクセスしか検証しておらず、他人のinquiryNoを指定した場合の挙動が未検証
 * だった。本シナリオでは、これらのIDOR（Insecure Direct Object Reference）対策が実際に
 * 機能し、他人のリソースの内容が一切画面に漏れないことを、一般ユーザーの画面操作で確認する。
 *
 * 写真編集APIは`photoAccountId`（URLパス）から解決したアカウント番号の配下でのみ写真番号を
 * 検索するため（`PhotoServiceImpl#getPhotoDetail`）、他人の写真番号を指定しても他人の写真
 * データには到達せず「写真が存在しません。」となる（`PhotoSettingForm`の所有者不一致チェック
 * による「この操作を行う権限がありません」は、この経路では発生しない）。
 */
const PHOTO_1 = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");

authTest.describe("他人のリソースへの細工アクセス防御", () => {
  authTest.describe.configure({ timeout: 60_000 });

  authTest(
    "自分のphotoAccountIdでも、クエリの写真番号が他人のものなら「写真が存在しません」となり内容が漏れないこと",
    async ({ workerPage: ownerPage, testUser, browser }, testInfo) => {
      const attackerAccountId = generateTestAccountId(testInfo.workerIndex);

      let editUrl = "";
      await authTest.step("所有者が写真を登録し、編集URL（accountNo/photoNoクエリ）を取得する", async () => {
        await ownerPage.goto(`/photo/${testUser.accountId}/photo_setting`);
        await authExpect(ownerPage.getByTestId("image-input")).toBeAttached();
        await ownerPage.getByTestId("image-input").setInputFiles(PHOTO_1);
        await authExpect(ownerPage.getByTestId("image-preview-item-0")).toBeVisible();
        await ownerPage.getByTestId("japanese-title-input").fill("クロスアカウントテスト写真");
        await ownerPage.getByTestId("submit-button").click();
        await authExpect(ownerPage.getByTestId("success-modal")).toBeVisible({ timeout: 10000 });
        await ownerPage.getByRole("button", { name: "閉じる" }).click();

        const detailLinks = ownerPage.locator('a[href*="/photo_detail?photoNo="]');
        await authExpect(detailLinks).toHaveCount(1, { timeout: 10000 });
        const href = await detailLinks.first().getAttribute("href");
        authExpect(href).toBeTruthy();
        await ownerPage.goto(href!);

        await ownerPage.getByRole("button", { name: "編集" }).click();
        await authExpect(ownerPage).toHaveURL(/photo_setting\?accountNo=\d+&photoNo=\d+/, {
          timeout: 10000,
        });
        editUrl = ownerPage.url();
      });

      await authTest.step("別アカウントが自分のphotoAccountIdへ差し替えた同URLへアクセスしても、写真が存在しない扱いになり内容が表示されないこと", async () => {
        const context = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
        const attackerPage = await context.newPage();
        await registerAccount(attackerPage, attackerAccountId, "E2E Attacker User");
        await login(attackerPage, attackerAccountId);

        const query = new URL(editUrl).search;
        await attackerPage.goto(`/photo/${attackerAccountId}/photo_setting${query}`);

        await authExpect(
          attackerPage.getByRole("alert").filter({ hasText: "写真が存在しません。" })
        ).toBeVisible({ timeout: 10000 });
        // 他人の写真データ（タイトル）が読み込まれてフォームに反映されていないこと
        await authExpect(attackerPage.getByTestId("japanese-title-input")).toHaveValue("");
        await context.close();
      });
    }
  );

  authTest(
    "他人のinquiryNoを指定してお問い合わせ詳細へアクセスすると「存在しません」と表示されること",
    async ({ workerPage: senderPage, browser }, testInfo) => {
      const subject = `E2Eクロスアカウントテスト${testInfo.workerIndex}${Date.now()}`;
      const attackerAccountId = generateTestAccountId(testInfo.workerIndex);

      let inquiryDetailUrl = "";
      await authTest.step("送信者がお問い合わせを送信し、詳細URLを取得する", async () => {
        await senderPage.goto("/inquiry");
        await senderPage.getByLabel("件名").fill(subject);
        await senderPage.getByLabel("本文").fill("E2Eクロスアカウントテストの本文です。");
        await senderPage.getByRole("button", { name: "送信" }).click();
        await authExpect(senderPage.getByText("お問い合わせを受け付けました")).toBeVisible({
          timeout: 10000,
        });
        await senderPage.getByRole("button", { name: "お問い合わせ一覧へ" }).click();
        await authExpect(senderPage).toHaveURL(/\/inquiry\/list$/, { timeout: 10000 });

        const link = senderPage.getByRole("link", { name: subject });
        await authExpect(link).toBeVisible({ timeout: 10000 });
        const href = await link.getAttribute("href");
        authExpect(href).toBeTruthy();
        inquiryDetailUrl = href!;
      });

      await authTest.step("別アカウントが同じ詳細URLへアクセスすると「存在しません」と表示されること", async () => {
        const context = await browser.newContext({ baseURL: testInfo.project.use.baseURL });
        const attackerPage = await context.newPage();
        await registerAccount(attackerPage, attackerAccountId, "E2E Attacker User");
        await login(attackerPage, attackerAccountId);

        await attackerPage.goto(inquiryDetailUrl);

        await authExpect(attackerPage.getByText("お問い合わせが存在しません。")).toBeVisible({
          timeout: 10000,
        });
        await context.close();
      });
    }
  );
});
