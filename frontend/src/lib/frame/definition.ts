/**
 * 額縁定義（額縁テンプレート・額縁型ポートフォリオで共通の構造）
 *
 * 位置・大きさは額縁（外枠の内側の台紙）に対する %、太さ・文字サイズは額縁の横幅に対する % で持つ。
 * 構造・値の範囲はバックエンドの `FrameDefinitionPolicy` と一致させる（バックエンドは範囲外の値を受け付けない）。
 */

/** 額縁のサイズ（mm） */
export interface FrameSize {
  width: number;
  height: number;
}

/** 写真枠 */
export interface FrameSlot {
  /** 左端の位置（%） */
  x: number;
  /** 上端の位置（%） */
  y: number;
  /** 横幅（%） */
  width: number;
  /** 縦幅（%） */
  height: number;
  /** 傾き（度。時計回りが正） */
  rotation: number;
  /** 枠線の色（#RRGGBB） */
  borderColor: string;
  /** 枠線の太さ（額縁の横幅に対する %） */
  borderWidth: number;
  /** 影を付けるかどうか */
  shadow: boolean;
}

/** メッセージ枠のフォント */
export type FrameFontFamily = "gothic" | "mincho" | "maru" | "handwriting" | "serif" | "script";

/** メッセージ枠の揃え */
export type FrameTextAlign = "left" | "center" | "right";

/** メッセージ枠（文面はポートフォリオごとに入力する） */
export interface FrameText {
  /** 左端の位置（%） */
  x: number;
  /** 上端の位置（%） */
  y: number;
  /** 横幅（%。文面はこの幅で折り返す） */
  width: number;
  /** 傾き（度。時計回りが正） */
  rotation: number;
  fontFamily: FrameFontFamily;
  /** 文字サイズ（額縁の横幅に対する %） */
  fontSize: number;
  /** 文字の色（#RRGGBB） */
  color: string;
  align: FrameTextAlign;
  bold: boolean;
  italic: boolean;
}

/** 額縁定義 */
export interface FrameDefinition {
  size: FrameSize;
  /** 額縁の背景（台紙） */
  background: { color: string };
  /** 額縁の外枠 */
  border: { color: string; width: number };
  slots: FrameSlot[];
  texts: FrameText[];
}

/** 額縁テンプレートのテンプレート名の最大文字数（バックエンドと一致させる） */
export const FRAME_TEMPLATE_NAME_MAX_LENGTH = 50;

/** 1つのアカウントが作成できる額縁テンプレートの数の上限（バックエンドと一致させる） */
export const FRAME_TEMPLATE_MAX_SIZE = 50;

/** 1つの額縁に配置できる写真枠の数の上限（バックエンドと一致させる） */
export const FRAME_SLOT_MAX_SIZE = 30;

/** 1つの額縁に配置できるメッセージ枠の数の上限（バックエンドと一致させる） */
export const FRAME_TEXT_MAX_SIZE = 10;

/** 額縁に添えるメッセージ1件あたりの最大文字数（バックエンドと一致させる） */
export const FRAME_MESSAGE_MAX_LENGTH = 200;

/** 数値項目の範囲（バックエンドの検証と一致させる） */
export const FRAME_LIMITS = {
  sizeMin: 50,
  sizeMax: 3000,
  borderWidthMax: 20,
  slotBorderWidthMax: 10,
  rotationMax: 180,
  fontSizeMin: 0.5,
  fontSizeMax: 30,
  /** 写真枠・メッセージ枠の大きさの下限（%。0 は受け付けないため、操作で潰れないようにする） */
  boxSizeMin: 1,
} as const;

/**
 * フォントの選択肢
 *
 * 外部へのフォント取得（Google Fonts 等）を発生させないよう、閲覧者の OS に入っているフォントを優先順に並べたスタックで指定する。
 * 先頭のフォントがない環境では、同じ系統の次のフォントで表示される。
 */
