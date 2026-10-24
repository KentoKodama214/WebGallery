import type { PortfolioStatusKbn, PortfolioTemplateKbn } from "@/lib/api/client";

/** 公開状態区分の表示ラベル */
export const PORTFOLIO_STATUS_LABELS: Record<PortfolioStatusKbn, string> = {
  draft: "下書き",
  published: "公開中",
};

/** テンプレート区分の表示ラベル（テンプレート一覧APIの取得前・取得失敗時にも使う） */
export const PORTFOLIO_TEMPLATE_LABELS: Record<PortfolioTemplateKbn, string> = {
  feature: "フィーチャー",
  grid: "グリッド",
};
