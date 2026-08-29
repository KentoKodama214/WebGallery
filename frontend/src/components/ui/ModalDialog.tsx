"use client";

import type { CSSProperties, ReactNode } from "react";
import { useDialog } from "@/lib/a11y";

interface ModalDialogProps {
  /** Escape キー押下時に呼ぶハンドラ（省略時は Escape で閉じない） */
  onClose?: () => void;
  /** ダイアログのアクセシブルネーム（`labelledBy` 未指定時に使用） */
  label?: string;
  /** ダイアログ名を持つ要素の id（見出し要素などを指す場合に使用） */
  labelledBy?: string;
  children: ReactNode;
  /** 背景オーバーレイの className */
  overlayClassName?: string;
  /** 背景オーバーレイの style */
  overlayStyle?: CSSProperties;
  /** ダイアログ本体（`role="dialog"` を付与する要素）の className */
  containerClassName?: string;
  /** ダイアログ本体の style */
  containerStyle?: CSSProperties;
  /** オーバーレイに付与する data-testid */
  testId?: string;
}

/**
 * モーダルダイアログの共通ラッパー
 *
 * - `role="dialog"` / `aria-modal="true"` とアクセシブルネームを付与する
 * - 開いた瞬間にダイアログ内へフォーカスを移し、Tab を内部で循環させる
 * - Escape キーで `onClose` を呼ぶ
 * - 閉じたときに、開く前のフォーカス位置へ戻す
 *
 * 表示・非表示は呼び出し側の条件付きレンダリングで制御する
 * （このコンポーネントがマウントされている間だけダイアログが開いている状態）。
 */
export function ModalDialog({
  onClose,
  label,
  labelledBy,
  children,
  overlayClassName,
  overlayStyle,
  containerClassName,
  containerStyle,
  testId,
}: ModalDialogProps) {
  const dialogRef = useDialog<HTMLDivElement>(onClose);

  return (
    <div
      className={overlayClassName}
      style={overlayStyle}
      data-testid={testId}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-label={labelledBy ? undefined : label}
        aria-labelledby={labelledBy}
        tabIndex={-1}
        className={containerClassName}
        style={containerStyle}
      >
        {children}
      </div>
    </div>
  );
}