export const FRAME_FONT_OPTIONS: { value: FrameFontFamily; label: string; css: string }[] = [
  {
    value: "gothic",
    label: "ゴシック",
    css: '"Hiragino Kaku Gothic ProN", "Hiragino Sans", "Yu Gothic", YuGothic, Meiryo, "Noto Sans JP", sans-serif',
  },
  {
    value: "mincho",
    label: "明朝",
    css: '"Hiragino Mincho ProN", "Yu Mincho", YuMincho, "Noto Serif JP", "MS PMincho", serif',
  },
  {
    value: "maru",
    label: "丸ゴシック",
    css: '"Hiragino Maru Gothic ProN", "Zen Maru Gothic", "Kosugi Maru", "Arial Rounded MT Bold", "Yu Gothic", sans-serif',
  },
  {
    value: "handwriting",
    label: "手書き風",
    css: 'Klee, "Klee One", "UD Digi Kyokasho N-R", "UD デジタル 教科書体 N-R", "Yu Kyokasho", cursive',
  },
  {
    value: "serif",
    label: "セリフ（欧文）",
    css: 'Didot, "Bodoni 72", Georgia, "Times New Roman", serif',
  },
  {
    value: "script",
    label: "筆記体（欧文）",
    css: '"Snell Roundhand", "Segoe Script", "Brush Script MT", "Apple Chancery", cursive',
  },
];

/** 揃えの選択肢 */
export const FRAME_TEXT_ALIGN_OPTIONS: { value: FrameTextAlign; label: string }[] = [
  { value: "left", label: "左揃え" },
  { value: "center", label: "中央揃え" },
  { value: "right", label: "右揃え" },
];

/**
 * 額縁のサイズの選択肢（写真展で一般的な額のサイズ）
 *
 * 写真用紙の判（六つ切等）は横長で持ち、縦長は縦横を入れ替える（向きは「横長・縦長」の切り替えに従う）。
 * A判は横向き・縦向きを別の選択肢として持ち（{@code oriented}）、選んだ向きのまま使う。
 */
export const FRAME_SIZE_PRESETS: { label: string; size: FrameSize; oriented?: boolean }[] = [
  { label: "六つ切（254×203mm）", size: { width: 254, height: 203 } },
  { label: "四つ切（305×254mm）", size: { width: 305, height: 254 } },
  { label: "ワイド四つ切（366×254mm）", size: { width: 366, height: 254 } },
  { label: "半切（432×356mm）", size: { width: 432, height: 356 } },
  { label: "全紙（560×457mm）", size: { width: 560, height: 457 } },
  { label: "A4 横（297×210mm）", size: { width: 297, height: 210 }, oriented: true },
  { label: "A4 縦（210×297mm）", size: { width: 210, height: 297 }, oriented: true },
  { label: "A3 横（420×297mm）", size: { width: 420, height: 297 }, oriented: true },
  { label: "A3 縦（297×420mm）", size: { width: 297, height: 420 }, oriented: true },
  { label: "A2 横（594×420mm）", size: { width: 594, height: 420 }, oriented: true },
  { label: "A2 縦（420×594mm）", size: { width: 420, height: 594 }, oriented: true },
  { label: "正方形（300×300mm）", size: { width: 300, height: 300 } },
];

/**
 * 額縁のサイズに当てはまる選択肢の位置を取得する
 *
 * 向きを持つ選択肢（A判）は縦横が一致するものだけ、それ以外は縦横を入れ替えたものも当てはまるとする。
 *
 * @param size 額縁のサイズ
 * @returns {@link FRAME_SIZE_PRESETS} 内の位置（当てはまらなければ -1）
 */
export function findFrameSizePresetIndex(size: FrameSize): number {
  return FRAME_SIZE_PRESETS.findIndex((preset) =>
    preset.oriented
      ? preset.size.width === size.width && preset.size.height === size.height
      : Math.max(preset.size.width, preset.size.height) === Math.max(size.width, size.height) &&
        Math.min(preset.size.width, preset.size.height) === Math.min(size.width, size.height)
  );
}

/**
 * フォントの選択肢から CSS の font-family を取得する
 *
 * @param fontFamily フォント
 * @returns CSS の font-family（未知の値はゴシック）
 */
