import { expect, type Locator } from "@playwright/test";

/**
 * 入力値が定着するまで`fill`を再試行する
 *
 * Reactのハイドレーション（サーバーが返したHTMLにクライアント側のイベントハンドラを結び付ける処理）が
 * 完了する前に`fill`すると、ハイドレーション時に制御コンポーネントの初期値（空文字）で
 * 入力値が上書きされて失われる。`page.goto`直後にそのまま`fill`すると、CIの共有ランナーのように
 * ハイドレーションが遅い環境で「1つ目のフィールドだけ空になり、2つ目以降は入力できている」という
 * 形で不定期に失敗する。
 *
 * `next dev`はルートを初回リクエスト時にオンデマンドでコンパイルするため、
 * そのルートを最初に訪れるテストで特に発生しやすい（`global-setup.ts`の`warmUpRoutes`で
 * 事前コンパイルして窓を狭めているが、完全には無くならないためこのヘルパーで再試行する）。
 *
 * @param locator 入力対象のフィールド
 * @param value 入力する値
 * @param timeout 値が定着するまで再試行する上限時間（ミリ秒）
 */
export async function fillStable(
  locator: Locator,
  value: string,
  timeout: number = 15000
): Promise<void> {
  await expect(async () => {
    await locator.fill(value);
    // 内側は短いタイムアウトにして、上書きされた場合は`fill`のやり直しへ早く戻る
    await expect(locator).toHaveValue(value, { timeout: 1000 });
  }).toPass({ timeout });
}
