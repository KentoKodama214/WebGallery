"use client";

import { sanitizeImageUrl } from "@/lib/url";
import { GRID_ROW_PATTERN_PHOTO_COUNT, GRID_ROW_PATTERN_RATIOS, type SelectedPhoto } from "./types";

interface GridPreviewProps {
  /** 表示順に並べた写真 */
  photos: SelectedPhoto[];
  /** 行パターンの一覧（上の行から順に、写真を表示順に消費する） */
  rows: string[];
}

/**
 * グリッド型テンプレートのレイアウトプレビュー
 *
 * 各行は写真を表示順に、行パターンの枚数ずつ消費する。行の枚数の合計と写真の枚数が
 * 一致しない場合（公開できない状態）は、空き枠・行に割り当てられていない写真をそれぞれ示す。
 */
export function GridPreview({ photos, rows }: GridPreviewProps) {
  let cursor = 0;
  const rowItems = rows.map((pattern) => {
    const count = GRID_ROW_PATTERN_PHOTO_COUNT[pattern] ?? 0;
    const ratios = GRID_ROW_PATTERN_RATIOS[pattern] ?? [];
    const cells = ratios.slice(0, count).map((ratio) => {
      const photo = photos[cursor];
      cursor++;
      return { ratio, photo };
    });
    return { pattern, cells };
  });
  const unassigned = photos.slice(Math.min(cursor, photos.length));

  return (
    <div className="flex flex-col gap-2" data-testid="grid-preview">
      {rowItems.map((row, rowIndex) => (
        <div key={rowIndex} className="flex gap-2" data-testid="grid-preview-row">
          {row.cells.map((cell, cellIndex) => (
            <div
              key={cellIndex}
              style={{ flex: cell.ratio, aspectRatio: cell.ratio === 2 ? "2 / 1" : "1 / 1" }}
              className="bg-gray-200 overflow-hidden rounded flex items-center justify-center"
            >
              {cell.photo ? (
                sanitizeImageUrl(cell.photo.imageFilePath) ? (
                  <img
                    src={sanitizeImageUrl(cell.photo.imageFilePath)}
                    alt={cell.photo.label}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <span className="text-xs text-gray-600">{cell.photo.label}</span>
                )
              ) : (
                <span className="text-xs text-gray-600">空き</span>
              )}
            </div>
          ))}
        </div>
      ))}
      {unassigned.length > 0 && (
        <p className="text-sm text-red-700">
          行に割り当てられていない写真が{unassigned.length}枚あります
        </p>
      )}
    </div>
  );
}