export function toFontFamilyCss(fontFamily: string): string {
  return (FRAME_FONT_OPTIONS.find((option) => option.value === fontFamily) ?? FRAME_FONT_OPTIONS[0]).css;
}

/**
 * 新しく追加する写真枠を作る（額縁の中央付近に置く。追加するたびに少しずつずらす）
 *
 * @param index 追加後の写真枠の位置（0始まり）
 * @returns 写真枠
 */
export function createSlot(index: number): FrameSlot {
  const offset = (index % 5) * 4;
  return {
    x: 30 + offset,
    y: 20 + offset,
    width: 30,
    height: 40,
    rotation: 0,
    borderColor: "#ffffff",
    borderWidth: 0.8,
    shadow: true,
  };
}

/**
 * 新しく追加するメッセージ枠を作る（額縁の下部に置く）
 *
 * @param index 追加後のメッセージ枠の位置（0始まり）
 * @returns メッセージ枠
 */
export function createText(index: number): FrameText {
  return {
    x: 10,
    y: Math.min(80 + (index % 3) * 5, 95),
    width: 80,
    rotation: 0,
    fontFamily: "mincho",
    fontSize: 2.5,
    color: "#333333",
    align: "center",
    bold: false,
    italic: false,
  };
}

/**
 * 新しく作る額縁テンプレートの額縁定義（ワイド四つ切・横長に写真枠3つとメッセージ枠1つ）
 *
 * @returns 額縁定義
 */
export function createDefaultDefinition(): FrameDefinition {
  return {
    size: { width: 366, height: 254 },
    background: { color: "#f7f4ee" },
    border: { color: "#2b2118", width: 2.5 },
    slots: [
      { ...createSlot(0), x: 6, y: 12, width: 27, height: 55, rotation: -2 },
      { ...createSlot(0), x: 36.5, y: 10, width: 27, height: 55 },
      { ...createSlot(0), x: 67, y: 12, width: 27, height: 55, rotation: 2 },
    ],
    texts: [{ ...createText(0), y: 78 }],
  };
}

/**
 * 値を範囲内に収める（数値でない・有限でない場合は既定値にする）
 *
 * @param value 値
 * @param min 下限
 * @param max 上限
 * @param fallback 数値でない場合の値
 * @returns 範囲内の値
 */
export function clamp(value: unknown, min: number, max: number, fallback: number): number {
  if (typeof value !== "number" || !Number.isFinite(value)) return fallback;
  return Math.min(Math.max(value, min), max);
}

/**
 * 小数点以下の桁数を揃える（数値入力欄・保存する値に余分な桁を持たせないため）
 *
 * @param value 値
 * @param digits 小数点以下の桁数
 * @returns 丸めた値
 */
export function round(value: number, digits = 1): number {
  const scale = 10 ** digits;
  return Math.round(value * scale) / scale;
}

const COLOR_PATTERN = /^#[0-9a-fA-F]{6}$/;

function toColor(value: unknown, fallback: string): string {
  return typeof value === "string" && COLOR_PATTERN.test(value) ? value : fallback;
}

function toObject(value: unknown): Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : {};
}

function toObjectList(value: unknown): Record<string, unknown>[] {
  return Array.isArray(value)
    ? value.filter((item) => item !== null && typeof item === "object" && !Array.isArray(item))
    : [];
}

/**
 * APIから取得した額縁定義（JSON）を、画面で扱える額縁定義に変換する
 *
 * 保存済みの額縁定義はバックエンドで検証済みだが、型の上で安全に扱えるよう、欠けた項目は既定値で補い、
 * 範囲外の値は範囲内に収める。写真枠・メッセージ枠は上限の数までとする。
 *
 * @param value 額縁定義（JSON）
 * @returns 額縁定義
 */
