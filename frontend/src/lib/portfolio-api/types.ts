/**
 * portfolio-api（公開ページ向けのポートフォリオ表示API。GraphQL）のレスポンスの型
 *
 * スキーマの正本は `portfolio-api/graph/schema.graphqls`。スキーマを変更した場合は、
 * ここと `queries.ts` のクエリを合わせて更新すること。
 */

/** 写真の向き */
export type PhotoDirection = "VERTICAL" | "HORIZONTAL" | "SQUARE" | "NONE";

/** ポートフォリオのテンプレート */
export type PortfolioTemplate = "FEATURE" | "GRID" | "FRAME";

/** グリッド型の行パターン */
export type GridRowPattern = "TWO_EQUAL" | "THREE_EQUAL" | "WIDE_NARROW" | "NARROW_WIDE";

/** 写真 */
export interface PublicPhoto {
  photoNo: number;
  /** 画像の署名付きURL（有効期限あり） */
  url: string;
  japaneseTitle: string;
  englishTitle: string;
  direction: PhotoDirection;
}

/** タグ */
export interface PublicTag {
  japaneseName: string;
  englishName: string;
}

/** 公開中のポートフォリオ一覧の1件分 */
export interface PublicPortfolioSummary {
  portfolioNo: number;
  theme: string;
  description: string;
  template: PortfolioTemplate;
  tags: PublicTag[];
  coverPhoto: PublicPhoto | null;
  photoCount: number;
  updatedAt: string;
}

/** 公開中のポートフォリオ一覧の1ページ分 */
export interface PublicPortfolioSummaryPage {
  /** 表示順のポートフォリオ */
  portfolios: PublicPortfolioSummary[];
  /** 続きのポートフォリオがあるかどうか */
  hasNext: boolean;
}

/** フィーチャー型のレイアウト */
export interface FeatureLayout {
  __typename: "FeatureLayout";
  sections: {
    photo: PublicPhoto;
    theme: string;
    subTheme: string;
    description: string;
  }[];
}

/** グリッド型のレイアウト */
export interface GridLayout {
  __typename: "GridLayout";
  rows: {
    pattern: GridRowPattern;
    /** 左から順の写真の枠（管理者により非公開にされた写真の枠は photo が null） */
    photos: { photo: PublicPhoto | null }[];
  }[];
}

/** 額縁型のメッセージ枠のフォント */
export type FrameFontFamily = "GOTHIC" | "MINCHO" | "MARU" | "HANDWRITING" | "SERIF" | "SCRIPT";

/** 額縁型のメッセージ枠の揃え */
export type FrameTextAlign = "LEFT" | "CENTER" | "RIGHT";

/** 額縁型のレイアウト（位置・大きさ・太さ・文字サイズは額縁に対する %） */
export interface FrameLayout {
  __typename: "FrameLayout";
  /** 額縁の横幅（mm。縦横比の算出に使う） */
  width: number;
  /** 額縁の縦幅（mm） */
  height: number;
  backgroundColor: string;
  borderColor: string;
  borderWidth: number;
  /** 写真枠（管理者により非公開にされた写真の枠は photo が null） */
  slots: {
    x: number;
    y: number;
    width: number;
    height: number;
    rotation: number;
    borderColor: string;
    borderWidth: number;
    shadow: boolean;
    photo: PublicPhoto | null;
  }[];
  /** 文面を入力したメッセージ枠 */
  texts: {
    x: number;
    y: number;
    width: number;
    rotation: number;
    fontFamily: FrameFontFamily;
    fontSize: number;
    color: string;
    align: FrameTextAlign;
    bold: boolean;
    text: string;
  }[];
}

/** テンプレートごとのレイアウト（`__typename` で判別する） */
export type PortfolioLayout = FeatureLayout | GridLayout | FrameLayout;

/** 公開中のポートフォリオ */
export interface PublicPortfolio {
  portfolioNo: number;
  theme: string;
  description: string;
  template: PortfolioTemplate;
  tags: PublicTag[];
  updatedAt: string;
  layout: PortfolioLayout;
}
