import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PortfolioList } from "../PortfolioList";

const mockGetPortfolioList = jest.fn();
const mockGetPortfolioUpperLimit = jest.fn();
const mockUpdatePortfolioDisplayOrder = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPortfolioList: (...args: unknown[]) => mockGetPortfolioList(...args),
  getPortfolioUpperLimit: (...args: unknown[]) => mockGetPortfolioUpperLimit(...args),
  updatePortfolioDisplayOrder: (...args: unknown[]) => mockUpdatePortfolioDisplayOrder(...args),
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

  it("公開中のポートフォリオにだけ公開ページへのリンクを表示し、一覧全体の「公開ページを見る」は表示しないこと", async () => {
    mockGetPortfolioList.mockResolvedValue([
      samplePortfolio,
      { ...samplePortfolio, portfolioNo: 1, theme: "光と影", statusKbn: "draft" },
    ]);

    render(<PortfolioList accountId="user1" />);

    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
    });
    const publicLink = screen.getByRole("link", { name: "日本の自然の公開ページ（新しいタブで開く）" });
    expect(publicLink).toHaveAttribute("href", "/portfolio/user1/2");
    expect(publicLink).toHaveAttribute("target", "_blank");
    expect(screen.queryByRole("link", { name: /光と影の公開ページ/ })).not.toBeInTheDocument();
    expect(screen.queryByRole("link", { name: "公開ページを見る" })).not.toBeInTheDocument();
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

  it("テーマ・タグの検索欄は1つで背景が白であること", async () => {
    mockGetPortfolioList.mockResolvedValue([samplePortfolio]);

    render(<PortfolioList accountId="user1" />);
    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(1);
    });

    const search = screen.getByRole("search", { name: "ポートフォリオの絞り込み" });
    expect(within(search).getAllByRole("textbox")).toHaveLength(1);
    const input = screen.getByLabelText("テーマ・タグ");
    expect(input).toHaveAttribute("maxLength", "100");
    expect(input).toHaveClass("bg-white");
  });

  it("検索語を入力して検索すると、その条件で一覧を取得し直し、クリアで条件を解除すること", async () => {
    mockGetPortfolioList.mockResolvedValueOnce([samplePortfolio]).mockResolvedValueOnce([]);
    mockGetPortfolioList.mockResolvedValueOnce([samplePortfolio]);

    render(<PortfolioList accountId="user1" />);
    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(1);
    });

    fireEvent.change(screen.getByLabelText("テーマ・タグ"), { target: { value: " 自然 " } });
    fireEvent.click(screen.getByRole("button", { name: "検索" }));

    expect(
      await screen.findByText("条件に一致するポートフォリオはありません")
    ).toBeInTheDocument();
    expect(mockGetPortfolioList).toHaveBeenLastCalledWith("user1", { keyword: "自然" });

    fireEvent.click(screen.getByRole("button", { name: "クリア" }));

    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(1);
    });
    expect(mockGetPortfolioList).toHaveBeenLastCalledWith("user1", {});
    expect(screen.getByLabelText("テーマ・タグ")).toHaveValue("");
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

  describe("並び替え", () => {
    const threePortfolios = [
      { ...samplePortfolio, portfolioNo: 3, theme: "春" },
      { ...samplePortfolio, portfolioNo: 1, theme: "夏" },
      { ...samplePortfolio, portfolioNo: 2, theme: "秋" },
    ];

    const themesInOrder = () =>
      screen.getAllByTestId("portfolio-row").map((row) => row.querySelector("a")?.textContent);

    it("先頭の行は上へ、末尾の行は下へ移動できないこと", async () => {
      mockGetPortfolioList.mockResolvedValue(threePortfolios);

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      expect(screen.getByRole("button", { name: "春を上へ移動" })).toBeDisabled();
      expect(screen.getByRole("button", { name: "春を下へ移動" })).toBeEnabled();
      expect(screen.getByRole("button", { name: "秋を上へ移動" })).toBeEnabled();
      expect(screen.getByRole("button", { name: "秋を下へ移動" })).toBeDisabled();
    });

    it("下へ移動すると隣の行と入れ替わり、全件の並び順を保存してフォーカスを移動した行に残すこと", async () => {
      mockGetPortfolioList.mockResolvedValue(threePortfolios);
      mockUpdatePortfolioDisplayOrder.mockResolvedValue(undefined);

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.click(screen.getByRole("button", { name: "春を下へ移動" }));

      expect(themesInOrder()).toEqual(["夏", "春", "秋"]);
      expect(mockUpdatePortfolioDisplayOrder).toHaveBeenCalledWith("user1", [1, 3, 2]);
      await waitFor(() => {
        expect(screen.getByRole("status")).toHaveTextContent("「春」の並び順を変更しました");
      });
      expect(screen.getByRole("button", { name: "春を下へ移動" })).toHaveFocus();
    });

    it("末尾まで移動して下へのボタンが無効になった場合は、上へのボタンにフォーカスを移すこと", async () => {
      mockGetPortfolioList.mockResolvedValue(threePortfolios);
      mockUpdatePortfolioDisplayOrder.mockResolvedValue(undefined);

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.click(screen.getByRole("button", { name: "夏を下へ移動" }));

      await waitFor(() => {
        expect(screen.getByRole("button", { name: "夏を上へ移動" })).toHaveFocus();
      });
      expect(themesInOrder()).toEqual(["春", "秋", "夏"]);
      expect(mockUpdatePortfolioDisplayOrder).toHaveBeenCalledWith("user1", [3, 2, 1]);
    });

    it("保存中は並び替えのボタンを無効にすること", async () => {
      mockGetPortfolioList.mockResolvedValue(threePortfolios);
      let resolveSave: () => void = () => {};
      mockUpdatePortfolioDisplayOrder.mockReturnValue(
        new Promise<void>((resolve) => {
          resolveSave = resolve;
        })
      );

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.click(screen.getByRole("button", { name: "秋を上へ移動" }));

      expect(screen.getByRole("button", { name: "春を下へ移動" })).toBeDisabled();
      resolveSave();
      await waitFor(() => {
        expect(screen.getByRole("button", { name: "春を下へ移動" })).toBeEnabled();
      });
    });

    it("保存に失敗した場合はエラーを表示し、サーバーから一覧を取り直すこと", async () => {
      mockGetPortfolioList.mockResolvedValueOnce(threePortfolios);
      mockGetPortfolioList.mockResolvedValueOnce(threePortfolios);
      mockUpdatePortfolioDisplayOrder.mockRejectedValue(
        new Error("ポートフォリオの一覧が最新ではありません。再読み込みしてから並び替えてください。")
      );

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.click(screen.getByRole("button", { name: "春を下へ移動" }));

      await waitFor(() => {
        expect(screen.getByRole("status")).toHaveTextContent(
          "ポートフォリオの一覧が最新ではありません。再読み込みしてから並び替えてください。"
        );
      });
      await waitFor(() => {
        expect(themesInOrder()).toEqual(["春", "夏", "秋"]);
      });
      expect(mockGetPortfolioList).toHaveBeenCalledTimes(2);
    });

    it("絞り込み中は並び替えできず、その旨を表示すること", async () => {
      mockGetPortfolioList.mockResolvedValueOnce(threePortfolios);
      mockGetPortfolioList.mockResolvedValueOnce(threePortfolios.slice(0, 2));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.change(screen.getByLabelText("テーマ・タグ"), { target: { value: "春" } });
      fireEvent.click(screen.getByRole("button", { name: "検索" }));

      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      });
      expect(
        screen.getByText("絞り込み中は並び替えできません。並び替えるには条件をクリアしてください")
      ).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "春を下へ移動" })).toBeDisabled();
      expect(mockUpdatePortfolioDisplayOrder).not.toHaveBeenCalled();
    });
  });
});
