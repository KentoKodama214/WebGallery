import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import PortfolioEditPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));
jest.mock("@/lib/auth/AuthGuard", () => ({
  AuthGuard: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

const mockPortfolioEditForm = jest.fn();
jest.mock("../PortfolioEditForm", () => ({
  PortfolioEditForm: (props: { accountId: string; initialPortfolioNo?: number }) => {
    mockPortfolioEditForm(props);
    return <div data-testid="portfolio-edit-form" />;
  },
}));

/** Next.js の searchParams/params は Promise で渡される */
function makeProps(
  accountId: string,
  searchParams: Record<string, string | string[] | undefined>
) {
  return {
    params: Promise.resolve({ accountId }),
    searchParams: Promise.resolve(searchParams),
  };
}

describe("PortfolioEditPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("portfolioNo が未指定の場合は新規作成としてフォームを表示すること", async () => {
    render(await PortfolioEditPage(makeProps("validaccount1", {})));

    expect(screen.getByTestId("portfolio-edit-form")).toBeInTheDocument();
    expect(mockPortfolioEditForm).toHaveBeenCalledWith({
      accountId: "validaccount1",
      initialPortfolioNo: undefined,
    });
  });

  it("portfolioNo が正の整数の場合は編集としてフォームを表示すること", async () => {
    render(await PortfolioEditPage(makeProps("validaccount1", { portfolioNo: "3" })));

    expect(mockPortfolioEditForm).toHaveBeenCalledWith({
      accountId: "validaccount1",
      initialPortfolioNo: 3,
    });
  });

  it("accountIdが不正な形式の場合は404として扱う（notFoundを呼ぶ）", async () => {
    await expect(PortfolioEditPage(makeProps("a", {}))).rejects.toThrow();
    expect(mockPortfolioEditForm).not.toHaveBeenCalled();
  });

  it.each([
    ["数値でない", "abc"],
    ["ゼロ", "0"],
    ["負数", "-1"],
    ["小数", "1.5"],
    ["指数表記", "1e3"],
    ["複数指定", ["1", "2"]],
  ])("portfolioNoが%s場合は404として扱う（notFoundを呼ぶ）", async (_label, value) => {
    await expect(
      PortfolioEditPage(makeProps("validaccount1", { portfolioNo: value }))
    ).rejects.toThrow();
    expect(mockPortfolioEditForm).not.toHaveBeenCalled();
  });
});
