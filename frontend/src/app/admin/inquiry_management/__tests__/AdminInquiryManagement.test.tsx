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

  it("取り下げステータスで絞り込めること", async () => {
    mockGetAdminInquiryList.mockResolvedValue({
      isLast: true,
      inquiryList: [{ ...sampleInquiry, statusKbn: "withdrawn" as const }],
    });

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText("ステータス"), {
      target: { value: "withdrawn" },
    });

    await waitFor(() => {
      expect(mockGetAdminInquiryList).toHaveBeenCalledWith(1, "withdrawn");
    });
    expect(screen.getAllByText("取り下げ").length).toBeGreaterThan(0);
  });

  it("isLastがfalseのとき「もっと見る」ボタンが表示されること", async () => {
    mockGetAdminInquiryList.mockResolvedValue({ isLast: false, inquiryList: [sampleInquiry] });

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });
  });

  it("認証確認中は読み込み中と表示され、一覧取得は行われないこと", () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      user: null,
      isLoading: true,
      login: jest.fn(),
      logout: jest.fn(),
    });

    render(<AdminInquiryManagement />);

    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
    expect(mockGetAdminInquiryList).not.toHaveBeenCalled();
  });

  it("一覧取得に失敗した場合はエラーメッセージが表示され、再読み込みで再取得されること", async () => {
    mockGetAdminInquiryList.mockRejectedValueOnce(new Error("サーバーエラー"));

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("サーバーエラー")).toBeInTheDocument();
    });

    mockGetAdminInquiryList.mockResolvedValueOnce({
      isLast: true,
      inquiryList: [sampleInquiry],
    });
    fireEvent.click(screen.getByRole("button", { name: "再読み込み" }));

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });
    expect(mockGetAdminInquiryList).toHaveBeenCalledTimes(2);
  });

  it("一覧取得失敗時にErrorインスタンスでない例外は既定メッセージになること", async () => {
    mockGetAdminInquiryList.mockRejectedValueOnce("network down");

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("エラーが発生しました")).toBeInTheDocument();
    });
  });

  it("「もっと見る」で次ページを取得し、一覧に追記されること", async () => {
    mockGetAdminInquiryList.mockResolvedValueOnce({
      isLast: false,
      inquiryList: [sampleInquiry],
    });

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });

    const secondInquiry = {
      ...sampleInquiry,
      inquiryId: 2,
      accountId: "user2",
      subject: "お気に入りが解除できない",
    };
    mockGetAdminInquiryList.mockResolvedValueOnce({
      isLast: true,
      inquiryList: [secondInquiry],
    });

    fireEvent.click(screen.getByTestId("show-more-button"));

    await waitFor(() => {
      expect(screen.getByText("お気に入りが解除できない")).toBeInTheDocument();
    });
    expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    expect(mockGetAdminInquiryList).toHaveBeenLastCalledWith(2, undefined);
    expect(screen.queryByTestId("show-more-button")).not.toBeInTheDocument();
  });

  it("「もっと見る」が失敗した場合はエラーメッセージを表示し、一覧は保持されること", async () => {
    mockGetAdminInquiryList.mockResolvedValueOnce({
      isLast: false,
      inquiryList: [sampleInquiry],
    });

    render(<AdminInquiryManagement />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });

    mockGetAdminInquiryList.mockRejectedValueOnce(new Error("追加取得に失敗しました"));
    fireEvent.click(screen.getByTestId("show-more-button"));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("追加取得に失敗しました");
    });
    expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
  });
});
