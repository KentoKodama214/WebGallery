import { test, expect } from "@playwright/test";
import { expectNoAccessibilityViolations } from "../fixtures/a11y";
import { generateTestAccountId, login, registerAccount } from "../fixtures/auth";

test.describe("写真一覧ページ", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/photo/e2etestaccount/photo_list");
  });

  test("ページタイトルが正しいこと", async ({ page }) => {
    await expect(page).toHaveTitle(/写真一覧/);
  });

  test("フィルタートリガーが表示されること", async ({ page }) => {
    await expect(page.getByTestId("filter-trigger")).toBeVisible();
  });

  test("フィルターパネルの開閉ができること", async ({ page }) => {
    const filterPanel = page.getByTestId("filter-panel");

    await page.getByTestId("filter-trigger").click();
    await expect(filterPanel).toHaveClass(/filterOpen/);

    await page.getByTestId("filter-close-button").click();
    await expect(filterPanel).not.toHaveClass(/filterOpen/);
  });

  test("フィルター条件テキストの外側（行の余白）をクリックしてもパネルが開かないこと", async ({
    page,
  }) => {
    const row = page.getByTestId("filter-trigger-row");
    const trigger = page.getByTestId("filter-trigger");
    const rowBox = (await row.boundingBox())!;
    const triggerBox = (await trigger.boundingBox())!;

    // トリガー（アイコン＋フィルター条件テキスト）は行より狭く、右側に余白が残る
    expect(rowBox.width).toBeGreaterThan(triggerBox.width);

    // 余白（トリガーの右外）をクリックしてもパネルは開かない
    await row.click({
      position: { x: rowBox.width - 5, y: rowBox.height - 5 },
    });
    await expect(page.getByTestId("filter-panel")).not.toHaveClass(/filterOpen/);

    // トリガー自体のクリックでは開く
    await trigger.click();
    await expect(page.getByTestId("filter-panel")).toHaveClass(/filterOpen/);
  });

  test("抽出条件パネルは背景と抽出項目が同じ距離・同じ時間で左からスライドすること", async ({
    page,
  }) => {
    const panel = page.getByTestId("filter-panel");
    const form = page.getByTestId("filter-form");
    const closeButton = page.getByTestId("filter-close-button");

    // 閉じている間は、背景（::before）も抽出項目もパネル幅（300px）だけ左外へ退避している
    const background = await panel.evaluate((el) => {
      const style = getComputedStyle(el, "::before");
      return { left: style.left, duration: style.transitionDuration };
    });
    expect(background.left).toBe("-300px");
    expect(await form.evaluate((el) => getComputedStyle(el).transform)).toBe(
      "matrix(1, 0, 0, 1, -300, 0)"
    );
    expect(await closeButton.evaluate((el) => getComputedStyle(el).transform)).toBe(
      "matrix(1, 0, 0, 1, -300, 0)"
    );
    // 背景と抽出項目が同時に動くよう、トランジション時間も一致させている
    expect(await form.evaluate((el) => getComputedStyle(el).transitionDuration)).toBe(
      background.duration
    );

    await page.getByTestId("filter-trigger").click();
    await expect(panel).toHaveClass(/filterOpen/);

    // スライド完了後は所定位置（移動量0）に収まる
    await expect(async () => {
      expect(await form.evaluate((el) => getComputedStyle(el).transform)).toBe(
        "matrix(1, 0, 0, 1, 0, 0)"
      );
      expect(await closeButton.evaluate((el) => getComputedStyle(el).transform)).toBe(
        "matrix(1, 0, 0, 1, 0, 0)"
      );
    }).toPass({ timeout: 10000 });
  });

  test("存在しないアカウントの場合は『写真が存在しません。』が表示されること", async ({
    page,
  }) => {
    // e2e.sh実行時はバックエンドが必ず起動しているため、アカウント不存在時の
    // 明確なエラー文言のみを検証する（getByRole("alert")で明示的に絞り込む必要はなく、
    // このページのエラーはページ本文に直接テキストとして表示される）
    await expect(page.getByText("写真が存在しません。")).toBeVisible({ timeout: 10000 });
  });

  test("アカウントID形式でない photoAccountId は『ギャラリーが見つかりません』を表示する", async ({
    page,
  }) => {
    // `;` を含む細工されたセグメント（Cookie名インジェクション対策の検証）
    await page.goto("/photo/aaaa1111%3B%20x/photo_list");
    await expect(page.getByText("ギャラリーが見つかりません")).toBeVisible();
    await expect(page.getByTestId("filter-trigger")).toHaveCount(0);
  });

  test("アクセシビリティ違反がないこと", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");
    await expect(page.getByText("写真が存在しません。")).toBeVisible({ timeout: 10000 });
    await expectNoAccessibilityViolations(page);
  });

  test("フィルターパネル展開中もアクセシビリティ違反がないこと", async ({ page }, testInfo) => {
    // パネルは閉じている間 visibility:hidden のため axe の走査対象外になる。
    // 絞り込みのコントロール（select・キーワード入力）は展開してはじめて検証できる
    test.skip(testInfo.project.name !== "chromium", "a11y検証はchromiumプロジェクトのみで実施する");
    await expect(page.getByText("写真が存在しません。")).toBeVisible({ timeout: 10000 });

    await page.getByTestId("filter-trigger").click();
    await expect(page.getByTestId("filter-panel")).toHaveClass(/filterOpen/);

    await expectNoAccessibilityViolations(page);
  });

  test("フィルターパネルは展開時に内部へフォーカスが移り、Escapeで閉じてトリガーへ戻ること", async ({
    page,
  }) => {
    const filterPanel = page.getByTestId("filter-panel");
    const filterTrigger = page.getByTestId("filter-trigger");

    // パネルは常にDOM上にあるため、閉じている間はダイアログとしての属性を持たないこと
    await expect(filterPanel).not.toHaveAttribute("aria-modal", "true");

    await filterTrigger.click();
    await expect(filterPanel).toHaveClass(/filterOpen/);
    // 挙動がモーダルダイアログと同じであることを示す属性が付いていること
    await expect(filterPanel).toHaveAttribute("role", "dialog");
    await expect(filterPanel).toHaveAttribute("aria-modal", "true");
    // パネル内の最初のフォーカス可能要素（閉じるボタン）へフォーカスが移る
    await expect(page.getByTestId("filter-close-button")).toBeFocused();
    // 背面（トリガーを含む写真コンテナ）は inert で不活性化される。
    // トリガーの親要素はクリック範囲を限定するための行（filterTriggerRow）であり
    // 写真コンテナそのものではないため、コンテナを直接指定して検証する
    await expect(page.getByTestId("photos-container")).toHaveAttribute("inert", "");

    await page.keyboard.press("Escape");
    await expect(filterPanel).not.toHaveClass(/filterOpen/);
    await expect(filterTrigger).toBeFocused();
  });
});

test.describe("写真一覧ページ（ログイン済み・写真0件の実アカウント）", () => {
  test("写真が1枚もない場合は『写真がありません』が表示されること", async ({
    page,
  }, testInfo) => {
    // fixtures/auth.ts の workerPage は同一ワーカー内の他ファイルと使い回すため、
    // 「写真0件」を前提とするこのテストでは使わず、ここで使い捨てアカウントを
    // 個別に登録する（他テストの写真登録と実行順で競合しないようにするため）
    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E No Photo User");
    await login(page, accountId);

    await expect(page.getByText("写真がありません")).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("filter-trigger")).toBeVisible();
  });

  test("画面表示が崩れていないこと（視覚回帰）", async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== "chromium", "視覚回帰はchromiumプロジェクトのみで検証する");

    const accountId = generateTestAccountId(testInfo.workerIndex);
    await registerAccount(page, accountId, "E2E Visual Regression User");
    await login(page, accountId);

    await expect(page.getByText("写真がありません")).toBeVisible({ timeout: 10000 });
    await expect(page).toHaveScreenshot("photo_list_empty.png", {
      fullPage: true,
      maxDiffPixelRatio: 0.02,
    });
  });
});
