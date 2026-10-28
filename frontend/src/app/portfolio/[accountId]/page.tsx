import type { Metadata } from "next";
import Form from "next/form";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { PortfolioPhotoImage } from "@/components/portfolio/PortfolioPhotoImage";
import { getPublishedPortfolios } from "@/lib/portfolio-api/client";
import { isValidAccountId, PORTFOLIO_THEME_MAX_LENGTH } from "@/lib/validation";

interface PageProps {
  params: Promise<{ accountId: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}

/**
 * クエリパラメータの検索語を取り出す（前後の空白を除く。複数指定された場合は先頭を使う）
 *
 * @param value クエリパラメータ `keyword` の値
 * @returns 検索語。未指定の場合は空文字
 */
function toKeyword(value: string | string[] | undefined): string {
  const keyword = Array.isArray(value) ? value[0] : value;
  return (keyword ?? "").trim();
}

/** 1ページ（「＋もっと見る」1回）あたりの表示件数 */
const PORTFOLIO_COUNT_PER_PAGE = 20;

/**
 * 表示するページ数の上限
 *
 * 先頭から読み込み済みの件数をまとめて取り直すため、portfolio-api が一度に返せる件数の上限（1000件）を超えないようにする
 */
const MAX_PAGE = 1000 / PORTFOLIO_COUNT_PER_PAGE;

/**
 * クエリパラメータのページ番号（読み込み済みのページ数）を取り出す
 *
 * 正の整数でない値は1ページ目、上限を超える値は上限のページとして扱う。
 *
 * @param value クエリパラメータ `page` の値
 * @returns ページ番号（1〜{@link MAX_PAGE}）
 */
function toPage(value: string | string[] | undefined): number {
  const page = Array.isArray(value) ? value[0] : value;
  if (!page || !/^\d+$/.test(page)) return 1;
  return Math.min(Math.max(Number(page), 1), MAX_PAGE);
}

/**
 * 公開ページのタイトル
 */
export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const { accountId } = await params;
  return { title: `${accountId} のポートフォリオ - WebGallery` };
}

/**
 * 公開中のポートフォリオ一覧ページ（未ログインでも閲覧できる）
 *
 * 一覧は所有者が設定した表示順に20件ずつ並ぶ。検索語（クエリパラメータ `keyword`）を指定すると、
 * テーマ・タグ名のいずれかに部分一致するものに絞り込む。
 *
 * 「＋もっと見る」は `page`（読み込み済みのページ数）を1つ進めたURLへのリンクで、先頭から page × 20 件をまとめて描画する。
 * スクロール位置は保つため、続きが下に追加されたように表示される（JavaScriptなしでも動き、URLを共有すれば同じ件数まで表示できる）。
 */
export default async function PublicPortfolioListPage({ params, searchParams }: PageProps) {
  const { accountId } = await params;
  const query = await searchParams;
  const keyword = toKeyword(query.keyword);
  const page = toPage(query.page);

  // accountId はURLの動的セグメントで細工可能なため、API に渡す前に形式を検証する
  if (!isValidAccountId(accountId)) {
    notFound();
  }

  const { portfolios, hasNext } = await getPublishedPortfolios(
    accountId,
    keyword,
    page * PORTFOLIO_COUNT_PER_PAGE
  );
  // 検索語を保ったまま、次のページまで表示するURL
  const nextPageQuery = new URLSearchParams({
    ...(keyword ? { keyword } : {}),
    page: String(page + 1),
  });

  return (
    <>
      <Header />
      <div className="flex flex-col items-center py-12 px-4">
        <div className="w-full max-w-[1100px] flex flex-col gap-8">
          <h1 className="text-2xl font-bold">{accountId} のポートフォリオ</h1>
          {/* GET で同じページへ遷移し、検索語をクエリパラメータに載せる（検索結果のURLを共有できる） */}
          <Form
            action=""
            role="search"
            aria-label="ポートフォリオの検索"
            className="flex flex-wrap items-end gap-3"
          >
            <div className="flex flex-col">
              <label htmlFor="public-portfolio-keyword" className="text-sm font-bold mb-1">
                テーマ・タグ
              </label>
              <input
                id="public-portfolio-keyword"
                name="keyword"
                type="search"
                defaultValue={keyword}
                maxLength={PORTFOLIO_THEME_MAX_LENGTH}
                className="border border-gray-300 rounded px-3 py-2 w-72 max-w-full"
              />
            </div>
            <button
              type="submit"
              className="px-4 py-2 text-sm bg-blue-700 text-white rounded hover:bg-blue-800"
            >
              検索
            </button>
            {keyword && (
              <Link
                href={`/portfolio/${accountId}`}
                className="px-4 py-2 text-sm border border-gray-400 rounded hover:bg-gray-50"
              >
                クリア
              </Link>
            )}
          </Form>
          {portfolios.length === 0 ? (
            <p className="text-gray-700">
              {keyword
                ? "条件に一致するポートフォリオはありません"
                : "公開中のポートフォリオはありません"}
            </p>
          ) : (
            <ul className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
              {portfolios.map((portfolio) => (
                <li key={portfolio.portfolioNo} data-testid="public-portfolio-card">
                  <Link
                    href={`/portfolio/${accountId}/${portfolio.portfolioNo}`}
                    className="flex flex-col gap-3 group"
                  >
                    {portfolio.coverPhoto ? (
                      <PortfolioPhotoImage
                        photo={portfolio.coverPhoto}
                        className="w-full object-cover rounded"
                        style={{ aspectRatio: "4 / 3" }}
                      />
                    ) : (
                      <div
                        className="w-full bg-gray-200 rounded"
                        style={{ aspectRatio: "4 / 3" }}
                        aria-hidden="true"
                      />
                    )}
                    {/* テーマは2行まで表示し、収まらない分は「…」で切る */}
                    <h2 className="text-lg font-bold text-blue-700 group-hover:underline line-clamp-2 break-words">
                      {portfolio.theme}
                    </h2>
                  </Link>
                  {portfolio.description && (
                    <p className="text-sm text-gray-700 line-clamp-3">{portfolio.description}</p>
                  )}
                  {portfolio.tags.length > 0 && (
                    <ul className="flex flex-wrap gap-2 mt-2" aria-label="タグ">
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
                </li>
              ))}
            </ul>
          )}
          {hasNext && portfolios.length > 0 && (
            <div className="flex justify-center">
              <Link
                href={`/portfolio/${accountId}?${nextPageQuery.toString()}`}
                scroll={false}
                className="px-6 py-2 text-sm border border-gray-400 rounded hover:bg-gray-50"
              >
                ＋もっと見る
              </Link>
            </div>
          )}
        </div>
      </div>
      <Footer variant="light" />
    </>
  );
}
