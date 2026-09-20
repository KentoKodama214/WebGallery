import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { AdminAccountManagement } from "../AdminAccountManagement";

// モック
const mockGetAdminAccountList = jest.fn();
const mockUnlockAccount = jest.fn();
const mockLockAccount = jest.fn();
const mockUpdateAccountAuthority = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getAdminAccountList: (...args: unknown[]) => mockGetAdminAccountList(...args),
  unlockAccount: (...args: unknown[]) => mockUnlockAccount(...args),
  lockAccount: (...args: unknown[]) => mockLockAccount(...args),
  updateAccountAuthority: (...args: unknown[]) => mockUpdateAccountAuthority(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const sampleAccount = {
  accountNo: 1,
  accountId: "user1",
  accountName: "ユーザー1",
  authorityKbn: "normal-user",
  isDeleted: false,
  lastLoginDatetime: null,
  loginFailureCount: 0,
};

describe("AdminAccountManagement", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "admin1", role: "ROLE_ADMIN" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
  });

  it("管理者権限がない場合はエラーメッセージが表示されること", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "user1", role: "ROLE_USER" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("管理者権限がありません。")).toBeInTheDocument();
    });
    expect(mockGetAdminAccountList).not.toHaveBeenCalled();
  });

  it("isLastがfalseのとき「もっと見る」ボタンが表示されること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: false,
      accountList: [sampleAccount],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });
  });

  it("isLastがtrueのとき「もっと見る」ボタンが表示されないこと", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [sampleAccount],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    expect(screen.queryByText("＋もっと見る")).not.toBeInTheDocument();
  });

  it("「もっと見る」ボタンをクリックすると追加のアカウントが読み込まれること", async () => {
    mockGetAdminAccountList
      .mockResolvedValueOnce({
        isLast: false,
        accountList: [sampleAccount],
      })
      .mockResolvedValueOnce({
        isLast: true,
        accountList: [
          { ...sampleAccount, accountNo: 2, accountId: "user2", accountName: "ユーザー2" },
        ],
      });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("＋もっと見る"));

    await waitFor(() => {
      expect(screen.getByText("user2")).toBeInTheDocument();
    });

    expect(screen.getByText("user1")).toBeInTheDocument();
    expect(mockGetAdminAccountList).toHaveBeenNthCalledWith(1, 1);
    expect(mockGetAdminAccountList).toHaveBeenNthCalledWith(2, 2);
    expect(screen.queryByText("＋もっと見る")).not.toBeInTheDocument();
  });

  it("強制ロックは確認ダイアログを経てAPIが呼ばれること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ ...sampleAccount, loginFailureCount: 2 }],
    });
    mockLockAccount.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "ロックしました",
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "強制ロック" }));

    // ネイティブconfirmではなくダイアログが表示される
    expect(screen.getByTestId("lock-confirm-dialog")).toBeInTheDocument();
    expect(mockLockAccount).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole("button", { name: "実行" }));

    await waitFor(() => {
      expect(mockLockAccount).toHaveBeenCalledWith(1);
      expect(screen.getByText("ロックしました")).toBeInTheDocument();
    });
    expect(screen.queryByTestId("lock-confirm-dialog")).not.toBeInTheDocument();
  });

  it("ロック操作が失敗しても一覧は維持され、操作失敗の通知のみ表示されること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ ...sampleAccount, loginFailureCount: 2 }],
    });
    mockLockAccount.mockRejectedValue(new Error("ロックに失敗しました"));

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "強制ロック" }));
    fireEvent.click(screen.getByRole("button", { name: "実行" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("ロックに失敗しました");
    });

    // 一覧取得の全体エラー画面（再読み込みボタン）ではなく、一覧が維持されていること
    expect(screen.getByText("user1")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "再読み込み" })).not.toBeInTheDocument();
  });

  it("確認ダイアログをキャンセルするとAPIは呼ばれないこと", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ ...sampleAccount, loginFailureCount: 2 }],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "ロック解除" }));
    expect(screen.getByTestId("lock-confirm-dialog")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "キャンセル" }));

    expect(screen.queryByTestId("lock-confirm-dialog")).not.toBeInTheDocument();
    expect(mockUnlockAccount).not.toHaveBeenCalled();
  });

  it("ログイン失敗回数がしきい値（3）以上のアカウントはロック中と表示され強制ロックボタンが無効になること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ ...sampleAccount, loginFailureCount: 3 }],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    expect(screen.getByText("ロック中")).toBeInTheDocument();
    expect(screen.queryByText("有効")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "強制ロック" })).toBeDisabled();
    // ロック解除は失敗回数 > 0 なら可能
    expect(screen.getByRole("button", { name: "ロック解除" })).toBeEnabled();
  });

  it("ログイン失敗回数がしきい値未満のアカウントは有効と表示されること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ ...sampleAccount, loginFailureCount: 2 }],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    expect(screen.getByText("有効")).toBeInTheDocument();
    expect(screen.queryByText("ロック中")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "強制ロック" })).toBeEnabled();
  });

  it("自身の管理者アカウントの行には権限「編集」ボタンが表示されないこと", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      user: { accountId: "admin1", accountNo: 1, role: "ROLE_ADMIN" },
      isLoading: false,
      login: jest.fn(),
      logout: jest.fn(),
    });
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [
        sampleAccount,
        { ...sampleAccount, accountNo: 2, accountId: "user2", accountName: "ユーザー2" },
      ],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user2")).toBeInTheDocument();
    });

    // 自身の行（accountNo=1）には編集ボタンがない、他者の行（accountNo=2）にはある
    expect(screen.getAllByRole("button", { name: "編集" })).toHaveLength(1);
  });

  it("権限編集ダイアログで権限を選択し「登録」を押すとAPIが呼ばれ一覧が再取得されること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [sampleAccount],
    });
    mockUpdateAccountAuthority.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "権限を変更しました",
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "編集" }));
    expect(screen.getByTestId("authority-edit-dialog")).toBeInTheDocument();
    expect(mockUpdateAccountAuthority).not.toHaveBeenCalled();

    fireEvent.change(screen.getByRole("combobox"), {
      target: { value: "administrator" },
    });
    fireEvent.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(mockUpdateAccountAuthority).toHaveBeenCalledWith(1, "administrator");
      expect(screen.getByText("権限を変更しました")).toBeInTheDocument();
    });
    expect(screen.queryByTestId("authority-edit-dialog")).not.toBeInTheDocument();
    expect(mockGetAdminAccountList).toHaveBeenCalledTimes(2);
  });

  it("権限変更に失敗しても一覧は維持され、操作失敗の通知のみ表示されること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [sampleAccount],
    });
    mockUpdateAccountAuthority.mockRejectedValue(new Error("権限の変更に失敗しました"));

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "編集" }));
    fireEvent.click(screen.getByRole("button", { name: "登録" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("権限の変更に失敗しました");
    });

    expect(screen.getByText("user1")).toBeInTheDocument();
    expect(screen.queryByTestId("authority-edit-dialog")).not.toBeInTheDocument();
  });

  it("権限編集ダイアログをキャンセルするとAPIは呼ばれないこと", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [sampleAccount],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "編集" }));
    expect(screen.getByTestId("authority-edit-dialog")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "キャンセル" }));

    expect(screen.queryByTestId("authority-edit-dialog")).not.toBeInTheDocument();
    expect(mockUpdateAccountAuthority).not.toHaveBeenCalled();
  });

  it("認証確認中は読み込み中と表示され、一覧取得は行われないこと", () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      isLoading: true,
      login: jest.fn(),
      logout: jest.fn(),
    });

    render(<AdminAccountManagement />);

    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
    expect(mockGetAdminAccountList).not.toHaveBeenCalled();
  });

  it("初回の一覧取得に失敗した場合はエラー画面が表示され、再読み込みで復帰すること", async () => {
    mockGetAdminAccountList.mockRejectedValueOnce(
      new Error("アカウント一覧の取得に失敗しました")
    );

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(
        screen.getByText("アカウント一覧の取得に失敗しました")
      ).toBeInTheDocument();
    });

    mockGetAdminAccountList.mockResolvedValueOnce({
      isLast: true,
      accountList: [sampleAccount],
    });
    fireEvent.click(screen.getByRole("button", { name: "再読み込み" }));

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });
  });

  it("「もっと見る」で取得に失敗した場合、一覧を維持したまま通知すること", async () => {
    mockGetAdminAccountList
      .mockResolvedValueOnce({ isLast: false, accountList: [sampleAccount] })
      .mockRejectedValueOnce(new Error("追加取得に失敗しました"));

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("＋もっと見る"));

    await waitFor(() => {
      expect(screen.getByText("追加取得に失敗しました")).toBeInTheDocument();
    });
    expect(screen.getByText("user1")).toBeInTheDocument();
  });

  it("ロック操作は成功しても、その後の一覧再取得が失敗した場合はエラー画面になること", async () => {
    mockGetAdminAccountList
      .mockResolvedValueOnce({
        isLast: true,
        accountList: [{ ...sampleAccount, loginFailureCount: 2 }],
      })
      .mockRejectedValueOnce(new Error("アカウント一覧の取得に失敗しました"));
    mockLockAccount.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "ロックしました",
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "強制ロック" }));
    fireEvent.click(screen.getByRole("button", { name: "実行" }));

    await waitFor(() => {
      expect(
        screen.getByText("アカウント一覧の取得に失敗しました")
      ).toBeInTheDocument();
    });
    expect(screen.getByRole("button", { name: "再読み込み" })).toBeInTheDocument();
  });

  it("最終ログイン日時が設定されている場合は日本語ロケールで表示されること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [
        { ...sampleAccount, lastLoginDatetime: "2024-05-01T12:00:00+09:00" },
      ],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    const expected = new Date("2024-05-01T12:00:00+09:00").toLocaleString(
      "ja-JP"
    );
    expect(screen.getByText(expected)).toBeInTheDocument();
  });

  it("行にマウスを乗せる/離すと背景色が切り替わること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [sampleAccount],
    });

    render(<AdminAccountManagement />);

    const row = await screen.findByText("user1").then((el) => el.closest("tr")!);

    fireEvent.mouseEnter(row);
    expect(row).toHaveStyle({ backgroundColor: "#fffae9" });

    fireEvent.mouseLeave(row);
    expect(row).toHaveStyle({ backgroundColor: "rgb(255, 255, 255)" });
  });

  it("ロック確認ダイアログはEscapeキーで閉じること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [{ ...sampleAccount, loginFailureCount: 2 }],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "強制ロック" }));
    expect(screen.getByTestId("lock-confirm-dialog")).toBeInTheDocument();

    fireEvent.keyDown(screen.getByRole("dialog"), { key: "Escape" });

    expect(screen.queryByTestId("lock-confirm-dialog")).not.toBeInTheDocument();
    expect(mockLockAccount).not.toHaveBeenCalled();
  });

  it("権限編集ダイアログはEscapeキーで閉じること", async () => {
    mockGetAdminAccountList.mockResolvedValue({
      isLast: true,
      accountList: [sampleAccount],
    });

    render(<AdminAccountManagement />);

    await waitFor(() => {
      expect(screen.getByText("user1")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "編集" }));
    expect(screen.getByTestId("authority-edit-dialog")).toBeInTheDocument();

    fireEvent.keyDown(screen.getByRole("dialog"), { key: "Escape" });

    expect(screen.queryByTestId("authority-edit-dialog")).not.toBeInTheDocument();
    expect(mockUpdateAccountAuthority).not.toHaveBeenCalled();
  });
});
