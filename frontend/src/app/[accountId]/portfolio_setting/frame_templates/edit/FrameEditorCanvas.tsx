"use client";

import { useRef, useState, type KeyboardEvent, type PointerEvent } from "react";
import { FrameCanvas, frameBoxStyle } from "@/components/frame/FrameCanvas";
import { FRAME_LIMITS, clamp, round, type FrameDefinition } from "@/lib/frame/definition";
import { NO_GUIDES, snapMove, snapResize, type FrameGuides, type GuideBox } from "@/lib/frame/guides";

/** 選択中の要素（写真枠・メッセージ枠の種類と、定義内の位置） */
export interface FrameSelection {
  kind: "slot" | "text";
  index: number;
}

/** 写真枠・メッセージ枠の位置・大きさ・傾きの変更内容 */
export interface FrameBoxChange {
  x?: number;
  y?: number;
  width?: number;
  height?: number;
  rotation?: number;
}

interface FrameEditorCanvasProps {
  definition: FrameDefinition;
  /** メッセージ枠に表示する文面（編集中の見た目の確認用。保存しない） */
  sampleTexts: string[];
  selection: FrameSelection | null;
  onSelect: (selection: FrameSelection | null) => void;
  /** 写真枠・メッセージ枠の位置・大きさ・傾きを変更する */
  onChangeBox: (selection: FrameSelection, change: FrameBoxChange) => void;
  /** 選択中の要素を削除する（Delete・Backspace キー） */
  onDelete: (selection: FrameSelection) => void;
}

/** ドラッグ操作の種類 */
type DragMode = "move" | "resize" | "rotate";

/** ドラッグ操作の開始時点の状態 */
interface DragState {
  mode: DragMode;
  selection: FrameSelection;
  pointerId: number;
  startClientX: number;
  startClientY: number;
  /** 台紙の大きさ（px）。% への換算に使う */
  matWidth: number;
  matHeight: number;
  /** 操作開始時の要素の位置・大きさ・傾き */
  start: { x: number; y: number; width: number; height: number; rotation: number };
  /** 要素の中心（画面上の px）。回転の軸にする */
  centerX: number;
  centerY: number;
}

/** キー操作1回あたりの移動量（%）。Shift キーを押している間は大きく動かす */
const KEY_STEP = 0.5;
const KEY_STEP_LARGE = 5;

/** 回転の角度を 0° に吸着させる範囲（度） */
const ROTATION_SNAP = 2;

/** 枠の端・中央を補助線に吸着させる範囲（画面上の px） */
const GUIDE_SNAP_PX = 6;

/** メッセージ枠の操作用の枠の高さ（%）。メッセージ枠は高さを持たないため、文字サイズから見積もる */
function textBoxHeight(fontSize: number, definition: FrameDefinition): number {
  // 文字サイズは額縁の横幅に対する %。台紙の縦幅に対する % に直し、1行分の行の高さ（1.5倍）にする
  return (fontSize * 1.5 * definition.size.width) / definition.size.height;
}

/**
 * 額縁テンプレートの編集キャンバス
 *
 * 額縁の上に写真枠・メッセージ枠の操作用の枠を重ね、ドラッグで移動、右下のハンドルで大きさの変更、
 * 上部のハンドルで回転（Shift キーで 15° 刻み、0° 付近は吸着）ができる。移動・大きさの変更中は、枠の端・中央が
 * 額縁の端・中央や他の枠の端・中央に近づくと吸着し、揃っている位置に補助線を表示する（Alt キーを押している間は吸着しない。
 * 傾けた枠の大きさの変更は吸着しない）。選択中の要素は矢印キーで移動（Shift キーで大きく移動）、Delete・Backspace キーで
 * 削除できる。写真枠には写真を当てはめる順の番号を表示する。
 */
