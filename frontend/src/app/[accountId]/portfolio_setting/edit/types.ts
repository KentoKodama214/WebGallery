import type { PortfolioTemplateKbn } from "@/lib/api/client";

/** 編集中のポートフォリオに設定した写真 */
export interface SelectedPhoto {
  /** 写真番号 */
  photoNo: number;
  /** 画像URL（署名付きURL） */
  imageFilePath: string;
  /** 一覧・プレビューで表示する写真の名前 */
  label: string;
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
  /** グリッド型：キャプション */
  caption: string;
}

/** 未入力の写真単位の設定 */
export const EMPTY_PHOTO_SETTINGS: PhotoSettingValues = {
  theme: "",
  subTheme: "",
  description: "",
  caption: "",
};

/** テンプレートごとに送信する写真単位の設定項目（バックエンドは未知の項目を受け付けない） */
export const PHOTO_SETTING_KEYS: Record<PortfolioTemplateKbn, (keyof PhotoSettingValues)[]> = {
  feature: ["theme", "subTheme", "description"],
  grid: ["caption"],
};

/** グリッド型の行パターンごとの1行あたりの写真枚数（テンプレート一覧APIの取得前・取得失敗時にも使う） */
export const GRID_ROW_PATTERN_PHOTO_COUNT: Record<string, number> = {
  TWO_EQUAL: 2,
  THREE_EQUAL: 3,
  WIDE_NARROW: 2,
};

/** グリッド型の行パターンの表示名（テンプレート一覧APIの取得前・取得失敗時にも使う） */
export const GRID_ROW_PATTERN_LABELS: Record<string, string> = {
  TWO_EQUAL: "2枚均等",
  THREE_EQUAL: "3枚均等",
  WIDE_NARROW: "横長1枚+正方形1枚",
};

/** グリッド型の行パターンごとの、各写真の横幅の比率 */
export const GRID_ROW_PATTERN_RATIOS: Record<string, number[]> = {
  TWO_EQUAL: [1, 1],
  THREE_EQUAL: [1, 1, 1],
  WIDE_NARROW: [2, 1],
};
