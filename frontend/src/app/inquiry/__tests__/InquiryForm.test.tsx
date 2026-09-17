import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { InquiryForm } from "../InquiryForm";

const mockRegisterInquiry = jest.fn();

jest.mock("@/lib/api/client", () => ({
  registerInquiry: (...args: unknown[]) => mockRegisterInquiry(...args),
}));

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

describe("InquiryForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("件名・本文が未入力の場合は送信されずエラーが表示されること", async () => {
    render(<InquiryForm />);

    fireEvent.click(screen.getByRole("button", { name: "送信" }));

    await waitFor(() => {
      expect(screen.getByText("件名を入力してください")).toBeInTheDocument();
    });
    expect(screen.getByText("本文を入力してください")).toBeInTheDocument();
    expect(mockRegisterInquiry).not.toHaveBeenCalled();
  });

  it("件名・本文を入力して送信すると登録APIが呼ばれ完了ダイアログが表示されること", async () => {
    mockRegisterInquiry.mockResolvedValue({
      httpStatus: 200,
      isSuccess: true,
      message: "お問い合わせを受け付けました。",
      inquiryNo: 1,
    });

    render(<InquiryForm />);

    fireEvent.change(screen.getByLabelText("件名"), {
      target: { value: "写真が表示されない" },
    });
    fireEvent.change(screen.getByLabelText("本文"), {
      target: { value: "アップロードした写真が表示されません。" },
    });
    fireEvent.click(screen.getByRole("button", { name: "送信" }));

    await waitFor(() => {
      expect(mockRegisterInquiry).toHaveBeenCalledWith({
        subject: "写真が表示されない",
        body: "アップロードした写真が表示されません。",
      });
    });
    expect(screen.getByText("お問い合わせを受け付けました")).toBeInTheDocument();
  });

  it("登録に失敗した場合はエラーメッセージが表示されること", async () => {
    mockRegisterInquiry.mockRejectedValue(new Error("お問い合わせの登録に失敗しました"));

    render(<InquiryForm />);

    fireEvent.change(screen.getByLabelText("件名"), {
      target: { value: "写真が表示されない" },
    });
    fireEvent.change(screen.getByLabelText("本文"), {
      target: { value: "アップロードした写真が表示されません。" },
    });
    fireEvent.click(screen.getByRole("button", { name: "送信" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("お問い合わせの登録に失敗しました");
    });
  });

  it("件名が上限文字数を超える場合は送信されずエラーが表示されること", async () => {
    render(<InquiryForm />);

    fireEvent.change(screen.getByLabelText("件名"), {
      target: { value: "あ".repeat(101) },
    });
    fireEvent.change(screen.getByLabelText("本文"), {
      target: { value: "本文" },
    });
    fireEvent.click(screen.getByRole("button", { name: "送信" }));

    await waitFor(() => {
      expect(screen.getByText("件名は100文字以内で入力してください")).toBeInTheDocument();
    });
    expect(mockRegisterInquiry).not.toHaveBeenCalled();
  });
});