export function FrameEditorCanvas({
  definition,
  sampleTexts,
  selection,
  onSelect,
  onChangeBox,
  onDelete,
}: FrameEditorCanvasProps) {
  const dragRef = useRef<DragState | null>(null);
  /** ドラッグ中に表示する補助線 */
  const [guides, setGuides] = useState<FrameGuides>(NO_GUIDES);

  const boxOf = (target: FrameSelection) => {
    if (target.kind === "slot") {
      const slot = definition.slots[target.index];
      return { x: slot.x, y: slot.y, width: slot.width, height: slot.height, rotation: slot.rotation };
    }
    const text = definition.texts[target.index];
    return {
      x: text.x,
      y: text.y,
      width: text.width,
      height: textBoxHeight(text.fontSize, definition),
      rotation: text.rotation,
    };
  };

  /** 操作中の要素以外の枠（補助線の吸着先） */
  const otherBoxes = (target: FrameSelection): GuideBox[] => [
    ...definition.slots.map((_, index) => ({ kind: "slot" as const, index })),
    ...definition.texts.map((_, index) => ({ kind: "text" as const, index })),
  ]
    .filter((other) => other.kind !== target.kind || other.index !== target.index)
    .map((other) => boxOf(other));

  const startDrag = (event: PointerEvent<HTMLElement>, target: FrameSelection, mode: DragMode) => {
    if (event.button !== 0) return;
    event.preventDefault();
    event.stopPropagation();
    onSelect(target);
    const boxElement = (event.currentTarget.closest("[data-frame-box]") as HTMLElement | null) ?? event.currentTarget;
    const mat = boxElement.parentElement;
    if (!mat) return;
    const matRect = mat.getBoundingClientRect();
    const boxRect = boxElement.getBoundingClientRect();
    event.currentTarget.setPointerCapture?.(event.pointerId);
    dragRef.current = {
      mode,
      selection: target,
      pointerId: event.pointerId,
      startClientX: event.clientX,
      startClientY: event.clientY,
      matWidth: matRect.width || 1,
      matHeight: matRect.height || 1,
      start: boxOf(target),
      centerX: boxRect.left + boxRect.width / 2,
      centerY: boxRect.top + boxRect.height / 2,
    };
  };

  const handlePointerMove = (event: PointerEvent<HTMLElement>) => {
    const drag = dragRef.current;
    if (!drag || drag.pointerId !== event.pointerId) return;
    const dx = ((event.clientX - drag.startClientX) / drag.matWidth) * 100;
    const dy = ((event.clientY - drag.startClientY) / drag.matHeight) * 100;
    const threshold = event.altKey
      ? { x: 0, y: 0 }
      : { x: (GUIDE_SNAP_PX / drag.matWidth) * 100, y: (GUIDE_SNAP_PX / drag.matHeight) * 100 };
    if (drag.mode === "move") {
      const snapped = snapMove(
        { x: drag.start.x + dx, y: drag.start.y + dy, width: drag.start.width, height: drag.start.height },
        otherBoxes(drag.selection),
        threshold
      );
      setGuides(snapped.guides);
      onChangeBox(drag.selection, {
        x: round(clamp(snapped.x, 0, 100, drag.start.x), 2),
        y: round(clamp(snapped.y, 0, 100, drag.start.y), 2),
      });
      return;
    }
    if (drag.mode === "resize") {
      // 傾けた枠は、枠の向きに沿って大きさを変える（ポインタの移動量を枠の傾きの分だけ逆回転する）
      const radian = (-drag.start.rotation * Math.PI) / 180;
      const pxX = event.clientX - drag.startClientX;
      const pxY = event.clientY - drag.startClientY;
      const localX = pxX * Math.cos(radian) - pxY * Math.sin(radian);
      const localY = pxX * Math.sin(radian) + pxY * Math.cos(radian);
      const resizeHeight = drag.selection.kind === "slot";
      let width = drag.start.width + (localX / drag.matWidth) * 100;
      let height = resizeHeight ? drag.start.height + (localY / drag.matHeight) * 100 : drag.start.height;
      // 傾けた枠は右端・下端が台紙の縦横と揃わないため、吸着させない
      if (drag.start.rotation === 0) {
        const snapped = snapResize(
          { x: drag.start.x, y: drag.start.y, width, height },
          otherBoxes(drag.selection),
          threshold,
          resizeHeight
        );
        width = snapped.width;
        height = snapped.height;
        setGuides(snapped.guides);
      }
      const change: FrameBoxChange = {
        width: round(clamp(width, FRAME_LIMITS.boxSizeMin, 100, drag.start.width), 2),
      };
      if (resizeHeight) {
        change.height = round(clamp(height, FRAME_LIMITS.boxSizeMin, 100, drag.start.height), 2);
      }
      onChangeBox(drag.selection, change);
      return;
    }
    // 回転：枠の中心からポインタへの向きを角度にする（ハンドルは枠の上にあるため 90° ずらす）
    let angle =
      (Math.atan2(event.clientY - drag.centerY, event.clientX - drag.centerX) * 180) / Math.PI + 90;
    if (angle > 180) angle -= 360;
    if (event.shiftKey) {
      angle = Math.round(angle / 15) * 15;
    } else if (Math.abs(angle) < ROTATION_SNAP) {
      angle = 0;
    }
    onChangeBox(drag.selection, {
      rotation: round(clamp(angle, -FRAME_LIMITS.rotationMax, FRAME_LIMITS.rotationMax, 0)),
    });
  };

  const endDrag = (event: PointerEvent<HTMLElement>) => {
    if (dragRef.current?.pointerId === event.pointerId) {
      dragRef.current = null;
      setGuides(NO_GUIDES);
    }
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLElement>, target: FrameSelection) => {
    if (event.key === "Delete" || event.key === "Backspace") {
      event.preventDefault();
      onDelete(target);
      return;
    }
    const step = event.shiftKey ? KEY_STEP_LARGE : KEY_STEP;
    const moves: Record<string, [number, number]> = {
      ArrowLeft: [-step, 0],
      ArrowRight: [step, 0],
      ArrowUp: [0, -step],
      ArrowDown: [0, step],
    };
    const move = moves[event.key];
    if (!move) return;
    event.preventDefault();
    const box = boxOf(target);
    onChangeBox(target, {
      x: round(clamp(box.x + move[0], 0, 100, box.x)),
      y: round(clamp(box.y + move[1], 0, 100, box.y)),
    });
  };

  const isSelected = (target: FrameSelection) =>
    selection?.kind === target.kind && selection.index === target.index;

  const targets: (FrameSelection & { label: string })[] = [
    ...definition.slots.map((_, index) => ({
      kind: "slot" as const,
      index,
      label: `写真枠${index + 1}`,
    })),
    ...definition.texts.map((_, index) => ({
      kind: "text" as const,
      index,
      label: `メッセージ枠${index + 1}`,
    })),
  ];

  return (
    <div
      onPointerDown={(event) => {
        // 枠の外（台紙）を押したら選択を解除し、額縁全体の設定を表示する
        const testId = (event.target as HTMLElement).dataset.testid;
        if (testId === "frame-overlay" || testId === "frame-mat" || testId === "frame-canvas") {
          onSelect(null);
        }
      }}
      className="touch-none select-none"
      data-testid="frame-editor-canvas"
    >
      <FrameCanvas
        size={definition.size}
        backgroundColor={definition.background.color}
        borderColor={definition.border.color}
        borderWidth={definition.border.width}
        slots={definition.slots.map((slot) => ({ ...slot, photo: null }))}
        texts={definition.texts.map((text, index) => ({
          ...text,
          text: sampleTexts[index] || `メッセージ${index + 1}`,
        }))}
        emptySlotLabel={(index) => `写真${index + 1}`}
      >
        {targets.map((target) => {
          const box = boxOf(target);
          const selected = isSelected(target);
          return (
            <div
              key={`${target.kind}-${target.index}`}
              data-frame-box
              data-testid={`frame-box-${target.kind}-${target.index}`}
              role="button"
              tabIndex={0}
              aria-label={target.label}
              aria-pressed={selected}
              onPointerDown={(event) => startDrag(event, target, "move")}
              onPointerMove={handlePointerMove}
              onPointerUp={endDrag}
              onPointerCancel={endDrag}
              onFocus={() => onSelect(target)}
              onKeyDown={(event) => handleKeyDown(event, target)}
              className={`cursor-move outline-none ${
                selected
                  ? "ring-2 ring-blue-600"
                  : "hover:ring-1 hover:ring-blue-400 focus-visible:ring-2 focus-visible:ring-blue-600"
              }`}
              style={{ ...frameBoxStyle(box), zIndex: selected ? 2 : 1 }}
            >
              {target.kind === "slot" && (
                <span
                  aria-hidden="true"
                  className="absolute left-0 top-0 m-1 px-1.5 rounded bg-blue-700/90 text-white text-xs leading-5"
                >
                  {target.index + 1}
                </span>
              )}
              {selected && (
                <>
                  <span
                    aria-hidden="true"
                    data-testid="frame-handle-resize"
                    onPointerDown={(event) => startDrag(event, target, "resize")}
                    onPointerMove={handlePointerMove}
                    onPointerUp={endDrag}
                    onPointerCancel={endDrag}
                    className="absolute -right-2 -bottom-2 w-4 h-4 bg-white border-2 border-blue-600 rounded-sm cursor-se-resize"
                  />
                  <span
                    aria-hidden="true"
                    className="absolute left-1/2 -top-6 h-6 border-l-2 border-blue-600 pointer-events-none"
                  />
                  <span
                    aria-hidden="true"
                    data-testid="frame-handle-rotate"
                    onPointerDown={(event) => startDrag(event, target, "rotate")}
                    onPointerMove={handlePointerMove}
                    onPointerUp={endDrag}
                    onPointerCancel={endDrag}
                    className="absolute left-1/2 -top-8 -ml-2 w-4 h-4 bg-white border-2 border-blue-600 rounded-full cursor-grab"
                  />
                </>
              )}
            </div>
          );
        })}
        {guides.vertical.map((position) => (
          <span
            key={`vertical-${position}`}
            aria-hidden="true"
            data-testid="frame-guide-vertical"
            className="absolute top-0 bottom-0 border-l border-dashed border-pink-500 pointer-events-none"
            style={{ left: `${position}%`, zIndex: 3 }}
          />
        ))}
        {guides.horizontal.map((position) => (
          <span
            key={`horizontal-${position}`}
            aria-hidden="true"
            data-testid="frame-guide-horizontal"
            className="absolute left-0 right-0 border-t border-dashed border-pink-500 pointer-events-none"
            style={{ top: `${position}%`, zIndex: 3 }}
          />
        ))}
      </FrameCanvas>
    </div>
  );
}
