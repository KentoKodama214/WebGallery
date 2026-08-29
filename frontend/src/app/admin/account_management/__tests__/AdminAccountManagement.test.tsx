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
});
