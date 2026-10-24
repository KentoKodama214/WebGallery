import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PortfolioList } from "../PortfolioList";

const mockGetPortfolioList = jest.fn();
const mockGetPortfolioUpperLimit = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPortfolioList: (...args: unknown[]) => mockGetPortfolioList(...args),
  getPortfolioUpperLimit: (...args: unknown[]) => mockGetPortfolioUpperLimit(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const samplePortfolio = {
  portfolioNo: 2,
  theme: "日本の自然",
  templateKbn: "grid" as const,
  statusKbn: "published" as const,
  photoCount: 5,
  createdAt: "2026-01-01T00:00:00+09:00",
  updatedAt: "2026-01-02T00:00:00+09:00",
};

describe("PortfolioList", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({ user: { accountId: "user1", accountNo: 1 } });
    mockGetPortfolioUpperLimit.mockResolvedValue({ isReachedUpperLimit: false });
  });

  it("ポートフォリオが0件の場合はその旨と新規作成リンクが表示されること", async () => {
    mockGetPortfolioList.mockResolvedValue([]);

    render(<PortfolioList accountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("ポートフォリオはありません")).toBeInTheDocument();
    });
    expect(mockGetPortfolioList).toHaveBeenCalledWith("user1");
    expect(mockGetPortfolioUpperLimit).toHaveBeenCalledWith("user1");
    expect(screen.getByRole("link", { name: "新規作成" })).toHaveAttribute(
      "href",
      "/user1/portfolio_setting/edit"
    );
  });

  it("一覧にテーマ（編集画面へのリンク）・テンプレート・写真の枚数・公開状態が表示されること", async () => {
    mockGetPortfolioList.mockResolvedValue([
      samplePortfolio,
      { ...samplePortfolio, portfolioNo: 1, theme: "光と影", templateKbn: "feature", statusKbn: "draft" },
    ]);

    render(<PortfolioList accountId="user1" />);

    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
    });
    expect(screen.getByRole("link", { name: "日本の自然" })).toHaveAttribute(
      "href",
      "/user1/portfolio_setting/edit?portfolioNo=2"
    );
    expect(screen.getByText("グリッド")).toBeInTheDocument();
    expect(screen.getByText("フィーチャー")).toBeInTheDocument();
    expect(screen.getByText("公開中")).toBeInTheDocument();
    expect(screen.getByText("下書き")).toBeInTheDocument();
    expect(screen.getAllByText("5枚")).toHaveLength(2);
  });

  it("取得に失敗した場合はエラーを表示し、再読み込みで取り直せること", async () => {
    mockGetPortfolioList.mockRejectedValueOnce(new Error("ポートフォリオ一覧の取得に失敗しました"));
    mockGetPortfolioList.mockResolvedValueOnce([samplePortfolio]);

    render(<PortfolioList accountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("ポートフォリオ一覧の取得に失敗しました")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByRole("button", { name: "再読み込み" }));

    await waitFor(() => {
      expect(screen.getByRole("link", { name: "日本の自然" })).toBeInTheDocument();
    });
    expect(mockGetPortfolioList).toHaveBeenCalledTimes(2);
  });

  it("テーマ・タグを入力して検索すると、その条件で一覧を取得し直し、クリアで条件を解除すること", async () => {
    mockGetPortfolioList.mockResolvedValueOnce([samplePortfolio]).mockResolvedValueOnce([]);
    mockGetPortfolioList.mockResolvedValueOnce([samplePortfolio]);

    render(<PortfolioList accountId="user1" />);
    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(1);
    });

    fireEvent.change(screen.getByLabelText("テーマ"), { target: { value: " 自然 " } });
    fireEvent.change(screen.getByLabelText("タグ"), { target: { value: "風景" } });
    fireEvent.click(screen.getByRole("button", { name: "検索" }));

    expect(
      await screen.findByText("条件に一致するポートフォリオはありません")
    ).toBeInTheDocument();
    expect(mockGetPortfolioList).toHaveBeenLastCalledWith("user1", { theme: "自然", tag: "風景" });

    fireEvent.click(screen.getByRole("button", { name: "クリア" }));

    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(1);
    });
    expect(mockGetPortfolioList).toHaveBeenLastCalledWith("user1", {});
    expect(screen.getByLabelText("テーマ")).toHaveValue("");
  });

  it("作成数が上限に達している場合は、新規作成を押せず、上限に達した旨を表示すること", async () => {
    mockGetPortfolioList.mockResolvedValue([samplePortfolio]);
    mockGetPortfolioUpperLimit.mockResolvedValue({ isReachedUpperLimit: true });

    render(<PortfolioList accountId="user1" />);

    const createButton = await screen.findByRole("button", { name: "新規作成" });
    expect(createButton).toBeDisabled();
    expect(createButton).toHaveAccessibleDescription(
      "作成できるポートフォリオの数が上限に達しています"
    );
    expect(screen.queryByRole("link", { name: "新規作成" })).not.toBeInTheDocument();
  });

  it("作成上限の取得に失敗した場合は、新規作成を妨げないこと", async () => {
    mockGetPortfolioList.mockResolvedValue([]);
    mockGetPortfolioUpperLimit.mockRejectedValue(new Error("取得失敗"));

    render(<PortfolioList accountId="user1" />);

    await waitFor(() => {
      expect(screen.getByText("ポートフォリオはありません")).toBeInTheDocument();
    });
    expect(screen.getByRole("link", { name: "新規作成" })).toBeInTheDocument();
  });

  it("他人のアカウントIDの場合は権限がない旨を表示し、一覧を取得しないこと", () => {
    render(<PortfolioList accountId="otheruser1" />);

    expect(screen.getByText("この操作を行う権限がありません")).toBeInTheDocument();
    expect(mockGetPortfolioList).not.toHaveBeenCalled();
  });
});
