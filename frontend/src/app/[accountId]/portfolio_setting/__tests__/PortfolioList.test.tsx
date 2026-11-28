import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import "@testing-library/jest-dom";
import { PortfolioList } from "../PortfolioList";

const mockGetPortfolioList = jest.fn();
const mockGetPortfolioUpperLimit = jest.fn();
const mockMovePortfolioDisplayOrder = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getPortfolioList: (...args: unknown[]) => mockGetPortfolioList(...args),
  getPortfolioUpperLimit: (...args: unknown[]) => mockGetPortfolioUpperLimit(...args),
  movePortfolioDisplayOrder: (...args: unknown[]) => mockMovePortfolioDisplayOrder(...args),
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

/** 一覧APIの1ページ分の応答を作る（既定は最後のページ） */
const page = (portfolioList: unknown[], isLast = true) => ({ portfolioList, isLast });

describe("PortfolioList", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({ user: { accountId: "user1", accountNo: 1 } });
    mockGetPortfolioUpperLimit.mockResolvedValue({ isReachedUpperLimit: false });
  });

  it("ポートフォリオが0件の場合はその旨と新規作成リンクが表示されること", async () => {
    mockGetPortfolioList.mockResolvedValue(page([]));

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

  it("一覧にテーマ（編集画面へのリンク）・公開状態が表示され、テンプレート・写真の枚数・更新日時は表示されないこと", async () => {
    mockGetPortfolioList.mockResolvedValue(
      page([
        samplePortfolio,
        { ...samplePortfolio, portfolioNo: 1, theme: "光と影", templateKbn: "feature", statusKbn: "draft" },
      ])
    );

    render(<PortfolioList accountId="user1" />);

    await waitFor(() => {
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
    });
    expect(screen.getByRole("link", { name: "日本の自然" })).toHaveAttribute(
      "href",
      "/user1/portfolio_setting/edit?portfolioNo=2"
    );
    expect(screen.getByText("公開中")).toBeInTheDocument();
    expect(screen.getByText("下書き")).toBeInTheDocument();
    expect(screen.queryByRole("columnheader", { name: "テンプレート" })).not.toBeInTheDocument();
    expect(screen.queryByRole("columnheader", { name: "写真" })).not.toBeInTheDocument();
    expect(screen.queryByRole("columnheader", { name: "更新日時" })).not.toBeInTheDocument();
    expect(screen.queryByText("グリッド")).not.toBeInTheDocument();
    expect(screen.queryByText("フィーチャー")).not.toBeInTheDocument();
    expect(screen.queryByText("5枚")).not.toBeInTheDocument();
  });

  it("公開中のポートフォリオにだけ公開ページへのリンクを表示し、一覧全体の「公開ページを見る」は表示しないこと", async () => {
    mockGetPortfolioList.mockResolvedValue(
      page([
        samplePortfolio,
        { ...samplePortfolio, portfolioNo: 1, theme: "光と影", statusKbn: "draft" },
      ])
    );

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
    mockGetPortfolioList.mockResolvedValueOnce(page([samplePortfolio]));

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
    mockGetPortfolioList.mockResolvedValue(page([samplePortfolio]));

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
    mockGetPortfolioList.mockResolvedValueOnce(page([samplePortfolio])).mockResolvedValueOnce(page([]));
    mockGetPortfolioList.mockResolvedValueOnce(page([samplePortfolio]));

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
    mockGetPortfolioList.mockResolvedValue(page([samplePortfolio]));
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
    mockGetPortfolioList.mockResolvedValue(page([]));
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
      mockGetPortfolioList.mockResolvedValue(page(threePortfolios));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      expect(screen.getByRole("button", { name: "春を上へ移動" })).toBeDisabled();
      expect(screen.getByRole("button", { name: "春を下へ移動" })).toBeEnabled();
      expect(screen.getByRole("button", { name: "秋を上へ移動" })).toBeEnabled();
      expect(screen.getByRole("button", { name: "秋を下へ移動" })).toBeDisabled();
    });

    it("下へ移動すると隣の行と入れ替わり、移動したポートフォリオと方向を保存してフォーカスを移動した行に残すこと", async () => {
      mockGetPortfolioList.mockResolvedValue(page(threePortfolios));
      mockMovePortfolioDisplayOrder.mockResolvedValue(undefined);

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.click(screen.getByRole("button", { name: "春を下へ移動" }));

      expect(themesInOrder()).toEqual(["夏", "春", "秋"]);
      expect(mockMovePortfolioDisplayOrder).toHaveBeenCalledWith("user1", 3, "down");
      await waitFor(() => {
        expect(screen.getByRole("status")).toHaveTextContent("「春」の並び順を変更しました");
      });
      expect(screen.getByRole("button", { name: "春を下へ移動" })).toHaveFocus();
    });

    it("末尾まで移動して下へのボタンが無効になった場合は、上へのボタンにフォーカスを移すこと", async () => {
      mockGetPortfolioList.mockResolvedValue(page(threePortfolios));
      mockMovePortfolioDisplayOrder.mockResolvedValue(undefined);

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.click(screen.getByRole("button", { name: "夏を下へ移動" }));

      await waitFor(() => {
        expect(screen.getByRole("button", { name: "夏を上へ移動" })).toHaveFocus();
      });
      expect(themesInOrder()).toEqual(["春", "秋", "夏"]);
      expect(mockMovePortfolioDisplayOrder).toHaveBeenCalledWith("user1", 1, "down");
    });

    it("保存中は並び替えのボタンを無効にすること", async () => {
      mockGetPortfolioList.mockResolvedValue(page(threePortfolios));
      let resolveSave: () => void = () => {};
      mockMovePortfolioDisplayOrder.mockReturnValue(
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
      mockGetPortfolioList.mockResolvedValueOnce(page(threePortfolios));
      mockGetPortfolioList.mockResolvedValueOnce(page(threePortfolios));
      mockMovePortfolioDisplayOrder.mockRejectedValue(
        new Error("ポートフォリオが存在しません。")
      );

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(3);
      });

      fireEvent.click(screen.getByRole("button", { name: "春を下へ移動" }));

      await waitFor(() => {
        expect(screen.getByRole("status")).toHaveTextContent("ポートフォリオが存在しません。");
      });
      await waitFor(() => {
        expect(themesInOrder()).toEqual(["春", "夏", "秋"]);
      });
      expect(mockGetPortfolioList).toHaveBeenCalledTimes(2);
    });

    it("絞り込み中は並び替えできず、その旨を表示すること", async () => {
      mockGetPortfolioList.mockResolvedValueOnce(page(threePortfolios));
      mockGetPortfolioList.mockResolvedValueOnce(page(threePortfolios.slice(0, 2)));

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
      expect(mockMovePortfolioDisplayOrder).not.toHaveBeenCalled();
    });
  });

  describe("ページング", () => {
    const portfolioOf = (portfolioNo: number) => ({
      ...samplePortfolio,
      portfolioNo,
      theme: `テーマ${portfolioNo}`,
    });
    const firstPage = [1, 2].map(portfolioOf);
    const secondPage = [3, 4].map(portfolioOf);

    const themesInOrder = () =>
      screen.getAllByTestId("portfolio-row").map((row) => row.querySelector("a")?.textContent);

    it("続きがある場合は「＋もっと見る」で次のページを末尾に追加し、最後のページを読み込んだら非表示にすること", async () => {
      mockGetPortfolioList
        .mockResolvedValueOnce(page(firstPage, false))
        .mockResolvedValueOnce(page(secondPage, true));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      });

      fireEvent.click(screen.getByRole("button", { name: "＋もっと見る" }));

      await waitFor(() => {
        expect(themesInOrder()).toEqual(["テーマ1", "テーマ2", "テーマ3", "テーマ4"]);
      });
      expect(mockGetPortfolioList).toHaveBeenLastCalledWith("user1", {}, 2);
      expect(screen.queryByRole("button", { name: "＋もっと見る" })).not.toBeInTheDocument();
    });

    it("最後のページの場合は「＋もっと見る」を表示しないこと", async () => {
      mockGetPortfolioList.mockResolvedValue(page(firstPage, true));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      });

      expect(screen.queryByRole("button", { name: "＋もっと見る" })).not.toBeInTheDocument();
    });

    it("続きがある間は、読み込み済みの最後の行を下へ移動できず、その理由が読み上げられること", async () => {
      mockGetPortfolioList.mockResolvedValue(page(firstPage, false));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      });

      const lastDown = screen.getByRole("button", { name: "テーマ2を下へ移動" });
      expect(lastDown).toBeDisabled();
      expect(lastDown).toHaveAccessibleDescription(
        "続きのポートフォリオと入れ替えるには、「＋もっと見る」で続きを表示してください"
      );
      expect(screen.getByRole("button", { name: "テーマ1を下へ移動" })).toBeEnabled();
    });

    it("追加読み込みでは、表示済みのポートフォリオを重複して追加しないこと", async () => {
      mockGetPortfolioList
        .mockResolvedValueOnce(page(firstPage, false))
        .mockResolvedValueOnce(page([portfolioOf(2), portfolioOf(3)], true));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      });

      fireEvent.click(screen.getByRole("button", { name: "＋もっと見る" }));

      await waitFor(() => {
        expect(themesInOrder()).toEqual(["テーマ1", "テーマ2", "テーマ3"]);
      });
    });

    it("追加読み込みに失敗した場合はエラーを表示し、読み込み済みの一覧は残すこと", async () => {
      mockGetPortfolioList
        .mockResolvedValueOnce(page(firstPage, false))
        .mockRejectedValueOnce(new Error("ポートフォリオ一覧の取得に失敗しました"));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      });

      fireEvent.click(screen.getByRole("button", { name: "＋もっと見る" }));

      expect(await screen.findByRole("alert")).toHaveTextContent(
        "ポートフォリオ一覧の取得に失敗しました"
      );
      expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      expect(screen.getByRole("button", { name: "＋もっと見る" })).toBeEnabled();
    });

    it("検索すると1ページ目から取得し直し、その条件のまま次のページを読み込むこと", async () => {
      mockGetPortfolioList
        .mockResolvedValueOnce(page(firstPage, false))
        .mockResolvedValueOnce(page([portfolioOf(1)], false))
        .mockResolvedValueOnce(page([portfolioOf(3)], true));

      render(<PortfolioList accountId="user1" />);
      await waitFor(() => {
        expect(screen.getAllByTestId("portfolio-row")).toHaveLength(2);
      });

      fireEvent.change(screen.getByLabelText("テーマ・タグ"), { target: { value: "テーマ" } });
      fireEvent.click(screen.getByRole("button", { name: "検索" }));
      await waitFor(() => {
        expect(themesInOrder()).toEqual(["テーマ1"]);
      });
      expect(mockGetPortfolioList).toHaveBeenLastCalledWith("user1", { keyword: "テーマ" });

      fireEvent.click(screen.getByRole("button", { name: "＋もっと見る" }));
      await waitFor(() => {
        expect(themesInOrder()).toEqual(["テーマ1", "テーマ3"]);
      });
      expect(mockGetPortfolioList).toHaveBeenLastCalledWith("user1", { keyword: "テーマ" }, 2);
    });
  });
});
