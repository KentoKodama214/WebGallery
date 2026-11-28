import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import PublicPortfolioListPage, { generateMetadata } from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));

// next/form はルーターに依存するため、GET で送信する素の form に置き換える
jest.mock("next/form", () => ({
  __esModule: true,
  default: ({ action, ...rest }: React.FormHTMLAttributes<HTMLFormElement> & { action: string }) => (
    <form action={action} method="get" {...rest} />
  ),
}));

const mockGetPublishedPortfolios = jest.fn();
jest.mock("@/lib/portfolio-api/client", () => ({
  getPublishedPortfolios: (...args: unknown[]) => mockGetPublishedPortfolios(...args),
}));

const props = (accountId: string, query: { [key: string]: string | string[] | undefined } = {}) => ({
  params: Promise.resolve({ accountId }),
  searchParams: Promise.resolve(query),
});

/** portfolio-api の一覧の応答を作る（既定は続きなし） */
const page = (portfolios: unknown[], hasNext = false) => ({ portfolios, hasNext });

/** 一覧の1件分 */
const summaryOf = (portfolioNo: number) => ({
  portfolioNo,
  theme: `テーマ${portfolioNo}`,
  description: "",
  template: "FEATURE",
  tags: [],
  coverPhoto: null,
  photoCount: 0,
  updatedAt: "2026-09-01T00:00:00Z",
});

describe("PublicPortfolioListPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("公開中のポートフォリオを、詳細ページへのリンク・先頭の写真・タグ付きで表示すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue(page([
      {
        portfolioNo: 2,
        theme: "日本の自然",
        description: "説明文",
        template: "FEATURE",
        tags: [{ japaneseName: "自然", englishName: "nature" }],
        coverPhoto: { photoNo: 5, url: "/photos/5.jpg", japaneseTitle: "金閣寺", englishTitle: "", direction: "SQUARE" },
        photoCount: 3,
        updatedAt: "2026-10-01T00:00:00Z",
      },
      {
        portfolioNo: 1,
        theme: "光と影",
        description: "",
        template: "GRID",
        tags: [],
        coverPhoto: null,
        photoCount: 0,
        updatedAt: "2026-09-01T00:00:00Z",
      },
    ]));

    render(await PublicPortfolioListPage(props("owner123")));

    expect(mockGetPublishedPortfolios).toHaveBeenCalledWith("owner123", "", 20);
    expect(screen.getAllByTestId("public-portfolio-card")).toHaveLength(2);
    expect(screen.getByRole("link", { name: /日本の自然/ })).toHaveAttribute("href", "/portfolio/owner123/2");
    expect(screen.getByRole("img", { name: "金閣寺" })).toBeInTheDocument();
    expect(screen.getByText("自然")).toBeInTheDocument();
    expect(screen.getByText("説明文")).toBeInTheDocument();
  });

  it("公開中のポートフォリオがない場合はその旨を表示すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue(page([]));

    render(await PublicPortfolioListPage(props("owner123")));

    expect(screen.getByText("公開中のポートフォリオはありません")).toBeInTheDocument();
    // 検索していない場合はクリアのリンクを表示しない
    expect(screen.queryByRole("link", { name: "クリア" })).not.toBeInTheDocument();
  });

  it("テーマ・タグの検索欄は1つで、keyword をクエリパラメータとして同じページへ送信すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue(page([]));

    render(await PublicPortfolioListPage(props("owner123")));

    const form = screen.getByRole("search", { name: "ポートフォリオの検索" });
    expect(form).toHaveAttribute("action", "");
    expect(form).toHaveAttribute("method", "get");
    const input = screen.getByRole("searchbox", { name: "テーマ・タグ" });
    expect(input).toHaveAttribute("name", "keyword");
    expect(input).toHaveAttribute("maxLength", "100");
    expect(screen.getAllByRole("searchbox")).toHaveLength(1);
  });

  it("検索語（前後の空白を除く）で絞り込み、入力欄に残し、クリアのリンクを表示すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue(page([]));

    render(await PublicPortfolioListPage(props("owner123", { keyword: [" 自然 ", "無視"] })));

    expect(mockGetPublishedPortfolios).toHaveBeenCalledWith("owner123", "自然", 20);
    expect(screen.getByRole("searchbox", { name: "テーマ・タグ" })).toHaveValue("自然");
    expect(screen.getByText("条件に一致するポートフォリオはありません")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "クリア" })).toHaveAttribute("href", "/portfolio/owner123");
  });

  it("続きがある場合は、検索語を保ったまま次のページまで表示する「＋もっと見る」のリンクを表示すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue(page([summaryOf(1)], true));

    render(await PublicPortfolioListPage(props("owner123", { keyword: "自然" })));

    const more = screen.getByRole("link", { name: "＋もっと見る" });
    const url = new URL(more.getAttribute("href")!, "http://localhost");
    expect(url.pathname).toBe("/portfolio/owner123");
    expect(url.searchParams.get("keyword")).toBe("自然");
    expect(url.searchParams.get("page")).toBe("2");
  });

  it("続きがない場合は「＋もっと見る」を表示しないこと", async () => {
    mockGetPublishedPortfolios.mockResolvedValue(page([summaryOf(1)], false));

    render(await PublicPortfolioListPage(props("owner123")));

    expect(screen.queryByRole("link", { name: "＋もっと見る" })).not.toBeInTheDocument();
  });

  it("page で指定したページ数分（page × 20件）を先頭からまとめて取得し、次のページへのリンクを表示すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue(page([summaryOf(1)], true));

    render(await PublicPortfolioListPage(props("owner123", { page: "3" })));

    expect(mockGetPublishedPortfolios).toHaveBeenCalledWith("owner123", "", 60);
    const url = new URL(
      screen.getByRole("link", { name: "＋もっと見る" }).getAttribute("href")!,
      "http://localhost"
    );
    expect(url.searchParams.get("page")).toBe("4");
    expect(url.searchParams.has("keyword")).toBe(false);
  });

  it.each([
    ["正の整数でない", "abc", 20],
    ["0", "0", 20],
    ["指数表記", "1e3", 20],
    ["上限（50ページ）を超える", "999", 1000],
  ])("page が%sの場合は、範囲内のページ数として扱うこと", async (_label, pageParam, limit) => {
    mockGetPublishedPortfolios.mockResolvedValue(page([]));

    render(await PublicPortfolioListPage(props("owner123", { page: pageParam })));

    expect(mockGetPublishedPortfolios).toHaveBeenCalledWith("owner123", "", limit);
  });

  it("アカウントIDの形式が不正な場合は、APIを呼ばずに404として扱う（notFoundを呼ぶ）", async () => {
    await expect(PublicPortfolioListPage(props("a"))).rejects.toThrow();
    expect(mockGetPublishedPortfolios).not.toHaveBeenCalled();
  });

  it("タイトルにアカウントIDを含めること", async () => {
    await expect(generateMetadata(props("owner123"))).resolves.toEqual({
      title: "owner123 のポートフォリオ - WebGallery",
    });
  });
});
