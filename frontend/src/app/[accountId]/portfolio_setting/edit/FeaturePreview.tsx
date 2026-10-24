"use client";

import { sanitizeImageUrl } from "@/lib/url";
import type { SelectedPhoto } from "./types";

interface FeaturePreviewProps {
  /** 表示順に並べた写真 */
  photos: SelectedPhoto[];
}

/**
 * フィーチャー型テンプレートのレイアウトプレビュー
 *
 * 写真1枚ごとに、写真とテーマ・サブテーマ・説明のセクションを、左右を交互に入れ替えて並べる
 * （公開ページのフィーチャー型テンプレートと同じ並び）。テーマが未入力の写真は、公開できないことを示す。
 */
export function FeaturePreview({ photos }: FeaturePreviewProps) {
  return (
    <div className="flex flex-col gap-6" data-testid="feature-preview">
      {photos.map((photo, index) => {
        const src = sanitizeImageUrl(photo.imageFilePath);
        const { theme, subTheme, description } = photo.settings;
        return (
          <section
            key={photo.photoNo}
            data-testid="feature-preview-section"
            className={`flex flex-col gap-3 md:items-center ${
              index % 2 === 0 ? "md:flex-row" : "md:flex-row-reverse"
            }`}
          >
            <div className="md:w-1/2 bg-gray-200 rounded overflow-hidden flex items-center justify-center min-h-[120px]">
              {src ? (
                <img src={src} alt={photo.label} className="w-full h-auto object-contain" />
              ) : (
                <span className="text-xs text-gray-600">{photo.label}</span>
              )}
            </div>
            <div className="md:w-1/2 flex flex-col gap-1">
              {theme ? (
                <h3 className="text-lg font-bold">{theme}</h3>
              ) : (
                <p className="text-sm text-red-700">テーマが未入力です（公開するには入力してください）</p>
              )}
              {subTheme && <p className="text-gray-700">{subTheme}</p>}
              {description && <p className="text-sm text-gray-700 whitespace-pre-wrap">{description}</p>}
            </div>
          </section>
        );
      })}
    </div>
  );
}
