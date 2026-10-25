import "server-only";
import { PUBLISHED_PORTFOLIO_QUERY, PUBLISHED_PORTFOLIOS_QUERY } from "./queries";
import type { PublicPortfolio, PublicPortfolioSummary } from "./types";

/**
 * portfolio-api（公開ページ向けのポートフォリオ表示API。GraphQL）のクライアント
 *
 * portfolio-api はブラウザに公開しないため、Server Component からだけ呼び出す（`server-only`）。
 * 接続先は `PORTFOLIO_API_URL`（サーバー側だけで参照する環境変数。`NEXT_PUBLIC_` を付けない）。
 */

const PORTFOLIO_API_URL = process.env.PORTFOLIO_API_URL || "http://localhost:8081/graphql";

/** portfolio-api からの取得に失敗したことを表すエラー（`error.tsx` で表示する） */
export class PortfolioApiError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "PortfolioApiError";
  }
}

/**
 * GraphQL クエリを GET で送り、`data` を返す
 *
 * Next.js のデータキャッシュには載せない（`no-store`）。レスポンスに含む画像の署名付きURLには
 * 有効期限（portfolio-api の `APP_S3_PRESIGN_EXPIRY_SECONDS`、既定 900 秒）があり、データキャッシュは
 * 期限切れ後の最初のリクエストに古いデータを返しながら裏で再取得する（stale-while-revalidate）ため、
 * アクセスの少ないページでは失効した URL を返して画像が表示されなくなる。あわせて、下書きに戻した・
 * 削除したポートフォリオが公開ページに残り続けることもない。
 * 同じクエリ・変数の GET は1リクエストの描画中に重複排除されるため、`generateMetadata` とページ本体から
 * 呼んでも portfolio-api への問い合わせは1回になる。
 *
 * @param query クエリ
 * @param variables 変数
 * @returns レスポンスの `data`
 */
async function request<T>(query: string, variables: Record<string, unknown>): Promise<T> {
  const url = new URL(PORTFOLIO_API_URL);
  url.searchParams.set("query", query);
  url.searchParams.set("variables", JSON.stringify(variables));

  let response: Response;
  try {
    response = await fetch(url, {
      headers: { Accept: "application/json" },
      cache: "no-store",
      signal: AbortSignal.timeout(10_000),
    });
  } catch (error) {
    throw new PortfolioApiError(`portfolio-api request failed: ${String(error)}`);
  }
  if (!response.ok) {
    throw new PortfolioApiError(`portfolio-api responded with status ${response.status}`);
  }

  const body = (await response.json().catch(() => null)) as {
    data?: T;
    errors?: { message: string }[];
  } | null;
  if (!body || body.errors?.length || !body.data) {
    throw new PortfolioApiError(
      `portfolio-api returned errors: ${body?.errors?.map((e) => e.message).join(", ") ?? "invalid response"}`
    );
  }
  return body.data;
}

/**
 * 公開中のポートフォリオ一覧を取得する（アカウントが存在しない場合も空の一覧）
 *
 * @param accountId アカウントID
 * @param keyword 検索語。テーマ・タグ名のいずれかに部分一致するものに絞り込む（空の場合は絞り込まない）
 * @returns 公開中のポートフォリオ一覧（表示順）
 */
export async function getPublishedPortfolios(
  accountId: string,
  keyword = ""
): Promise<PublicPortfolioSummary[]> {
  const data = await request<{ publishedPortfolios: PublicPortfolioSummary[] }>(
    PUBLISHED_PORTFOLIOS_QUERY,
    { accountId, keyword }
  );
  return data.publishedPortfolios;
}

/**
 * 公開中のポートフォリオを取得する
 *
 * @param accountId アカウントID
 * @param portfolioNo ポートフォリオ番号
 * @returns 公開中のポートフォリオ。存在しない・公開されていない場合は null
 */
export async function getPublishedPortfolio(
  accountId: string,
  portfolioNo: number
): Promise<PublicPortfolio | null> {
  const data = await request<{ publishedPortfolio: PublicPortfolio | null }>(
    PUBLISHED_PORTFOLIO_QUERY,
    { accountId, portfolioNo }
  );
  return data.publishedPortfolio;
}
