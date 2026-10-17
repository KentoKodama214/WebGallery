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
    fetchMock.mockResolvedValue(jsonResponse({ data: { publishedPortfolios: [{ portfolioNo: 1 }] } }));

    const result = await getPublishedPortfolios("owner123");

    expect(result).toEqual([{ portfolioNo: 1 }]);
    const [url, init] = fetchMock.mock.calls[0];
    const parsed = new URL(String(url));
    expect(parsed.origin + parsed.pathname).toBe("http://localhost:8081/graphql");
    expect(parsed.searchParams.get("query")).toContain("publishedPortfolios(accountId: $accountId)");
    expect(JSON.parse(parsed.searchParams.get("variables")!)).toEqual({ accountId: "owner123" });
    // 署名付きURLの失効後に古いデータを返さないよう、キャッシュしない
    expect(init.cache).toBe("no-store");
    expect(init.next).toBeUndefined();
    expect(init.method).toBeUndefined();
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
