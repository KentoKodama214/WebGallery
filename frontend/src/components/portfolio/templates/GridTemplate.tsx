import type { GridLayout, GridRowPattern } from "@/lib/portfolio-api/types";
import { PortfolioPhotoImage } from "../PortfolioPhotoImage";

/** 行パターンごとの、各写真の横幅の比率と縦横比 */
const ROW_CELLS: Record<GridRowPattern, { flex: number; aspectRatio: string }[]> = {
  TWO_EQUAL: [
    { flex: 1, aspectRatio: "4 / 3" },
    { flex: 1, aspectRatio: "4 / 3" },
  ],
  THREE_EQUAL: [
    { flex: 1, aspectRatio: "1 / 1" },
    { flex: 1, aspectRatio: "1 / 1" },
    { flex: 1, aspectRatio: "1 / 1" },
  ],
  // 横長（3:2）と縦長（3:4）を同じ高さで並べるため、横幅を 2:1 にする
  WIDE_NARROW: [
    { flex: 2, aspectRatio: "3 / 2" },
    { flex: 1, aspectRatio: "3 / 4" },
  ],
  // 縦長を左に置く以外は WIDE_NARROW と同じ
  NARROW_WIDE: [
    { flex: 1, aspectRatio: "3 / 4" },
    { flex: 2, aspectRatio: "3 / 2" },
  ],
};

/**
 * グリッド型テンプレート
 *
 * 行パターンに従って、1行に2〜3枚の写真を並べる。キャプションは写真の下に表示する。
 */
export function GridTemplate({ layout }: { layout: GridLayout }) {
  return (
    <div className="flex flex-col gap-3" data-testid="grid-template">
      {layout.rows.map((row, rowIndex) => (
        <div key={rowIndex} className="flex gap-3" data-testid="grid-row" data-pattern={row.pattern}>
          {row.photos.map((gridPhoto, cellIndex) => {
            const cell = ROW_CELLS[row.pattern][cellIndex] ?? { flex: 1, aspectRatio: "1 / 1" };
            return (
              <figure key={gridPhoto.photo.photoNo} className="m-0 min-w-0" style={{ flex: cell.flex }}>
                <PortfolioPhotoImage
                  photo={gridPhoto.photo}
                  className="w-full object-cover rounded"
                  style={{ aspectRatio: cell.aspectRatio }}
                />
                {gridPhoto.caption && (
                  <figcaption className="text-sm text-gray-700 mt-1">{gridPhoto.caption}</figcaption>
                )}
              </figure>
            );
          })}
        </div>
      ))}
    </div>
  );
}
