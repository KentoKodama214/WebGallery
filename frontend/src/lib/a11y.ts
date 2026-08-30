import { useEffect, useRef } from "react";
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

/**
 * ダイアログの外側（＝背景コンテンツ）を `inert` にして、支援技術の仮想カーソルや
 * バックグラウンドのフォーカス移動を遮断する
 *
 * ダイアログのオーバーレイ要素から `document.body` までの経路上で、経路に含まれない
 * 兄弟要素すべてに `inert` を付与する。ネストしたダイアログでも、既に `inert` が
 * 付いている要素は触らないため、内側ダイアログの解除で外側の `inert` が外れることはない。
 *
 * @param overlay ダイアログのオーバーレイ要素（この要素とその子孫はアクティブなまま）
 * @returns 付与した `inert` をすべて元に戻すクリーンアップ関数
 */
function markBackgroundInert(overlay: HTMLElement): () => void {
  const marked: HTMLElement[] = [];
  let node: HTMLElement | null = overlay;
  while (node && node.parentElement && node !== document.body) {
    for (const sibling of Array.from(node.parentElement.children)) {
      if (
        sibling !== node &&
        sibling instanceof HTMLElement &&
        !sibling.hasAttribute("inert")
      ) {
        sibling.setAttribute("inert", "");
        marked.push(sibling);
      }
    }
    node = node.parentElement;
  }
  return () => {
    for (const el of marked) el.removeAttribute("inert");
  };
}

/** ダイアログ内でフォーカス可能な要素のセレクタ */
const FOCUSABLE_SELECTOR = [
  "a[href]",
  "button:not([disabled])",
  "input:not([disabled])",
  "select:not([disabled])",
  "textarea:not([disabled])",
  '[tabindex]:not([tabindex="-1"])',
].join(",");

/**
 * モーダルダイアログのアクセシビリティ制御フック
 *
 * 返した ref を `role="dialog"` / `aria-modal="true"` を付与したコンテナ要素へ
 * セットして使う。マウント（＝ダイアログを開いた瞬間）で以下を行う。
 *
 * - ダイアログ内の最初のフォーカス可能要素（無ければコンテナ自身）へフォーカスを移動
 * - Tab / Shift+Tab をダイアログ内で循環させる（フォーカストラップ）
 * - 背景コンテンツを `inert` にして支援技術・バックグラウンドのフォーカスから隔離する
 * - Escape キーで `onClose` を呼ぶ（未指定なら何もしない）
 * - アンマウント時に、開く前にフォーカスされていた要素へフォーカスを戻す
 *
 * コンテナ要素には `tabIndex={-1}` を付与しておくこと。
 *
 * @param onClose Escape キー押下時に呼ぶハンドラ（省略可）
 * @returns ダイアログのコンテナ要素へ渡す ref
 */
export function useDialog<T extends HTMLElement = HTMLDivElement>(
  onClose?: () => void
) {
  const containerRef = useRef<T>(null);
  const onCloseRef = useRef(onClose);

  // 最新の onClose を ref に保持する（keydown ハンドラは発火時点の値を参照する）
  useEffect(() => {
    onCloseRef.current = onClose;
  });

  useEffect(() => {
    const container = containerRef.current;
    const previouslyFocused = document.activeElement as HTMLElement | null;

    // 背景コンテンツを inert にして支援技術・バックグラウンドのフォーカスから隔離する。
    // container の親要素がオーバーレイ（ModalDialog 参照）で、その外側すべてを対象にする。
    const overlay = container?.parentElement ?? null;
    const restoreInert = overlay ? markBackgroundInert(overlay) : () => {};

    // 初期フォーカスをダイアログ内へ移す
    const initialTarget =
      container?.querySelector<HTMLElement>(FOCUSABLE_SELECTOR) ?? container;
    initialTarget?.focus();

    const handleKeyDown = (e: globalThis.KeyboardEvent) => {
      if (e.key === "Escape") {
        if (onCloseRef.current) {
          e.preventDefault();
          onCloseRef.current();
        }
        return;
      }
      if (e.key !== "Tab" || !container) return;

      const focusable = Array.from(
        container.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR)
      ).filter(
        (el) =>
          !el.hasAttribute("hidden") &&
          el.getAttribute("aria-hidden") !== "true"
      );

      if (focusable.length === 0) {
        e.preventDefault();
        container.focus();
        return;
      }

      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      const active = document.activeElement;

      if (e.shiftKey) {
        if (active === first || !container.contains(active)) {
          e.preventDefault();
          last.focus();
        }
      } else if (active === last || !container.contains(active)) {
        e.preventDefault();
        first.focus();
      }
    };

    document.addEventListener("keydown", handleKeyDown, true);
    return () => {
      document.removeEventListener("keydown", handleKeyDown, true);
      restoreInert();
      previouslyFocused?.focus?.();
    };
  }, []);

  return containerRef;
}
