import type { ReactNode } from "react";
import type { PortfolioLayout } from "@/lib/portfolio-api/types";
import { FeatureTemplate } from "./FeatureTemplate";
import { GridTemplate } from "./GridTemplate";

/**
 * レイアウトの型（`__typename`）ごとの描画関数
 *
 * テンプレートを追加した場合は、`PortfolioLayout` に型を加え、ここに描画関数を追加する
 * （`PortfolioLayout` の全ての `__typename` を網羅しないと型エラーになる）。
 */
const TEMPLATE_RENDERERS: {
  [K in PortfolioLayout["__typename"]]: (layout: Extract<PortfolioLayout, { __typename: K }>) => ReactNode;
} = {
  FeatureLayout: (layout) => <FeatureTemplate layout={layout} />,
  GridLayout: (layout) => <GridTemplate layout={layout} />,
};

/**
 * レイアウトの型に応じたテンプレートで描画する
 *
 * portfolio-api が未対応の型を返した場合（API だけ先に新しいテンプレートへ対応した場合等）は、
 * 画面全体を壊さずにメッセージを表示する。
 *
 * @param layout テンプレートごとのレイアウト
 * @returns 描画結果
 */
export function renderPortfolioLayout(layout: PortfolioLayout): ReactNode {
  const render = TEMPLATE_RENDERERS[layout.__typename] as
    | ((layout: PortfolioLayout) => ReactNode)
    | undefined;
  if (!render) {
    return <p className="text-gray-700">このポートフォリオは表示できません</p>;
  }
  return render(layout);
}
