import type { FrameLayout } from "@/lib/portfolio-api/types";
import type { FrameFontFamily, FrameTextAlign } from "@/lib/frame/definition";
import { FrameCanvas } from "@/components/frame/FrameCanvas";

/**
 * 額縁型テンプレート
 *
 * 1枚の額縁に、写真枠・メッセージ枠を額縁テンプレートの定義どおりの位置・大きさ・傾きで配置する。
 * 管理者により非公開にされた写真の枠（photo が null）は、組写真の構成を保つため空けたまま表示する。
 * 写真の閲覧性を保つため、額縁は画面の横幅に合わせつつ最大幅を設ける。
 */
export function FrameTemplate({ layout }: { layout: FrameLayout }) {
  const photoTitles = layout.slots
    .map((slot) => slot.photo?.japaneseTitle || slot.photo?.englishTitle)
    .filter((title): title is string => !!title);
  return (
    <div className="w-full max-w-[1100px] mx-auto px-2 py-4" data-testid="frame-template">
      <FrameCanvas
        size={{ width: layout.width, height: layout.height }}
        backgroundColor={layout.backgroundColor}
        borderColor={layout.borderColor}
        borderWidth={layout.borderWidth}
        label={photoTitles.length > 0 ? `組写真：${photoTitles.join("、")}` : "組写真"}
        slots={layout.slots.map((slot) => ({
          ...slot,
          photo: slot.photo
            ? { url: slot.photo.url, alt: slot.photo.japaneseTitle || slot.photo.englishTitle }
            : null,
        }))}
        texts={layout.texts.map((text) => ({
          ...text,
          fontFamily: text.fontFamily.toLowerCase() as FrameFontFamily,
          align: text.align.toLowerCase() as FrameTextAlign,
        }))}
      />
    </div>
  );
}
