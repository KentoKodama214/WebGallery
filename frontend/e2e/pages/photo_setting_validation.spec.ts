import fs from "fs";
import path from "path";
import { test as authTest, expect as authExpect } from "../fixtures/auth";

/**
 * `photo_setting.spec.ts` は正常系のフォーム表示・権限エラーのみを検証しており、
 * CLAUDE.md に明記されているアップロード上限（1ファイル5MB）や、バックエンドが
 * 多層で行っている画像ファイル検証（Content-Type→マジックバイト→拡張子の順、
 * `PhotoServiceImpl#registPhoto`）、保存APIがサーバーエラーを返した場合の異常系は
 * 未検証だった。本ファイルではそれらのエッジケースを検証する。
 */
const OVERSIZED_PHOTO = path.join(__dirname, "../fixtures/images/e2e-photo-oversized.png");
const NON_IMAGE_FILE = path.join(__dirname, "../fixtures/images/e2e-photo-invalid.txt");
const VALID_PHOTO_PATH = path.join(__dirname, "../fixtures/images/e2e-photo-1.png");
const VALID_PHOTO_BUFFER = fs.readFileSync(VALID_PHOTO_PATH);

authTest.describe("写真設定ページ（アップロード異常系）", () => {
  authTest(
    "5MBを超える画像を選択すると、選択時点でエラーが表示され画像が追加されないこと",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles(OVERSIZED_PHOTO);

      await authExpect(page.getByTestId("validation-errors")).toBeVisible();
      await authExpect(
        page.getByText("画像ファイルは5MB以下にしてください")
      ).toBeVisible();
      // 拒否されるため、プレビューには追加されない
      await authExpect(page.getByTestId("image-preview-item-0")).toHaveCount(0);
    }
  );

  authTest(
    "画像として不正な内容のファイルを送信すると、Content-Type不正のエラーメッセージが表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      // input要素の accept="image/*" はファイル選択ダイアログ上のヒントに過ぎず、
      // setInputFiles によるプログラム的な選択は拡張子・内容を問わず通るため、
      // クライアント側にファイル内容のチェックがない本アプリでは実際にサーバーまで
      // 到達させて検証する必要がある
      await page.getByTestId("image-input").setInputFiles(NON_IMAGE_FILE);
      await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
      // タイトルは必須項目（backend側の @NotBlank）。未入力だとファイル内容チェックの手前で
      // 汎用のバリデーションエラーになってしまうため、後続のチェックまで到達させるために入力する
      await page
        .getByTestId("japanese-title-input")
        .fill("Content-Type検証テスト写真");

      await page.getByTestId("submit-button").click();

      // テキストファイルはContent-Typeが image/* にならないため、拡張子チェックより
      // 手前のContent-Typeチェック（ImageFileValidationPolicy#isAllowedContentType）で弾かれる
      await authExpect(
        page.getByRole("alert").filter({ hasText: "許可されていないファイル形式です。" })
      ).toBeVisible({ timeout: 10000 });
      // 保存に失敗しているため、一覧へは遷移しない
      await authExpect(page).toHaveURL(new RegExp(`/photo/${testUser.accountId}/photo_setting$`));
    }
  );

  authTest(
    "内容は画像だが許可されていない拡張子のファイルを送信すると、拡張子不正のエラーメッセージが表示されること",
    async ({ workerPage: page, testUser }) => {
      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      // Content-Type・マジックバイトは正規のPNGのまま、ファイル名の拡張子だけ
      // 許可リスト外（.bmp）にすることで、拡張子チェック単体を踏ませる
      await page.getByTestId("image-input").setInputFiles({
        name: "e2e-photo-invalid.bmp",
        mimeType: "image/png",
        buffer: VALID_PHOTO_BUFFER,
      });
      await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
      await page
        .getByTestId("japanese-title-input")
        .fill("拡張子検証テスト写真");

      await page.getByTestId("submit-button").click();

      await authExpect(
        page.getByRole("alert").filter({
          hasText:
            "許可されていないファイル形式です。（アップロード可能な拡張子：jpg, jpeg, png, gif, webp）",
        })
      ).toBeVisible({ timeout: 10000 });
      await authExpect(page).toHaveURL(new RegExp(`/photo/${testUser.accountId}/photo_setting$`));
    }
  );

  authTest(
    "保存APIがサーバーエラーを返した場合、エラーメッセージが表示され一覧へ遷移しないこと",
    async ({ workerPage: page, testUser }) => {
      // workerPage は同一ワーカー内の他のspecファイルとも使い回す共有ページのため、
      // ここで登録したルートモックを外し忘れると、後続の別テストが行う本物の写真保存
      // リクエストまで横取りしてしまう。必ず finally で unroute する
      const photosUrlPattern = `**/api/v1/accounts/${testUser.accountId}/photos`;
      const mockServerError = (route: import("@playwright/test").Route) => {
        if (route.request().method() === "POST") {
          return route.fulfill({
            status: 500,
            contentType: "application/json",
            body: JSON.stringify({ errorMessage: "内部サーバーエラーが発生しました。" }),
          });
        }
        return route.continue();
      };
      await page.route(photosUrlPattern, mockServerError);

      try {
        await page.goto(`/photo/${testUser.accountId}/photo_setting`);
        await authExpect(page.getByTestId("image-input")).toBeAttached();

        await page.getByTestId("image-input").setInputFiles(VALID_PHOTO_PATH);
        await authExpect(page.getByTestId("image-preview-item-0")).toBeVisible();
        await page.getByTestId("japanese-title-input").fill("サーバーエラーテスト写真");

        await page.getByTestId("submit-button").click();

        // 5xx応答時はレスポンス本文のメッセージを使わず、固定のフォールバック文言を表示する仕様
        // （frontend/src/lib/api/client.ts の readErrorMessage）
        await authExpect(
          page.getByRole("alert").filter({ hasText: "写真の登録に失敗しました" })
        ).toBeVisible({ timeout: 10000 });
        await authExpect(page).toHaveURL(
          new RegExp(`/photo/${testUser.accountId}/photo_setting$`)
        );
      } finally {
        await page.unroute(photosUrlPattern, mockServerError);
      }
    }
  );

  authTest(
    "新規一括登録でファイル名が重複する場合、登録全体が失敗し一覧へ遷移しないこと",
    async ({ workerPage: page, testUser }) => {
      // `PhotoServiceImpl#savePhotos` は複数枚をひとつのトランザクションで順に登録し、
      // 途中の1枚がファイル名重複（`PhotoAggregateRepositoryImpl#regist`）で失敗すると
      // 全体をロールバックする（既に登録済みの分もDB・S3双方から補償削除される）。
      // 「1枚だけ失敗して残りは保存される」という部分成功は起こらないことを検証する。
      const duplicateFileName = `e2e-duplicate-${Date.now()}.png`;

      await page.goto(`/photo/${testUser.accountId}/photo_setting`);
      await authExpect(page.getByTestId("image-input")).toBeAttached();

      await page.getByTestId("image-input").setInputFiles([
        { name: duplicateFileName, mimeType: "image/png", buffer: VALID_PHOTO_BUFFER },
        { name: duplicateFileName, mimeType: "image/png", buffer: VALID_PHOTO_BUFFER },
      ]);
      await authExpect(page.getByTestId("image-preview-item-1")).toBeVisible();
      await page.getByTestId("japanese-title-input").fill("重複ファイル名一括登録テスト");

      await page.getByTestId("submit-button").click();

      await authExpect(
        page.getByRole("alert").filter({
          hasText: "写真登録でエラーが発生しました。（既に同じファイル名でアップロード済みです）",
        })
      ).toBeVisible({ timeout: 10000 });
      // 保存に失敗しているため、一覧へは遷移しない（＝1枚も登録されていない）
      await authExpect(page).toHaveURL(new RegExp(`/photo/${testUser.accountId}/photo_setting$`));
    }
  );
});
