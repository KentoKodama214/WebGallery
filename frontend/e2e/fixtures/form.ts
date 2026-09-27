import { expect, type Locator } from "@playwright/test";

/**
 * 要素がReactにハイドレーションされている（Reactが入力イベントを受け取れる状態）かを返す
 *
 * Reactはハイドレーション時に要素へ`__reactFiber$<乱数>`というプロパティを付与する。Reactの
 * 内部実装だがReact 16以降で命名が安定しており、E2Eからハイドレーション完了を判定する事実上の
 * 手段になっている。名称が変わった場合は常に`false`になるだけで、呼び出し側は待ち切ったあと
 * 入力を試みるため、テストが一律に失敗することはない。
 *
 * この関数は`locator.evaluate`でブラウザ側へ渡して実行するため、モジュールスコープの変数を
 * 参照せず自己完結させている。
 */
function hasReactFiber(element: Element): boolean {
  return Object.keys(element).some((key) => key.startsWith("__reactFiber$"));
}

/**
 * 直近のレンダリングでReactが要素へ渡した`value`を返す
 *
 * Reactは要素へ`__reactProps$<乱数>`で直近のpropsを保持する。制御コンポーネント
 * （`value={state}`）ではReactのstateと一致するため、「入力がstateへ反映されたか」を
 * 直接判定できる。判定できない場合は`undefined`を返し、呼び出し側はDOMの値のみで検証する。
 *
 * `hasReactFiber`と同様、`locator.evaluate`で渡すため自己完結させている。
 */
function reactPropsValue(element: Element): string | undefined {
  const key = Object.keys(element).find((k) => k.startsWith("__reactProps$"));
  if (!key) return undefined;
  const props = (element as unknown as Record<string, { value?: unknown }>)[key];
  return typeof props?.value === "string" ? props.value : undefined;
}

/**
 * Reactのstateへ確実に反映される形でフォームへ入力する
 *
 * `page.goto`の直後にそのまま`fill`すると、Reactがその要素をハイドレーションする前に
 * 入力イベントが発生し、DOMの値だけが変わってReactのstateへ反映されない。制御コンポーネント
 * （`value={state}`）では送信時にstateが読まれるため、画面には値が見えているのに空文字が
 * 送信され、バックエンドのバリデーションエラーになる。
 *
 * CIでこの状態を実際に観測している（Playwrightのトレース上、送信ボディが
 * `{"accountId":"","password":"invalidpass"}`となり「入力内容に誤りがあります。再度入力して
 * ください。」が返っていた）。`next dev`はルートを初回リクエスト時にオンデマンドで
 * コンパイルするため、そのルートを最初に訪れるテストで特に起きやすい
 * （`global-setup.ts`の`warmUpRoutes`で窓を狭めているが、完全には無くならないためここで待つ）。
 *
 * @param locator 入力対象のフィールド
 * @param value 入力する値
 * @param timeout ハイドレーション待ちと入力の再試行それぞれの上限時間（ミリ秒）
 */
export async function fillStable(
  locator: Locator,
  value: string,
  timeout: number = 15000
): Promise<void> {
  await locator.waitFor({ state: "visible" });

  // Reactが入力イベントを受け取れる状態になるまで待つ。判定できないまま待ち切った場合も
  // 入力自体は試みる（Reactの内部プロパティ名が変わってもテストを止めないため）
  try {
    await expect
      .poll(async () => locator.evaluate(hasReactFiber), { timeout, intervals: [50, 100, 250] })
      .toBe(true);
  } catch {
    console.warn(
      "[fillStable] 入力欄のハイドレーションを確認できませんでした。そのまま入力を試みます"
    );
  }

  // DOMの値とReactが保持する値の双方が一致するまで入力を再試行する
  await expect(async () => {
    await locator.fill(value);
    await expect(locator).toHaveValue(value, { timeout: 1000 });
    const propsValue = await locator.evaluate(reactPropsValue);
    if (propsValue !== undefined) {
      expect(propsValue).toBe(value);
    }
  }).toPass({ timeout });
}
