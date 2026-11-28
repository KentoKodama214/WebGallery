import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { renderPortfolioLayout } from "@/components/portfolio/templates/registry";
import { getPublishedPortfolio } from "@/lib/portfolio-api/client";
import { isValidAccountId } from "@/lib/validation";

interface PageProps {
  params: Promise<{ accountId: string; portfolioNo: string }>;
}

/**
 * パスパラメータを正の整数として解釈する
 *
 * 指数表記（`1e3`）や16進表記（`0x10`）・前後空白を含む値を弾くため、
 * まず10進数字のみで構成されているかを確認してから数値化する。
 *
 * @param value パスパラメータの値
 * @returns 正の整数。解釈できない場合はnull
 */
function parsePositiveInt(value: string): number | null {
  if (!/^\d+$/.test(value)) return null;
  const num = Number(value);
  return Number.isSafeInteger(num) && num > 0 ? num : null;
}

/**
 * パスパラメータを検証し、公開中のポートフォリオを取得する（不正・存在しない場合は 404）
 *
 * `generateMetadata` とページ本体の両方から呼ぶ。同じ URL への fetch は1リクエスト内で
 * 重複排除されるため、portfolio-api への問い合わせは1回になる。
 */
async function loadPortfolio(params: PageProps["params"]) {
  const { accountId, portfolioNo } = await params;
  const parsedPortfolioNo = parsePositiveInt(portfolioNo);
  if (!isValidAccountId(accountId) || parsedPortfolioNo === null) {
    notFound();
  }
  const portfolio = await getPublishedPortfolio(accountId, parsedPortfolioNo);
  if (!portfolio) {
    notFound();
  }
  return { accountId, portfolio };
}

/**
 * 公開ページのタイトル・説明（ポートフォリオのテーマ・説明文）
 */
export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const { accountId, portfolio } = await loadPortfolio(params);
  return {
    title: `${portfolio.theme} - ${accountId} のポートフォリオ - WebGallery`,
    description: portfolio.description || undefined,
  };
}

/**
 * 公開中のポートフォリオページ（未ログインでも閲覧できる）
 *
 * レイアウトはテンプレート（`layout.__typename`）ごとのコンポーネントで描画する。
 */
export default async function PublicPortfolioPage({ params }: PageProps) {
  const { accountId, portfolio } = await loadPortfolio(params);

  return (
    <>
      <Header />
      <div className="flex flex-col items-center py-12 px-4">
        <article className="w-full max-w-[1100px] flex flex-col gap-10">
          <div className="flex flex-col gap-3">
            <Link href={`/portfolio/${accountId}`} className="text-sm text-blue-700 hover:underline">
              ← {accountId} のポートフォリオ一覧
            </Link>
            <h1 className="text-3xl font-bold">{portfolio.theme}</h1>
            {portfolio.description && (
              <p className="leading-relaxed whitespace-pre-wrap text-gray-800">{portfolio.description}</p>
            )}
            {portfolio.tags.length > 0 && (
              <ul className="flex flex-wrap gap-2" aria-label="タグ">
                {portfolio.tags.map((tag) => (
                  <li
                    key={tag.japaneseName}
                    className="text-xs px-2 py-0.5 border border-gray-400 rounded-full text-gray-700"
                  >
                    {tag.japaneseName}
                  </li>
                ))}
              </ul>
            )}
          </div>
          {renderPortfolioLayout(portfolio.layout)}
        </article>
      </div>
      <Footer variant="light" />
    </>
  );
}
