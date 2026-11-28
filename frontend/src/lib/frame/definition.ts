/**
 * 額縁定義（額縁テンプレート・額縁型ポートフォリオで共通の構造）
 *
 * 額縁のサイズ・外枠の太さ・写真枠とメッセージ枠の位置と大きさ・枠線の太さ・文字サイズは、すべて実寸（mm）で持つ。
 * 写真枠・メッセージ枠の位置は、台紙（額縁から外枠を除いた部分）の左上からの距離とする。
 * 構造・値の範囲はバックエンドの `FrameDefinitionPolicy` と一致させる（バックエンドは範囲外の値を受け付けない）。
 */

/** 額縁のサイズ（mm） */
export interface FrameSize {
  width: number;
  height: number;
}

/** 写真枠 */
export interface FrameSlot {
  /** 左端の位置（台紙の左端からの mm） */
  x: number;
  /** 上端の位置（台紙の上端からの mm） */
  y: number;
  /** 横幅（mm） */
  width: number;
  /** 縦幅（mm） */
  height: number;
  /** 傾き（度。時計回りが正） */
  rotation: number;
  /** 枠線の色（#RRGGBB） */
  borderColor: string;
  /** 枠線の太さ（mm） */
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
  /** 左端の位置（台紙の左端からの mm） */
  x: number;
  /** 上端の位置（台紙の上端からの mm） */
  y: number;
  /** 横幅（mm。文面はこの幅で折り返す） */
  width: number;
  /** 傾き（度。時計回りが正） */
  rotation: number;
  fontFamily: FrameFontFamily;
  /** 文字サイズ（mm） */
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
  /** 額縁の外枠（太さは mm） */
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

/** 数値項目の範囲（単位は角度を除き mm。バックエンドの検証と一致させる） */
export const FRAME_LIMITS = {
  sizeMin: 50,
  sizeMax: 3000,
  borderWidthMax: 200,
  /** 台紙（額縁から外枠を除いた部分）の縦横の下限。外枠の太さはこれを残せる範囲までとする */
  matSizeMin: 10,
  slotBorderWidthMax: 50,
  rotationMax: 180,
  fontSizeMin: 1,
  fontSizeMax: 200,
  /** 写真枠・メッセージ枠の大きさの下限（0 は受け付けないため、操作で潰れないようにする） */
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
 * 台紙（額縁から外枠を除いた部分）の大きさを取得する
 *
 * @param size 額縁のサイズ（mm）
 * @param borderWidth 外枠の太さ（mm）
 * @returns 台紙の大きさ（mm）
 */
export function getMatSize(size: FrameSize, borderWidth: number): FrameSize {
  return { width: size.width - borderWidth * 2, height: size.height - borderWidth * 2 };
}

/**
 * 額縁のサイズに対する外枠の太さの上限を取得する（台紙の縦横が {@link FRAME_LIMITS.matSizeMin} 以上残る範囲）
 *
 * @param size 額縁のサイズ（mm）
 * @returns 外枠の太さの上限（mm）
 */
export function getBorderWidthMax(size: FrameSize): number {
  return Math.min(
    FRAME_LIMITS.borderWidthMax,
    Math.floor(((Math.min(size.width, size.height) - FRAME_LIMITS.matSizeMin) / 2) * 10) / 10
  );
}

/**
 * 外枠の太さと、写真枠・メッセージ枠の位置・大きさを台紙の範囲内に収める
 *
 * 位置・大きさを実寸で持つため、額縁のサイズを小さくしたり外枠を太くしたりすると、枠が台紙からはみ出すことがある。
 * 左上の位置は台紙の内側、大きさは台紙の縦横以下に収める（はみ出した枠をそのまま保存しようとして検証で弾かれないようにする）。
 *
 * @param definition 額縁定義
 * @returns 範囲内に収めた額縁定義
 */
export function fitToMat(definition: FrameDefinition): FrameDefinition {
  const borderWidth = clamp(definition.border.width, 0, getBorderWidthMax(definition.size), 0);
  const mat = getMatSize(definition.size, borderWidth);
  return {
    ...definition,
    border: { ...definition.border, width: borderWidth },
    slots: definition.slots.map((slot) => ({
      ...slot,
      x: clamp(slot.x, 0, mat.width, 0),
      y: clamp(slot.y, 0, mat.height, 0),
      width: clamp(slot.width, FRAME_LIMITS.boxSizeMin, mat.width, mat.width),
      height: clamp(slot.height, FRAME_LIMITS.boxSizeMin, mat.height, mat.height),
    })),
    texts: definition.texts.map((text) => ({
      ...text,
      x: clamp(text.x, 0, mat.width, 0),
      y: clamp(text.y, 0, mat.height, 0),
      width: clamp(text.width, FRAME_LIMITS.boxSizeMin, mat.width, mat.width),
    })),
  };
}

/**
 * 新しく追加する写真枠を作る（台紙の中央付近に、台紙に対して一定の比率の大きさで置く。追加するたびに少しずつずらす）
 *
 * @param index 追加後の写真枠の位置（0始まり）
 * @param mat 台紙の大きさ（mm）
 * @returns 写真枠
 */
export function createSlot(index: number, mat: FrameSize): FrameSlot {
  const offset = (index % 5) * 0.04;
  return {
    x: round(mat.width * (0.3 + offset)),
    y: round(mat.height * (0.2 + offset)),
    width: round(mat.width * 0.3),
    height: round(mat.height * 0.4),
    rotation: 0,
    borderColor: "#ffffff",
    borderWidth: 3,
    shadow: true,
  };
}

/**
 * 新しく追加するメッセージ枠を作る（台紙の下部に置く）
 *
 * @param index 追加後のメッセージ枠の位置（0始まり）
 * @param mat 台紙の大きさ（mm）
 * @returns メッセージ枠
 */
export function createText(index: number, mat: FrameSize): FrameText {
  return {
    x: round(mat.width * 0.1),
    y: round(mat.height * Math.min(0.8 + (index % 3) * 0.05, 0.95)),
    width: round(mat.width * 0.8),
    rotation: 0,
    fontFamily: "mincho",
    fontSize: 9,
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
  const size = { width: 366, height: 254 };
  const mat = getMatSize(size, 9);
  return {
    size,
    background: { color: "#f7f4ee" },
    border: { color: "#2b2118", width: 9 },
    slots: [
      { ...createSlot(0, mat), x: 21, y: 28, width: 94, height: 130, rotation: -2 },
      { ...createSlot(0, mat), x: 127, y: 24, width: 94, height: 130 },
      { ...createSlot(0, mat), x: 233, y: 28, width: 94, height: 130, rotation: 2 },
    ],
    texts: [{ ...createText(0, mat), y: 184 }],
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
  const frameSize = {
    width: clamp(size.width, FRAME_LIMITS.sizeMin, FRAME_LIMITS.sizeMax, 366),
    height: clamp(size.height, FRAME_LIMITS.sizeMin, FRAME_LIMITS.sizeMax, 254),
  };
  const borderWidth = clamp(border.width, 0, getBorderWidthMax(frameSize), 0);
  const mat = getMatSize(frameSize, borderWidth);
  const fontValues = FRAME_FONT_OPTIONS.map((option) => option.value) as string[];
  const alignValues = FRAME_TEXT_ALIGN_OPTIONS.map((option) => option.value) as string[];
  return {
    size: frameSize,
    background: { color: toColor(background.color, "#ffffff") },
    border: { color: toColor(border.color, "#000000"), width: borderWidth },
    slots: toObjectList(definition.slots)
      .slice(0, FRAME_SLOT_MAX_SIZE)
      .map((slot) => ({
        x: clamp(slot.x, 0, mat.width, 0),
        y: clamp(slot.y, 0, mat.height, 0),
        width: clamp(slot.width, FRAME_LIMITS.boxSizeMin, mat.width, mat.width),
        height: clamp(slot.height, FRAME_LIMITS.boxSizeMin, mat.height, mat.height),
        rotation: clamp(slot.rotation, -FRAME_LIMITS.rotationMax, FRAME_LIMITS.rotationMax, 0),
        borderColor: toColor(slot.borderColor, "#ffffff"),
        borderWidth: clamp(slot.borderWidth, 0, FRAME_LIMITS.slotBorderWidthMax, 0),
        shadow: slot.shadow === true,
      })),
    texts: toObjectList(definition.texts)
      .slice(0, FRAME_TEXT_MAX_SIZE)
      .map((text) => ({
        x: clamp(text.x, 0, mat.width, 0),
        y: clamp(text.y, 0, mat.height, 0),
        width: clamp(text.width, FRAME_LIMITS.boxSizeMin, mat.width, mat.width),
        rotation: clamp(text.rotation, -FRAME_LIMITS.rotationMax, FRAME_LIMITS.rotationMax, 0),
        fontFamily: (fontValues.includes(text.fontFamily as string)
          ? text.fontFamily
          : "gothic") as FrameFontFamily,
        fontSize: clamp(text.fontSize, FRAME_LIMITS.fontSizeMin, FRAME_LIMITS.fontSizeMax, 10),
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
 * 数値の桁数を揃え（額縁のサイズは 1mm、それ以外は 0.1mm・0.1° 単位）、写真枠・メッセージ枠がはみ出さないよう台紙の範囲内に収める。
 *
 * @param definition 額縁定義
 * @returns 保存する額縁定義（JSON）
 */
export function toFrameDefinitionJson(definition: FrameDefinition): Record<string, unknown> {
  const fitted = fitToMat({
    ...definition,
    size: { width: round(definition.size.width, 0), height: round(definition.size.height, 0) },
    border: { ...definition.border, width: round(definition.border.width) },
  });
  const mat = getMatSize(fitted.size, fitted.border.width);
  // 丸めた値が台紙の縦横をわずかに超えないよう、上限は切り捨てた値にする
  const fit = (value: number, min: number, max: number) =>
    Math.min(Math.max(round(value), min), Math.floor(max * 10 + 1e-6) / 10);
  return {
    size: fitted.size,
    background: { color: fitted.background.color },
    border: { color: fitted.border.color, width: fitted.border.width },
    slots: fitted.slots.map((slot) => ({
      x: fit(slot.x, 0, mat.width),
      y: fit(slot.y, 0, mat.height),
      width: fit(slot.width, FRAME_LIMITS.boxSizeMin, mat.width),
      height: fit(slot.height, FRAME_LIMITS.boxSizeMin, mat.height),
      rotation: round(slot.rotation),
      borderColor: slot.borderColor,
      borderWidth: round(slot.borderWidth),
      shadow: slot.shadow,
    })),
    texts: fitted.texts.map((text) => ({
      x: fit(text.x, 0, mat.width),
      y: fit(text.y, 0, mat.height),
      width: fit(text.width, FRAME_LIMITS.boxSizeMin, mat.width),
      rotation: round(text.rotation),
      fontFamily: text.fontFamily,
      fontSize: round(text.fontSize),
      color: text.color,
      align: text.align,
      bold: text.bold,
      italic: text.italic,
    })),
  };
}
