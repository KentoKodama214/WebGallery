import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { InquiryList } from "../InquiryList";

const mockGetInquiryList = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getInquiryList: (...args: unknown[]) => mockGetInquiryList(...args),
}));

const sampleInquiry = {
  inquiryNo: 1,
  subject: "写真が表示されない",
  statusKbn: "unreplied" as const,
  isReadByUser: true,
  createdAt: "2026-01-01T00:00:00+09:00",
};

describe("InquiryList", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("お問い合わせが0件の場合はその旨が表示されること", async () => {
    mockGetInquiryList.mockResolvedValue({ isLast: true, inquiryList: [] });

    render(<InquiryList />);

    await waitFor(() => {
      expect(screen.getByText("お問い合わせはありません")).toBeInTheDocument();
    });
  });

  it("一覧が表示され、未対応ステータスが表示されること", async () => {
    mockGetInquiryList.mockResolvedValue({ isLast: true, inquiryList: [sampleInquiry] });

    render(<InquiryList />);

    await waitFor(() => {
      expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    });
    expect(screen.getByText("未対応")).toBeInTheDocument();
  });

  it("返信済みかつ未読の場合は未読バッジが表示されること", async () => {
    mockGetInquiryList.mockResolvedValue({
      isLast: true,
      inquiryList: [{ ...sampleInquiry, statusKbn: "replied", isReadByUser: false }],
    });

    render(<InquiryList />);

    await waitFor(() => {
      expect(screen.getByText("回答あり")).toBeInTheDocument();
    });
    expect(screen.getByText("未読")).toBeInTheDocument();
  });

  it("取り下げステータスが表示されること", async () => {
    mockGetInquiryList.mockResolvedValue({
      isLast: true,
      inquiryList: [{ ...sampleInquiry, statusKbn: "withdrawn" as const }],
    });

    render(<InquiryList />);

    await waitFor(() => {
      expect(screen.getByText("取り下げ")).toBeInTheDocument();
    });
  });

  it("isLastがfalseのとき「もっと見る」ボタンが表示され、クリックで追加取得されること", async () => {
    mockGetInquiryList
      .mockResolvedValueOnce({ isLast: false, inquiryList: [sampleInquiry] })
      .mockResolvedValueOnce({
        isLast: true,
        inquiryList: [{ ...sampleInquiry, inquiryNo: 2, subject: "その他のお問い合わせ" }],
      });

    render(<InquiryList />);

    await waitFor(() => {
      expect(screen.getByText("＋もっと見る")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("＋もっと見る"));

    await waitFor(() => {
      expect(screen.getByText("その他のお問い合わせ")).toBeInTheDocument();
    });
    expect(screen.getByText("写真が表示されない")).toBeInTheDocument();
    expect(mockGetInquiryList).toHaveBeenNthCalledWith(1, 1);
    expect(mockGetInquiryList).toHaveBeenNthCalledWith(2, 2);
  });

  it("一覧取得に失敗した場合はエラーメッセージと再読み込みボタンが表示されること", async () => {
    mockGetInquiryList.mockRejectedValue(new Error("お問い合わせ一覧の取得に失敗しました"));

    render(<InquiryList />);

    await waitFor(() => {
      expect(screen.getByText("お問い合わせ一覧の取得に失敗しました")).toBeInTheDocument();
    });
    expect(screen.getByRole("button", { name: "再読み込み" })).toBeInTheDocument();
  });
});
