import type { PortfolioTemplateKbn } from "@/lib/api/client";

/** 編集中のポートフォリオに設定した写真 */
export interface SelectedPhoto {
  /** 写真番号 */
  photoNo: number;
  /** 画像URL（署名付きURL） */
  imageFilePath: string;
  /** 一覧・プレビューで表示する写真の名前 */
  label: string;
  /** 向き区分（vertical / horizontal / square / none）。写真選択ダイアログの枠の縦横比に使う */
  directionKbn: string;
  /** 写真単位のテンプレート設定（テンプレートを切り替えても入力値を保持するため、全テンプレートの項目を持つ） */
  settings: PhotoSettingValues;
}

/** 写真単位のテンプレート設定の入力値 */
export interface PhotoSettingValues {
  /** フィーチャー型：テーマ */
  theme: string;
  /** フィーチャー型：サブテーマ */
  subTheme: string;
  /** フィーチャー型：説明 */
  description: string;
}

/** 未入力の写真単位の設定 */
export const EMPTY_PHOTO_SETTINGS: PhotoSettingValues = {
  theme: "",
  subTheme: "",
  description: "",
};

/** テンプレートごとに送信する写真単位の設定項目（バックエンドは未知の項目を受け付けない） */
export const PHOTO_SETTING_KEYS: Record<PortfolioTemplateKbn, (keyof PhotoSettingValues)[]> = {
  feature: ["theme", "subTheme", "description"],
  // グリッド型は写真単位の設定を持たない
  grid: [],
};

/** グリッド型の行パターンごとの1行あたりの写真枚数（テンプレート一覧APIの取得前・取得失敗時にも使う） */
export const GRID_ROW_PATTERN_PHOTO_COUNT: Record<string, number> = {
  TWO_EQUAL: 2,
  THREE_EQUAL: 3,
  WIDE_NARROW: 2,
  NARROW_WIDE: 2,
};

/** グリッド型の行パターンの表示名（テンプレート一覧APIの取得前・取得失敗時にも使う） */
export const GRID_ROW_PATTERN_LABELS: Record<string, string> = {
  TWO_EQUAL: "2枚均等",
  THREE_EQUAL: "3枚均等",
  WIDE_NARROW: "横長1枚+縦長1枚（縦長が右）",
  NARROW_WIDE: "縦長1枚+横長1枚（縦長が左）",
};

/** グリッド型の行の、1枠あたりの横幅の比率（flex）と縦横比 */
export interface GridRowCell {
  /** 行内での横幅の比率 */
  flex: number;
  /** 縦横比（CSS の aspect-ratio） */
  aspectRatio: string;
}

/**
 * グリッド型の行パターンごとの、各枠の横幅の比率と縦横比
 *
 * 横長1枚+縦長1枚は横幅を2:1に分け、横長を3:2・縦長を3:4にすると高さが揃う
 */
export const GRID_ROW_PATTERN_CELLS: Record<string, GridRowCell[]> = {
  TWO_EQUAL: [
    { flex: 1, aspectRatio: "1 / 1" },
    { flex: 1, aspectRatio: "1 / 1" },
  ],
  THREE_EQUAL: [
    { flex: 1, aspectRatio: "1 / 1" },
    { flex: 1, aspectRatio: "1 / 1" },
    { flex: 1, aspectRatio: "1 / 1" },
  ],
  WIDE_NARROW: [
    { flex: 2, aspectRatio: "3 / 2" },
    { flex: 1, aspectRatio: "3 / 4" },
  ],
  NARROW_WIDE: [
    { flex: 1, aspectRatio: "3 / 4" },
    { flex: 2, aspectRatio: "3 / 2" },
  ],
};

/** 向き区分ごとの写真の枠の縦横比 */
export const DIRECTION_ASPECT_RATIOS: Record<string, string> = {
  vertical: "3 / 4",
  horizontal: "4 / 3",
  square: "1 / 1",
};

/** 向きが未設定の写真の枠の縦横比 */
export const DEFAULT_ASPECT_RATIO = "1 / 1";

/** 向き区分の表示名（向きが未設定の写真は表示しない） */
export const DIRECTION_LABELS: Record<string, string> = {
  vertical: "縦向き",
  horizontal: "横向き",
  square: "正方形",
};
