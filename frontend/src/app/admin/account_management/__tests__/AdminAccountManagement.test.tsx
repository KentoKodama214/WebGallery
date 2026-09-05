import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { AdminAccountManagement } from "../AdminAccountManagement";

// モック
const mockGetAdminAccountList = jest.fn();
const mockUnlockAccount = jest.fn();
const mockLockAccount = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getAdminAccountList: (...args: unknown[]) => mockGetAdminAccountList(...args),
  unlockAccount: (...args: unknown[]) => mockUnlockAccount(...args),
  lockAccount: (...args: unknown[]) => mockLockAccount(...args),
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
});
