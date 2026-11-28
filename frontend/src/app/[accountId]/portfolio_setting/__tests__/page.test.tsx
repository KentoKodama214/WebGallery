import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import PortfolioSettingPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));
jest.mock("@/lib/auth/AuthGuard", () => ({
  AuthGuard: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

const mockPortfolioList = jest.fn();
jest.mock("../PortfolioList", () => ({
  PortfolioList: (props: { accountId: string }) => {
    mockPortfolioList(props);
    return <div data-testid="portfolio-list" />;
  },
}));

describe("PortfolioSettingPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("accountIdが正しい場合はポートフォリオ一覧を表示すること", async () => {
    render(
      await PortfolioSettingPage({ params: Promise.resolve({ accountId: "validaccount1" }) })
    );

    expect(screen.getByTestId("portfolio-list")).toBeInTheDocument();
    expect(mockPortfolioList).toHaveBeenCalledWith({ accountId: "validaccount1" });
  });

  it("accountIdが不正な形式の場合は404として扱う（notFoundを呼ぶ）", async () => {
    await expect(
      PortfolioSettingPage({ params: Promise.resolve({ accountId: "a" }) })
    ).rejects.toThrow();
    expect(mockPortfolioList).not.toHaveBeenCalled();
  });
});
