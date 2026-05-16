import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { AccountList } from "../AccountList";

// モック
const mockGetAccountList = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getAccountList: (...args: unknown[]) => mockGetAccountList(...args),
}));

describe("AccountList", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("読み込み中の表示がされること", () => {
    mockGetAccountList.mockReturnValue(new Promise(() => {}));

    render(<AccountList />);

    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
  });

  it("アカウント一覧が正しく表示されること", async () => {
    mockGetAccountList.mockResolvedValue([
      { accountId: "user1", accountName: "ユーザー1" },
      { accountId: "user2", accountName: "ユーザー2" },
    ]);

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
      expect(screen.getByText("ユーザー1")).toBeInTheDocument();
      expect(screen.getByText("user2")).toBeInTheDocument();
      expect(screen.getByText("ユーザー2")).toBeInTheDocument();
    });

    const links = screen.getAllByText("ギャラリーを見る");
    expect(links).toHaveLength(2);
    expect(links[0].closest("a")).toHaveAttribute(
      "href",
      "/photo/user1/photo_list"
    );
    expect(links[1].closest("a")).toHaveAttribute(
      "href",
      "/photo/user2/photo_list"
    );
  });

  it("アカウントが0件の場合はテーブルヘッダーのみ表示されること", async () => {
    mockGetAccountList.mockResolvedValue([]);

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText("ID")).toBeInTheDocument();
      expect(screen.getByText("アカウント名")).toBeInTheDocument();
      expect(screen.getByText("ギャラリー")).toBeInTheDocument();
    });

    expect(screen.queryByText("ギャラリーを見る")).not.toBeInTheDocument();
  });

  it("エラー時にエラーメッセージが表示されること", async () => {
    mockGetAccountList.mockRejectedValue(
      new Error("アカウント一覧の取得に失敗しました")
    );

    render(<AccountList />);

    await waitFor(() => {
      expect(
        screen.getByText("アカウント一覧の取得に失敗しました")
      ).toBeInTheDocument();
    });
  });
});
