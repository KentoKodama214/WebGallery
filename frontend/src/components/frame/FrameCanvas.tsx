import type { CSSProperties, ReactNode } from "react";
import {
  toFontFamilyCss,
  type FrameSize,
  type FrameSlot,
  type FrameText,
} from "@/lib/frame/definition";
import { sanitizeImageUrl } from "@/lib/url";

/** 写真枠に当てはめる写真 */
export interface FrameCanvasPhoto {
  /** 画像URL（署名付きURL。`sanitizeImageUrl` で許可したオリジンのものだけを表示する） */
  url: string;
  /** 代替テキスト（写真のタイトル） */
  alt: string;
}

/** 描画する写真枠（写真がない枠は空き枠として描画する） */
export interface FrameCanvasSlot extends FrameSlot {
  photo: FrameCanvasPhoto | null;
}

/** 描画するメッセージ枠 */
export interface FrameCanvasText extends FrameText {
  text: string;
}

interface FrameCanvasProps {
  /** 額縁のサイズ（mm。縦横比にだけ使う） */
  size: FrameSize;
  /** 額縁の背景（台紙）の色 */
  backgroundColor: string;
  /** 額縁の外枠の色 */
  borderColor: string;
  /** 額縁の外枠の太さ（額縁の横幅に対する %） */
  borderWidth: number;
  slots: FrameCanvasSlot[];
  texts: FrameCanvasText[];
  /**
   * 空き枠に表示する文言（写真の番号を受け取る）。未指定の場合は何も表示しない
   *
   * 編集画面では写真を当てはめる順を示すために使い、公開ページでは使わない（管理者により非公開にされた写真の枠を空けるだけにする）
   */
  emptySlotLabel?: (index: number) => string;
  /** 額縁全体の代替テキスト（children を指定した場合は使わない） */
  label?: string;
  className?: string;
  /** 台紙の上に重ねて描画する要素（編集画面の操作用の枠など。台紙と同じ % の座標で配置する。台紙の外にはみ出しても切れない） */
  children?: ReactNode;
  testId?: string;
}

/**
 * 写真枠・メッセージ枠の位置・大きさ・傾きのスタイル
 *
 * 位置・大きさは台紙に対する %、傾きは枠の中心を軸にする。
 *
 * @param box 写真枠・メッセージ枠
 * @returns スタイル
 */
export function frameBoxStyle(box: {
  x: number;
  y: number;
  width: number;
  height?: number;
  rotation: number;
}): CSSProperties {
  return {
    position: "absolute",
    left: `${box.x}%`,
    top: `${box.y}%`,
    width: `${box.width}%`,
    height: box.height === undefined ? undefined : `${box.height}%`,
    transform: box.rotation ? `rotate(${box.rotation}deg)` : undefined,
    transformOrigin: "center center",
  };
}

/**
 * 額縁（組写真）を描画する
 *
 * 額縁の縦横比を保ったまま親の横幅いっぱいに広がり、太さ・文字サイズはコンテナクエリの単位（cqw。額縁の横幅に対する %）で
 * 指定するため、表示する画面の幅によらず同じ見た目になる。写真枠・メッセージ枠は定義の順に重ね、後ろのものほど手前に描画する。
 * 編集画面・ポートフォリオ設定のプレビュー・公開ページで共通に使う。
 */
export function FrameCanvas({
  size,
  backgroundColor,
  borderColor,
  borderWidth,
  slots,
  texts,
  emptySlotLabel,
  label,
  className,
  children,
  testId = "frame-canvas",
}: FrameCanvasProps) {
  return (
    <div
      // 操作用の要素を重ねる編集画面では、中の要素を読み上げ・操作できるよう画像として扱わない
      role={children ? undefined : "img"}
      aria-label={children ? undefined : label}
      data-testid={testId}
      className={`relative w-full ${className ?? ""}`}
      style={{
        aspectRatio: `${size.width} / ${size.height}`,
        containerType: "inline-size",
        backgroundColor: borderColor,
        padding: `${borderWidth}cqw`,
        boxShadow: "0 6px 18px rgba(0, 0, 0, 0.25)",
      }}
    >
      <div
        data-testid="frame-mat"
        className="relative w-full h-full overflow-hidden"
        style={{ backgroundColor }}
      >
        {slots.map((slot, index) => {
          const src = slot.photo ? sanitizeImageUrl(slot.photo.url) : "";
          return (
            <div
              key={`slot-${index}`}
              data-testid="frame-slot"
              style={{
                ...frameBoxStyle(slot),
                border: slot.borderWidth > 0 ? `${slot.borderWidth}cqw solid ${slot.borderColor}` : undefined,
                boxShadow: slot.shadow ? "0.3cqw 0.5cqw 1.2cqw rgba(0, 0, 0, 0.35)" : undefined,
                backgroundColor: slot.borderWidth > 0 ? slot.borderColor : "rgba(0, 0, 0, 0.06)",
              }}
            >
              {src ? (
                <img
                  src={src}
                  alt={slot.photo?.alt ?? ""}
                  className="block w-full h-full object-cover"
                  loading="lazy"
                  draggable={false}
                />
              ) : (
                emptySlotLabel && (
                  <div
                    className="w-full h-full flex items-center justify-center bg-gray-300 text-gray-700"
                    style={{ fontSize: "1.6cqw" }}
                  >
                    {slot.photo?.alt || emptySlotLabel(index)}
                  </div>
                )
              )}
            </div>
          );
        })}
        {texts.map((text, index) =>
          text.text ? (
            <p
              key={`text-${index}`}
              data-testid="frame-text"
              className="m-0"
              style={{
                ...frameBoxStyle(text),
                fontFamily: toFontFamilyCss(text.fontFamily),
                fontSize: `${text.fontSize}cqw`,
                lineHeight: 1.5,
                color: text.color,
                textAlign: text.align,
                fontWeight: text.bold ? 700 : 400,
                fontStyle: text.italic ? "italic" : "normal",
                whiteSpace: "pre-wrap",
                overflowWrap: "anywhere",
              }}
            >
              {text.text}
            </p>
          ) : null
        )}
      </div>
      {children && (
        // 操作用の要素は、ハンドルが台紙からはみ出しても切れないよう、台紙と同じ位置・大きさの別のレイヤーに描画する
        <div
          data-testid="frame-overlay"
          className="absolute"
          style={{ inset: `${borderWidth}cqw` }}
        >
          {children}
        </div>
      )}
    </div>
  );
}
