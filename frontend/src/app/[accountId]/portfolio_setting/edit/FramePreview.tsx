import { FrameCanvas } from "@/components/frame/FrameCanvas";
import type { FrameDefinition } from "@/lib/frame/definition";
import type { SelectedPhoto } from "./types";

interface FramePreviewProps {
  /** ポートフォリオにコピーした額縁定義 */
  definition: FrameDefinition;
  /** 表示順の写真（先頭から順に写真枠へ当てはめる） */
  photos: SelectedPhoto[];
  /** メッセージ枠ごとの文面 */
  messages: string[];
  /** 額縁の最大幅のクラス（設定画面の中の小さな見本と、プレビューのダイアログで使い分ける） */
  className?: string;
}

/**
 * 額縁型のプレビュー
 *
 * 公開ページと同じく、写真を表示順に写真枠へ1枚ずつ当てはめる。写真が足りない枠は、当てはめる写真の番号を表示する。
 * 管理者により非公開にされた写真は公開ページに表示されないため、その枠も空けて表示する。
 */
export function FramePreview({ definition, photos, messages, className }: FramePreviewProps) {
  return (
    <div className={className} data-testid="frame-preview">
      <FrameCanvas
        size={definition.size}
        backgroundColor={definition.background.color}
        borderColor={definition.border.color}
        borderWidth={definition.border.width}
        label="額縁のプレビュー"
        slots={definition.slots.map((slot, index) => {
          const photo = photos[index];
          return {
            ...slot,
            photo: photo && !photo.isHiddenByAdmin ? { url: photo.imageFilePath, alt: photo.label } : null,
          };
        })}
        texts={definition.texts.map((text, index) => ({ ...text, text: messages[index] ?? "" }))}
        emptySlotLabel={(index) => `写真${index + 1}`}
      />
    </div>
  );
}
