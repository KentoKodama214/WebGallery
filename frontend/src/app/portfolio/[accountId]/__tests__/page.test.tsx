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

describe("PublicPortfolioListPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("公開中のポートフォリオを、詳細ページへのリンク・先頭の写真・タグ付きで表示すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue([
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
    ]);

    render(await PublicPortfolioListPage(props("owner123")));

    expect(mockGetPublishedPortfolios).toHaveBeenCalledWith("owner123", "");
    expect(screen.getAllByTestId("public-portfolio-card")).toHaveLength(2);
    expect(screen.getByRole("link", { name: /日本の自然/ })).toHaveAttribute("href", "/portfolio/owner123/2");
    expect(screen.getByRole("img", { name: "金閣寺" })).toBeInTheDocument();
    expect(screen.getByText("自然")).toBeInTheDocument();
    expect(screen.getByText("説明文")).toBeInTheDocument();
  });

  it("公開中のポートフォリオがない場合はその旨を表示すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue([]);

    render(await PublicPortfolioListPage(props("owner123")));

    expect(screen.getByText("公開中のポートフォリオはありません")).toBeInTheDocument();
    // 検索していない場合はクリアのリンクを表示しない
    expect(screen.queryByRole("link", { name: "クリア" })).not.toBeInTheDocument();
  });

  it("テーマ・タグの検索欄は1つで、keyword をクエリパラメータとして同じページへ送信すること", async () => {
    mockGetPublishedPortfolios.mockResolvedValue([]);

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
    mockGetPublishedPortfolios.mockResolvedValue([]);

    render(await PublicPortfolioListPage(props("owner123", { keyword: [" 自然 ", "無視"] })));

    expect(mockGetPublishedPortfolios).toHaveBeenCalledWith("owner123", "自然");
    expect(screen.getByRole("searchbox", { name: "テーマ・タグ" })).toHaveValue("自然");
    expect(screen.getByText("条件に一致するポートフォリオはありません")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "クリア" })).toHaveAttribute("href", "/portfolio/owner123");
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
