import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { InquiryDetail } from "../InquiryDetail";

const mockGetInquiryDetail = jest.fn();
const mockWithdrawInquiry = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getInquiryDetail: (...args: unknown[]) => mockGetInquiryDetail(...args),
  withdrawInquiry: (...args: unknown[]) => mockWithdrawInquiry(...args),
}));

const sampleDetail = {
  inquiryNo: 1,
  subject: "写真が表示されない",
  body: "アップロードした写真が表示されません。",
  statusKbn: "unreplied" as const,
  createdAt: "2026-01-01T00:00:00+09:00",
  replyList: [],
};

describe("InquiryDetail", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("お問い合わせの件名・本文が表示されること", async () => {
    mockGetInquiryDetail.mockResolvedValue(sampleDetail);

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });
    expect(screen.getByText("アップロードした写真が表示されません。")).toBeInTheDocument();
    expect(mockGetInquiryDetail).toHaveBeenCalledWith(1);
  });

  it("返信がある場合は返信一覧が表示されること", async () => {
    mockGetInquiryDetail.mockResolvedValue({
      ...sampleDetail,
      statusKbn: "replied",
      replyList: [
        { replyNo: 1, body: "ご報告ありがとうございます。調査いたします。", createdAt: "2026-01-02T00:00:00+09:00" },
      ],
    });

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("ご報告ありがとうございます。調査いたします。")).toBeInTheDocument();
    });
    expect(screen.getByText("回答済み")).toBeInTheDocument();
  });

  it("取得に失敗した場合はエラーメッセージが表示されること", async () => {
    mockGetInquiryDetail.mockRejectedValue(new Error("お問い合わせ詳細の取得に失敗しました"));

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("お問い合わせ詳細の取得に失敗しました")).toBeInTheDocument();
    });
  });

  it("取り下げ済みの場合は取り下げボタンが表示されないこと", async () => {
    mockGetInquiryDetail.mockResolvedValue({ ...sampleDetail, statusKbn: "withdrawn" });

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("取り下げ")).toBeInTheDocument();
    });
    expect(screen.queryByText("このお問い合わせを取り下げる")).not.toBeInTheDocument();
  });

  it("取り下げるボタン押下で確認ダイアログが表示され、確認すると取り下げが実行されること", async () => {
    mockGetInquiryDetail.mockResolvedValue(sampleDetail);
    mockWithdrawInquiry.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "お問い合わせを取り下げました。",
    });

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("このお問い合わせを取り下げる")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("このお問い合わせを取り下げる"));

    expect(screen.getByTestId("withdraw-confirm-dialog")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "取り下げる" }));

    await waitFor(() => {
      expect(mockWithdrawInquiry).toHaveBeenCalledWith(1);
    });
    await waitFor(() => {
      expect(screen.queryByTestId("withdraw-confirm-dialog")).not.toBeInTheDocument();
    });
    expect(screen.getByText("取り下げ")).toBeInTheDocument();
  });

  it("確認ダイアログでキャンセルすると取り下げが実行されないこと", async () => {
    mockGetInquiryDetail.mockResolvedValue(sampleDetail);

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("このお問い合わせを取り下げる")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("このお問い合わせを取り下げる"));
    fireEvent.click(screen.getByRole("button", { name: "キャンセル" }));

    expect(screen.queryByTestId("withdraw-confirm-dialog")).not.toBeInTheDocument();
    expect(mockWithdrawInquiry).not.toHaveBeenCalled();
  });

  it("確認ダイアログはEscapeキーでも閉じられること", async () => {
    mockGetInquiryDetail.mockResolvedValue(sampleDetail);

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("このお問い合わせを取り下げる")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("このお問い合わせを取り下げる"));
    expect(screen.getByTestId("withdraw-confirm-dialog")).toBeInTheDocument();

    fireEvent.keyDown(screen.getByRole("dialog"), { key: "Escape" });

    await waitFor(() => {
      expect(screen.queryByTestId("withdraw-confirm-dialog")).not.toBeInTheDocument();
    });
    expect(mockWithdrawInquiry).not.toHaveBeenCalled();
  });

  it("取り下げに失敗した場合はダイアログ内にエラーメッセージが表示されること", async () => {
    mockGetInquiryDetail.mockResolvedValue(sampleDetail);
    mockWithdrawInquiry.mockRejectedValue(new Error("お問い合わせの取り下げに失敗しました"));

    render(<InquiryDetail inquiryNo={1} />);

    await waitFor(() => {
      expect(screen.getByText("このお問い合わせを取り下げる")).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText("このお問い合わせを取り下げる"));
    fireEvent.click(screen.getByRole("button", { name: "取り下げる" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("お問い合わせの取り下げに失敗しました");
    });
    expect(screen.getByTestId("withdraw-confirm-dialog")).toBeInTheDocument();
  });
});
