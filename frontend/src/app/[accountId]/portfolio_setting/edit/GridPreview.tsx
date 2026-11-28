"use client";

import { sanitizeImageUrl } from "@/lib/url";
import {
  GRID_ROW_PATTERN_CELLS,
  GRID_ROW_PATTERN_PHOTO_COUNT,
  HIDDEN_BY_ADMIN_LABEL,
  type SelectedPhoto,
} from "./types";

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
 * 管理者により非公開にされた写真は、外す必要があることが分かるよう枠に表示する。
 */
export function GridPreview({ photos, rows }: GridPreviewProps) {
  let cursor = 0;
  const rowItems = rows.map((pattern) => {
    const count = GRID_ROW_PATTERN_PHOTO_COUNT[pattern] ?? 0;
    const shapes = GRID_ROW_PATTERN_CELLS[pattern] ?? [];
    const cells = shapes.slice(0, count).map((shape) => {
      const photo = photos[cursor];
      cursor++;
      return { shape, photo };
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
              style={{ flex: cell.shape.flex, aspectRatio: cell.shape.aspectRatio }}
              className="relative bg-gray-200 overflow-hidden rounded flex items-center justify-center"
            >
              {cell.photo?.isHiddenByAdmin && (
                <span
                  data-testid={`grid-preview-hidden-badge-${cell.photo.photoNo}`}
                  className="absolute top-1 left-1 px-1.5 py-0.5 text-xs text-white bg-red-600 rounded"
                >
                  {HIDDEN_BY_ADMIN_LABEL}
                </span>
              )}
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