export function parseFrameDefinition(value: unknown): FrameDefinition {
  const definition = toObject(value);
  const size = toObject(definition.size);
  const background = toObject(definition.background);
  const border = toObject(definition.border);
  const fontValues = FRAME_FONT_OPTIONS.map((option) => option.value) as string[];
  const alignValues = FRAME_TEXT_ALIGN_OPTIONS.map((option) => option.value) as string[];
  return {
    size: {
      width: clamp(size.width, FRAME_LIMITS.sizeMin, FRAME_LIMITS.sizeMax, 366),
      height: clamp(size.height, FRAME_LIMITS.sizeMin, FRAME_LIMITS.sizeMax, 254),
    },
    background: { color: toColor(background.color, "#ffffff") },
    border: {
      color: toColor(border.color, "#000000"),
      width: clamp(border.width, 0, FRAME_LIMITS.borderWidthMax, 0),
    },
    slots: toObjectList(definition.slots)
      .slice(0, FRAME_SLOT_MAX_SIZE)
      .map((slot) => ({
        x: clamp(slot.x, 0, 100, 0),
        y: clamp(slot.y, 0, 100, 0),
        width: clamp(slot.width, FRAME_LIMITS.boxSizeMin, 100, 30),
        height: clamp(slot.height, FRAME_LIMITS.boxSizeMin, 100, 40),
        rotation: clamp(slot.rotation, -FRAME_LIMITS.rotationMax, FRAME_LIMITS.rotationMax, 0),
        borderColor: toColor(slot.borderColor, "#ffffff"),
        borderWidth: clamp(slot.borderWidth, 0, FRAME_LIMITS.slotBorderWidthMax, 0),
        shadow: slot.shadow === true,
      })),
    texts: toObjectList(definition.texts)
      .slice(0, FRAME_TEXT_MAX_SIZE)
      .map((text) => ({
        x: clamp(text.x, 0, 100, 0),
        y: clamp(text.y, 0, 100, 0),
        width: clamp(text.width, FRAME_LIMITS.boxSizeMin, 100, 80),
        rotation: clamp(text.rotation, -FRAME_LIMITS.rotationMax, FRAME_LIMITS.rotationMax, 0),
        fontFamily: (fontValues.includes(text.fontFamily as string)
          ? text.fontFamily
          : "gothic") as FrameFontFamily,
        fontSize: clamp(text.fontSize, FRAME_LIMITS.fontSizeMin, FRAME_LIMITS.fontSizeMax, 3),
        color: toColor(text.color, "#333333"),
        align: (alignValues.includes(text.align as string) ? text.align : "left") as FrameTextAlign,
        bold: text.bold === true,
        italic: text.italic === true,
      })),
  };
}

/**
 * 額縁定義を保存するJSONに変換する
 *
 * 写真枠・メッセージ枠がはみ出さないよう位置を範囲内に収め、数値の桁数を揃える。
 *
 * @param definition 額縁定義
 * @returns 保存する額縁定義（JSON）
 */
export function toFrameDefinitionJson(definition: FrameDefinition): Record<string, unknown> {
  return {
    size: { width: round(definition.size.width, 0), height: round(definition.size.height, 0) },
    background: { color: definition.background.color },
    border: { color: definition.border.color, width: round(definition.border.width) },
    slots: definition.slots.map((slot) => ({
      x: round(clamp(slot.x, 0, 100, 0)),
      y: round(clamp(slot.y, 0, 100, 0)),
      width: round(clamp(slot.width, FRAME_LIMITS.boxSizeMin, 100, 30)),
      height: round(clamp(slot.height, FRAME_LIMITS.boxSizeMin, 100, 40)),
      rotation: round(slot.rotation),
      borderColor: slot.borderColor,
      borderWidth: round(slot.borderWidth, 2),
      shadow: slot.shadow,
    })),
    texts: definition.texts.map((text) => ({
      x: round(clamp(text.x, 0, 100, 0)),
      y: round(clamp(text.y, 0, 100, 0)),
      width: round(clamp(text.width, FRAME_LIMITS.boxSizeMin, 100, 80)),
      rotation: round(text.rotation),
      fontFamily: text.fontFamily,
      fontSize: round(text.fontSize, 2),
      color: text.color,
      align: text.align,
      bold: text.bold,
      italic: text.italic,
    })),
  };
}
