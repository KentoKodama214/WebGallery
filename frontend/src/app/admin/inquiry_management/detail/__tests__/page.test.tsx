import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import AdminInquiryDetailPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));

const mockAdminInquiryDetail = jest.fn();
jest.mock("../AdminInquiryDetail", () => ({
  AdminInquiryDetail: (props: { inquiryId: number }) => {
    mockAdminInquiryDetail(props);
    return <div data-testid="admin-inquiry-detail" />;
  },
}));

function makeProps(searchParams: Record<string, string | string[] | undefined>) {
  return { searchParams: Promise.resolve(searchParams) };
}

describe("AdminInquiryDetailPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("inquiryIdが正しい場合はAdminInquiryDetailを表示すること", async () => {
    const ui = await AdminInquiryDetailPage(makeProps({ inquiryId: "7" }));
    render(ui);

    expect(screen.getByTestId("admin-inquiry-detail")).toBeInTheDocument();
    expect(mockAdminInquiryDetail).toHaveBeenCalledWith({ inquiryId: 7 });
  });

  it.each([
    ["未指定", undefined],
    ["数値でない", "abc"],
    ["ゼロ", "0"],
    ["負数", "-1"],
  ])("inquiryIdが%s場合は「お問い合わせが見つかりません」を表示すること", async (_label, value) => {
    const ui = await AdminInquiryDetailPage(makeProps({ inquiryId: value }));
    render(ui);

    expect(screen.getByText("お問い合わせが見つかりません")).toBeInTheDocument();
    expect(screen.queryByTestId("admin-inquiry-detail")).not.toBeInTheDocument();
  });

  it("Header・Footerを表示すること", async () => {
    const ui = await AdminInquiryDetailPage(makeProps({ inquiryId: "7" }));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
