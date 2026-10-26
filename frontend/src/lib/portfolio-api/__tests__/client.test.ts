import { getPublishedPortfolio, getPublishedPortfolios, PortfolioApiError } from "../client";

function jsonResponse(body: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as Response;
}

describe("portfolio-api client", () => {
  let fetchMock: jest.Mock;

  beforeEach(() => {
    fetchMock = jest.fn();
    global.fetch = fetchMock as unknown as typeof fetch;
  });

  it("クエリと変数を GET のクエリパラメータで送り、データキャッシュには載せないこと", async () => {
    fetchMock.mockResolvedValue(
      jsonResponse({ data: { publishedPortfolios: { portfolios: [{ portfolioNo: 1 }], hasNext: false } } })
    );

    const result = await getPublishedPortfolios("owner123");

    expect(result).toEqual({ portfolios: [{ portfolioNo: 1 }], hasNext: false });
    const [url, init] = fetchMock.mock.calls[0];
    const parsed = new URL(String(url));
    expect(parsed.origin + parsed.pathname).toBe("http://localhost:8081/graphql");
    expect(parsed.searchParams.get("query")).toContain(
      "publishedPortfolios(accountId: $accountId, keyword: $keyword, limit: $limit)"
    );
    // 検索語を指定しない場合は空文字（絞り込まない）、件数を指定しない場合は1ページ分（20件）を送る
    expect(JSON.parse(parsed.searchParams.get("variables")!)).toEqual({
      accountId: "owner123",
      keyword: "",
      limit: 20,
    });
    // 署名付きURLの失効後に古いデータを返さないよう、キャッシュしない
    expect(init.cache).toBe("no-store");
    expect(init.next).toBeUndefined();
    expect(init.method).toBeUndefined();
  });

  it("検索語・取得件数を変数として送ること", async () => {
    fetchMock.mockResolvedValue(
      jsonResponse({ data: { publishedPortfolios: { portfolios: [], hasNext: false } } })
    );

    await getPublishedPortfolios("owner123", "自然", 60);

    const parsed = new URL(String(fetchMock.mock.calls[0][0]));
    expect(JSON.parse(parsed.searchParams.get("variables")!)).toEqual({
      accountId: "owner123",
      keyword: "自然",
      limit: 60,
    });
  });

  it("ポートフォリオが公開されていない場合は null を返すこと", async () => {
    fetchMock.mockResolvedValue(jsonResponse({ data: { publishedPortfolio: null } }));

    await expect(getPublishedPortfolio("owner123", 9)).resolves.toBeNull();
    const parsed = new URL(String(fetchMock.mock.calls[0][0]));
    expect(JSON.parse(parsed.searchParams.get("variables")!)).toEqual({
      accountId: "owner123",
      portfolioNo: 9,
    });
  });

  it.each([
    ["HTTPエラー", () => jsonResponse({}, 500)],
    ["GraphQLのエラー", () => jsonResponse({ data: null, errors: [{ message: "internal server error" }] })],
    ["JSONでない応答", () => ({ ok: true, status: 200, json: async () => { throw new SyntaxError("x"); } }) as unknown as Response],
  ])("%sの場合は PortfolioApiError を投げること", async (_label, makeResponse) => {
    fetchMock.mockResolvedValue(makeResponse());

    await expect(getPublishedPortfolios("owner123")).rejects.toBeInstanceOf(PortfolioApiError);
  });

  it("通信に失敗した場合は PortfolioApiError を投げること", async () => {
    fetchMock.mockRejectedValue(new TypeError("fetch failed"));

    await expect(getPublishedPortfolio("owner123", 1)).rejects.toBeInstanceOf(PortfolioApiError);
  });
});
