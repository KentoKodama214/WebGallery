import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { AdminInquiryManagement } from "../AdminInquiryManagement";

const mockGetAdminInquiryList = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getAdminInquiryList: (...args: unknown[]) => mockGetAdminInquiryList(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const sampleInquiry = {
  inquiryId: 1,
  accountId: "user1",
  accountName: "ユーザー1",
  subject: "写真が表示されない",
  statusKbn: "unreplied" as const,
  createdAt: "2026-01-01T00:00:00+09:00",
};

describe("AdminInquiryManagement", () => {
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

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("管理者権限がありません。")).toBeInTheDocument();
    });
    expect(mockGetAdminInquiryList).not.toHaveBeenCalled();
  });

  it("一覧が表示されること", async () => {
    mockGetAdminInquiryList.mockResolvedValue({ isLast: true, inquiryList: [sampleInquiry] });

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });
    expect(screen.getByText("user1")).toBeInTheDocument();
    expect(mockGetAdminInquiryList).toHaveBeenCalledWith(1, undefined);
  });

  it("ステータスフィルタを変更すると絞り込み条件付きで再取得されること", async () => {
    mockGetAdminInquiryList.mockResolvedValue({ isLast: true, inquiryList: [sampleInquiry] });

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText("ステータス"), {
      target: { value: "unreplied" },
    });

    await waitFor(() => {
      expect(mockGetAdminInquiryList).toHaveBeenCalledWith(1, "unreplied");
    });
  });

  it("isLastがfalseのとき「もっと見る」ボタンが表示されること", async () => {
    mockGetAdminInquiryList.mockResolvedValue({ isLast: false, inquiryList: [sampleInquiry] });

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });
  });
});
