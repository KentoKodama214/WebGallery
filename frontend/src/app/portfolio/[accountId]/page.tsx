import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { PortfolioPhotoImage } from "@/components/portfolio/PortfolioPhotoImage";
import { getPublishedPortfolios } from "@/lib/portfolio-api/client";
import { isValidAccountId } from "@/lib/validation";

interface PageProps {
  params: Promise<{ accountId: string }>;
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
 */
export default async function PublicPortfolioListPage({ params }: PageProps) {
  const { accountId } = await params;

  // accountId はURLの動的セグメントで細工可能なため、API に渡す前に形式を検証する
  if (!isValidAccountId(accountId)) {
    notFound();
  }

  const portfolios = await getPublishedPortfolios(accountId);

  return (
    <>
      <Header />
      <div className="flex flex-col items-center py-12 px-4">
        <div className="w-full max-w-[1100px] flex flex-col gap-8">
          <h1 className="text-2xl font-bold">{accountId} のポートフォリオ</h1>
          {portfolios.length === 0 ? (
            <p className="text-gray-700">公開中のポートフォリオはありません</p>
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
                    <h2 className="text-lg font-bold text-blue-700 group-hover:underline">
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
        </div>
      </div>
      <Footer variant="light" />
    </>
  );
}
