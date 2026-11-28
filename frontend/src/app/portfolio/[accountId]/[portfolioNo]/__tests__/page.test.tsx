import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import PublicPortfolioPage, { generateMetadata } from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));

const mockGetPublishedPortfolio = jest.fn();
jest.mock("@/lib/portfolio-api/client", () => ({
  getPublishedPortfolio: (...args: unknown[]) => mockGetPublishedPortfolio(...args),
}));

const props = (accountId: string, portfolioNo: string) => ({
  params: Promise.resolve({ accountId, portfolioNo }),
});

const portfolio = {
  portfolioNo: 3,
  theme: "日本の自然",
  description: "日本各地の風景",
  template: "FEATURE",
  tags: [{ japaneseName: "自然", englishName: "nature" }],
  updatedAt: "2026-10-01T00:00:00Z",
  layout: {
    __typename: "FeatureLayout",
    sections: [
      {
        photo: { photoNo: 1, url: "/photos/1.jpg", japaneseTitle: "金閣寺", englishTitle: "", direction: "HORIZONTAL" },
        theme: "京都",
        subTheme: "",
        description: "",
      },
    ],
  },
};

describe("PublicPortfolioPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("テーマ・説明文・タグと、テンプレートに応じたレイアウトを表示すること", async () => {
    mockGetPublishedPortfolio.mockResolvedValue(portfolio);

    render(await PublicPortfolioPage(props("owner123", "3")));

    expect(mockGetPublishedPortfolio).toHaveBeenCalledWith("owner123", 3);
    expect(screen.getByRole("heading", { level: 1, name: "日本の自然" })).toBeInTheDocument();
    expect(screen.getByText("日本各地の風景")).toBeInTheDocument();
    expect(screen.getByText("自然")).toBeInTheDocument();
    expect(screen.getByTestId("feature-template")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /owner123 のポートフォリオ一覧/ })).toHaveAttribute(
      "href",
      "/portfolio/owner123"
    );
  });

  it("公開されていない・存在しない場合は404として扱う（notFoundを呼ぶ）", async () => {
    mockGetPublishedPortfolio.mockResolvedValue(null);

    await expect(PublicPortfolioPage(props("owner123", "9"))).rejects.toThrow();
  });

  it.each([
    ["アカウントIDの形式が不正", "a", "1"],
    ["ポートフォリオ番号が数値でない", "owner123", "abc"],
    ["ポートフォリオ番号がゼロ", "owner123", "0"],
    ["ポートフォリオ番号が指数表記", "owner123", "1e3"],
    ["ポートフォリオ番号が安全な整数の範囲外", "owner123", "99999999999999999999"],
  ])("%sの場合は、APIを呼ばずに404として扱う", async (_label, accountId, portfolioNo) => {
    await expect(PublicPortfolioPage(props(accountId, portfolioNo))).rejects.toThrow();
    expect(mockGetPublishedPortfolio).not.toHaveBeenCalled();
  });

  it("タイトルにテーマを、説明にポートフォリオの説明文を使うこと", async () => {
    mockGetPublishedPortfolio.mockResolvedValue(portfolio);

    await expect(generateMetadata(props("owner123", "3"))).resolves.toEqual({
      title: "日本の自然 - owner123 のポートフォリオ - WebGallery",
      description: "日本各地の風景",
    });
  });
});
