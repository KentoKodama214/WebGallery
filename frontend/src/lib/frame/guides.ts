/**
 * 額縁テンプレートの編集キャンバスの補助線（スマートガイド）
 *
 * 写真枠・メッセージ枠をドラッグしている間、枠の端・中央が額縁（台紙）の端・中央や他の枠の端・中央に近づいたら
 * その位置に吸着させ、揃っている位置に補助線を表示するための計算を行う。位置・大きさはすべて台紙に対する %。
 */

/** 補助線の計算に使う枠の位置・大きさ（傾きは考慮せず、傾ける前の枠で揃える） */
export interface GuideBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

/** 表示する補助線の位置（台紙に対する %） */
export interface FrameGuides {
  /** 縦の補助線の横位置 */
  vertical: number[];
  /** 横の補助線の縦位置 */
  horizontal: number[];
}

/** 吸着の結果 */
export interface SnapResult {
  x: number;
  y: number;
  width: number;
  height: number;
  guides: FrameGuides;
}

/** 吸着する範囲（台紙に対する %。横・縦それぞれ） */
export interface SnapThreshold {
  x: number;
  y: number;
}

/** 揃っているとみなす誤差（%） */
const ALIGN_EPSILON = 0.01;

/** 補助線を引かない状態 */
export const NO_GUIDES: FrameGuides = { vertical: [], horizontal: [] };

/**
 * 吸着先の線を取得する（台紙の端・中央と、他の枠の端・中央）
 *
 * @param others 他の枠
 * @param axis 横方向（x）か縦方向（y）か
 * @returns 吸着先の線の位置
 */
function targetLines(others: GuideBox[], axis: "x" | "y"): number[] {
  const lines = [0, 50, 100];
  for (const box of others) {
    const start = axis === "x" ? box.x : box.y;
    const size = axis === "x" ? box.width : box.height;
    lines.push(start, start + size / 2, start + size);
  }
  return lines;
}

/**
 * 枠の線のうち吸着先に最も近いものとの差を取得する
 *
 * @param lines 枠の線の位置
 * @param targets 吸着先の線の位置
 * @param threshold 吸着する範囲
 * @returns 吸着するために動かす量（範囲内に吸着先がなければ null）
 */
function nearestOffset(lines: number[], targets: number[], threshold: number): number | null {
  let best: number | null = null;
  for (const line of lines) {
    for (const target of targets) {
      const diff = target - line;
      if (Math.abs(diff) <= threshold && (best === null || Math.abs(diff) < Math.abs(best))) {
        best = diff;
      }
    }
  }
  return best;
}

/**
 * 枠の線と揃っている吸着先の線を取得する（補助線として表示する位置）
 *
 * @param lines 枠の線の位置
 * @param targets 吸着先の線の位置
 * @returns 揃っている線の位置（重複なし・昇順）
 */
function alignedLines(lines: number[], targets: number[]): number[] {
  const aligned = targets.filter((target) => lines.some((line) => Math.abs(line - target) <= ALIGN_EPSILON));
  return [...new Set(aligned.map((line) => Math.round(line * 100) / 100))].sort((a, b) => a - b);
}

/**
 * 移動中の枠を吸着させる（枠の左端・中央・右端、上端・中央・下端のいずれかを吸着先に揃える）
 *
 * @param box 移動後の枠
 * @param others 他の枠
 * @param threshold 吸着する範囲
 * @returns 吸着後の枠と、表示する補助線
 */
export function snapMove(box: GuideBox, others: GuideBox[], threshold: SnapThreshold): SnapResult {
  const xTargets = targetLines(others, "x");
  const yTargets = targetLines(others, "y");
  const offsetX = nearestOffset([box.x, box.x + box.width / 2, box.x + box.width], xTargets, threshold.x) ?? 0;
  const offsetY = nearestOffset([box.y, box.y + box.height / 2, box.y + box.height], yTargets, threshold.y) ?? 0;
  const x = box.x + offsetX;
  const y = box.y + offsetY;
  return {
    x,
    y,
    width: box.width,
    height: box.height,
    guides: {
      vertical: alignedLines([x, x + box.width / 2, x + box.width], xTargets),
      horizontal: alignedLines([y, y + box.height / 2, y + box.height], yTargets),
    },
  };
}

/**
 * 大きさを変更中の枠を吸着させる（右端・下端を吸着先に揃える。左上の位置は動かさない）
 *
 * @param box 大きさを変更した後の枠
 * @param others 他の枠
 * @param threshold 吸着する範囲
 * @param resizeHeight 高さも変更するか（メッセージ枠は高さを持たないため横幅だけ揃える）
 * @returns 吸着後の枠と、表示する補助線
 */
export function snapResize(
  box: GuideBox,
  others: GuideBox[],
  threshold: SnapThreshold,
  resizeHeight: boolean
): SnapResult {
  const xTargets = targetLines(others, "x");
  const yTargets = targetLines(others, "y");
  const width = box.width + (nearestOffset([box.x + box.width], xTargets, threshold.x) ?? 0);
  const height = resizeHeight
    ? box.height + (nearestOffset([box.y + box.height], yTargets, threshold.y) ?? 0)
    : box.height;
  return {
    x: box.x,
    y: box.y,
    width,
    height,
    guides: {
      vertical: alignedLines([box.x + width], xTargets),
      horizontal: resizeHeight ? alignedLines([box.y + height], yTargets) : [],
    },
  };
}
