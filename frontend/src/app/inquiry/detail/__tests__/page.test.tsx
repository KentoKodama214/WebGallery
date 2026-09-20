import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import InquiryDetailPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));
jest.mock("@/lib/auth/AuthGuard", () => ({
  AuthGuard: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

const mockInquiryDetail = jest.fn();
jest.mock("../InquiryDetail", () => ({
  InquiryDetail: (props: { inquiryNo: number }) => {
    mockInquiryDetail(props);
    return <div data-testid="inquiry-detail" />;
  },
}));

function makeProps(searchParams: Record<string, string | string[] | undefined>) {
  return { searchParams: Promise.resolve(searchParams) };
}

describe("InquiryDetailPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("inquiryNoが正しい場合はInquiryDetailを表示すること", async () => {
    const ui = await InquiryDetailPage(makeProps({ inquiryNo: "3" }));
    render(ui);

    expect(screen.getByTestId("inquiry-detail")).toBeInTheDocument();
    expect(mockInquiryDetail).toHaveBeenCalledWith({ inquiryNo: 3 });
  });

  it.each([
    ["未指定", undefined],
    ["数値でない", "abc"],
    ["ゼロ", "0"],
    ["負数", "-1"],
    ["小数", "1.5"],
  ])("inquiryNoが%s場合は「お問い合わせが見つかりません」を表示すること", async (_label, value) => {
    const ui = await InquiryDetailPage(makeProps({ inquiryNo: value }));
    render(ui);

    expect(screen.getByText("お問い合わせが見つかりません")).toBeInTheDocument();
    expect(screen.queryByTestId("inquiry-detail")).not.toBeInTheDocument();
  });

  it("Header・Footerを表示すること", async () => {
    const ui = await InquiryDetailPage(makeProps({ inquiryNo: "3" }));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
