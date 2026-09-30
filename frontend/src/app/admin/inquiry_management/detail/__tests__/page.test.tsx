import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import AdminInquiryDetailPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));
jest.mock("@/lib/auth/AuthGuard", () => ({
  AuthGuard: ({ children }: { children: React.ReactNode }) => <>{children}</>,
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
  ])("inquiryIdが%s場合は404として扱う（notFoundを呼ぶ）", async (_label, value) => {
    // 存在しないリソースは 200 でメッセージを出すのではなく 404 として扱う
    // （`not-found.tsx` が描画され、noindex も付与される）
    await expect(
      AdminInquiryDetailPage(makeProps({ inquiryId: value }))
    ).rejects.toThrow();
    expect(mockAdminInquiryDetail).not.toHaveBeenCalled();
  });

  it("Header・Footerを表示すること", async () => {
    const ui = await AdminInquiryDetailPage(makeProps({ inquiryId: "7" }));
    render(ui);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
  });
});
