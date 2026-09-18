import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { AdminInquiryDetail } from "../AdminInquiryDetail";

const mockGetAdminInquiryDetail = jest.fn();
const mockReplyToInquiry = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getAdminInquiryDetail: (...args: unknown[]) => mockGetAdminInquiryDetail(...args),
  replyToInquiry: (...args: unknown[]) => mockReplyToInquiry(...args),
}));

const mockUseAuth = jest.fn();

jest.mock("@/lib/auth/AuthProvider", () => ({
  useAuth: () => mockUseAuth(),
}));

const sampleDetail = {
  inquiryId: 1,
  accountId: "user1",
  accountName: "ユーザー1",
  subject: "写真が表示されない",
  body: "アップロードした写真が表示されません。",
  statusKbn: "unreplied" as const,
  createdAt: "2026-01-01T00:00:00+09:00",
  replyList: [],
};

describe("AdminInquiryDetail", () => {
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

    render(<AdminInquiryDetail inquiryId={1} />);

    await waitFor(() => {
      expect(screen.getByText("管理者権限がありません。")).toBeInTheDocument();
    });
    expect(mockGetAdminInquiryDetail).not.toHaveBeenCalled();
  });

  it("お問い合わせの詳細が表示されること", async () => {
    mockGetAdminInquiryDetail.mockResolvedValue(sampleDetail);

    render(<AdminInquiryDetail inquiryId={1} />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });
    expect(screen.getByText(/user1/)).toBeInTheDocument();
    expect(mockGetAdminInquiryDetail).toHaveBeenCalledWith(1);
  });

  it("返信を空欄で送信するとエラーが表示され、APIが呼ばれないこと", async () => {
    mockGetAdminInquiryDetail.mockResolvedValue(sampleDetail);

    render(<AdminInquiryDetail inquiryId={1} />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: "返信を送信" }));

    await waitFor(() => {
      expect(screen.getByText("返信内容を入力してください")).toBeInTheDocument();
    });
    expect(mockReplyToInquiry).not.toHaveBeenCalled();
  });

  it("返信を入力して送信すると返信APIが呼ばれ、詳細が再取得されること", async () => {
    mockGetAdminInquiryDetail.mockResolvedValueOnce(sampleDetail).mockResolvedValueOnce({
      ...sampleDetail,
      statusKbn: "replied",
      replyList: [
        { replyNo: 1, body: "調査いたします。", createdAt: "2026-01-02T00:00:00+09:00" },
      ],
    });
    mockReplyToInquiry.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "返信を登録しました。",
      replyNo: 1,
    });

    render(<AdminInquiryDetail inquiryId={1} />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText("返信する"), {
      target: { value: "調査いたします。" },
    });
    fireEvent.click(screen.getByRole("button", { name: "返信を送信" }));

    await waitFor(() => {
      expect(mockReplyToInquiry).toHaveBeenCalledWith(1, "調査いたします。");
      expect(screen.getByText("返信を登録しました。")).toBeInTheDocument();
    });
    expect(mockGetAdminInquiryDetail).toHaveBeenCalledTimes(2);
  });

  it("返信登録に失敗した場合はエラーメッセージが表示されること", async () => {
    mockGetAdminInquiryDetail.mockResolvedValue(sampleDetail);
    mockReplyToInquiry.mockRejectedValue(new Error("返信の登録に失敗しました"));

    render(<AdminInquiryDetail inquiryId={1} />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText("返信する"), {
      target: { value: "調査いたします。" },
    });
    fireEvent.click(screen.getByRole("button", { name: "返信を送信" }));

    await waitFor(() => {
      expect(screen.getByText("返信の登録に失敗しました")).toBeInTheDocument();
    });
  });

  it("取り下げ済みの場合は返信フォームが表示されないこと", async () => {
    mockGetAdminInquiryDetail.mockResolvedValue({ ...sampleDetail, statusKbn: "withdrawn" });

    render(<AdminInquiryDetail inquiryId={1} />);

    await waitFor(() => {
      expect(screen.getByText("取り下げ")).toBeInTheDocument();
    });
    expect(
      screen.getByText("このお問い合わせは取り下げられているため、返信できません。")
    ).toBeInTheDocument();
    expect(screen.queryByLabelText("返信する")).not.toBeInTheDocument();
  });
});
