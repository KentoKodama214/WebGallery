import type { KeyboardEvent } from "react";

/**
 * キーボード操作のためのアクティベーションハンドラを生成する
 *
 * `<button>` に置き換えられない（CSS 依存の強い）要素へ `role="button"` と併せて
 * 付与し、Enter / Space キーでクリック相当の操作を可能にする。
 *
 * @param handler 実行する処理
 * @returns onKeyDown ハンドラ
 */
export function onActivateKey(handler: () => void) {
  return (e: KeyboardEvent) => {
    if (e.key === "Enter" || e.key === " " || e.key === "Spacebar") {
      e.preventDefault();
      handler();
    }
  };
}
