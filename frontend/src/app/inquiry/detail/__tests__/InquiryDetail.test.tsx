import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { InquiryDetail } from "../InquiryDetail";

const mockGetInquiryDetail = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getInquiryDetail: (...args: unknown[]) => mockGetInquiryDetail(...args),
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
});
