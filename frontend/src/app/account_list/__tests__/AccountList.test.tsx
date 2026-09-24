import { fireEvent, render, screen, waitFor } from "@testing-library/react";
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
    mockGetAccountList.mockResolvedValue({
      isLast: true,
      accountList: [
        { accountId: "user1", accountName: "ユーザー1" },
        { accountId: "user2", accountName: "ユーザー2" },
      ],
    });

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText("ユーザー1")).toBeInTheDocument();
      expect(screen.getByText("ユーザー2")).toBeInTheDocument();
    });
    // アカウントID（ログインID）は一覧に表示しない（他ユーザーの列挙防止）
    expect(screen.queryByText("user1")).not.toBeInTheDocument();
    expect(screen.queryByText("user2")).not.toBeInTheDocument();

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
    mockGetAccountList.mockResolvedValue({ isLast: true, accountList: [] });

    render(<AccountList />);

    await waitFor(() => {
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

  it("isLastがfalseのとき「もっと見る」ボタンが表示されること", async () => {
    mockGetAccountList.mockResolvedValue({
      isLast: false,
      accountList: [{ accountId: "user1", accountName: "ユーザー1" }],
    });

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });
  });

  it("isLastがtrueのとき「もっと見る」ボタンが表示されないこと", async () => {
    mockGetAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ accountId: "user1", accountName: "ユーザー1" }],
    });

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText("ユーザー1")).toBeInTheDocument();
    });

    expect(screen.queryByText("＋もっと見る")).not.toBeInTheDocument();
  });

  it("「もっと見る」ボタンをクリックすると追加のアカウントが読み込まれること", async () => {
    mockGetAccountList
      .mockResolvedValueOnce({
        isLast: false,
        accountList: [{ accountId: "user1", accountName: "ユーザー1" }],
      })
      .mockResolvedValueOnce({
        isLast: true,
        accountList: [{ accountId: "user2", accountName: "ユーザー2" }],
      });

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("＋もっと見る"));

    await waitFor(() => {
      expect(screen.getByText("ユーザー2")).toBeInTheDocument();
    });

    expect(screen.getByText("ユーザー1")).toBeInTheDocument();
    expect(mockGetAccountList).toHaveBeenNthCalledWith(1, 1);
    expect(mockGetAccountList).toHaveBeenNthCalledWith(2, 2);
    expect(screen.queryByText("＋もっと見る")).not.toBeInTheDocument();
  });

  it("エラー画面で「再読み込み」ボタンを押すと一覧が再取得されること", async () => {
    mockGetAccountList.mockRejectedValueOnce(
      new Error("アカウント一覧の取得に失敗しました")
    );

    render(<AccountList />);

    await waitFor(() => {
      expect(
        screen.getByText("アカウント一覧の取得に失敗しました")
      ).toBeInTheDocument();
    });

    mockGetAccountList.mockResolvedValueOnce({
      isLast: true,
      accountList: [{ accountId: "user1", accountName: "ユーザー1" }],
    });

    fireEvent.click(screen.getByText("再読み込み"));

    await waitFor(() => {
      expect(screen.getByText("ユーザー1")).toBeInTheDocument();
    });
    expect(
      screen.queryByText("アカウント一覧の取得に失敗しました")
    ).not.toBeInTheDocument();
    expect(mockGetAccountList).toHaveBeenNthCalledWith(1, 1);
    expect(mockGetAccountList).toHaveBeenNthCalledWith(2, 1);
  });

  it("「もっと見る」で取得に失敗した場合、既存の一覧を維持したままエラーを通知すること", async () => {
    mockGetAccountList
      .mockResolvedValueOnce({
        isLast: false,
        accountList: [{ accountId: "user1", accountName: "ユーザー1" }],
      })
      .mockRejectedValueOnce(new Error("追加取得に失敗しました"));

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("＋もっと見る"));

    await waitFor(() => {
      expect(screen.getByText("追加取得に失敗しました")).toBeInTheDocument();
    });
    // 取得済みの一覧は維持され、ボタンも再度表示される
    expect(screen.getByText("ユーザー1")).toBeInTheDocument();
    expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
  });

  it("行にマウスを乗せる/離すと背景色が切り替わること", async () => {
    mockGetAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ accountId: "user1", accountName: "ユーザー1" }],
    });

    render(<AccountList />);

    const row = await screen.findByText("ユーザー1").then((el) => el.closest("tr")!);

    fireEvent.mouseEnter(row);
    expect(row).toHaveStyle({ backgroundColor: "#fffae9" });

    fireEvent.mouseLeave(row);
    expect(row).toHaveStyle({ backgroundColor: "rgb(255, 255, 255)" });
  });
});
